package controllers;

import networking.*;
import GUI.MineSweeperGUI;
import java.util.logging.*;
import logics.Field;

/**
 *
 * @author S
 */
public class Controller {

    private MineSweeperGUI msgui;

    /**
     *
     * @return current gui
     */
    public MineSweeperGUI getMsgui() {
        return msgui;
    }

    private Field field = null;
    private boolean exploded = false;
    private boolean justStarted = true;
    private String ipad, port;
    private Client cl;
    private Thread networkThread, guiThread;
    private boolean isServer, byebye = false;
    private int w, h;

    /**
     *
     * @return instance of the client class
     */
    public Client getCl() {
        return cl;
    }
    private Server sv;

    /**
     *
     * @return if the player has discovered a bomb
     */
    public boolean isExploded() {
        return exploded;
    }

    /**
     *
     * @param exploded if the parameter should be set or unset
     */
    public void setExploded(boolean exploded) {
        this.exploded = exploded;
    }

    /**
     *
     * @return network port
     */
    public String getPort() {
        return port;
    }

    /**
     *
     * @return if the game has just started or been reset
     */
    public boolean isJustStarted() {
        return justStarted;
    }

    /**
     *
     * @return returns the field that holds the current state of the game
     */
    public Field getField() {
        return field;
    }

    /**
     *
     * @param i as x
     * @param j as y
     */
    public void setFlag(int i, int j) {
        field.setFlagged(i, j);
        msgui.updateInterface();
    }

    /**
     *
     * @return how many bombs are left of all as a string
     */
    public String bombCounter() {
        return field.bombsLeft() + "/" + field.bombsAll();
    }

    /**
     *
     * @return how many bombs are left
     */
    public int bombsLeft() {
        return field.bombsLeft();
    }

    /**
     *
     * @return how many flags are in places they should not be
     */
    public int falseFlags() {
        return field.falseFlags();
    }

    /**
     * start the game over
     */
    public void restart() {
        field.resetField();
        field.setEnd(false);
        justStarted = true;
        exploded = false;
        msgui.updateInterface();
        System.out.println(field.gameView(true, false));
    }

    /**
     * process the action that some button has registered
     *
     * @param i as x
     * @param j as j
     */
    public void respondToPress(int i, int j) {
        boolean safe = field.openCell(i, j);
        System.out.println(field.gameView(true, false));

        if (!safe) {
            if (justStarted) {
                field.giveMeNewFieldFor(i, j);
                respondToPress(i, j);
                safe = true;
            } else {
                field.setCellOpened(i, j, false);
                field.setEnd(true);
            }
        } else {
            justStarted = false;
        }

        if (!safe) {
            exploded = true;
            msgui.updateInterface();
        } else {
            msgui.updateInterface();
        }
    }

    private void serverWork() {
        System.out.println("starting server");

        sv = new Server(Integer.parseInt(port), this);
        networkThread = new Thread(sv);
        networkThread.start();

    }

    /**
     *
     * @param s
     */
    public void passToGUI(String s) {
        //System.out.println(s);
        msgui.restoreFieldFromString(s);
    }

    /**
     *
     * @return
     */
    public boolean isByebye() {
        return byebye;
    }

    /**
     *
     */
    public void sayBye() {
        this.byebye = true;
        endNetworking();
    }

    /**
     *
     */
    public void endNetworking() {
        
            networkThread.interrupt();
        
    }

    /**
     *
     * @param isServer
     * @param w
     * @param h
     * @param ip
     * @param port
     */
    public Controller(boolean isServer, int w, int h, String ip, String port) {
        this.port = port;
        this.ipad = ip;
        this.isServer = isServer;
        this.w = w;
        this.h = h;
    }

    /**
     *
     */
    public void work() {
        System.out.print("Controller got input : ");
        if (isServer) {
            System.out.println("Server ");
        } else {
            System.out.print("Client ");
        }
        System.out.println("Width : " + w + ", Height : " + h + ", ip " + ipad + ":" + port);

        if (isServer) {
            field = new Field(w, h, 0.15);
            msgui = new MineSweeperGUI(isServer, this);
            guiThread = new Thread(msgui);
            guiThread.start();
            serverWork();

        } else {
            cl = new Client(ipad, Integer.parseInt(port), this);
            networkThread = new Thread(cl);
            w = Integer.parseInt(cl.responseTest("width please"));
            h = Integer.parseInt(cl.responseTest("height please"));
            System.out.println("w = " + w + ", h = " + h);
            field = new Field(w, h, 0);
            msgui = new MineSweeperGUI(isServer, this);
            guiThread = new Thread(msgui);
            guiThread.start();
            try {
                guiThread.join(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
            //String desc = cl.responseTest("description please");
            //System.out.println("got desc" + desc);
            msgui.restoreFieldFromString(cl.responseTest("description please"));
            networkThread.start();
        }
    }
}

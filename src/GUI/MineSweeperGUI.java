package GUI;

import controllers.Controller;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import javax.sound.sampled.*;

/**
 *
 * @author S
 */
public class MineSweeperGUI extends JFrame implements Runnable {

    private JPanel entire = new JPanel();
    private JPanel grid;
    private static FieldButton buttons[];
    private String myip = "error";
    private int fieldWidth;
    private int fieldHeight;
    private final Controller controller;
    private JLabel bombCounter;
    private boolean isServer;
    private long stateCounter;

    /**
     *
     * @return state in which the field resides
     */
    public long getStateCounter() {
        return stateCounter;
    }

    /**
     *
     * @return if game is in server mode
     */
    public boolean isServer() {
        return isServer;
    }

    /**
     *
     * @return array of buttons
     */
    public static FieldButton[] getButtons() {
        return buttons;
    }

    /**
     *
     * @return controller
     */
    public Controller getController() {
        return controller;
    }

    /**
     * updates buttons, synchronizes with the model
     */
    public void updateInterface() {

        boolean fresh = controller.isJustStarted();
        boolean fin = controller.isExploded();

        if (fin) {
            playSound(false);
        }

        for (FieldButton a : buttons) {
            int x = a.get_x(), y = a.get_y();
            boolean opened = controller.getField().isCellOpened(x, y);
            boolean flagged = controller.getField().isFlagged(x, y);

            if (opened || flagged) {
                if (!flagged) {
                    a.setEnabled(false);
                }
                a.setText(controller.getField().getCellString(x, y));
            } else {
                a.setEnabled(true);
                a.setText("");
            }

            if (fin && controller.getField().isCellBomb(x, y)) {
                a.setText("B");
                a.setBackground(Color.red);
            } else if (fresh) {
                a.setBackground(new JButton().getBackground());
                a.setEnabled(true);
            }
        }
        if (isServer) {
            updateBombCount();
        }
        stateCounter++;
    }

    /**
     *
     * @return current state of the buttons on the field as a string
     */
    public String fieldDescription() {
        String res = "";

        for (int i = 0; i < this.fieldHeight * this.fieldWidth; i++) {
            res += buttons[i].toString() + "\n";
        }

        return res;
    }

    /**
     *
     * @param s string to restore the state of the buttons on the field from
     */
    public void restoreFieldFromString(String s) {
        //System.out.println("starting restore with" + s);
        int i = 0;
        for (String part : s.split("\n")) {

            if (i >= 100) {
                break;
            }

            if (part.equals("") || (part.length() - part.replaceAll("\\:", "").length()) != 3) {
                continue;
            }

            if (Integer.parseInt(part.split(":")[0]) == 1) {
                buttons[i].setEnabled(true);
            } else {
                buttons[i].setEnabled(false);
            }

            buttons[i].setText(part.split(":")[1]);

            if (Integer.parseInt(part.split(":")[2]) == 0) {
                buttons[i].setBackground(new JButton().getBackground());
            } else if (Integer.parseInt(part.split(":")[2]) == 1) {
                buttons[i].setBackground(Color.red);
            } else if (Integer.parseInt(part.split(":")[2]) == 2) {
                buttons[i].setBackground(Color.green);
            }
            i++;
        }
        //System.out.println("Restored");
    }

    private void celebrate() {
        for (FieldButton a : buttons) {
            if (a.isEnabled()) {
                a.setBackground(Color.green);
            }
        }

    }

    private void updateBombCount() {
        bombCounter.setText("Bombs left : " + controller.bombCounter());
        if (controller.bombsLeft() == 0 && controller.falseFlags() == 0 && isServer) {
            playSound(true);
            celebrate();
        }
    }

    /**
     *
     * @param isServer if the game should be started as a server
     * @param controller points at the controller
     */
    public MineSweeperGUI(boolean isServer, Controller controller) {
        super("MineSweeper");

        this.fieldHeight = controller.getField().getHeight();
        this.fieldWidth = controller.getField().getWidth();

        this.controller = controller;
        this.isServer = isServer;

    }

    @Override
    public void run() {//define window
        int someconst = (fieldWidth < 10 || fieldHeight < 10) ? 60 : 40;
        setSize(fieldWidth * someconst, fieldHeight * someconst);
        setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Closing the window");
                controller.sayBye();
                e.getWindow().dispose();
            }
        });

        //define mine field
        grid = new JPanel();
        grid.setLayout(new GridLayout(fieldHeight, fieldWidth));
        buttons = new FieldButton[fieldHeight * fieldWidth];
        for (int i = 0; i < fieldHeight * fieldWidth; i++) {
            buttons[i] = new FieldButton((i / fieldWidth), (i % fieldHeight), this);
            buttons[i].setFocusable(false);
            grid.add(buttons[i]);
        }

        System.out.println("Buttons were created ok");

        //define top panel
        JPanel top = new JPanel();
        if (isServer) {
            top.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            InetAddress ip;
            try {
                ip = InetAddress.getLocalHost();
                System.out.println("Current IP address : " + ip.getHostAddress() + ":" + controller.getPort());
                myip = ip.getHostAddress() + ":" + controller.getPort();
            } catch (UnknownHostException e) {
                System.out.println("Something went wrong when trying to determine your ip");
            }
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            top.add(new JLabel(myip), c);
            c.gridx = 1;
            c.gridy = 0;

            RestartButton rb = new RestartButton(this);
            top.add(rb, c);
            c.gridx = 2;
            c.gridy = 0;

            bombCounter = new JLabel();
            updateBombCount();
            top.add(bombCounter, c);
            top.setBackground(Color.WHITE);
        }

        entire.setLayout(new BorderLayout());

        if (isServer) {
            entire.add(top, BorderLayout.NORTH);
            entire.add(grid, BorderLayout.CENTER);
            add(entire);
        } else {
            add(grid);
        }
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void playSound(boolean win) {
        try {
            AudioInputStream stream;
            AudioFormat format;
            DataLine.Info info;
            Clip clip;

            if (!win) {
                stream = AudioSystem.getAudioInputStream(this.getClass().getResource("sounds/horn.wav"));
            } else {
                stream = AudioSystem.getAudioInputStream(this.getClass().getResource("sounds/tada.wav"));
            }
            format = stream.getFormat();
            info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Sound failed to play");
        }
    }

}

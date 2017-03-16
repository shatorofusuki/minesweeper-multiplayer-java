package networking;

//import Game.MineSweeper;
import controllers.Controller;
import java.io.*;
import java.net.*;
import java.util.logging.*;
import javax.swing.JOptionPane;

public class Client implements Runnable {

    private Socket socket;
    private Controller ctrl;
    private String lastAnswer = null;
    private PrintWriter out;
    private BufferedReader in;

    public String getLastAnswer() {
        return lastAnswer;
    }

    public Client(String hostname, int port, Controller ctrl) {

        this.ctrl = ctrl;
        try {
            socket = new Socket(hostname, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            System.out.println("Uh-uh");
            JOptionPane.showMessageDialog(null,
                    "Couldn't connect to the server.",
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public String responseTest(String s) {
        out.println(s);
        String fromServer = "", response = "";
        try {
            while ((fromServer = in.readLine()) != null) {
                //System.out.println("From server : " + fromServer);
                if ("endmessage".equals(fromServer)) {
                    break;
                }
                if (response != "") {
                    response += "\n";
                }
                if (!"".equals(fromServer)) {
                    response += fromServer;
                }
            }
//            System.out.println("Response ~~~~~~~");
//            System.out.println(response); 
//            System.out.println("~~~~~~~~~~~~~~~~");
        } catch (IOException ex) {
            System.out.println("Some error in responseTest");
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }

    public void catchEverything() {
        int i = 0;
        out.println("now update me");
        String fromServer = "", response = "";
        try {
            while ((fromServer = in.readLine()) != null) {
                System.out.println("From server : " + fromServer);
                if ("endmessage".equals(fromServer)) {
                    deliver(response);
                }
                if (response != "") {
                    response += "\n";
                }
                if (!"".equals(fromServer)) {
                    response += fromServer;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void deliver(String s) {
        ctrl.passToGUI(s);
    }

    public void run() {

        out.println("now update me");
        String fromServer = "", response = "";
        try {
            while ((fromServer = in.readLine()) != null) {
                //System.out.println("From server : " + fromServer + i++);
                if ("endmessage".equals(fromServer)) {
                    deliver(response);
                    response = "";
                }
                if ("byebye".equals(fromServer)) {
                    System.out.println("Server quit");
                    JOptionPane.showMessageDialog(null,
                            "Server disappered",
                            "Error",
                            JOptionPane.WARNING_MESSAGE);
                }
                if (!"".equals(response)) {
                    response += "\n";
                }
                if (!"".equals(fromServer)) {
                    response += fromServer;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

        //final
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}

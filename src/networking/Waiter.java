package networking;

import controllers.Controller;
import java.net.*;
import java.io.*;
import static java.lang.Thread.sleep;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Waiter implements Runnable {

    private Socket socket = null;
    private Controller ctrl = null;
    private PrintWriter out;
    private BufferedReader in;
    private long prevguistate;
            
    public Waiter(Socket client, Controller ctrl) {
        this.socket = client;
        this.ctrl = ctrl;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException ex) {
            Logger.getLogger(Waiter.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(Waiter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void respond (String s) {
        out.println(s + "\nendmessage");
    }
    
    private void keepClientsUpdated() {
        while (true) {
            if (this.ctrl.isByebye())
            {respond("byebye"); break;}
            if (this.prevguistate != ctrl.getMsgui().getStateCounter()) {
                
                respond(ctrl.getMsgui().fieldDescription());
                this.prevguistate = ctrl.getMsgui().getStateCounter();
            }
            try { sleep(100); } catch (InterruptedException ex) { 
                System.out.println("Something went wrong went trying to send info to the client");
            }   
        }
    }

    @Override
    public void run() {
        this.prevguistate = ctrl.getMsgui().getStateCounter();
        try {
            System.out.println("Starting waiter thread" + Thread.currentThread().getName());
            
            String userInput;
            while ((userInput = in.readLine()) != null) {
                System.out.println("Got mail : " + userInput);
                
                switch (userInput) {
                    case "TIME?" :
                        respond("yeah, time");
                        break;
                    case "width please" :
                        respond(Integer.toString(ctrl.getField().getWidth()));
                        break;
                    case "height please" :
                        respond(Integer.toString(ctrl.getField().getHeight()));
                        break;
                    case "description please" :
                        respond(ctrl.getMsgui().fieldDescription());
                        break;
                    case "now update me" : 
                        keepClientsUpdated();
                        break;
                    default :
                        respond("sorry");
                        break;    
                }
                
            }
            System.out.println("Waiter : Going to say goodbye");
            out.println("byebye");
            socket.close();
            
            System.out.println("Ending thread " + Thread.currentThread().getName());
        } catch (IOException e) {
            e.printStackTrace(System.out);
        } finally {
            out.close();
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(Waiter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

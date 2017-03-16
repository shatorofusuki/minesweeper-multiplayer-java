package networking;

import controllers.Controller;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable {

    private ServerSocket serverSocket;
    private int port;
    private Controller ctrl;

    public Server(int port, Controller ctrl) {
        this.port = port;
        this.ctrl = ctrl;
    }

    @Override
    public void run() {
        System.out.println("Starting the socket server at port:" + port);
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

        Socket clientSocket = null;
        System.out.println("Waiting for clients...");

        while (!Thread.currentThread().isInterrupted() && !ctrl.isByebye()) {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    serverSocket.setSoTimeout(5000);
                    clientSocket = serverSocket.accept();
                    break;
                } catch (IOException ex) {
                    System.out.println("Server timeout reached, restarting");
                }
            }

            if (clientSocket != null) {
                System.out.println("Connected:" + clientSocket.getInetAddress().getCanonicalHostName());
                Waiter w = new Waiter(clientSocket, ctrl);
                Thread thread = new Thread(w);
                thread.start();
            }
        }
    }
}

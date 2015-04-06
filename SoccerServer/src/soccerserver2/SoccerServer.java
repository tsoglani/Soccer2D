package soccerserver2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author gaitanesnikos
 */
public class SoccerServer {

    private final int port = 2000;
    private ArrayList<Clienthandler> clients = new ArrayList<Clienthandler>();

    public SoccerServer() {
        init();
    }

    private void init() {
        try {
            ServerSocket ss = new ServerSocket(port);
            while (true) {
                Clienthandler clientHandler = new Clienthandler(ss.accept());
                System.out.println("client conncted.");
                clientHandler.start();
                clients.add(clientHandler);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    HashMap<String, ArrayList<Clienthandler>> registry = new HashMap<String, ArrayList<Clienthandler>>();

    class Clienthandler extends Thread {

        BufferedReader in;
        PrintWriter out;
        String ip = "";
        //intial-message format: 

        Clienthandler(Socket cs) {
            try {
                ip = cs.getRemoteSocketAddress().toString();
                in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
                out = new PrintWriter(cs.getOutputStream(), true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String abstractWidgetId = in.readLine().trim();
//                System.out.println("first widgetId " + abstractWidgetId);
                if (!registry.containsKey(abstractWidgetId)) {
                    ArrayList<Clienthandler> handlerList = new ArrayList<Clienthandler>();
                    handlerList.add(Clienthandler.this);
                    registry.put(abstractWidgetId, handlerList);
                } else {
                    registry.get(abstractWidgetId).add(Clienthandler.this);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            while (true) {
                try {
//                   a#11|$|.......
                    String receivedMsg = in.readLine();
                    System.out.println("receivedMsg= " + receivedMsg);

                    String rcvdAbstractWidgetId = receivedMsg.split("\\|\\$\\|")[0];
                    ArrayList<Clienthandler> handlerList = registry.get(rcvdAbstractWidgetId);
                    for (int i = 0; i < handlerList.size(); i++) {
                        if (handlerList.get(i) != this) {
                            handlerList.get(i).out.println(receivedMsg.split("\\|\\$\\|")[1]);
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        SoccerServer soccerServer = new SoccerServer();
    }
}

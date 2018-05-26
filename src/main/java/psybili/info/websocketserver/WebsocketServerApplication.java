package psybili.info.websocketserver;

import org.glassfish.tyrus.server.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.client.WebSocketClient;

import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.IOException;
import java.util.*;

@SpringBootApplication
public class WebsocketServerApplication {

    protected static boolean WELCOME_MESSAGE = true;
    protected static boolean ECHO_SERVER = false;

    private static ArrayList<Session> clients = new ArrayList<Session>();
    private static int PORT = 8080;

    private enum Commands {
        DEFAULT,
        HELP,
        PRINT,
        PRIVATE,
        EXIT,
        STREAM
    }

    static Set<String> stockChannels = new HashSet<String>();

    public static void main(String[] args) {
        runServer();
    }

    private static void runServer() {
        Server server = new Server("localhost", WebsocketServerApplication.PORT, "/", WebSocketClient.class);
        boolean shutDown = false;

        try {
            server.start();
            System.out.println("You can visit http://websocket.org/echo.html to test!\n - For help, write the HELP command - ");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                String commandLine = scanner.nextLine();
                String[] command = commandLine.split(" ");

                Commands enumVal = Commands.DEFAULT;
                try {
                    enumVal = Commands.valueOf(command[0].toUpperCase());
                } catch (IllegalArgumentException ignored) {
                }

                switch (enumVal) {
                    case HELP:
                        String help = "Commands:";
                        help += "\n\tPRINT [string] - Write to all connected clients.";
                        help += "\n\tPRIVATE [session-id] [string] - Write to current client";
                        help += "\n\tSTREAM - stream stock data for subscribed items";
                        help += "\n\tEXIT - c ya!";
                        System.out.println(help);
                        break;
                    case PRINT:
                        if (commandLine.length() < 5) {
                            System.out.println("You did not enter a message");
                        } else {
                            String message = commandLine.substring(6);
                            sendMessage(message);
                        }
                        break;
                    case PRIVATE:
                        if (commandLine.length() < 44) {
                            System.out.println("You did not enter a message");
                        } else {
                            String message = commandLine.substring(45);
                            String searchingId = commandLine.substring(8, 44);
                            sendMessage(message, searchingId);
                        }
                        break;
                    case EXIT:
                        shutDown = true;
                        break;
                    case STREAM:
                        Thread t = new Thread(new Runnable() {
                            public void run() {
                                while (true) {
                                    String[] channelArr = stockChannels.toArray(new String[0]);
                                    for (String channel : channelArr) {
                                        String message = getRandomStockDataForChannel(channel);
                                        sendMessage(message);
                                    }
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                        t.start();
                        break;
                    default:
                        System.err.println("Unknown command! Try help to show command list");
                        break;
                }

                if (shutDown) {
                    System.out.println("The process ends...");
                    break;
                }
            }
        } catch (DeploymentException e) {
            throw new RuntimeException(e);
        } finally {
            server.stop();
        }
    }

    private static String getRandomStockDataForChannel(String channel) {
        Random r = new Random();

        String isin = channel;
        double price = r.nextDouble();
        double bid = r.nextDouble();
        double ask = r.nextDouble();

        return "{" +
                "isin:" + isin +
                ",price:" + price +
                ",bid:" + bid +
                ",ask:" + ask
                + "}";
    }

    protected static void addClient(Session session) {
        WebsocketServerApplication.clients.add(session);
    }

    protected static ArrayList<Session> getClients() {
        return WebsocketServerApplication.clients;
    }

    protected static void removeClient(Session session) {
        WebsocketServerApplication.clients.remove(session);
    }

    private static void sendMessage(String message) {
        sendMessage(message, null);
    }

    private static void sendMessage(String message, String sessionId) {
        if (WebsocketServerApplication.getClients().size() > 0) {
            try {
                int count = 0;
                for (Session session : WebsocketServerApplication.getClients()) {
                    if (sessionId != null) {
                        if (session.getId().equals(sessionId)) {
                            session.getBasicRemote().sendText(message);
                            count++;
                            break;
                        }
                    } else {
                        session.getBasicRemote().sendText(message);
                        count++;
                    }
                }
                System.out.println("Has been sent to " + count + " client" + (count > 1 ? "s" : ""));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No connected clients");
        }
    }
}

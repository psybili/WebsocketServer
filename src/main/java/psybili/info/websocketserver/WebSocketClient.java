package psybili.info.websocketserver;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint(value = "/")
public class WebSocketClient {
    @OnOpen
    public void onOpen(Session session) {
        System.out.println(session.getId() + " has opened a connection");
        if(WebsocketServerApplication.WELCOME_MESSAGE) {
            try {
                session.getBasicRemote().sendText("Connection Established. Echo mode = " + (WebsocketServerApplication.ECHO_SERVER ? "on" : "off"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        WebsocketServerApplication.addClient(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("[" + session.getId() + "]: " + message);
        String[] parts = message.split(":");
        if(parts.length > 1) {
            message = parts[1].trim();
            message = message.substring(1, message.length()-2);
            if(parts[0].contains("subscribe")){
                WebsocketServerApplication.stockChannels.add(message);
            }
            if(parts[0].contains("unsubscribe")) {
                WebsocketServerApplication.stockChannels.remove(message);
            }
        } else {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if(WebsocketServerApplication.ECHO_SERVER) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Session " +session.getId()+" has ended");
        WebsocketServerApplication.removeClient(session);
    }

    @OnError
    public void onError(Throwable exception, Session session) {
        exception.printStackTrace();
        System.err.println("Error for client: " + session.getId());
    }
}

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;

public class OwnerPeer extends Peer {
    int altruisticMultiplier;
    int altruisticFactor;
    public static final String NODE_TYPE = "OWNER";

    /**
     * Initiate an owner node instance
     * @throws IOException generic IO exception
     */
    @Override
    public void init() throws IOException {
        super.init();
        this.altruisticMultiplier = 1;
        super.serverThread = new ServerThread(String.valueOf(this.portNum), NODE_TYPE);
        super.serverThread.start();
    }

    /**
     * Main method to start an owner node instance
     * @param args arguments
     * @throws Exception generic exception
     */
    public static void main(String[] args) throws Exception {
        OwnerPeer peer = new OwnerPeer();
        peer.init();
        peer.updateListenToPeers();
    }

    /**
     * Allow direct connection to other peers via entering the address
     * @throws Exception generic exception
     */
    public void updateListenToPeers() throws Exception {
        System.out.println("> enter (space separated) hostname:port# (ownerPeer)");
        System.out.println("  peers to receive messages from (s to skip):");
        String input = this.in.readLine();
        String[] inputValues = input.split(" ");
        if (!input.equals("s")) for (int i = 0; i < inputValues.length; i++) {
            String[] address = inputValues[i].split(":");
            listenToPeer(address[0], Integer.parseInt(address[1]));
        }
        communicate(this.in, this.peerName, serverThread);
    }

    /**
     * Begin listening for message from a specific address
     * @param hostname IP address to listen to
     * @param portNum Port number to listen to
     * @throws Exception generic exception
     */
    @Override
    protected void listenToPeer(String hostname, int portNum) throws Exception {
        Socket socket = null;
        try {
            socket = new Socket(hostname, portNum);
            new OwnerPeerThread(this, socket).start();
            //communicate(this.in, this.peerName, serverThread);
        } catch (Exception e) {
            if (socket != null) socket.close();
            else System.out.println("invalid input. skipping to next step.");
        }
    }

    /**
     * Continuously listen for command from console and execute them either by sending P2P messages or by performing local tasks
     * @param bufferedReader Reader listening for input from console
     * @param username Username string of this peer
     * @param serverThread Server Thread of this peer to send message
     */
    @Override
    public void communicate(BufferedReader bufferedReader, String username, ServerThread serverThread) {
        try {
            System.out.println("> you can now communicate (e to exit)");
            boolean flag = true;
            while(flag) {
                String message = bufferedReader.readLine();
                if (message.equals("e")) {
                    StringWriter writer = new StringWriter();
                    Json.createWriter(writer).writeObject(Json.createObjectBuilder()
                            .add("request", "disconnect")
                            .add("node_type", NODE_TYPE)
                            .add("address", this.ip + ":" + this.portNum)
                            .build());
                    sendPostRequest(BOOTSTRAP_ADDRESS, writer.toString());
                    flag = false;
                    break;
                } else {
                    String cmd = message.split(" ")[0];
                    StringWriter stringWriter = new StringWriter();
                    switch (cmd) {
                        case "chat":
                            Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
                                    .add("type", cmd)
                                    .add("username", username)
                                    .add("message", message.substring(message.indexOf(" ") + 1))
                                    .build());
                            serverThread.sendMessage(stringWriter.toString());
                            break;
                        case "list":
                            Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
                                    .add("type", cmd)
                                    .add("username", username)
                                    .add("model", message.substring(message.indexOf(" ") + 1))
                                    .add("factor", altruisticFactor)
                                    .build()
                            );
                            serverThread.sendP2PMessage(stringWriter.toString());
                            break;
                        case "bootstrap":
                            StringWriter writer = new StringWriter();
                            Json.createWriter(writer).writeObject(Json.createObjectBuilder()
                                    .add("request", "connect")
                                    .add("node_type", NODE_TYPE)
                                    .add("address", this.ip + ":" + this.portNum)
                                    .build());
                            System.out.println(writer);
                            String response = sendPostRequest(BOOTSTRAP_ADDRESS, writer.toString());
                            JsonReader reader = Json.createReader(new StringReader(response));
                            JsonObject json = reader.readObject();
                            if (json.getBoolean("isNotEmpty")) {
                                JsonArray connections = json.getJsonArray("list");
                                connections.forEach(connection -> {
                                    String[] addr = connection.toString().replace("\"", "").split(":");
                                    try {
                                        listenToPeer(addr[0], Integer.parseInt(addr[1]));
                                        System.out.println("Connection opened with user " + connection);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            } else {
                                System.out.println("No available connection. Try again later.");
                            }
                            break;
                        default:
                            System.out.println("unknown command: " + cmd);
                            break;
                    }
                }
            }
            System.exit(0);
        } catch (Exception e) {}
    }
}

class PreProcessor{
    public JsonObject preprocess(JsonObject json) {
        return Json.createObjectBuilder()
                .add("make", json.getString("make"))
                .add("asking_price", json.getString("asking_price"))
                .add("msrp", json.getString("msrp"))
                .add("zip_code", json.getString("zip_code"))
                .add("IPaddress", json.getString("IPaddress"))
                .build();
    }
}

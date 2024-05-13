import javax.json.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.PriorityQueue;

public class UserPeer extends Peer {
    PriorityQueue<WeightedModel> publicListing;
    public static final String NODE_TYPE = "USER";
    public static final String UDP_PORT = "8888";

    /**
    * Initiate a user peer instance
    * */
    @Override
    public void init() throws IOException {
        super.init();
        this.publicListing = new PriorityQueue<>();
        super.serverThread = new ServerThread(String.valueOf(this.portNum), NODE_TYPE);
        super.serverThread.start();
    }

    /**
     * Main method to start a user peer instance
     * @param args arguments
     * @throws Exception generic exception
     */
    public static void main(String[] args) throws Exception {
        UserPeer peer = new UserPeer();
        peer.init();
        peer.updateListenToPeers();
    }

    /**
     * Allow direct connection to other peers via entering the address
     * @throws Exception generic exception
     */
    public void updateListenToPeers() throws Exception {
        System.out.println("> enter (space separated) hostname:port# (userPeer)");
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
            new UserPeerThread(this, socket).start();
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
                        case "preprocess":
                            serverThread.sendP2PMessage(getPreProcessInfo(this.in));
                            try {
                                DatagramSocket socket = new DatagramSocket(Integer.parseInt(UDP_PORT));
                                System.out.println("UDP Server is listening on port " + Integer.parseInt(UDP_PORT));

                                while (true) {
                                    byte[] buffer = new byte[1024]; // Buffer to store incoming data
                                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                                    socket.receive(packet);

                                    String received = new String(packet.getData(), 0, packet.getLength());
                                    System.out.println("Received: " + received);
                                    if (received.contains(":")) {
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "show_list":
                            System.out.println("Printing existing listings (top 10):");
                            PriorityQueue<WeightedModel> view = new PriorityQueue<>(publicListing);
                            for (int i = 0; i < 10 && !view.isEmpty(); i++) {
                                WeightedModel model = view.poll();
                                System.out.println(model.getModel() + " " + model.getWeight());
                            }
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
                                                System.out.println("Connection opened with owner " + connection);
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

    /**
     * Initiate a series of query to obtain information that will be sent to ML model
     * @param br reader to read from console input
     * @return constructed JSON string used as body of P2P message
     * @throws IOException generic IO exception
     */
    private String getPreProcessInfo(BufferedReader br) throws IOException {
        StringWriter stringWriter = new StringWriter();
        System.out.println("> enter make:");
        String make = br.readLine();
        System.out.println("> enter asking price:");
        String askingPrice = br.readLine();
        System.out.println("> enter msrp:");
        String msrp = br.readLine();
        System.out.println("> enter zip code:");
        String zip = br.readLine();
//        JsonObjectBuilder builder = Json.createObjectBuilder();
//        builder.add("type", "preprocess");
        Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
                .add("type", "preprocess")
                .add("username", this.peerName)
                .add("make", make)
                .add("asking_price", askingPrice)
                .add("msrp", msrp)
                .add("zip_code", zip)
                .add("IPaddress", this.ip)
                .build());
        return stringWriter.toString();
    }
}

class WeightedModel implements Comparable<WeightedModel> {
    private int weight;
    private String model;

    public WeightedModel(int weight, String model) {
        this.weight = weight;
        this.model = model;
    }

    public int getWeight() {
        return weight;
    }

    public String getModel() {
        return model;
    }

    @Override
    public int compareTo(WeightedModel other) {
        return Integer.compare(other.weight, this.weight);
    }
}

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class OwnerPeerThread extends PeerThread {
    private final OwnerPeer ownerPeer;
    public OwnerPeerThread(OwnerPeer ownerPeer, Socket socket) throws IOException {
        super(socket);
        this.ownerPeer = ownerPeer;
    }

    /**
     * Main function of the thread, continuously listen for messages from a known connection
     */
    public void run() {
        boolean flag = true;
        while (flag) {
            try {
                JsonObject jsonObject = Json.createReader(super.bufferedReader).readObject();
                if (jsonObject.containsKey("type")) {
                    switch (jsonObject.getString("type")) {
                        case "chat":
                            if (jsonObject.containsKey("username"))
                                System.out.println("["+jsonObject.getString("username")+"]: "+jsonObject.getString("message"));
                            break;
                        case "preprocess":
                            incrementAltruism(this.ownerPeer.altruisticMultiplier);
                            System.out.println("You got some preprocessing to do from " + jsonObject.getString("username"));
                            System.out.println("The content is " + jsonObject);
                            System.out.println("Your altruistic factor is now " + this.ownerPeer.altruisticFactor);
                            JsonObject preProcessResult = new PreProcessor().preprocess(jsonObject);
                            System.out.println("The result of preprocessing is " + preProcessResult);
                            sendUDP(preProcessResult.toString());
                            break;
                        default:
                            System.out.println("Unknown type: "+jsonObject.getString("type"));
                            break;
                    }
                }
            } catch(Exception e) {
                flag = false;
                interrupt();
            }
        }
    }

    /**
     * Synchronized function to increment the altruistic factor of parent node
     * @param multiplier determines the amount to increment
     */
    private synchronized void incrementAltruism(int multiplier) {
        this.ownerPeer.altruisticFactor += multiplier;
    }

    /**
     * Sends a UDP datagram to the ML model
     * @param message JSON string message for ML input
     */
    private void sendUDP(String message) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName("54.82.54.73");
            byte[] buffer = message.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 8888);
            socket.send(packet); // Send the packet
            System.out.println("Message sent to " + "54.82.54.73" + ":" + 8888);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

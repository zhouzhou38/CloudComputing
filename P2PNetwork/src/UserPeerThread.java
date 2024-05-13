import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.net.Socket;

public class UserPeerThread extends PeerThread {
    private final UserPeer userPeer;
    public UserPeerThread(UserPeer userPeer, Socket socket) throws IOException {
        super(socket);
        this.userPeer = userPeer;
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
                        case "list":
                            System.out.println(jsonObject);
                            String userName = jsonObject.getString("username");
                            String model = jsonObject.getString("model");
                            int factor = jsonObject.getInt("factor");
                            System.out.println(userName + " just posted a listing for you");
                            updateListing(userName, model, factor);
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
     * Synchronized function to concatenate the arraylist of public model listing
     * @param userName the source of this public listing
     * @param model the specific model listed
     * @param factor the altruistic factor (priority) of the listing
     */
    private synchronized void updateListing(String userName, String model, int factor) {
        this.userPeer.publicListing.add(new WeightedModel(factor, userName + ": " + model));
    }
}

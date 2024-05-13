import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import javax.json.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.HashSet;

public class BootstrapServer {
    private static HashSet<String> userPeerSet = new HashSet<>();
    private static HashSet<String> ownerPeerSet = new HashSet<>();

    /**
     * Main method to start a bootstrap server
     * @param args arguments
     * @throws IOException generic IO exception
     */
    public static void main(String[] args) throws IOException {
        int port = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server is listening on port " + port);
    }

    static class MyHandler implements HttpHandler {
        /**
         * method to handle an incoming HTTP request
         * @param t the exchange containing the request from the
         *                 client and used to send the response
         * @throws IOException genetic IO exception
         */
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "";
            InputStream in = t.getRequestBody();
            StringBuilder builder = new StringBuilder();
            try (InputStreamReader isr = new InputStreamReader(in);
                 BufferedReader br = new BufferedReader(isr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    builder.append(line);
                }
            }
            JsonReader reader = Json.createReader(new StringReader(builder.toString()));
            JsonObject body = reader.readObject();
            if (body.getString("request").equals("connect")) {
                if (body.containsKey("node_type")) {
                    if (body.getString("node_type").equals("USER")) {
                        userPeerSet.add(body.getString("address"));
                        response += createResponse(ownerPeerSet);
                        System.out.println("CONNECT USER " + body.getString("address"));
                    } else if (body.getString("node_type").equals("OWNER")) {
                        ownerPeerSet.add(body.getString("address"));
                        response += createResponse(userPeerSet);
                        System.out.println("CONNECT OWNER " + body.getString("address"));
                    }
                }
            } else {
                if (body.containsKey("node_type")) {
                    if (body.getString("node_type").equals("USER")) {
                        userPeerSet.remove(body.getString("address"));
                        System.out.println("DISCONNECT USER " + body.getString("address"));
                    } else {
                        ownerPeerSet.remove(body.getString("address"));
                        System.out.println("DISCONNECT OWNER " + body.getString("address"));
                    }
                    response += "Disconnect request received";
                }
            }
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        /**
         * Method to create a response for Bootstrap requests
         * @param set set of existing peers the requester may connect to
         * @return constructed JSON string of HTTP response
         */
        private String createResponse(HashSet<String> set) {
            JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
            set.forEach(jsonArrayBuilder::add);
            JsonArray jsonArray = jsonArrayBuilder.build();
            StringWriter stringWriter = new StringWriter();
            Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
                    .add("type", "response")
                    .add("isNotEmpty", !jsonArray.isEmpty())
                    .add("list", jsonArray)
                    .build());
            return stringWriter.toString();
        }
    }
}

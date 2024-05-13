import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class Peer {
	protected String peerName;
	protected String ip;
	protected int portNum;
	protected ServerThread serverThread;
	protected BufferedReader in;
	protected final String BOOTSTRAP_ADDRESS = "http://48.216.186.190:8000";

	/**
	 * Generic init function to set basic instance variables
	 * @throws IOException generic IO exception
	 */
	protected void init() throws IOException {
		this.in = new BufferedReader(new InputStreamReader(System.in));
		this.ip = getPublicIP();
		System.out.println("> enter username & port # for this peer:");
		String[] setupValues = in.readLine().split(" ");
		this.peerName = setupValues[0];
		this.portNum = Integer.parseInt(setupValues[1]);
	}
	public abstract void updateListenToPeers() throws Exception;

	protected abstract void listenToPeer(String hostname, int portNum) throws Exception;

    public abstract void communicate(BufferedReader bufferedReader, String username, ServerThread serverThread);

	/**
	 * Method to return the public IP address of the P2P node
	 * @return public IP address of the P2P node
	 */
	protected String getPublicIP() {
		String ipServiceURL = "https://api.ipify.org";
		StringBuilder response = new StringBuilder();

		try {
			URL url = new URL(ipServiceURL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);

			int responseCode = connection.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				reader.close();
			} else {
				System.out.println("GET request not worked. Response Code: " + responseCode);
				return null;
			}
		} catch (Exception e) {
			System.out.println("Error in getting IP Address: " + e.getMessage());
			e.printStackTrace();
			return null;
		}

		return response.toString();
	}

	/**
	 * Method to send an HTTP POST request, typically called when connecting to the Bootstrap server
	 * @param targetUrl URL of the HTTP Server
	 * @param content HTTP request body
	 * @return HTTP response body
	 */
	public static String sendPostRequest(String targetUrl, String content) {
		HttpURLConnection connection = null;
		String response = "";
		try {
			URL url = new URL(targetUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Charset", "utf-8");
			connection.setDoOutput(true);

			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = content.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			int responseCode = connection.getResponseCode();
			System.out.println("POST Response Code :: " + responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) { //success
				System.out.println("POST was successful.");
				InputStream inputStream = connection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				StringBuilder builder = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				reader.close();
				response = builder.toString();
			} else {
				System.out.println("POST request failed.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return response;
	}
}

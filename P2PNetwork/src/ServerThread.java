import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ServerThread extends Thread {
	String nodeType;
	private ServerSocket serverSocket;
	private Set<ServerThreadThread> serverThreadThreads = new HashSet<ServerThreadThread>(); 
	public ServerThread(String portNumb, String type) throws IOException {
		serverSocket = new ServerSocket(Integer.valueOf(portNumb));
		this.nodeType = type;
	}

	/**
	 * Main function of the server thread
	 */
	public void run() {
		try {
			while (true) {
				ServerThreadThread serverThreadThread = new ServerThreadThread(serverSocket.accept(), this);
				serverThreadThreads.add(serverThreadThread);
				serverThreadThread.start();
			}
		} catch (Exception e) { e.printStackTrace(); }
	}

	/**
	 * Broadcast a string message to all connected peer
	 * @param message string to be broadcast
	 */
	void sendMessage(String message) {
		try { serverThreadThreads.forEach(t-> t.getPrintWriter().println(message));
		} catch(Exception e) { e.printStackTrace(); }
	}

	/**
	 * Send a string message to one peer
	 * @param message string message to be sent
	 */
	void sendP2PMessage(String message) {
		try { serverThreadThreads.stream()
				.skip(new Random().nextInt(serverThreadThreads.size()))
				.findFirst()
				.ifPresent(t->t.getPrintWriter().println(message));
		} catch(Exception e) { e.printStackTrace(); }
	}

	/**
	 * Return the set of all message recipients
	 * @return set of all message recipients
	 */
	public Set<ServerThreadThread> getServerThreadThreads() { return serverThreadThreads; }
}

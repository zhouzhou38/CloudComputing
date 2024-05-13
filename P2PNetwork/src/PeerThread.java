import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public abstract class PeerThread extends Thread {
	protected BufferedReader bufferedReader;
	public PeerThread(Socket socket) throws IOException {
		bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));	
	}
	public abstract void run();
}

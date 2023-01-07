package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;

public class Server {
	static final int PORT = 4444;
	private ServerSocket serverSocket;
	private Set<ServerThread> serverThreads = new HashSet<ServerThread>();
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Server server = new Server();
		server.serverSocket = new ServerSocket(PORT);
		System.out.println("[Passive Eve]: eavesdropping on all communications");
		while(true) {
			ServerThread serverThread = new ServerThread(server.serverSocket.accept(), server);
			server.serverThreads.add(serverThread);
			serverThread.start();
		}
	}
	void forwardMessage(String message, ServerThread originatingServerThread) {
		serverThreads.forEach(serverThread -> {
			if(serverThread != originatingServerThread) serverThread.forwardMessage(message);
		});
	}
	public Set<ServerThread> getServerThreads() { return serverThreads; }

}

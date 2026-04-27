import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class ClientHandler implements Runnable{
	private Socket client;
	public String ipAddress;

	public ClientHandler(Socket client) {
		this.client = client;
		this.ipAddress = client.getInetAddress().getHostAddress()+"";
	}

	public void run(){
		System.out.println("A client connected to the fitting room server.");
	}
}

public class FittingRoomServer {
	//Shared state
	static int inside = 0; 
	static int outside = 0;
	ClientHandler client;
	
	//THESE PROLLY NEED TO BE CHANGED. -AE
	//static final int MAX_INSIDE = 1;
	//static final int MAX_OUTSIDE = 2; 
	
	public static void main(String[] args) throws IOException {
		//port

		//int port = getPortFromArgs(args[0]);
		int port = 50002;
        startServer(port);
	}
	
	//Starts the server given this port number.
	private static void startServer(int port) throws IOException {
		ClientHandler client;

		Socket central = new Socket("localhost", 50001); 
		System.out.println("Fitting Room Server connected to Central Server on port 50001...");
		
		//ServerSocket
        ServerSocket server = new ServerSocket(port);
		
		System.out.println("Fitting Room Server is now starting!");
		while(true) {
			//accept client
			try {
				
				Socket clientSocket = server.accept();
				ClientHandler clientObject = new ClientHandler(clientSocket);
				Thread t = new Thread(clientObject);
				t.start();

			}catch(Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	//Handling the client.
	private static void handleClient(Socket socket) {
		try {
			//1. Try to enter room
			//2. Simluate fitting room usage 
			//3.Exit Room 
			//4. close connection
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//Allows one thread in the method with synchroinized
	private static synchronized boolean enterRoom(Socket socket) throws Exception {
		//CASE 1: FULL = REJECT

		//CASE 2: Room occupied = wait 
		
		//CASE 3: Enter Room 
		
		return false; 
	}
	
	private static void simulateUsage() throws InterruptedException{
		//Thread goes to sleep 
	}
	
	private static void rejectClient(Socket socket) throws IOException{
		//send "ROOM FULL" 
		//close socket 
	}
	
	private static void allowClient(Socket socket) throws IOException{
		//send success message
	}
	
	private static int getPortFromArgs(String args) {
		//parse port
		int port = Integer.parseInt(args);
		return port; 
	}

    
}
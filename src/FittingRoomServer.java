import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FittingRoomServer {
	//Shared state
	static int inside = 0; 
	static int outside = 0;
	
	//THESE PROLLY NEED TO BE CHANGED. -AE
	//static final int MAX_INSIDE = 1;
	//static final int MAX_OUTSIDE = 2; 
	
	public static void main(String[] args) throws IOException {
		//port
        int port = 50000;
		//startServer
        startServer(port);
	}
	
	//Starts the server given this port number.
	private static void startServer(int port) throws IOException {
		//ServerSocket
        ServerSocket serv = new ServerSocket(port);
		
		while(true) {
			//accept client 
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
	
	private static int getPortFromArgs(String[] args) {
		//parse port
		return -1; 
	}

    
}
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.WebSocket;

/*

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
 */
public class FittingRoomServer {
	//Shared state
	static int inside = 0; 
	static int outside = 0;

	//Other Variables
	int changingInside = 0;
	int currentlyWaiting = 0;
	int serverNumber = 0;
	
	//THESE PROLLY NEED TO BE CHANGED. -AE
	//static final int MAX_INSIDE = 1;
	//static final int MAX_OUTSIDE = 2; 
	
	public static void main(String[] args) throws IOException {
		//port
		//int port = getPortFromArgs(args[0]);
		//int numberOfFittingRooms = Integer.parseInt(args[1]);

		//HARD CODE FOR NOW, BUT CHANGE THIS WHEN SUBMITTING.
		int port = 50001;
		String ipAddress = "127.0.0.1"; //"192.168.10.1";
        startServer(port,ipAddress);
	}
	
	//Starts the server given this port number.
	private static void startServer(int port, String ipAddress) throws IOException {
		//Connection acts like a client to the central server. This will get a connection from the fitting room to the central server.
		Socket s = new Socket(ipAddress,port);
		System.out.println("Fitting Room server is currently listening for another server...");
		System.out.println("Fitting Room: Connected to the Central Server!");
		/*
		I connect to the central server acts like a client.

		ID, fitting time, wait time
		32, 400, 300
		possibly trim waiting time
		This information is being sent from the central server.

		store values in a list or dictionary or any data structure that keeps track of string from data.

		 */
		
		
		while(true) {
			//accept client
			String getDataFromCentral = "";
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
				PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
				
				//Sends a message from the central server that a client has connected.
				//System.out.println(br.readLine()); //RETURNS: "REQUEST_ROOM"

				while((getDataFromCentral = br.readLine()) != null) {
					if(getDataFromCentral.equals("REQUEST_ROOM")) {
						System.out.println("Tested the message.");
						handleClient(s);
					}
				}

			}catch(Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	//Handling the client.
	private static void handleClient(Socket socket) {
		try {
			//1. Try to enter room
			enterRoom(socket);

			//2. Simluate fitting room usage 
			//3.Exit Room 
			//4. close connection
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//Allows one thread in the method with synchroinized
	private static synchronized boolean enterRoom(Socket socket) throws Exception {
		System.out.println("The client has entered the room!");
		//CASE 1: FULL = REJECT

		//if(roomFull)
			//rejectClient(socket)

		//CASE 2: Room occupied = wait 

		/*else-if(roomsOccupied > numberOfRooms) 
			client then waits

		*/
		
		//CASE 3: Enter Room
		/* 
		else
			simulateUsage(t)
			allowClient(socket);
			
		*/
		return false; 
	}
	
	//private static void simulateUsage() throws InterruptedException{
	private static void simulateUsage(Thread t) throws InterruptedException{
		//Thread goes to sleep
		int simulationTime = 0;
		t.sleep(simulationTime);
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
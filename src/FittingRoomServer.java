import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.WebSocket;

/*
java fittingRoom 5
5 - Fitting Rooms

5 + (5 * 2) = 15 Number of chairs.
*/

class ClientHandlerFitting implements Runnable{
	public Socket client;
	public String ipAddress;

	//Other variables?
	int numOfCustomers = 5;
	int simulateUsageTime = 0;
	int numberOfChairs = numOfCustomers * 2;

	//Output Variables
	int changingCustomers = 0;
	int waitingCustomers = 0;

	public ClientHandlerFitting(Socket client) {
		this.client = client;
		this.ipAddress = client.getInetAddress().getHostAddress()+"";
	}


	public void run(){
		System.out.println("A client has connected to the server:");
		
		 
		try {
			System.out.println("Testing the run method.");
			BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
				
			//Sends a message from the central server that a client has connected.
			System.out.println(br.readLine()); //RETURNS: "REQUEST_ROOM"


			/*
			 
			String getDataFromCentral = "";
			while((getDataFromCentral = br.readLine()) != null) {
				if(getDataFromCentral.equals("REQUEST_ROOM")) {
					System.out.println("Tested the message.");
					handleClient(client);
				}
			}
			*/
			client.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			
	}


	 

	//Handling the client.
	private void handleClient(Socket socket) {
		try {
			//1. Try to enter room
			enterRoom(socket);
			//2. Simluate fitting room usage 
			
			//3.Exit Room 

			//4. close connection
			socket.close();
		}catch(Exception e) {
			//e.printStackTrace();
			System.out.println("ERROR: " + e);
		}
	}
	
	//Allows one thread in the method with synchroinized
	private synchronized boolean enterRoom(Socket socket) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
		PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
		
		
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
	private void simulateUsage() throws InterruptedException{
		//Thread goes to sleep - possible for the client
		//int simulationTime = 0;
		Thread.sleep(simulateUsageTime);
	}
	
	private void rejectClient(Socket socket) throws IOException{
		//send "ROOM FULL" 
		//close socket
		PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
		pw.println("ROOM FULL");
		socket.close();
	}
	
	private void allowClient(Socket socket) throws IOException{
		//send success message
		BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
		PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
		pw.println("The client has successfully entered the room!");
	}

 
public class FittingRoomServer {
	//Shared state
	static int inside = 0; 
	static int outside = 0;

	//Other Variables
	int changingInside = 0;
	int currentlyWaiting = 0;
	int serverNumber = 0;
	Socket socket;
	
	//THESE PROLLY NEED TO BE CHANGED. -AE
	//static final int MAX_INSIDE = 1;
	//static final int MAX_OUTSIDE = 2; 
	
	public static void main(String[] args) throws IOException {

		//HARD CODE FOR NOW, BUT CHANGE THIS WHEN SUBMITTING.
		int port = 50001; //50001
		String ipAddress = "127.0.0.1"; //"192.168.10.1";
        startServer(port,ipAddress);
	}

	//Starts the server given this port number.
	private static void startServer(int port, String ipAddress) throws IOException {
		//Connection acts like a client to the central server. This will get a connection from the fitting room to the central server.
		
		//Socket s = new Socket(ipAddress,port);
		//ServerSocket serverSocket = new ServerSocket(port);


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
			try {
				Socket s =  new Socket(ipAddress,port);
				ClientHandlerFitting fittingClient = new ClientHandlerFitting(s);
				Thread clientThread = new Thread(fittingClient);
				System.out.println("Client connected to the fitting room server: " + clientThread.getName());
				//pw.println("Connected!");
				
				clientThread.start();
				
			}catch(IOException e) {
				//e.printStackTrace();
				System.out.println("java.net.SocketException: Connection reset");
				
			}

		}

			 
		}
	}

	
	private static int getPortFromArgs(String args) {
		//parse port
		int port = Integer.parseInt(args);
		return port; 
	}
    
}
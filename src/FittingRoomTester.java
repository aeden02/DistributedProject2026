import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.WebSocket;

class ClientHandlerFitting implements Runnable{
	public Socket client;
	public String ipAddress;

	//Other variables?
	int simulateUsageTime = 0;
  

	public ClientHandlerFitting(Socket client) {
		this.client = client;
		this.ipAddress = client.getInetAddress().getHostAddress()+"";
	}


	public void run(){
        //all the logic is in here. I did not move them into the methods. -AE
		System.out.println("A client has connected to the server:");
		
		 
		try {
			
			BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
				
			//Sends a message from the central server that a client has connected.
			//System.out.println(br.readLine()); //RETURNS: "REQUEST_ROOM"


			String getDataFromCentral = "";
			while((getDataFromCentral = br.readLine()) != null) {
                System.out.println("FITTING ROOM RECEIVED: " + getDataFromCentral);

                //REQUEST ROOM FROM CENTRAL SERVER
				if(getDataFromCentral.equals("Request Room")) {

                    synchronized(FittingRoomTester.lock){
                        if(FittingRoomTester.roomsAvailable > 0){
                            FittingRoomTester.roomsAvailable--; //decrease available rooms
                            
                            System.out.println(Thread.currentThread().getName() + 
                            ": allocated room. Available rooms: " + FittingRoomTester.roomsAvailable);
                            pw.println("Room Allocated"); //send response to client
                        }else{
                            System.out.println("ROOMS FULL. Client must wait.");
                            pw.println("ROOMS FULL"); //send response to client
                        }
                    }
					// System.out.println("Tested the message.");
					// handleClient(client);

                    //RELEASING ROOM 
				}else if(getDataFromCentral.equals("Release Room")) {
                    // System.out.println("Processing room release..."); 
                    synchronized(FittingRoomTester.lock){

                        //only increment if below max 
                        if(FittingRoomTester.roomsAvailable < FittingRoomTester.MAX_ROOMS){
                            FittingRoomTester.roomsAvailable++; //increase available rooms
                            
                            System.out.println(Thread.currentThread().getName() + 
                            ": released room. Available rooms: " + FittingRoomTester.roomsAvailable);
                            
                        }else{
                            System.out.println("No Rooms to release"); 
                            pw.println("No Rooms to release"); //send response to client
                        }

                        pw.println("Room Released"); //send response to client
                    }
                }else{
                    System.out.println("Invalid message received: " + getDataFromCentral); 
                }    
			}
			br.close();
			pw.close(); 

			}catch(Exception e) {
                System.out.println("Client disconnected from fitting room"); 
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
	private void simulateUsage() throws InterruptedException{
		//Thread goes to sleep
		//int simulationTime = 0;
		Thread.sleep(simulateUsageTime);
	}
	
	private void rejectClient(Socket socket) throws IOException{
		//send "ROOM FULL" 
		//close socket 
		socket.close();
	}
	
	private void allowClient(Socket socket) throws IOException{
		//send success message
		BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
		PrintWriter pw = new PrintWriter(client.getOutputStream(), true);

		pw.println("The client has successfully entered the room!");
	}

 
}

public class FittingRoomTester {
	//Shared state
	static int inside = 0; 
	static int outside = 0;
    static int roomsAvailable = 5;
    static final int MAX_ROOMS = 5; //number of fitting rooms available
    static final Object lock = new Object(); //lock for synchronizing access to shared state
    //Object lock is just used for coordination, where synchronized(lock) = only one thread access at a time.

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
		int port = 50001;
		String ipAddress = "127.0.0.1"; //"192.168.10.1";
        startServer(port,ipAddress);
	}

	//Starts the server given this port number.
	private static void startServer(int port, String ipAddress) throws IOException {
		//Connection acts like a client to the central server. This will get a connection from the fitting room to the central server.
		
        try{
        //Socket s = new Socket(ipAddress,port);
		    ServerSocket serverSocket = new ServerSocket(50001);
		    System.out.println("Fitting Room Server running ...");

            while(true){
                Socket client = serverSocket.accept(); 
                
                System.out.println("Central Server Connected!");

                new Thread(new ClientHandlerFitting(client)).start(); 
            }
        }catch(Exception e){
            e.printStackTrace();
         System.out.println("Error in fitting room server");
        }
        /*
		I connect to the central server acts like a client. 

		ID, fitting time, wait time
		32, 400, 300
		possibly trim waiting time
		This information is being sent from the central server.
		store values in a list or dictionary or any data structure that keeps track of string from data.
		 */

		// BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
		// PrintWriter pw = new PrintWriter(client.getOutputStream(), true);

        // while(true) {
		// 	try {
		// 		Socket s = serverSocket.accept();
		// 		//BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		// 		//PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
		// 		ClientHandlerFitting fittingClient = new ClientHandlerFitting(s);
		// 		Thread clientThread = new Thread(fittingClient);
		// 		System.out.println("Client connected to the fitting room server: " + clientThread.getName());
		// 		//pw.println("Connected!");
				
		// 		clientThread.start();
				
		// 	}catch(Exception e) {
		// 		//e.printStackTrace();
		// 		System.out.println("java.net.SocketException: Connection reset");
				
		// 	}
			 
	}
    
	

	
	private static int getPortFromArgs(String args) {
		//parse port
		int port = Integer.parseInt(args);
		return port; 
	}

}
    

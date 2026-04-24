import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class CentralServer {

	static class RoomInfo{
	//Room data structure
		String host; 
		int port;
		int cap; 

		public RoomInfo(String h, int p, int c){
			this.host = h; 
			this.port = p; 
			this.cap = c; 
		}
	}
	
	//List of rooms
	//static List<RoomInfo> rooms = new List<>; 
	//how get room information from the fitting room. 
	public static void main(String[] args) {
		System.out.println("CentralServer starting..."); 

		//1. Initialize rooms
		//initializeRooms();
		
		//2. Start server socket
		startServerSockets(); 
	}
	
	private static void initializeRooms() {
		//Add rooms(host, port, capacity)
		//need information from fitting room 
		//GetRoomInfomartion.  

	}
	
	private static void startServerSockets() {
	
		try{
		//serversocket for fittingroom (FittingRoom connects to CentralServer)
			ServerSocket fitroomSocket = new ServerSocket(50001); 
			System.out.println("CENTRAL: Listening for Fitting Rooms on port 50001..."); 

		//serversocket for client(Client connects to CentralServer)
			ServerSocket server = new ServerSocket(50000);
			System.out.println("CentralServer running on port 50000...(waiting for Client)"); 


			while(true) {
			//accept fittingroom 
			Socket fittingRooom	= fitroomSocket.accept(); 
			System.out.println("CENTRAL: Fitting Room Connected!"); 

			BufferedReader fitIn = new BufferedReader(new InputStreamReader(fittingRooom.getInputStream()));
			PrintWriter fitOut = new PrintWriter(fittingRooom.getOutputStream(), true);
				
			//accept client
			Socket clientSocket = server.accept(); 
			System.out.println("CENTRAL: Client connected!"); 

			BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true); 

			//MICHAELS CHANGES
			//Testing statement.
			//fitOut.println("A client is communicating with the central server to send to the fitting room server.");
			fitOut.println("REQUEST_ROOM");

			//I am unsure if we actually need this section.
			//was trying to be able to send messages across
			//and it didn't work. - AE 
			/*
			 
			String request; 
			while(!(request = clientIn.readLine()).equalsIgnoreCase("EXIT")){
				System.out.println("Client says: " + request); 

				//send to fittingroom 
				fitOut.println("REQUEST_ROOM"); 

				//get response
				String response = fitIn.readLine(); 
				System.out.println("Fitting room says: " + response); 

				//send back to client
				clientOut.println(response); 
			}
			//handle each client in a new thread
			
			//handle each FittingRoom in a new thread


			*/
			}

			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	// public static void handleClient(Socket socket) {
	// 	//1. find available rooms
	// 	try{
	// 		RoomInfo room = getAvaliableRoom(); 

	// 		if(room == null){
				
	// 		}
	// 	}
	// 	//2. send response
	// 	//3. close connection
	// }
	
	// private static synchronized RoomInfo getAvaliableRoom() {
	// 	//loop through rooms 
	// 	for(RoomInfo room: rooms){
	// 		//if cap > 0 then reserve slot and return 
	// 		if(room.cap > 0){
	// 			room.cap--; //this reserves a slot 
	// 			System.out.println("Assigned room " + room.port +
	// 				" (remain capcaity: " + room.cap + ")"); 
	// 				return room; 
	// 		}else{
	// 			return null; 
	// 		}
	// 	}
	// }

	// private static void sendRoomAssignment(Socket socket, RoomInfo room) {
	// 	//write "host:port" 
	// }
	
}
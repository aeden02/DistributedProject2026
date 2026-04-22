import java.io.IOException;
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
	static List<RoomInfo> rooms = new ArrayList(); 
	//how get room information from the fitting room. 
	public static void main(String[] args) {
		System.out.println("Server starting..."); 

		//1. Initialize rooms
		//initializeRooms();
		
		//2. Start server socket
		System.out.println("STARTING SERVER SOCKETS");
		startServerSockets(); 
	}
	
	// private static void initializeRooms() {
	// 	//Add rooms(host, port, capacity)
	// 	//need information from fitting room 
	// 	//GetRoomInfomartion.  
	// 	RoomInfo r1 = new RoomInfo("localhost",5001,3); 
	// 	rooms.add(r1); 

	// 	System.out.println("Rooms initalized: " + rooms.size()); 
	// 	System.out.println(r1.host + " " + r1.port + " " + r1.cap); 
	// }
	
	private static void startServerSockets() {
	
		try{
			//serversocket for client
			ServerSocket server = new ServerSocket(50000);
			System.out.println("Server running on port 50000..."); 

			//serversocket for fittingroom 
			ServerSocket fitroom = new ServerSocket(50001); 
			System.out.println("Server running on port 50001..."); 
		
			while(true) {
			//accept client
			Socket socket = server.accept(); 
			System.out.println("Client connected!"); 

			//accept fittingroom 
			Socket fitRoomSocket = fitroom.accept(); 
			System.out.println("Fitting room connected!"); 
		
			//create new thread 

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

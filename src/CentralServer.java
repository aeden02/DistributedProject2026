import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
	static List<RoomInfo> rooms = new ArrayList(); 
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
		//socket for fittingroom (CentralServer connects to FittingRoom)
			Socket fitroomSocket = new Socket("localhost",50001); 
			System.out.println("Connected to FittingRoom"); 

		//Input&Output for fittingroom 
			BufferedReader fitIn = new BufferedReader(new InputStreamReader(fitroomSocket.getInputStream()));
			PrintWriter fitOut = new PrintWriter(fitroomSocket.getOutputStream(),true); 

		//serversocket for client(Client connects to CentralServer)
			ServerSocket server = new ServerSocket(50000);
			System.out.println("CentralServer running on port 50000...(waiting for Client)"); 
		
			while(true) {
			//accept client
			//Socket clientSocket = server.accept(); 
			//System.out.println("CENTRAL: Client connected!"); 

			//BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			//PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true); 

			//I am unsure if we actually need this section.
			//was trying to be able to send messages across
			//and it didn't work. - AE 

			ClientHandler client;
			try {
                client = new ClientHandler(server.accept(), fitroomSocket);
                    
                Thread t = new Thread(client);

                String message = "Thread " + t.getName() + " has been assigned to this client";
				
				System.out.println(message);

			} catch (Exception e) {
                    server.close();
                    e.printStackTrace();
			}

			/*
			String request; 
			while((request = clientIn.readLine())!=null){
				System.out.println("Client says: " + request); 

				//send to fittingroom 
				fitOut.println("REQUEST_ROOM"); 

				//get response
				String response = fitIn.readLine(); 
				System.out.println("Fitting room says: " + response); 

				//send back to client
				clientOut.println(response); 
				
			}
			*/
			//handle each client in a new thread
			

			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public static class ClientHandler implements Runnable{
		Socket client;
		Socket fit;

		public ClientHandler(Socket client, Socket fit){
			this.client = client;
			this.fit = fit;
		}

        @Override
        public void run() {
			System.out.println("running...");
            try{
                InputStream in = client.getInputStream();
                OutputStream out = client.getOutputStream();

                BufferedReader clientIn = new BufferedReader(new InputStreamReader(in));
                PrintWriter clientOut = new PrintWriter(out,true);
                
				//BufferedReader fitIn = new BufferedReader(new InputStreamReader(fit.getInputStream()));
				PrintWriter fitOut = new PrintWriter(fit.getOutputStream(),true); 

                String request;
				while((request = clientIn.readLine())!=null){
					//Client should request a fitting room which connects them to a fitting server, wait for a bit, then exit

					System.out.println("Client says: " + request); 

					//send to fittingroom 
					fitOut.println("REQUEST_ROOM"); 

					//get response
					String response = clientIn.readLine(); 
					System.out.println("Fitting room says: " + response); 

					//send back to client
					clientOut.println(response); 
				}

			}
			catch(Exception e){
				e.printStackTrace();
			}
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

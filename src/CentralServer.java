import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.LinkedList;

public class CentralServer {
	//static int roomsAvailable = 3; 
	static Socket fittingRoom; //socket for fitting room connection.
	static PrintWriter fitOut;
	static BufferedReader fitIn;

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
	


	//how get room information from the fitting room. 
	public static void main(String[] args) {
		System.out.println("CentralServer starting..."); 

		startServerSockets(); 
	}
	
	private static void initializeRooms() {
		//Add rooms(host, port, capacity)
		//need information from fitting room 
		//GetRoomInfomartion.  

	}
	

	//ONLY handle client connections now. Removed shared fittingroom socket 
	//Each client thread with create its own connection. -AE
	private static void startServerSockets() {
	
		try{
		//serversocket for fittingroom (FittingRoom connects to CentralServer)
			ServerSocket fitroomSocket = new ServerSocket(50001); 
			System.out.println("CENTRAL: Listening for Fitting Rooms on port 50001..."); 

		//serversocket for client(Client connects to CentralServer)
			ServerSocket server = new ServerSocket(50000);
			System.out.println("CentralServer running on port 50000...(waiting for Client)"); 

			
			//accept fittingroom
				// fittingRoom	= fitroomSocket.accept(); 
				// System.out.println("CENTRAL: Fitting Room Connected!"); 

				// fitOut = new PrintWriter(fittingRoom.getOutputStream(),true);
				// fitIn = new BufferedReader(new InputStreamReader(fittingRoom.getInputStream()));	
			
			while(true){
				//accept client
				System.out.println("CENTRAL: About to accept client connection..."); 
				Socket clientSocket = server.accept(); 

				//NEED TO ADD FITTING ROOM SOCKET TO THE CLIENT HANDLER
				//I removed it to test just the client connection and it worked. -AE

              	ClientHandler client = new ClientHandler(clientSocket,fitroomSocket);
                    
           		Thread t = new Thread(client);
				
            	String message = "Thread " + t.getName() + " has been assigned to this client";
				
				System.out.println(message);
				t.start(); 
			}
				
		} catch (Exception e) {
			//server.close(); 
            e.printStackTrace();
		}


	}



	//This is the Client handler class where all the clients will run the thread NOT TESTED YET
	public static class ClientHandler implements Runnable{
		private BufferedReader fitIn; 
		private PrintWriter fitOut;
		public Socket client;
		private ServerSocket fit;
		private Socket fitClient;
		private boolean hasRoom = false; 
		PrintWriter clientOut;
		private boolean isActive = true; 

		//waiting queue for clients when rooms are full. -AE
		static Queue<ClientHandler> waitingClients = new LinkedList<>();

		public ClientHandler(Socket client,ServerSocket fit){
			this.client = client;
			this.fit = fit;
		}

		public void assignRoom(){

			if(!isActive){
				System.out.println("Cannot assign room to inactive client.");
				return; 
			}

			synchronized(this){
				hasRoom = true; 
			}

			clientOut.println("Room Allocated"); 
		}

        @Override
        public void run() {
			
			System.out.println("running...");

            try{
				//CLIENT STREAMS
                InputStream in = client.getInputStream();
                OutputStream out = client.getOutputStream();

                BufferedReader clientIn = new BufferedReader(new InputStreamReader(in));
                clientOut = new PrintWriter(out,true);


				//Connect to fitting room here. Each thread gets its own connection. -AE
				//fit = new Socket("localhost", 50001);
				fitClient = fit.accept();

				fitIn = new BufferedReader(new InputStreamReader(fitClient.getInputStream()));
				fitOut = new PrintWriter(fitClient.getOutputStream(),true);
				
                String request;
				while((request = clientIn.readLine())!=null){
					//Client should request a fitting room which connects them to a fitting server, 
					// wait for a bit, then exit

					System.out.println("Client says: " + request); 

					//REQUESTING ROOM
					if(request.equals("Request Room")){

						if (!hasRoom){
							
							//send requestto fittingroom 
							fitOut.println("Request Room");
							//fitOut.flush();

							//read response
							String response = fitIn.readLine(); 
							System.out.println("CENTRAL GOT FROM FITTING ROOM: " + response);
								
							if(response==null){
								clientOut.println("ERROR WITH FITTING ROOM RESPONSE IS NULL");
							 }
								 
							 if(response.equals("Room Allocated")){//send response to client
									hasRoom = true; //mark client as having a room.
									clientOut.println("Room Allocated"); //send response to client
									System.out.println(Thread.currentThread().getName() + 
									" is in the fitting room.");
									
								}else if(response.equals("ROOMS FULL")){

									//ADD TO WAITING QUEUE AND WAIT FOR ROOM TO BE AVAILABLE. -AE
									synchronized(waitingClients){
										waitingClients.add(this); //add client to waiting queue if rooms are full. 
									}

									clientOut.println("No Rooms Available. Waiting..."); 
									System.out.println(Thread.currentThread().getName() + 
									" added to waiting queue. Queue size: " + waitingClients.size()); 
								}else{
									System.out.println("Invalid response from fitting room: " + response); 
									clientOut.println("ERROR WITH FITTING ROOM: INVALID RESPONSE"); //send response to client
								}

						}else{
							System.out.println("Client already has a room."); 
							clientOut.println("Already have a room"); //send response to client
						}
					
					//RELEASING ROOM
					}else if(request.equals("Release Room")){
												
						synchronized(CentralServer.class){
							System.out.println(Thread.currentThread().getName() + 
							" hasRoom before release: " + hasRoom);

							if(hasRoom){

							//release current client first. 
								hasRoom = false; //SET FIRST BEFORE RELEASING TO PREVENT MULTIPLE RELEASES.

								if(fitOut != null){
									fitOut.println("Release Room"); // only send if valid 
								}
							
							System.out.println(Thread.currentThread().getName() + " released a room.");

							//assign next waiting client if any. 
							synchronized(waitingClients){

								if(!waitingClients.isEmpty()){
																		
									ClientHandler next = waitingClients.poll();

										if(next != null && next.isActive){
											next.assignRoom(); //assign room to next client in queue.
											System.out.println("Room Allocated to waiting client. Queue size: "
											 + waitingClients.size()); 
										}
										
									}else{
										System.out.println("No waiting clients to assign room to."); 
									}
								} 
								
								clientOut.println("Room Released");
			
							}else{
								clientOut.println("No Rooms to release"); 
							}
						}

						//exiting the client connection
					}else if(request.equalsIgnoreCase("Exit")){
						System.out.println("Client requested exit. Closing connection...");
						break; 
					}else{
						clientOut.println("Invalid Request");
					}
				}
				//client.close(); 

			}catch(Exception e){
				System.out.println("Client disconnected unexpectedly.");
				//e.printStackTrace();
			} 

			//In case of any exception or client disconnection. -AE
			finally{

				try{

				isActive = false; //this marks client as dead. 

				//remove from queue if needed. 
				synchronized(waitingClients){
					waitingClients.remove(this);
					System.out.println(Thread.currentThread().getName() + 
						" removed from waiting queue due to disconnection."); 
				}

				//release room if cilinet disconnects while holidng room. 
				synchronized(CentralServer.class){
					if(hasRoom){
						System.out.println(Thread.currentThread().getName() + 
						" releasing a room in finally block.");

						hasRoom = false; //SET FIRST BEFORE RELEASING TO PREVENT MULTIPLE RELEASES.

					}	
					
					//close sockets
					if(fit !=null && !fit.isClosed())
						fit.close(); 
				

					if(client != null && !client.isClosed())
						client.close();
				}
				
				}catch(Exception e){
					e.printStackTrace();
				}	

			}
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
	


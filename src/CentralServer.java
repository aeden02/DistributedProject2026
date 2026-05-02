import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class CentralServer {
	//static int roomsAvailable = 3; 
	static Socket fittingRoom; //socket for fitting room connection.
	static PrintWriter fitOut;
	static BufferedReader fitIn;

	//Map that holding client id and corresponding threads. 
	private static Map<Integer, ClientHandler> clientIDs = new HashMap<>();

	
	


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
				fittingRoom	= fitroomSocket.accept(); 
				System.out.println("CENTRAL: Fitting Room Connected!"); 

				fitOut = new PrintWriter(fittingRoom.getOutputStream(),true);
				 fitIn = new BufferedReader(new InputStreamReader(fittingRoom.getInputStream()));	
			
			while(true){
				//accept client
				System.out.println("CENTRAL: About to accept client connection..."); 
				Socket clientSocket = server.accept(); 

				

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
		private boolean isWaiting = false; 
		PrintWriter clientOut;
		private boolean isActive = true; 
		public int clientID = -1; //client id from client request. AE
	

		//waiting queue for clients when rooms are full. -AE
		//static Queue<ClientHandler> waitingClients = new LinkedList<>();

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
	
                String request;
				while((request = clientIn.readLine())!=null){
					//Client should request a fitting room which connects them to a fitting server, 
					// wait for a bit, then exit

					System.out.println("Client says: " + request); 
			

					//REQUESTING ROOM
					if(request.startsWith("Request Room")){
						String [] parts = request.split(" ");
						if(parts.length >= 3){
							clientID = Integer.parseInt(parts[2]);
							CentralServer.clientIDs.put(clientID, this); 
							//store client handler in map with clientID as key.
						}

						if (!hasRoom){
							
							//send requestto fittingroom 
							System.out.println("FORWARDING REQUEST TO FITTING ROOM: " + request);

							CentralServer.fitOut.println("ALLOCATE " + clientID);
							//fitOut.flush();

							//read response
							String response = CentralServer.fitIn.readLine(); 
							

							System.out.println("CENTRAL GOT FROM FITTING ROOM: " + response);

								
							if(response==null){
								clientOut.println("ERROR WITH FITTING ROOM RESPONSE IS NULL");
							 }
								 
							 if(response.startsWith("Allocated")){ //send response to client

									isWaiting = false; //no longer waiting for room. 
									hasRoom = true; //mark client as having a room.
									clientOut.println("Room Allocated " + clientID); //send response to client
									System.out.println("Client "+ clientID + " is in the fitting room.");
									hasRoom = true; 

								}else if(response.startsWith("Wait")){
									
									isWaiting = true; 
									clientOut.println("Wait"); //send response to client
									System.out.println("Client " + clientID + 
									" added to waiting chairs"); 

								}else if(response.startsWith("Full")){

									clientOut.println("Full"); 
									System.out.println("Client " + clientID + 
									" informed that fitting rooms & waiting chairs are full. Client will exit.");
								
								}else if(response.startsWith("Next")){
									String[] responseParts = response.split(" ");
									int nextClientID = Integer.parseInt(responseParts[1]); 

									System.out.println("Client " + nextClientID + " is next in line for a fitting room.");
									
									ClientHandler nextClient = CentralServer.clientIDs.get(nextClientID);

									if(nextClient != null && nextClient.clientOut !=null){
										nextClient.clientOut.println("Room Allocated " + nextClientID); 
										nextClient.hasRoom = true; 
										//notify client that a room is available. 
										System.out.println("Routing NEXT to client " + nextClientID); 
									}else{
										System.out.println("WARNING: Client " + nextClientID + "not found or disconnected.");
									}
								
								}else{
									System.out.println("Invalid response from fitting room: " + response); 
									clientOut.println("ERROR WITH FITTING ROOM: INVALID RESPONSE"); //send response to client
								}

						}else{
							System.out.println("Client already has a room."); 
							clientOut.println("Already have a room"); //send response to client
						}
					
					
					//RELEASING ROOM
					} else if(request.startsWith("Release Room")){
													
						synchronized(CentralServer.class){
							System.out.println("Client hasRoom before release: " + hasRoom);

							if(hasRoom){

							//release current client first. 
								hasRoom = false; //SET FIRST BEFORE RELEASING TO PREVENT MULTIPLE RELEASES.

								if(CentralServer.fitOut != null){
									CentralServer.fitOut.println("RELEASE " + clientID);
									
								}
							
							System.out.println("Client released a room.");
						
								clientOut.println("Room Released");

			
							}else{
								clientOut.println("No Rooms to release"); 
							}
						}

						//exiting the client connection
					} else if(request.equalsIgnoreCase("Exit")){
						System.out.println("Client requested exit. Closing connection...");
						break; 
					} else{
						clientOut.println("Invalid Request");
					}
				
				}

			}catch(Exception e){

				System.out.println("CENTRAL ERROR: Connection Disconnected.  ");
				return; 
			} 

			//In case of any exception or client disconnection. -AE
			finally{

				try{

				isActive = false; //this marks client as dead. 
				if(!isWaiting){
					CentralServer.clientIDs.remove(clientID); //remove from client map 
				}
				
				//release room if cilinet disconnects while holidng room. 
				synchronized(CentralServer.class){
					if(hasRoom){
						
						System.out.println("Client " + clientID +
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
        }//end of run method
	}//end of client handler class

}//end of central server class
	




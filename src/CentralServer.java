/********************************
Name: Team 2:
Spencer Giles
Courtney Nguyen
Matthew Ringgold
Michael Delgado
Allison Eden
Problem Set: Final Group Project
Due Date: May 14, 2026
********************************/



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class CentralServer{
	
	static Socket fittingRoom; //socket for fitting room connection.
	static PrintWriter fitOut;
	static BufferedReader fitIn;

	//Map that holding client id and corresponding threads. 
		public static Map<Integer, ClientHandler> clientIDs = new HashMap<>();

		//waiting queue for clients when rooms are full. -AE
		static Queue<ClientHandler> waitingClients = new LinkedList<>();

		//queue for clients that are in the fitting server -Courtney
		static Queue<ClientHandler> fittingRoomClients = new LinkedList<>();

		//ArrayList to hold the FittingRoom Handlers
		static ArrayList<FittingRoomHandler> fittingRooms = new ArrayList<>(); 
	
	//how get room information from the fitting room. 
	public static void main(String[] args) {
		System.out.println("CentralServer starting..."); 
        new Thread(() -> {
            while (true) {
                System.out.println("Searching for fittingRooms");
                startFittingServers();
            }
        }).start();

        new Thread(() -> {
            while (true) {
                System.out.println("Searching for Clients");
                startServerSockets();
            }
        }).start();

	}

	

	//ONLY handle client connections now. Removed shared fittingroom socket 
	//Each client thread with create its own connection. -AE
	private static void startServerSockets() {
	
		try{

		//serversocket for client(Client connects to CentralServer)
			ServerSocket server = new ServerSocket(50000);
			System.out.println("CentralServer running on port 50000...(waiting for Client)"); 

			
			while(true){
				//accept fitting room
				//fittingRoom	= fitroomSocket.accept(); 
				//System.out.println("CENTRAL: Fitting Room Connected!"); 

				//accept client
				System.out.println("CENTRAL: About to accept client connection..."); 
				Socket clientSocket = server.accept(); 

              	ClientHandler client = new ClientHandler(clientSocket);
                    
           		Thread t = new Thread(client);
				
            	String message = "Thread " + t.getName() + " has been assigned to this client";
				
				System.out.println(message);

				//adds clients to a waiting queue
				waitingClients.add(client);

				t.start(); 
			}
				
		} catch (Exception e) {
			//server.close(); 
            e.printStackTrace();
		}


	}

	public static void printConnectedRooms(){
		System.out.println("====Connected Fitting Room Servers ====");
		for(FittingRoomHandler room : fittingRooms){
			if(room.active){
			System.out.println("IP: " + room.getFittingRoomIP() + "| Active: " + room.active);
			}else{
				System.out.println("IP: " + room.getFittingRoomIP() + "| Active: " + room.active);
			}
		}
		System.out.println("Total: " + fittingRooms.size()); 
	}
	public static void startFittingServers(){

		try{
		//serversocket for fittingroom (FittingRoom connects to CentralServer)
			ServerSocket fitroomSocket = new ServerSocket(50001); 
			System.out.println("CENTRAL: Listening for Fitting Rooms on port 50001..."); 

			
			while(true){
				//accept fitting room
				fittingRoom	= fitroomSocket.accept(); 
				System.out.println("CENTRAL: Fitting Room Connected!"); 

              	//ClientHandler client = new ClientHandler(clientSocket);
                FittingRoomHandler fitting = new FittingRoomHandler(fittingRoom);
				fittingRooms.add(fitting); 
				

           		Thread t = new Thread(fitting);
				
            	String message = "Fitting room ith ip: " + fittingRoom.getInetAddress().getHostAddress() + 
				" has connected. Total Fitting Rooms Servers: " + fittingRooms.size();
				
				System.out.println(message);
				
				printConnectedRooms(); 

				t.start(); 
			}
				
		} catch (Exception e) {
			//server.close(); 
            e.printStackTrace();
		}


	}

	private static boolean checkFittingServer(String host){
		try {
			InetAddress fitting = InetAddress.getByName(host);

			if(fitting.isReachable(1000)){
				return true;
			}
			else{
				return false;
			}

		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return false;

	}




	//This is the Client handler class where all the clients will run the thread 
	public static class ClientHandler implements Runnable{
		private BufferedReader fitIn; 
		private PrintWriter fitOut;
		public Socket client;
		private ServerSocket fit;
		private Socket fitClient;
		private FittingRoomHandler assignedRoom; 
		private boolean hasRoom = false; 
		private boolean wasChanging = false;
		private boolean isWaiting = false; 
		private boolean recoveringClient = false; 
		PrintWriter clientOut;
		private boolean isActive = true; 
		public int clientID = -1; //client id from client request. AE
	


		public ClientHandler(Socket client){
			this.client = client;
		}

		public void assignRoom(FittingRoomHandler room){

			if(!isActive){
				System.out.println("Cannot assign room to inactive client.");
				return; 
			}

			synchronized(this){
				assignedRoom = room; 

				hasRoom = true;
				isWaiting = false; 
				
				wasChanging = false;
				recoveringClient = false; 
			}

			
			if(!fittingRoomClients.contains(this)){
				fittingRoomClients.add(this);
			}

			waitingClients.remove(this);
			
			if(room!=null && !room.assignedClients.contains(this)){
				room.assignedClients.add(this); 
			}

			clientOut.println("Room Allocated " + clientID + ":" + room.getFittingRoomIP()); 
		}

		
		public void requestNewRoom(){
			String response;

			while(isActive){
			try{
				//check each fitting room for availablility and reassigns client
				for(FittingRoomHandler room : CentralServer.fittingRooms){
					if(room.active){

						if(wasChanging){
							response = room.sendRequest("PRIORITY " + clientID);
						}else{
							response = room.sendRequest("ALLOCATE " + clientID);

						}

						if(response.startsWith("Allocated ") || response.startsWith("Promoted")){
							assignRoom(room); 

							clientOut.println("Room Reassigned " + clientID); 
						
							return; 

						}else if(response.startsWith("Wait")){

							isWaiting = true;
							hasRoom = false;
							
							//client successfully entered a new waititng queue
							recoveringClient = false; 

							assignedRoom = room; 

							if(!room.assignedClients.contains(this)){
								room.assignedClients.add(this); 
							}

							clientOut.println("Wait " + clientID); 

							return; 
						}else if(response.startsWith("Replaced")){
							String[] parts = response.split(" ");

							int removedID = Integer.parseInt(parts[1]); 

							ClientHandler removedClient = CentralServer.clientIDs.get(removedID); 

							if(removedClient !=null){
								removedClient.isWaiting = false; 
								removedClient.hasRoom = false; 

								removedClient.assignedRoom = null;

								//stop removed client from endlessly retrying recovery
								removedClient.wasChanging = false;
								removedClient.recoveringClient = false; 

								removedClient.clientOut.println("Removed From Waiting Queue"); 
							}

							isWaiting = true;
							hasRoom = false; 

							//keep priority status
							wasChanging = true; 

							//recovery succesfull
							recoveringClient = false; 

							assignedRoom = room; 

							if(!room.assignedClients.contains(this)){
								room.assignedClients.add(this); 
							}

							clientOut.println("Wait " + clientID); 

							return; 
						
						}else if(response.startsWith("Full")){

							//try next fitting room 
							continue; 
						}
	
						
					}
				}

				//If this client came from a crashed server,
				//keep retrying instead of immediately exiting. 
				if(wasChanging || recoveringClient){
					try{
						Thread.sleep(500); 
					}catch(Exception e){
						e.printStackTrace();
					}//try again later

					continue; 
				}

				//Normal clients leave if all fitting rooms are full
				clientOut.println("Full " + clientID); 

				wasChanging = false;
				hasRoom = false; 
				isWaiting = false; 
				return; 
				
		
		
			}catch(Exception e){
				System.out.println("ERROR: REQUESTING A NEW ROOM"); 

				try{
					Thread.sleep(500);
					
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		
		}
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
							String response = null; 

							//** START OF LOGIC FOR THE ROUND ROBIN OF FITTING ROOMS
							

							for(int i = 0; i < CentralServer.fittingRooms.size(); i++){

								int index = (clientID + i) % CentralServer.fittingRooms.size(); 
								System.out.println("Trying fitting room index: " + index); 

								FittingRoomHandler room = CentralServer.fittingRooms.get(index); 

								try{
								response = room.sendRequest("ALLOCATE " + clientID); 
								}catch(IOException e){
									System.out.println("Skipping dead fitting room.");

									continue; 
								}
								if(!response.startsWith("Full")){

									assignedRoom = room; 

									break; 
								}
							}

							
							//END OF LOGIC FOR FITTING ROOM SERVERS
	

							System.out.println("CENTRAL GOT FROM FITTING ROOM: " + response);

								
							if(response==null){
								clientOut.println("Fitting Room Server Down");
							 }
								 
							 if(response.startsWith("Allocated")){ //send response to client

									assignRoom(assignedRoom);

									clientOut.println("Room Allocated " + clientID + ":" + assignedRoom.getFittingRoomIP()); //send response to client
									
									System.out.println("Client "+ clientID + " is in the fitting room.");
									

								}else if(response.startsWith("Wait")){
									
									isWaiting = true; 

									if(!assignedRoom.assignedClients.contains(this)){
									assignedRoom.assignedClients.add(this); //adding waiting clients to the queue.
									}

									clientOut.println("Wait " + ":" + assignedRoom.getFittingRoomIP()); //send response to client
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
								
								}
								else if(response.startsWith("Replaced")) {
									String[] responseParts = response.split(" ");

									System.out.println("Client " + responseParts[1] + " got replaced with " + responseParts[3]);
									
									clientOut.println("Wait"); //send response to client
									//System.out.println("Client " + clientID + 
									//" has been kicked"); 

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

							if(!hasRoom){
								System.out.println("Ignoring release from client with no room.");
								clientOut.println("No rooms to release"); 
								continue; 
							}

							if(hasRoom && (assignedRoom==null || !assignedRoom.active)){
								System.out.println("Assigned fitting room died before release.");
								hasRoom = false; 
								isWaiting = false; 
								assignedRoom = null; 

								clientOut.println("Fitting Room Server Down."); 

								return; 
							}

							if(hasRoom){

								String response = null; 

								try{
								response = assignedRoom.sendRequest("RELEASE " + clientID);  

								hasRoom = false; 

								assignedRoom.assignedClients.remove(this); 

								System.out.println("CENTRAL GOT FROM FITTING ROOM: " + response); 

								}catch(Exception e){

									System.out.println("Release failed because fitting room crashed."); 

									hasRoom = false; 
									isWaiting = false;
									assignedRoom = null; 
									
									return; 
								}

								

								if(response != null && response.startsWith("Promoted") ){
									String [] parts = response.split(" "); 

									int nextClientID = Integer.parseInt(parts[1]); 

									ClientHandler nextClient = CentralServer.clientIDs.get(nextClientID); 

									if(nextClient != null){

										nextClient.assignRoom(assignedRoom); 

										//client successfully recoverd
										nextClient.wasChanging = false; 
										nextClient.recoveringClient = false; 

										nextClient.clientOut.println("Room Allocated " + nextClientID); 

										System.out.println("Client " + nextClientID + " promoted from queue"); 
									}
									
								} 
							

							fittingRoomClients.remove(this);
							
							System.out.println("Client " + clientID + " released a room.");
							
							clientOut.println("Room released");
						}else{
							clientOut.println("No rooms to release"); 
						}
					}
						//exiting the client connection
					} else if(request.equalsIgnoreCase("Exit")){
						System.out.println("Client requested exit. Closing connection...");

						//mark client inactive
						isActive = false; 

						//remove client from global client map
						CentralServer.clientIDs.remove(clientID); 

						//remove from fitting room tracking
						if(assignedRoom != null){
							assignedRoom.assignedClients.remove(this); 
						}

						try{
							clientOut.close();
							clientIn.close(); 
							

						}catch(Exception e){
							e.printStackTrace();
						}

						return; 

					} else{
						clientOut.println("Invalid Request");
					}
				
				}

			}
			catch(Exception ex){

				System.out.println("CENTRAL ERROR:");
				System.out.println("FITTING ROOM SERVER FAILED.");
				System.out.println("Reassigning affected clients....\n");
				
				return; 
			} 
			//In case of any exception or client disconnection. -AE
			finally{

				try{

				isActive = false; //this marks client as dead. 
				
				
				
				
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
	public static class FittingRoomHandler implements Runnable{
		String fitIP;
		int rooms;
		Socket fitting;
		BufferedReader fitIn; 
		PrintWriter fitOut; 
		boolean active = true; 
		public boolean recovering = false; 
		ArrayList<ClientHandler> assignedClients = new ArrayList<>(); 

		public String getFittingRoomIP() {
			return fitting.getInetAddress().getHostAddress()+"";
		}

		public FittingRoomHandler(Socket fitting){
			this.fitting = fitting;
			this.fitIP = fitting.getInetAddress().getHostAddress();
			
			try{
			fitIn = new BufferedReader(new InputStreamReader(fitting.getInputStream()));
			fitOut = new PrintWriter(fitting.getOutputStream(),true); 
			}catch(IOException e){
				System.out.println("IO EXCEPTION FROM FITIN AND FITOUT"); 
			}
			
			
		}

        @Override
        public void run() {
			
			System.out.println("Fitting room handler thread started."); 
			
        }
		public void recoverClients(){

			ArrayList<ClientHandler> temp = new ArrayList<>(assignedClients); 
			ArrayList<ClientHandler> recoveryList = new ArrayList<>();
			assignedClients.clear(); 

			recovering = true;

			CentralServer.printConnectedRooms();
			
			//clean up client states 
			for(ClientHandler client : temp){
				try{
					
					if(client.hasRoom){

						client.wasChanging = true; 
						client.recoveringClient = true; 

						client.hasRoom = false; 
						client.isWaiting = false; 

						client.assignedRoom = null; 

						client.clientOut.println("Fitting Room Server Down");

						recoveryList.add(client); 


					}else if(client.isWaiting){
						client.wasChanging = false; 
						client.recoveringClient = true; 

						client.isWaiting = false;
						client.hasRoom = false;

						client.assignedRoom = null; 

						client.clientOut.println("Fitting Room Server Down");

						recoveryList.add(client);

					}else{

					}
					
				}catch(Exception e){
					e.printStackTrace(); 
				}
			}

				//after clean up, recover clients. 
			for(ClientHandler client : recoveryList){
					try{
						Thread.sleep(100); 

						//Each recovered client gets own reassignment thread. 
						//prevents the crashed fitting room recovery loop from blocking
						//while clients search for a new room and avoids duplicate requests 
						//caused by multiple threads modifying the same client state.

						new Thread(()->{
							client.requestNewRoom();
						}).start(); 

						
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				recovering=false; 
			}
		

		public synchronized String sendRequest(String msg) throws IOException{
			try{
				fitOut.println(msg); 

				String response = fitIn.readLine(); 

				
				if(response == null){

					if(!recovering){
						System.out.println("====CRASHED SERVER IP: " + getFittingRoomIP()); 

						synchronized(CentralServer.fittingRooms){
							recovering = true; 
							active = false; 
 
							CentralServer.fittingRooms.remove(this); 
							System.out.println("Remaining Fitting Room Servers: " + CentralServer.fittingRooms.size());
						}

						CentralServer.printConnectedRooms();				
						recoverClients();
					}


					throw new IOException("Fitting Room disconnected " + getFittingRoomIP()); 

					
				}

				return response; 
			}catch(IOException e){

				if(!recovering){
					System.out.println("===FITTING ROOM CRASHED==="); 
					System.out.println("=====Crashed Server IP: " + getFittingRoomIP()); 

					synchronized(CentralServer.fittingRoom){
						recovering = true;
						active = false; 

						System.out.println("FITTING ROOM SERVER DISCONNECTED"); 

						CentralServer.fittingRooms.remove(this); 
						System.out.println("Remaining Fitting Room Servers: " + CentralServer.fittingRooms.size()); 
					
					}
					
					recoverClients(); 
				}
			

				throw e; 
			}
			
		}

	}


}//end of central server class
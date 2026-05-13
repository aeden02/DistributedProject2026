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
	private static Map<Integer, ClientHandler> clientIDs = new HashMap<>();

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

				//adds clients to a waiting queue
				//waitingClients.add();

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
		PrintWriter clientOut;
		private boolean isActive = true; 
		public int clientID = -1; //client id from client request. AE
	


		public ClientHandler(Socket client){
			this.client = client;
		}

		public void assignRoom(){

			if(!isActive){
				System.out.println("Cannot assign room to inactive client.");
				return; 
			}

			synchronized(this){
				hasRoom = true; 
			}
			fittingRoomClients.add(this);
			waitingClients.remove(this);

			clientOut.println("Room Allocated"); 
		}

		public void requestNewRoom(){
			String response;
			try{
				//check each fitting room for availablility and reassigns client
				for(FittingRoomHandler room : CentralServer.fittingRooms){
					if(room.active){
						if(wasChanging){
							response = room.sendRequest("PRIORITY " + clientID);

						}
						else{
							response = room.sendRequest("ALLOCATE " + clientID);

						}

						if(response.startsWith("Allocated")){
							assignedRoom = room; 

							hasRoom = true;

							clientOut.println("Room Reassigned"); 

							room.assignedClients.add(this); 

							return; 
						}
						else if(response.startsWith("Full")){
							
						}
					}
				}

				clientOut.println("No fitting rooms available"); 
			}catch(Exception e){
				System.out.println("ERROR: REQUESTING A NEW ROOM"); 
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
								clientOut.println("ERROR WITH FITTING ROOM RESPONSE IS NULL");
							 }
								 
							 if(response.startsWith("Allocated")){ //send response to client

									isWaiting = false; //no longer waiting for room. 
									hasRoom = true; //mark client as having a room.
									wasChanging = true;
									assignedRoom.assignedClients.add(this); 
									clientOut.println("Room Allocated " + clientID + ":" + assignedRoom.getFittingRoomIP()); //send response to client
									System.out.println("Client "+ clientID + " is in the fitting room.");
									hasRoom = true; 

								}else if(response.startsWith("Wait")){
									
									isWaiting = true; 

									assignedRoom.assignedClients.add(this); //adding waiting clients to the queue. 
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

							if(hasRoom){

							//release current client first. 
								hasRoom = false; //SET FIRST BEFORE RELEASING TO PREVENT MULTIPLE RELEASES.

								String response = assignedRoom.sendRequest("RELEASE " + clientID);  
								assignedRoom.assignedClients.remove(this); 
								System.out.println("CENTRAL GOT FROM FITTING ROOM: " + response); 

								if(response != null && response.startsWith("Allocated") ){
									String [] parts = response.split(" "); 

									int nextClientID = Integer.parseInt(parts[1]); 

									ClientHandler nextClient = CentralServer.clientIDs.get(nextClientID); 

									if(nextClient != null){
										nextClient.clientOut.println("Room Allocated " + nextClientID); 

										nextClient.hasRoom = true; 

										System.out.println("Client " + nextClientID + " promoted from queue"); 
									}
								} 
							

							fittingRoomClients.remove(this);
							
							System.out.println("Client released a room.");
							
							clientOut.println("Room released");
						}else{
							clientOut.println("No rooms to release"); 
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
	public static class FittingRoomHandler implements Runnable{
		String fitIP;
		int rooms;
		Socket fitting;
		BufferedReader fitIn; 
		PrintWriter fitOut; 
		boolean active = true; 
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
			for(ClientHandler client : assignedClients){
				try{
					client.hasRoom = false; 
					client.isWaiting = false; 

					client.clientOut.println("Fitting Room Server Down");

					client.requestNewRoom(); 
				}catch(Exception e){
					e.printStackTrace(); 
				}
			}
		}
		public synchronized String sendRequest(String msg) throws IOException{
			try{
				fitOut.println(msg); 

				String response = fitIn.readLine(); 

				if(response == null){
					throw new IOException("Fitting Room disconnected"); 
				}

				return response; 
			}catch(IOException e){
				active = false; 

				System.out.println("FITTING ROOM SERVER DISCONNECTED"); 

				CentralServer.fittingRooms.remove(this); 

				recoverClients(); 

				throw e; 
			}
			
		}

	}


}//end of central server class
	




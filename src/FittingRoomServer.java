import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

class FittingThreadHandling implements Runnable{
	public Socket sockets;

	public FittingThreadHandling(Socket sockets) {
		this.sockets = sockets;
	}

	@Override
	public void run() {
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(sockets.getInputStream()));
    		PrintWriter pw = new PrintWriter(sockets.getOutputStream(), true);
			String message = br.readLine();
				System.out.println("MESSAGE: " + message);

				if (message == null) {
                    System.out.println("Central server disconnected");
                    //break;
					System.exit(0); //Terminate when central server disconnected..
                }

				String[] parts = message.split(" ");
				

				synchronized(FittingRoomServer.class){


					if(parts[0].equals("ALLOCATE")){
						int clientID = Integer.parseInt(parts[1]);

						if(FittingRoomServer.rooms.tryAcquire()){
							pw.println("Allocated to " + clientID);
							System.out.println("Allocated to " + clientID);

						}else if(FittingRoomServer.waitingQueue.size() < FittingRoomServer.waitMax){
							FittingRoomServer.waitingQueue.add(clientID);
							pw.println("Wait " + clientID);
							System.out.println("Wait " + clientID);

						}else{
							pw.println("Full " + clientID);
							System.out.println("Full " + clientID);
						}

					}else if(parts[0].equals("RELEASE")){
						System.out.println("Rooms avaiable BEFORE RELEASE: " + FittingRoomServer.rooms.availablePermits());

						if(FittingRoomServer.rooms.availablePermits() < FittingRoomServer.totalRooms){ // only release if there are clients waiting. 
							FittingRoomServer.rooms.release();
						}else{
							System.out.println("WARNING: Attempted to over-release rooms."); 
							return; 
						}

						//AFTER releasing, assing next client if possible
						if(!FittingRoomServer.waitingQueue.isEmpty()){
							int nextClient = FittingRoomServer.waitingQueue.poll();

							if(FittingRoomServer.rooms.tryAcquire()){
								pw.println("Next " + nextClient);
								System.out.println("Assigned Room to waiting client " + nextClient);
							}else{
								//safely fallback in case
								FittingRoomServer.waitingQueue.add(nextClient);

							}
						
						}

						System.out.println("Rooms avaiable AFTER RELEASE: " + FittingRoomServer.rooms.availablePermits());

					}	
				}
		}catch(IOException e) {
			System.out.println("ERROR: " + e);
			e.printStackTrace();
			return;
		}
	}
}
public class FittingRoomServer {
	static Semaphore rooms;
	static int waitMax;
	static Queue<Integer> waitingQueue = new LinkedList<>();
	static int totalRooms;
	//private int fitID;

	public static void main(String[] args) throws IOException{
		if (args.length < 1) {
            System.out.println("Usage: java FittingRoomServer <totalRooms>");
            return;
        }

		totalRooms = Integer.parseInt(args[0]);
		rooms = new Semaphore(totalRooms);
		waitMax = totalRooms*2;

		System.out.println("Fitting Room Server starting with " + totalRooms + " rooms and max waiting queue of " + waitMax);
		
		
		try {
			//ServerSocket socketCentral = new ServerSocket(50001);
			System.out.println("FittingRoomServer is connected to the Central Server!");
    		Socket central = new Socket("127.0.0.1", 50001);
			//BufferedReader br = new BufferedReader(new InputStreamReader(central.getInputStream()));
    		//PrintWriter pw = new PrintWriter(central.getOutputStream(), true);

			while(true){
				//System.out.println("Test");
				//Socket central = socketCentral.accept();
				//BufferedReader br = new BufferedReader(new InputStreamReader(central.getInputStream()));
    			//PrintWriter pw = new PrintWriter(central.getOutputStream(), true);
				
				//Threading FittingRoom
				FittingThreadHandling fitThreading = new FittingThreadHandling(central);
				Thread t = new Thread(fitThreading);
				t.start();

			}
		}catch(IOException e) {
			System.out.println("ERROR: " + e);
			e.printStackTrace();
			return;
		}
	}
}
				/*
				
				String message = br.readLine();
				System.out.println("MESSAGE: " + message);

				if (message == null) {
                    System.out.println("Central server disconnected");
                    break;
                }

				String[] parts = message.split(" ");
				//System.out.println(parts[0]);

				synchronized(FittingRoomServer.class){


					if(parts[0].equals("ALLOCATE")){
						int clientID = Integer.parseInt(parts[1]);

						if(rooms.tryAcquire()){
							pw.println("Allocated to " + clientID);
							System.out.println("Allocated to " + clientID);

						}else if(waitingQueue.size() < waitMax){
							waitingQueue.add(clientID);
							pw.println("Wait " + clientID);
							System.out.println("Wait " + clientID);

						}else{
							pw.println("Full " + clientID);
							System.out.println("Full " + clientID);
						}

					}else if(parts[0].equals("RELEASE")){
						System.out.println("Rooms avaiable BEFORE RELEASE: " + rooms.availablePermits());

						if(rooms.availablePermits() < totalRooms){ // only release if there are clients waiting. 
							rooms.release();
						}else{
							System.out.println("WARNING: Attempted to over-release rooms."); 
							return; 
						}

						//AFTER releasing, assing next client if possible
						if(!waitingQueue.isEmpty()){
							int nextClient = waitingQueue.poll();

							if(rooms.tryAcquire()){
								pw.println("Next " + nextClient);
								System.out.println("Assigned Room to waiting client " + nextClient);
							}else{
								//safely fallback in case
								waitingQueue.add(nextClient);

							}
						
						}

						System.out.println("Rooms avaiable AFTER RELEASE: " + rooms.availablePermits());

					}	
				}
			}	
			
		} catch (IOException e) {
    		System.out.println("Failed to connect to Central Server");
    		e.printStackTrace();
    		return; 
		}

	}
		
}
	*/
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;


public class FittingRoomServer {
	static Semaphore rooms;
	static int waitMax;
	static Queue<Integer> waitingQueue = new LinkedList<>();


	public static void main(String[] args) throws IOException{
		if (args.length < 1) {
            System.out.println("Usage: java FittingRoomServer <totalRooms>");
            return;
        }

		int totalRooms = Integer.parseInt(args[0]);
		rooms = new Semaphore(totalRooms);
		waitMax = totalRooms*2;

		
		
		try {
			//ServerSocket socketCentral = new ServerSocket(50001);
			System.out.println("FittingRoomServer is connected to the Central Server!");
    		Socket central = new Socket("127.0.0.1", 50001);
			BufferedReader br = new BufferedReader(new InputStreamReader(central.getInputStream()));
    		PrintWriter pw = new PrintWriter(central.getOutputStream(), true);

			while(true){
				//System.out.println("Test");
				//Socket central = socketCentral.accept();
				//BufferedReader br = new BufferedReader(new InputStreamReader(central.getInputStream()));
    			//PrintWriter pw = new PrintWriter(central.getOutputStream(), true);

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
							//System.out.println("Allocated to " + clientID);
						}else if(waitingQueue.size() < waitMax){
							waitingQueue.add(clientID);
							pw.println("Wait " + clientID);
							//System.out.println("Wait " + clientID);
						}else{
							pw.println("Full " + clientID);
							//System.out.println("Full " + clientID);
						}
					}else if(parts[0].equals("RELEASE")){
						rooms.release();

						if(!waitingQueue.isEmpty()){
							int nextClient = waitingQueue.poll();
							rooms.tryAcquire();
							pw.println("Next " + nextClient);
							//System.out.println("Next " + nextClient);
						}
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
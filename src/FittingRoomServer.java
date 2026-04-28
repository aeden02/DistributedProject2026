import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;


public class FittingRoomServer {
	static Semaphore rooms;
	static int waitMax;
	static Queue<Integer> waitingQueue = new LinkedList<>();


	public static void main(String[] args) {
		if (args.length < 1) {
            System.out.println("Usage: java FittingRoomServer <totalRooms>");
            return;
        }

		int totalRooms = Integer.parseInt(args[0]);
		rooms = new Semaphore(totalRooms);
		waitMax = totalRooms*2;

		try {
    		Socket central = new Socket("127.0.0.1", 5000);
    		BufferedReader br = new BufferedReader(new InputStreamReader(central.getInputStream()));
    		PrintWriter pw = new PrintWriter(central.getOutputStream(), true);

			while(true){
				String message = br.readLine();

				if (message == null) {
                    System.out.println("Central server disconnected");
                    break;
                }

				String[] parts = message.split(" ");

				synchronized(FittingRoomServer.class){


					if(parts[0].equals("ALLOCATE")){
						int clientID = Integer.parseInt(parts[1]);

						if(rooms.tryAcquire()){
							pw.println("Allocated to " + clientID);
						}else if(waitingQueue.size() < waitMax){
							waitingQueue.add(clientID);
							pw.println("Wait " + clientID);
						}else{
							pw.println("Full " + clientID);
						}
					}else if(parts[0].equals("RELEASE")){
						rooms.release();

						if(!waitingQueue.isEmpty()){
							int nextClient = waitingQueue.poll();
							rooms.tryAcquire();
							pw.println("Next " + nextClient);
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

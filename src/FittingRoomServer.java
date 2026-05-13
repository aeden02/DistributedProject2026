import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class FittingRoomServer {

    static Semaphore rooms;
    static int waitMax;
    static Queue<Integer> waitingQueue = new LinkedList<>();
    static Queue<Integer> priorityQueue = new LinkedList<>();
    static int totalRooms;

    public static int replaceWaiting(int clientID){
        for(Integer i: waitingQueue){
            if(!priorityQueue.contains(i)){
                
                waitingQueue.add(clientID);
                waitingQueue.remove(i);

                return i;
            }
        }

        return -1;
    }

    public static void main(String[] args) throws IOException {

        int totalRoomsArg = Integer.parseInt(args[0]);
        totalRooms = totalRoomsArg;

        rooms = new Semaphore(totalRoomsArg);
        waitMax = totalRoomsArg * 2;

        System.out.println("Fitting Room Server started...");

        Socket central = new Socket("127.0.0.1", 50001);

        BufferedReader br =
                new BufferedReader(new InputStreamReader(central.getInputStream()));

        PrintWriter pw =
                new PrintWriter(central.getOutputStream(), true);

        while (true) {

            String message = br.readLine();
            System.out.println("MESSAGE: " + message);

            if (message == null) break;

            String[] parts = message.split(" ");

            synchronized (FittingRoomServer.class) {

                if (parts[0].equals("ALLOCATE") || parts[0].equals("PRIORITY")) {

                    int clientID = Integer.parseInt(parts[1]);

                    if (rooms.tryAcquire()) {

                        pw.println("Allocated " + clientID);
                        System.out.println("Allocated " + clientID);

                    }else if (waitingQueue.size() < waitMax) {

                        waitingQueue.add(clientID);

                        pw.println("Wait " + clientID);
                        System.out.println("Wait " + clientID);

                    }else if (parts[0].equals("PRIORITY")) { //When fitting room shuts down, if a client was changing they will
                        priorityQueue.add(clientID);         //have priority status over other clients

                        int replaced = replaceWaiting(clientID);
                        if(replaced != -1){
                            pw.println("Replaced " + replaced + " with " + clientID);
                            System.out.println("Replaced " + replaced + " with " + clientID);

                        }
                        else{
                            priorityQueue.remove(clientID);         //if there are already prioritized clients, then leave angry
                            pw.println("Full " + clientID);
                            System.out.println("Full " + clientID);
                        }

                        
                    }else {

                        pw.println("Full " + clientID);
                        System.out.println("Full " + clientID);
                    }

                }else if (parts[0].equals("RELEASE")) {
                    int id = Integer.parseInt(parts[1]);

                    System.out.println("Rooms Before release: " + rooms.availablePermits());

                    if (rooms.availablePermits() < totalRooms) {
                        rooms.release();
                    }
                    if(priorityQueue.contains(id)){
                        priorityQueue.remove(id);
                    }

                    System.out.println("Rooms After release: " + rooms.availablePermits());

                    if (!waitingQueue.isEmpty()) {

                        if (rooms.tryAcquire()) {

                            int nextClient = waitingQueue.poll();

                            pw.println("Allocated " + nextClient);

                            System.out.println("Promoted from queue: " + nextClient);
                        }
                    }else{
						pw.println("Released."); 
						System.out.println("WAITING QUEUE IS EMPTY"); 
					}
                }
            }
        }
    }
}
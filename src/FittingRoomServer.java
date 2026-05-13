import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class FittingRoomServer {

    static Semaphore rooms;
    static int waitMax;
    static Queue<Integer> waitingQueue = new LinkedList<>();
    static int totalRooms;

    public static void replaceWaiting(int clientID){
        waitingQueue.remove();
        waitingQueue.add(clientID);
    }

    public static void main(String[] args) throws IOException {

        //Get Total Rooms from the arguments for us to create a semaphore.
        int totalRoomsArg = Integer.parseInt(args[0]);
        totalRooms = totalRoomsArg;

        rooms = new Semaphore(totalRoomsArg); //Create semaphore to have a limited pool of rooms.
        waitMax = totalRoomsArg * 2; //Setup waiting area.

        System.out.println("Fitting Room Server started...");

        Socket central = new Socket("127.0.0.1", 50001); //A socket connects to the central server.

        BufferedReader br =
                new BufferedReader(new InputStreamReader(central.getInputStream()));

        PrintWriter pw =
                new PrintWriter(central.getOutputStream(), true);

        while (true) {

            String message = br.readLine(); //Read whatever the central server sends to the fitting room server.
            System.out.println("MESSAGE: " + message);
            /*
            Message is as follows
            parts[0] - COMMAND (ALLOCATE, PRIORITY,)
            parts[1] - CLIENT ID

            NOTE: pw.println() - these print statements send the response back to the central server
            */

            if (message == null) break;

            String[] parts = message.split(" ");

            synchronized (FittingRoomServer.class) {

                if (parts[0].equals("ALLOCATE") || parts[0].equals("PRIORITY")) { 
                    //Priortize or allocate a room.

                    int clientID = Integer.parseInt(parts[1]); //Get ClientID

                    if (rooms.tryAcquire()) {

                        pw.println("Allocated " + clientID);
                        System.out.println("Allocated " + clientID);

                    }else if (waitingQueue.size() < waitMax) {
                        //If the waiting queue size is less than waitmax, seat a customer in a chair.
                        waitingQueue.add(clientID);

                        pw.println("Wait " + clientID);
                        System.out.println("Wait " + clientID);

                    }else if (parts[0].equals("PRIORITY")) {
                        //If a customer has priority, pop the front of the queue of a customer and add the new customer to the waiting queue.
                        replaceWaiting(clientID);

                        pw.println("Wait " + clientID);
                        System.out.println("Wait " + clientID);
                        
                    }else {
                        //Waiting Room is Full.
                        pw.println("Full " + clientID);
                        System.out.println("Full " + clientID);
                    }

                }else if (parts[0].equals("RELEASE")) {

                    System.out.println("Before release: " + rooms.availablePermits()); //Number of available rooms before release.

                    if (rooms.availablePermits() < totalRooms) { 
                        //If there are still rooms avaiable than the total number of rooms, release a room.
                        //Basically, when a customer finishes a room and leaves, can we still release it?
                        rooms.release();
                    }

                    System.out.println("After release: " + rooms.availablePermits());

                    //If the customer releases the room, and the waiting queue is not empty,
                    if (!waitingQueue.isEmpty()) { 

                        if (rooms.tryAcquire()) {

                            int nextClient = waitingQueue.poll(); //Pop the next client from waiting queue to allocate and promote it

                            pw.println("Allocated " + nextClient);

                            System.out.println("Promoted from queue: " + nextClient);
                        }
                    }else{
                        //Waiting Queue is empty...
						pw.println("Released."); 
						System.out.println("WAITING QUEUE IS EMPTY"); 
					}
                }
            }
        }
    }
}

/*
Usage of Semaphore explanation:
*/
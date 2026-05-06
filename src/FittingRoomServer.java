import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class FittingRoomServer {

    static Semaphore rooms;
    static int waitMax;
    static Queue<Integer> waitingQueue = new LinkedList<>();
    static int totalRooms;

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

                if (parts[0].equals("ALLOCATE")) {

                    int clientID = Integer.parseInt(parts[1]);

                    if (rooms.tryAcquire()) {

                        pw.println("Allocated " + clientID);
                        System.out.println("Allocated " + clientID);

                    }else if (waitingQueue.size() < waitMax) {

                        waitingQueue.add(clientID);

                        pw.println("Wait " + clientID);
                        System.out.println("Wait " + clientID);

                    }else {

                        pw.println("Full " + clientID);
                        System.out.println("Full " + clientID);
                    }

                }else if (parts[0].equals("RELEASE")) {

                    System.out.println("Before release: " + rooms.availablePermits());

                    if (rooms.availablePermits() < totalRooms) {
                        rooms.release();
                    }

                    System.out.println("After release: " + rooms.availablePermits());

                    if (!waitingQueue.isEmpty()) {

                        if (rooms.tryAcquire()) {

                            int nextClient = waitingQueue.poll();

                            pw.println("Allocated " + nextClient);

                            System.out.println("Promoted from queue: " + nextClient);
                        }
                    }else{
						System.out.println("WAITING QUEUE IS EMPTY"); 
					}
                }
            }
        }
    }
}
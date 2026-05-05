import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class FittingRoomServer {

    static Semaphore rooms;
    static int waitMax;
    static Queue<Integer> waitingQueue = new LinkedList<>();

    public static void main(String[] args) throws IOException {

        int totalRooms = Integer.parseInt(args[0]);

        rooms = new Semaphore(totalRooms);
        waitMax = totalRooms * 2;

        System.out.println("Fitting Room Server started");

        Socket central = new Socket("127.0.0.1", 50001);

        BufferedReader br = new BufferedReader(
                new InputStreamReader(central.getInputStream()));
        PrintWriter pw = new PrintWriter(central.getOutputStream(), true);

        while (true) {

            String message = br.readLine();
            if (message == null) break;

            String[] parts = message.split(" ");

            synchronized (FittingRoomServer.class) {

                if (parts[0].equals("ALLOCATE")) {

                    int clientID = Integer.parseInt(parts[1]);

                    if (rooms.tryAcquire()) {
                        pw.println("Allocated " + clientID);

                    } else if (waitingQueue.size() < waitMax) {
                        waitingQueue.add(clientID);
                        pw.println("Wait " + clientID);

                    } else {
                        pw.println("Full " + clientID);
                    }

                }
                else if (parts[0].equals("RELEASE")) {

                    if (rooms.availablePermits() < totalRooms) {
                        rooms.release();
                    }

                    if (!waitingQueue.isEmpty()) {
                        int next = waitingQueue.poll();

                        if (rooms.tryAcquire()) {
                            pw.println("Next " + next);
                        } else {
                            waitingQueue.add(next);
                        }
                    }
                }
            }
        }
    }
}
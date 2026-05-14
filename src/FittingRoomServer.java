import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class FittingRoomServer {

    static Semaphore rooms;
    static int totalRooms;

    static Queue<Integer> waitingQueue = new LinkedList<>();

    public static void main(String[] args) throws Exception {

        totalRooms = Integer.parseInt(args[0]);
        rooms = new Semaphore(totalRooms);

        System.out.println("Fitting Room Server started...");

        Socket central = new Socket("127.0.0.1", 50001);

        BufferedReader br =
                new BufferedReader(new InputStreamReader(central.getInputStream()));

        PrintWriter pw =
                new PrintWriter(central.getOutputStream(), true);

        while (true) {

            String message = br.readLine();

            if (message == null) break;

            String[] parts = message.split(" ");

            synchronized (FittingRoomServer.class) {

                if (parts[0].equals("ALLOCATE")) {

                    int id = Integer.parseInt(parts[1]);

                    if (rooms.tryAcquire()) {
                        pw.println("Allocated " + id);
                    }

                    else {
                        if (!waitingQueue.contains(id)) {
                            waitingQueue.add(id);
                        }
                        pw.println("Wait " + id);
                    }
                }

                else if (parts[0].equals("RELEASE")) {

                    int id = Integer.parseInt(parts[1]);

                    if (rooms.availablePermits() < totalRooms) {
                        rooms.release();
                    }

                    if (!waitingQueue.isEmpty()) {

                        int next = waitingQueue.poll();

                        rooms.tryAcquire();

                        pw.println("Promoted " + next);
                    }

                    else {
                        pw.println("Released");
                    }
                }
            }
        }
    }
}

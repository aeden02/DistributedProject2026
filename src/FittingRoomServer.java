import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class FittingRoomServer {

    static Semaphore rooms;
    static int totalRooms;

    static Queue<Integer> waitingQueue =
            new LinkedList<>();

    public static void main(String[] args) throws Exception {

        totalRooms = Integer.parseInt(args[0]);
        rooms = new Semaphore(totalRooms);

        Socket central =
                new Socket("127.0.0.1", 50001);

        BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(central.getInputStream()));

        PrintWriter pw =
                new PrintWriter(central.getOutputStream(), true);

        while (true) {

            String msg = br.readLine();
            if (msg == null) break;

            String[] p = msg.split(" ");

            synchronized (FittingRoomServer.class) {

                if (p[0].equals("ALLOCATE")) {

                    int id = Integer.parseInt(p[1]);

                    if (rooms.tryAcquire()) {
                        pw.println("Allocated " + id);
                    } else {
                        if (!waitingQueue.contains(id)) {
                            waitingQueue.add(id);
                        }
                        pw.println("Wait " + id);
                    }
                }

                else if (p[0].equals("RELEASE")) {

                    int id = Integer.parseInt(p[1]);

                    if (rooms.availablePermits() < totalRooms) {
                        rooms.release();
                    }

                    if (!waitingQueue.isEmpty()) {
                        int next = waitingQueue.poll();
                        rooms.tryAcquire();
                        pw.println("Promoted " + next);
                    } else {
                        pw.println("Released");
                    }
                }
            }
        }
    }
}

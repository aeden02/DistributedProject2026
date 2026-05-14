import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class FittingRoomServer {

    // ========================= ROOM CONTROL =========================

    static Semaphore rooms;

    static int totalRooms;

    static int waitMax;

    // ========================= QUEUES =========================

    // CLIENTS WHO WERE INSIDE A ROOM
    // WHEN A SERVER CRASHED
    static Queue<Integer> activeRecoveryQueue =
            new LinkedList<>();

    // CLIENTS WHO WERE WAITING
    // WHEN A SERVER CRASHED
    static Queue<Integer> waitingRecoveryQueue =
            new LinkedList<>();

    // NORMAL CLIENTS
    static Queue<Integer> normalWaitingQueue =
            new LinkedList<>();

    // ========================= MAIN =========================

    public static void main(String[] args)
            throws IOException {

        if (args.length < 1) {

            System.out.println(
                    "Usage: java FittingRoomServer <rooms>");

            return;
        }

        totalRooms =
                Integer.parseInt(args[0]);

        rooms =
                new Semaphore(totalRooms);

        waitMax =
                totalRooms * 2;

        System.out.println(
                "Fitting Room Server Started");

        // ========================= CONNECT TO CENTRAL

        Socket central =
                new Socket("127.0.0.1", 50001);

        BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(
                                central.getInputStream()));

        PrintWriter pw =
                new PrintWriter(
                        central.getOutputStream(),
                        true);

        System.out.println(
                "Connected to Central Server");

        // ========================= MAIN LOOP =========================

        while (true) {

            String message =
                    br.readLine();

            if (message == null) {

                System.out.println(
                        "Central Server disconnected.");

                break;
            }

            System.out.println(
                    "MESSAGE: " + message);

            String[] parts =
                    message.split(" ");

            synchronized (
                    FittingRoomServer.class) {

                // ========================= ALLOCATE

                if (parts[0].equals("ALLOCATE") ||

                        parts[0].equals(
                                "RECOVER_ACTIVE") ||

                        parts[0].equals(
                                "RECOVER_WAITING")) {

                    int clientID =
                            Integer.parseInt(
                                    parts[1]);

                    // ========================= ROOM AVAILABLE

                    if (rooms.tryAcquire()) {

                        pw.println(
                                "Allocated "
                                        + clientID);

                        System.out.println(
                                "Allocated "
                                        + clientID);

                    }

                    // ========================= NO ROOM

                    else {

                        // ================= ACTIVE RECOVERY
                        // HIGHEST PRIORITY

                        if (parts[0].equals(
                                "RECOVER_ACTIVE")) {

                            if (!activeRecoveryQueue
                                    .contains(clientID)) {

                                activeRecoveryQueue
                                        .add(clientID);
                            }

                            pw.println(
                                    "Wait "
                                            + clientID);

                            System.out.println(
                                    "RECOVER_ACTIVE WAIT "
                                            + clientID);
                        }

                        // ================= WAITING RECOVERY
                        // SECOND PRIORITY

                        else if (parts[0].equals(
                                "RECOVER_WAITING")) {

                            if (!waitingRecoveryQueue
                                    .contains(clientID)) {

                                waitingRecoveryQueue
                                        .add(clientID);
                            }

                            pw.println(
                                    "Wait "
                                            + clientID);

                            System.out.println(
                                    "RECOVER_WAITING WAIT "
                                            + clientID);
                        }

                        // ================= NORMAL CLIENTS

                        else {

                            int totalWaiting =
                                    activeRecoveryQueue.size()
                                            + waitingRecoveryQueue.size()
                                            + normalWaitingQueue.size();

                            if (totalWaiting < waitMax) {

                                if (!normalWaitingQueue
                                        .contains(clientID)) {

                                    normalWaitingQueue
                                            .add(clientID);
                                }

                                pw.println(
                                        "Wait "
                                                + clientID);

                                System.out.println(
                                        "WAIT "
                                                + clientID);

                            } else {

                                pw.println("Full");

                                System.out.println(
                                        "FULL "
                                                + clientID);
                            }
                        }
                    }
                }

                // ========================= RELEASE

                else if (parts[0].equals(
                        "RELEASE")) {

                    int clientID =
                            Integer.parseInt(
                                    parts[1]);

                    System.out.println(
                            "RELEASE FROM "
                                    + clientID);

                    // RELEASE ROOM

                    if (rooms.availablePermits()
                            < totalRooms) {

                        rooms.release();
                    }

                    int nextClient = -1;

                    // ========================= PRIORITY 1
                    // CLIENTS WHO LOST ACTIVE ROOM

                    if (!activeRecoveryQueue.isEmpty()) {

                        nextClient =
                                activeRecoveryQueue.poll();

                        System.out.println(
                                "PROMOTING ACTIVE RECOVERY "
                                        + nextClient);
                    }

                    // ========================= PRIORITY 2
                    // CLIENTS WHO LOST WAITING POSITION

                    else if (!waitingRecoveryQueue
                            .isEmpty()) {

                        nextClient =
                                waitingRecoveryQueue.poll();

                        System.out.println(
                                "PROMOTING WAITING RECOVERY "
                                        + nextClient);
                    }

                    // ========================= PRIORITY 3
                    // NORMAL WAITING CLIENTS

                    else if (!normalWaitingQueue
                            .isEmpty()) {

                        nextClient =
                                normalWaitingQueue.poll();

                        System.out.println(
                                "PROMOTING NORMAL "
                                        + nextClient);
                    }

                    // ========================= PROMOTE CLIENT

                    if (nextClient != -1) {

                        // IMMEDIATELY CLAIM ROOM
                        rooms.tryAcquire();

                        pw.println(
                                "Promoted "
                                        + nextClient);

                        System.out.println(
                                "Promoted "
                                        + nextClient);

                    }

                    // ========================= NO WAITING CLIENTS

                    else {

                        pw.println("Released");

                        System.out.println(
                                "No waiting clients.");
                    }
                }

                // ========================= INVALID

                else {

                    pw.println("Invalid");

                    System.out.println(
                            "Invalid Request");
                }
            }
        }

        // ========================= CLEANUP

        try {

            br.close();
            pw.close();
            central.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

import java.io.*;
import java.net.*;

public class Client {

    // ========================= STATES =========================

    public enum ClientState {
        IDLE,
        ACTIVE_ROOM,
        WAITING,
        DISCONNECTED
    }

    // ========================= VARIABLES =========================

    private Socket socket;

    private BufferedReader br;
    private PrintWriter pw;

    private ClientState state = ClientState.IDLE;

    private int port;
    private int clientID;

    private String serverIP;

    private String fittingRoomServerIP = "Unknown";

    // ========================= CONSTRUCTOR =========================

    public Client(String serverIP,
                  int port,
                  int clientID) {

        this.serverIP = serverIP;
        this.port = port;
        this.clientID = clientID;

        try {

            socket = new Socket(serverIP, port);

            System.out.println(
                    "Connected to Central Server");

            br = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));

            pw = new PrintWriter(
                    socket.getOutputStream(),
                    true);

            // START LISTENER THREAD
            new ServerListener().start();

        } catch (IOException e) {

            System.out.println(
                    "Unable to connect to Central Server");

            e.printStackTrace();
        }
    }

    // ========================= SERVER LISTENER =========================

    public class ServerListener extends Thread {

        @Override
        public void run() {

            try {

                String response;

                while (socket != null &&
                        !socket.isClosed() &&
                        (response = br.readLine()) != null) {

                    responseHandler(response);
                }

            } catch (IOException e) {

                if (socket != null &&
                        socket.isClosed()) {

                    System.out.println(
                            "Client "
                                    + clientID
                                    + " listener stopped.");
                } else {

                    System.out.println(
                            "Error in listener thread.");

                    e.printStackTrace();
                }
            }
        }
    }

    // ========================= DISPLAY =========================

    private void display(String msg) {

        System.out.println(
                "Customer #"
                        + clientID
                        + " "
                        + msg);
    }

    // ========================= RESPONSE HANDLER =========================

    private synchronized void responseHandler(
            String response) {

        System.out.println("Server: " + response);

        // ========================= PARSE IP

        if (response.contains(":")) {

            String[] parts =
                    response.split(":");

            response = parts[0];

            fittingRoomServerIP =
                    parts[1];
        }

        // ========================= ROOM ALLOCATED

        if (response.startsWith(
                "Room Allocated")) {

            state =
                    ClientState.ACTIVE_ROOM;

            display(
                    "leaves waiting area and enters fitting room "
                            + "<Server: "
                            + fittingRoomServerIP
                            + ">");

            simulateFittingRoomUse();
        }

        // ========================= WAIT

        else if (response.startsWith(
                "Wait")) {

            state =
                    ClientState.WAITING;

            display(
                    "enters the waiting area and takes a seat "
                            + "<Server: "
                            + fittingRoomServerIP
                            + ">");
        }

        // ========================= ROOM RELEASED

        else if (response.startsWith(
                "Room released")) {

            state =
                    ClientState.IDLE;

            display(
                    "leaves the fitting room "
                            + "<Server: "
                            + fittingRoomServerIP
                            + ">");

            exit();
        }

        // ========================= SERVER DOWN

        else if (response.startsWith(
                "Fitting Room Server Down")) {

            display(
                    "assigned fitting room crashed. Waiting for reassignment...");
        }

        // ========================= FULL

        else if (response.startsWith(
                "Full")) {

            display(
                    "leaves store because all fitting rooms are full.");

            exit();
        }

        // ========================= INVALID

        else {

            System.out.println(
                    "Unknown Response: "
                            + response);
        }
    }

    // ========================= REQUEST ROOM =========================

    public synchronized void requestFittingRoom() {

        if (pw == null) {

            System.out.println(
                    "Not connected to server.");

            return;
        }

        pw.println(
                "Request Room "
                        + clientID);

        display(
                "requests a fitting room");
    }

    // ========================= RELEASE ROOM =========================

    public synchronized void releaseFittingRoom() {

        if (state ==
                ClientState.ACTIVE_ROOM) {

            pw.println(
                    "Release Room "
                            + clientID);

            System.out.println(
                    "Released Fitting Room.");
        }
    }

    // ========================= EXIT =========================

    public synchronized void exit() {

        try {

            state =
                    ClientState.DISCONNECTED;

            if (pw != null) {

                pw.println("Exit");
                pw.close();
            }

            if (br != null) {
                br.close();
            }

            if (socket != null &&
                    !socket.isClosed()) {

                socket.close();
            }

            display("exits the system.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ========================= SIMULATE FITTING ROOM =========================

    private void simulateFittingRoomUse() {

        new Thread(() -> {

            try {

                int sleepTime =
                        (int)(Math.random() * 5000)
                                + 1000;

                Thread.sleep(sleepTime);

                releaseFittingRoom();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    // ========================= MAIN =========================

    public static void main(String[] args) {

        if (args.length < 1) {

            System.out.println(
                    "Usage: java Client <number_of_customers>");

            return;
        }

        int totalCustomers =
                Integer.parseInt(args[0]);

        String serverIP =
                "127.0.0.1";

        int port = 50000;

        int clientId = 1;

        while (clientId <= totalCustomers) {

            int id = clientId++;

            new Thread(() -> {

                Client client =
                        new Client(
                                serverIP,
                                port,
                                id);

                client.display(
                        "enters the system");

                client.requestFittingRoom();

                try {

                    while (client.state !=
                            ClientState.DISCONNECTED) {

                        Thread.sleep(100);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }).start();

            try {

                Thread.sleep(
                        (int)(Math.random() * 1000));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

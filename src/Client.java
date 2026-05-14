import java.io.*;
import java.net.*;

public class Client {

    Socket socket;
    BufferedReader in;
    PrintWriter out;

    int clientID;
    String fittingRoomServerIP;

    boolean hasRoom = false;
    boolean isWaiting = false;

    public Client(String ip, int port, int id) {

        this.clientID = id;

        try {

            socket = new Socket(ip, port);

            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            out = new PrintWriter(socket.getOutputStream(), true);

            new ServerListener().start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ServerListener extends Thread {

        public void run() {

            try {

                String response;

                while ((response = in.readLine()) != null) {

                    if (response.contains(":")) {

                        String[] p = response.split(":");

                        fittingRoomServerIP = p[1];

                        response = p[0];

                        System.out.println("Server: " + response);
                    }

                    if (response.startsWith("Room Allocated")) {

                        hasRoom = true;
                        isWaiting = false;

                        System.out.println(
                                "Customer #" + clientID +
                                        " enters fitting room <Server: " +
                                        fittingRoomServerIP + ">");

                        simulate();
                    }

                    else if (response.startsWith("Wait")) {

                        isWaiting = true;

                        System.out.println(
                                "Customer #" + clientID +
                                        " enters waiting area <Server: " +
                                        fittingRoomServerIP + ">");
                    }

                    else if (response.startsWith("Full")) {

                        System.out.println(
                                "Customer #" + clientID +
                                        " leaves store (full)");
                    }

                    else if (response.startsWith("Fitting Room Server Down")) {

                        System.out.println("Server crashed — retrying");
                        request();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void request() {
        out.println("Request Room " + clientID);
    }

    public void release() {
        out.println("Release Room " + clientID);
    }

    public void simulate() {

        new Thread(() -> {

            try {

                Thread.sleep((int)(Math.random() * 3000));

                release();

                Thread.sleep(100);

                System.out.println(
                        "Customer #" + clientID +
                                " Leaving Fitting Room...<Server: " +
                                fittingRoomServerIP + ">");

                out.println("Exit");

                socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    public static void main(String[] args) {

        int total = Integer.parseInt(args[0]);

        for (int i = 1; i <= total; i++) {

            int id = i;

            new Thread(() -> {

                Client c =
                        new Client("127.0.0.1", 50000, id);

                c.request();

            }).start();

            try {
                Thread.sleep((int)(Math.random() * 500));
            } catch (Exception ignored) {}
        }
    }
}

import java.io.*;
import java.net.*;

public class Client {

    Socket socket;
    BufferedReader in;
    PrintWriter out;

    int id;
    String roomIP = "Unknown";

    public Client(String host, int port, int id) {

        this.id = id;

        try {

            socket = new Socket(host, port);

            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            out = new PrintWriter(socket.getOutputStream(), true);

            new Thread(this::listen).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void listen() {

        try {

            String msg;

            while ((msg = in.readLine()) != null) {

                if (msg.contains(":")) {
                    String[] p = msg.split(":");
                    msg = p[0];
                    roomIP = p[1];
                }

                System.out.println("Client " + id + ": " + msg);

                if (msg.contains("Server Down")) {
                    request();
                }
            }

        } catch (Exception ignored) {}
    }

    void request() {
        out.println("Request Room " + id);
    }

    void release() {
        out.println("Release Room " + id);
    }

    public static void main(String[] args) {

        int n = Integer.parseInt(args[0]);

        for (int i = 1; i <= n; i++) {

            int id = i;

            new Thread(() -> {

                Client c =
                        new Client("127.0.0.1", 50000, id);

                c.request();

                try {
                    Thread.sleep(3000);
                    c.release();
                } catch (Exception ignored) {}

            }).start();
        }
    }
}

import java.io.PrintWriter;
import java.net.Socket;

public class ClientTester {
    private Socket client;

    public ClientTester(Socket client) {
        this.client = client;
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 50000;

        try {
            Socket socket = new Socket(host, port);
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}

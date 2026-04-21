import java.io.*;
import java.net.*;

public class Client{
    private Socket socket;
    private BufferedReader br;//input
    private PrintWriter pw;//output

    public Client(String serverIP, int port){
        try{
            socket = new Socket(serverIP,port);
            System.out.println("Connected to Server");
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream(),true);

            //start a new thread
            new ServerListener().start();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private class ServerListener extends Thread{
        public void run(){
            try{
                String response;
                while((response = br.readLine()) != null){
                    responseHandler(response);
                }

            }catch(IOException e){
                System.out.println("Disconnected from Server");
            }
        }
    }

    private void responseHandler(String response){
        
    }

    //When client is trying to get into a fitting room
    public void requestFittingRoom(){
        
    }

    //When client is done with fitting room
    public void releaseFittingRoom(){
        
    }

    //When client no longer wants to contact central server
    public void exit(){
        
    }

    private void simulateFittingRoomUse(){
        
    }
    
    public static void main(String[] args) {
        String serverIP = "127.0.0.1";
        int port = 5000;

        Client client = new Client(serverIP, port);
        client.requestFittingRoom();
    }

   
}

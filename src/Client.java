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
        System.out.println("Server: " + response);

        if (response.equals("Room Allocated")) {
            System.out.println("Entering fitting room...");
            simulateFittingRoomUse();
            //What happens if the client is given a fitting room

        } else if (response.equals("Wait")) {
            System.out.println("Waiting for available fitting room...");
            //What happens if a client is in the waiting room because all fitting rooms are full

        } else if (response.equals("Room Available")) {
            System.out.println("Room now available, requesting again...");
            requestFittingRoom();
            //what happens when rooms are available

        } else {
            System.out.println("Come back later");
            //what happens if waiting room and fitting rooms are all full
        }
    }

    //When client is trying to get into a fitting room
    public void requestFittingRoom(){
        pw.println("Request Room");
        System.out.println("Requested fitting room");
    }

    //When client is done with fitting room
    public void releaseFittingRoom(){
        pw.println("Release Room");
        System.out.println("Released Fitting Room");
    }

    //When client no longer wants to contact central server
    public void exit(){
        pw.println("Exit");
        try{
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void simulateFittingRoomUse(){
        try{
            Thread.sleep(3000);//simulates the time tha someone would use a fitting room
            releaseFittingRoom();
            exit();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    
    public static void main(String[] args) {
        String serverIP = "127.0.0.1";
        int port = 5000;

        Client client = new Client(serverIP, port);
        client.requestFittingRoom();
    }

   
}

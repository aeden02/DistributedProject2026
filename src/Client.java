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
            System.out.println("Unable to connect to Central Server");
            return; 
            
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
        if(pw == null){
            System.out.println("CLIENT: Not connected - cannot send"); 
            return;
        }
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
    	try {
    		int sleepTime = (int)(Math.random() * 1000);
    		Thread.sleep(sleepTime);

    		releaseFittingRoom();
    		exit();

		} catch (InterruptedException e) {
    		e.printStackTrace();
		}
    }

    
    public static void main(String[] args) {
        String serverIP = "127.0.0.1";
    	int port = 50000;

    	int clientId = 1;

    	while (true) {
        	int id = clientId++;

    		new Thread(new Runnable() {
    			@Override
    			public void run() {
        			Client client = new Client(serverIP, port);
        			System.out.println("Customer #" + id + " enters the system");
        			client.requestFittingRoom();
    			}
			}).start();

        	try {
            	Thread.sleep((int)(Math.random() * 1000));//this is the delay for the random entrance time
        	} catch (InterruptedException e) {
            	e.printStackTrace();
        	}
    	}

	}

   
}

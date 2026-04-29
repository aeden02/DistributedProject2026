import java.io.*;
import java.net.*;

public class Client{
    private Socket socket;
    private BufferedReader br;//input
    private PrintWriter pw;//output

    private int clientID;
    private String serverIP;

    public Client(String serverIP, int port, int clientID){
        this.serverIP = serverIP;
        this.clientID = clientID;

        try{
            socket = new Socket(serverIP,port);
            display("connected to server");
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
                display("disconnected from server");
            }
        }
    }

    private void display(String message){
        System.out.println("Customer #" + clientID + " " + message + " <Server: " + serverIP + ">");
    }

    private void responseHandler(String response){

        if (response.equals("Room Allocated")) {

            display("leaves waiting area and enters fitting room");
            simulateFittingRoomUse();

        } else if (response.equals("Wait")) {

            display("enters the waiting area and takes a seat");

        } else if (response.equals("Room Available")) {

            display("notified that a fitting room is available");
            requestFittingRoom();

        } else {

            display("leaves the store (no space available)");
            exit();
        }
}   

    //When client is trying to get into a fitting room
    public void requestFittingRoom(){
        pw.println("Request Room");
        display("requests a fitting room");
    }

    //When client is done with fitting room
    public void releaseFittingRoom(){
        pw.println("Release Room");
        display("leaves fitting room");    
    }

    //When client no longer wants to contact central server
    public void exit(){
        pw.println("Exit");
        try{
            pw.close();
            br.close();
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
        if (args.length < 1) {
        System.out.println("Usage: java Client <number_of_fitting_rooms>");
        return;
    }

        
        int totalCustomers = Integer.parseInt(args[0]);
        
        String serverIP = "127.0.0.1";
    	int port = 5000;

    	int clientId = 1;

        //System.out.println("Using arguments from command line");
        //System.out.println("Fitting Rooms = " + fittingRooms);
        //System.out.println("Number of chairs in the waiting area = " + chairs);
        //System.out.println("Number of customers = " + totalCustomers);

    	while (clientId <= totalCustomers) {
        	int id = clientId++;

    		new Thread(new Runnable() {
    			@Override
    			public void run() {
        			Client client = new Client(serverIP, port, id);
        			client.display("enters the system");
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

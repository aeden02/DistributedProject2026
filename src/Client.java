import java.io.*;
import java.net.*;

public class Client{
    public Socket socket;
    private BufferedReader br;//input
    private PrintWriter pw;//output
    private boolean hasRoom = false;
    boolean isWaiting = false; 

    public int port;
    public int clientID; //This was private.
    public String serverIP;

    public String fittingRoomServerIP;

    public Client(String serverIP, int port, int clientID){
        this.serverIP = serverIP;
        this.port = port;
        this.clientID = clientID;

        try{
            socket = new Socket(serverIP,port);
            //display("connected to server");
            System.out.println("Connected to server");
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream(),true);

            //start a new thread
            new ServerListener().start();
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("Unable to connect to central server");
            return;
        }
    }

    public class ServerListener extends Thread{
        public void run(){
            try{
                //Threaded to make responses.
                String response;
                while(socket !=null && !socket.isClosed() && (response = br.readLine()) != null){
                    //fittingRoomIP = CentralServer.ClientHandler.fittingRoomServerIP + "TESTTTY";
                    //System.out.println("SERVER IP: CONSOLE: " + fittingRoomIP);
                    //this.fittingRoomServerIP = response.substring(response.indexOf(":") + 1,response.length());
                    responseHandler(response);
                    
                    
                }

            }catch(IOException e){
                if(socket != null && socket.isClosed()){
                    System.out.println("Client " + clientID + " listener stopped (Socket closed)");
                }else{
                    System.out.println("Error in client listener");
                    e.printStackTrace();
                }
               
            }
        }
    }

    private void display(String message){

        //System.out.println("Customer #" + clientID + " " + message + " <Server: " + fittingRoomServerIP + ">");
        System.out.println("Customer #" + clientID + " " + message);
    }

    private void responseHandler(String response){
        if(response.contains(":")){

            this.fittingRoomServerIP = response.substring(response.indexOf(":") + 1); 
            System.out.println("Server: " + response.substring(0,response.indexOf(":")));
        }else{

            if(this.fittingRoomServerIP == null){
                this.fittingRoomServerIP = "Unavailable"; 
            } 

            System.out.println("Server: " + response); 
        }

        //this.fittingRoomServerIP = response.substring(response.indexOf(":") + 1, response.length());
       // System.out.println("Server: " + response.substring(0,response.indexOf(":")));

        if (response.startsWith("Room Allocated")) { //If Allocated, customer use the room.
            hasRoom = true;
            isWaiting = false; 
            display("leaves waiting area and enters fitting room <Server: " + this.fittingRoomServerIP + ">");
            simulateFittingRoomUse();
            

        } else if (response.startsWith("Wait")) { //If the customer waits, they sit in the waiting area for a room.
            isWaiting = true; 
            display("enters the waiting area and takes a seat <Server: " + this.fittingRoomServerIP + ">");

        } else if (response.equals("Room Available")) { //If the central server sends a room available message, the customer is notified the room is available.

            display("notified that a fitting room is available <Server: " + this.fittingRoomServerIP + ">");
            requestFittingRoom();

        } else if(response.startsWith("Full")) { //Customer leaves the store because no waiting seats or rooms are available.
            display("leaves the store (no space available) <Server: " + this.fittingRoomServerIP + ">");
            exit();
        }else if(response.equals("No fitting rooms available")){
            display("waiting for another fitting room server."); 

        }else if(response.startsWith("Removed From Waiting Queue")){
            display("removed from waiting queue");

            //client no longer active in system
            isWaiting = false;
            hasRoom = false; 
            
            //if want removed customers to leave. 
            display("leaves the store after losing waiting chair.");
            pw.println("Exit"); 
            
        
        }else if(response.equals("Fitting Room Server Down")){
            display("assigned fitting room server crashed."); 
        }else{

          System.out.println("Unknown response :" + response);
        }
}   

    //When client is trying to get into a fitting room
    public void requestFittingRoom(){
        if(pw == null ) {
            System.out.println("Client: Not Connected - Cannot Send.");
            return;
        }

        pw.println("Request Room " + clientID); //Send a request for a fittingroom to the central server.
        display("requests a fitting room"); //Prints message of status.
    }

    //When client is done with fitting room
    public void releaseFittingRoom(){
        if(hasRoom) {
            pw.println("Release Room " + clientID);
            hasRoom = false;
            System.out.println("Released Fitting Room.");
            //display("leaves fitting room" + "<Server: " + this.fittingRoomServerIP + ">");
        }    
    }

    //When client no longer wants to contact central server
    public void exit(){
        try{
            if(pw !=null){
                pw.println("Exit");
                pw.close(); 
            }

            if(br != null){
                br.close(); 
            }
            
            if(socket != null){
                socket.close(); 
            }
            
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void simulateFittingRoomUse(){
    	try {
    		int sleepTime = (int)(Math.random() * 5000);
    		Thread.sleep(sleepTime);

    		releaseFittingRoom();

            //adding a tiny delay to prevent msg getting lost before disconnect. 
            try{
                Thread.sleep(100);
            }catch(InterruptedException e){  }
    		
            display("Leaving Fitting Room..."+ "<Server: " + this.fittingRoomServerIP + ">");
            exit(); 

            return; 
		} catch (InterruptedException e) {
    		e.printStackTrace();
		}
    }

    
    public static void main(String[] args) {
        if (args.length < 1) {
        System.out.println("Usage: java Client <number_of_fitting_rooms>");
        return;
    }

    
        int totalCustomers = Integer.parseInt(args[0]); //How many customers will be sent to fitting room.
        
        String serverIP = "127.0.0.1";
    	int port = 50000;

    	int clientId = 1;


    	while (clientId <= totalCustomers) {
        	int id = clientId++;

    		new Thread(new Runnable() { //Continiously create customer threads that enters the system to request a fittingroom.
    			@Override
    			public void run() {
        			//Client client = new Client(serverIP, port, id);
                    Client client = new Client(serverIP, port,id);
        			client.display("enters the system");
        			client.requestFittingRoom();

                    //KEEP CLIENT ALIVE UNTIL IT EXITS ON ITS OWN. 
                    //OTHERWISE, MAIN THREAD WILL EXIT AND CLOSE CONNECTION. 
                    try{
                        //Adding a timer for each customer to request a room.
                        while(client.hasRoom || client.isWaiting|| (client.socket != null && !client.socket.isClosed())){
                            Thread.sleep(100);

                            //if client no longer has room or is waiting then force clean up. 
                            if(!client.hasRoom && !client.isWaiting && client.socket != null && !client.socket.isClosed()){
                                client.exit(); 
                            }
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
			}}).start();

        	try {
            	Thread.sleep((int)(Math.random() * 1000));//this is the delay for the random entrance time
        	} catch (InterruptedException e) {
            	e.printStackTrace();
        	}

        }
        
    	}

	}
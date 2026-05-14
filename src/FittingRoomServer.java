import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class FittingRoomServer {

    static Semaphore rooms;
    static int waitMax;
    static Queue<Integer> waitingQueue = new LinkedList<>();
    static Queue<Integer> priorityQueue = new LinkedList<>();
    static int totalRooms;

    public static int replaceWaiting(int clientID){

        int replacedClient = -1; 
        
        //make copy to safely remove from real queue
        ArrayList<Integer> tempQueue = new ArrayList<>(waitingQueue);

        for(Integer i: tempQueue){
            
            //remove dead clients
            if(CentralServer.clientIDs.get(i)==null){
                waitingQueue.remove(i);
                priorityQueue.remove(i); 

                continue; 
            }

            //find first normal waiting client
            if(!priorityQueue.contains(i)){
                replacedClient = i; 
                break;
            }
        }
    
        if(replacedClient != -1){
            waitingQueue.remove(Integer.valueOf(replacedClient));

            //avoid duplicates 
            if(!waitingQueue.contains(clientID)){
                waitingQueue.add(clientID); 
            }

            return replacedClient; 
        }
        

        return -1;
    }

    public static void main(String[] args) throws IOException {

        int totalRoomsArg = Integer.parseInt(args[0]);
        totalRooms = totalRoomsArg;

        rooms = new Semaphore(totalRoomsArg);
        waitMax = totalRoomsArg * 2;

        System.out.println("Fitting Room Server started...");

        Socket central = new Socket("127.0.0.1", 50001);

        BufferedReader br =
                new BufferedReader(new InputStreamReader(central.getInputStream()));

        PrintWriter pw =
                new PrintWriter(central.getOutputStream(), true);

        while (true) {

            String message = br.readLine();
            System.out.println("MESSAGE: " + message);

            if (message == null) break;

            String[] parts = message.split(" ");

            synchronized (FittingRoomServer.class) {

                if (parts[0].equals("ALLOCATE") || parts[0].equals("PRIORITY")) {

                    int clientID = Integer.parseInt(parts[1]);
                    if(parts[0].equals("PRIORITY")){
                        if(waitingQueue.contains(clientID) || priorityQueue.contains(clientID)){
                            pw.println("Wait " + clientID); 
                            continue; 
                        }
                    }

                    if (rooms.tryAcquire()) {

                        pw.println("Allocated " + clientID);
                        System.out.println("Allocated " + clientID);

                    }else if (parts[0].equals("PRIORITY")) { //When fitting room shuts down, if a client was changing they will
                       
                        if(waitingQueue.contains(clientID) || priorityQueue.contains(clientID)){
                            pw.println("Wait " + clientID); 
                            continue; 
                        }
                        if(!priorityQueue.contains(clientID)){
                            priorityQueue.add(clientID);         //have priority status over other clients
                        }
                        

                        int replaced = replaceWaiting(clientID);

                        if(replaced != -1){
                           
                            pw.println("Replaced " + replaced + " with " + clientID);
                            
                            System.out.println("Replaced " + replaced + " with " + clientID);
                            System.out.println("Priority client " + clientID + " replaced " + replaced); 
                            
                        }
                        else if(waitingQueue.size() < waitMax){

                            //remove old copies
                            waitingQueue.remove(Integer.valueOf(clientID));
                            priorityQueue.remove(Integer.valueOf(clientID)); 

                           //re-add once
                           waitingQueue.add(clientID);
                           priorityQueue.add(clientID); 
                            
                            pw.println("Wait " + clientID);

                            System.out.println("Priority client waiting: " + clientID); 
                        }else{

                            priorityQueue.remove(Integer.valueOf(clientID));
                            pw.println("Full " + clientID);

                            System.out.println("Full " +  clientID); 
                        }

                    }else if (waitingQueue.size() < waitMax) {
                        if(!waitingQueue.contains(clientID)){
                            waitingQueue.add(clientID);
                        }
                        
                        pw.println("Wait " + clientID);
                        System.out.println("Wait " + clientID);
                    }else {

                        pw.println("Full ");
                        System.out.println("Full ");
                    }

                }else if (parts[0].equals("RELEASE")) {
                    int id = Integer.parseInt(parts[1]);

                    System.out.println("Rooms Before release: " + rooms.availablePermits());

                    if (rooms.availablePermits() < totalRooms) {
                        rooms.release();
                    }
                    if(priorityQueue.contains(id)){
                        priorityQueue.remove(id);
                    }

                    System.out.println("Rooms After release: " + rooms.availablePermits());

                    if (!waitingQueue.isEmpty()) {

  
                        int nextClient;
                        if(!priorityQueue.isEmpty()){
                            nextClient = priorityQueue.poll(); 

                            waitingQueue.remove(Integer.valueOf(nextClient));
                        }else{
                            nextClient = waitingQueue.poll(); 
                        }

                        priorityQueue.remove(Integer.valueOf(nextClient));

                         pw.println("Promoted " + nextClient); 
                         System.out.println("Promoted from queue: " + nextClient);

                        
                    }else{
						pw.println("Released."); 
						System.out.println("WAITING QUEUE IS EMPTY"); 
					}
                }
            }
        }
    }
}
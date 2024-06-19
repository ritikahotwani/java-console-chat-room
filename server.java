package javaTcpChatRoom;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class server  implements Runnable{
    private ArrayList<ConnectionHandler> clients =new ArrayList<ConnectionHandler>();
    private ServerSocket server;
    private boolean done =false;
    private ExecutorService pool;
    public server(){
        clients =new ArrayList<>();
    }
    public void run() {
        try {

            server =new ServerSocket(9999);
            pool=Executors.newCachedThreadPool();

            while(!done){
                Socket client =server.accept();
                ConnectionHandler handler =new ConnectionHandler(client);
                clients.add(handler);
                pool.execute(handler);          //runs the handler in a separate thread , the run function of the handler is called
            
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
            shutDown();
        }
    }
    public void broadcastMessage(String message){
        for(ConnectionHandler client:clients){
           if(client!=null){
               client.sendMessage(message);
           }
        }
    }
    public void shutDown(){
       if(!server.isClosed()){
           try {
                done=true;
                pool.shutdown();
               server.close();
               for(ConnectionHandler client:clients){
                   client.shutDown();
               }
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
    }
    public class ConnectionHandler implements Runnable{

            private Socket client;
            private String name;
            private BufferedReader in;
            private PrintWriter out;

            public ConnectionHandler(Socket client){
                this.client=client;
            }


            public void run(){
                try {
                    out =new PrintWriter(client.getOutputStream(),true);
                    in =new BufferedReader(new InputStreamReader(client.getInputStream()));
                    out.println("Enter your name");
                    name=in.readLine();
                    out.println("Welcome to the chat room,  "+name);
                    System.out.println(name+" has joined the chat room");
                    broadcastMessage(name+" has joined the chat room");

                    String message;
                    while((message=in.readLine())!=null){

                       if(message.equalsIgnoreCase("exit")){
                           break;
                       }
                       else if(message.startsWith("/nick ")){
                        // changes the name of the user
                        String[] newName =message.split(" ",2);
                        if(newName.length==2){
                            broadcastMessage(name+" has changed name to "+newName[1]);
                            System.out.println(name+" has changed name to "+newName[1]);
                            name=newName[1];
                            System.out.println("Name changed to "+name);

                        }
                        else{
                            out.println("no name provided");
                        }

                       }
                       else if(message.startsWith("/quit")){
                        // lists all the users in the chat room
                        broadcastMessage(name+" has left the chat room");
                        shutDown();

                       }
                       else{
                           broadcastMessage(name+" : "+message);
                        }
                    }
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    shutDown();
                }
            }
            public void sendMessage(String message){
                out.println(message);
            }
            public void shutDown(){
                try {
                    in.close();
                    out.close();
                    if(!client.isClosed()){
                       
                        client.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    shutDown();
                }
            }
            
    }
    public static void main(String[] args) {
        server server =new server();
        server.run();
    }
}

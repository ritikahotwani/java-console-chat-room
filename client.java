package javaTcpChatRoom;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class client implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;

    private boolean done =false;
    public void run(){
        try {
             client =new Socket("localhost",9999);
            out=new PrintWriter(client.getOutputStream(),true);
            // client.getInputStream() is used to read data from the server
            in =new BufferedReader(new InputStreamReader(client.getInputStream()));
            inputHandler input =new inputHandler();
            Thread inputThread =new Thread(input);
            inputThread.start();

            String message;
            while((message=in.readLine())!=null){
                System.out.println(message);
            }
        } catch (Exception e) {
            // TODO: handle exception
            shutDown();
        }
    }
    public void shutDown(){
        done=true;
        try {
            in.close();
            out.close();
            if(!client.isClosed()){
                client.close();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
    class inputHandler implements Runnable{
        public void run(){
            try {
                // system.in because we are reading from the console
                BufferedReader inReader =new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message=inReader.readLine();
                    if(message.equalsIgnoreCase("/quit")){
                        out.println(message);
                        done=true;
                        inReader.close();
                        shutDown();
                    }
                    else{
                        // this is the message that is sent to the server which in turn broadcasts it to all the clients
                        out.println(message);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                shutDown();
            }
    }
}
public static void main(String[] args) {
    client client =new client();
    // Thread clientThread =new Thread(client);
    client.run();
}
}

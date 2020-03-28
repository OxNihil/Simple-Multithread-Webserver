package es.udc.redes.webserver;
import java.net.*;
import java.io.*;

/**
 * This is the main class of the web server, the class starts the TCP socket, 
 * the input channel and the output channel, defining the necessary exceptions
 * for the proper functioning of the server. This class only have
 * one attribute, the conf attribute, and this is an instance of the class 
 * config
 **/
public class WebServer {
    config conf;
   
    

    public WebServer() throws IOException{
        conf = new config();
        conf.getPropValues();
    }
   
    
    public void start(){
        /*
         *@params  dont recive any parameters
         *@return this is a void method , dont return anything
         *@throws this method have three exceptions.
         *SocketTimeoutException this exception its throwed if the socket 
         *dont recive anything in 300 secs
         *IOException its throwed if ocurred an Input/Output exception with the TCP Socket
         *and has a general exception catch that its throwed if ocurred an unexpected
         *error
         *
         **/
        ServerSocket socketTCP = null; 
        try {
            // Create a server socket
            socketTCP = new ServerSocket(this.conf.getPort());
            // Set a timeout of 300 secs
            socketTCP.setSoTimeout(300000);
            while (true) {
                // Wait for connections
                Socket socket = socketTCP.accept();
                socket.setSoTimeout(300000);
                // Set the input channel
                BufferedReader sInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // Set the output channel
                PrintWriter sOutput;
                sOutput = new PrintWriter(socket.getOutputStream(), true);
                //
                
                //Create Server Thread 
                ServerThread thread = new ServerThread(socket,sInput,sOutput,conf);
                thread.start();
            }
        // Uncomment next catch clause after implementing the logic            
        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            //Close the socket
            try {  
                socketTCP.close(); 
            } catch (IOException e){
                e.printStackTrace(); 
            } 
        }
    }
    
     public static void main(String[] args) throws IOException{      
        /*
        * @params recive an array with the command line parameters
        * @return this is a void method , dont return anything
        * @throws no exceptions are defined
        * this is the main method of this class, the method create a new
        * instance of webserver and call the start method
        */
        WebServer server = new WebServer();
        server.start();
    }
}

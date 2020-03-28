/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.udc.redes.webserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Date;

/**
 *
 *  this class implements the logic of the logging service, store the errors 
 *  (status code 200-300) and the correct accesses (status code 400-500) 
 */
public class logger implements Runnable{
    private String request;
    private String ip;
    private String date;
    private int statuscode;
    private String statusmsg;
    private long size;
    private config conf;
    
    public logger(){}
    
    @Override
    public void run(){}
  
    
    private String getIP(Socket socket) {
        /**        
        * @throws dont throws any exception
        * @params recive the connection socket
        * @return an String with the IP protocol version and the ip number
         *
         */
         SocketAddress socketAddress = socket.getRemoteSocketAddress();
         InetAddress inet = ((InetSocketAddress)socketAddress).getAddress();
         if (inet instanceof Inet4Address){
            return ("IPv4: " + inet);
         }else if (inet instanceof Inet6Address){
            return ("IPv6: " + inet);
         }else{
            System.err.println("No es una direccion IP valida.");
         }
        return socket.getInetAddress().getHostAddress();
    }
    
    public void logAccess(){
        /** 
        * this method calls a new thread for writing in the correct log file
        * @throws dont throws any exception
        * @params dont recive any parameters
        * @return dont return anything
        */
       String line = "";
       line += this.request+"\t\t"+this.ip+"\t"+this.date+"\t";
       line += this.statuscode+"\t"+this.size;
       Worker w = new Worker(line, "correct.log",this.conf.getDirectory());
       Thread t = new Thread(w);
       t.start();
    }
    
    public void logError(){
        /** 
        * this method calls a new thread for writing in the error log file
        * @throws dont throws any exception
        * @params dont recive any parameters
        * @return dont return anything
        */
        String line = "";
        line += this.request+"\t\t"+this.ip+"\t"+this.date+"\t"+this.statusmsg;
        Worker w = new Worker(line, "error.log",this.conf.getDirectory());
        Thread t = new Thread(w);
        t.start();
    }
    
    public void log(Socket s,HttpResponse r,String recibido, config conf) throws IOException{
        /** 
        *
        * @throws an Input/Output exception if the request file are not found
        * the exception are called with the contentLength() method
        * @params recive the connection socket, the HttpResponse class, the client
        * request and the config data
        * @return dont return anything
        */
        this.request = recibido;
        this.ip = getIP(s);
        this.date = r.getDate();
        this.statuscode = r.getStatusCode();
        this.size = r.getContentLenght();
        this.statusmsg = r.getStatusMessage(this.statuscode);
        this.conf = conf;
        int code = Integer.parseInt(Integer.toString(this.statuscode).substring(0, 1));
        switch(code){
            case 1: break;
            case 2: 
                logAccess();
                break;
            case 3: 
                logAccess();
                break;
            case 4: 
                logError();
                break;
            case 5: 
                logError();
                break;
            default:
                break;
        
        }
    }
    
    private class Worker implements Runnable {   
        /** this method is called by a new thread for writing the logs **/
        private final String linealog;
        private final String fichero;
        private final String directory;
        
        //Constructor
        public Worker(String log, String file, String dir) {
            this.linealog = log;
            this.fichero = file;
            this.directory = dir ;
        }
        
        @Override
        public void run() {
            /**
             * @throws dont throws any exception
             * @params dont recive any parameters
             * @return dont return anything
             */
            try (PrintWriter out = new PrintWriter(new FileOutputStream(new File(this.directory+"/"+fichero), true))) {
                out.append(this.linealog+"\r\n");
                out.close();
            } catch (FileNotFoundException notfound) {
                System.err.println("No se pudo abrir el fichero de log: " + fichero);
            }
        }
    }
}

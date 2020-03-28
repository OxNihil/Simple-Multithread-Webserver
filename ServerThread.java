/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.udc.redes.webserver;

import java.net.*;
import java.io.*;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Daniel Gonzalez 
 */
public class ServerThread extends Thread {
    private final Socket socket;
    private final BufferedReader din; 
    private final PrintWriter dout;
    private final config conf;
    private DataOutputStream dos;
    public ServerThread(Socket s,BufferedReader din,PrintWriter dout, config c){
        this.socket = s;
        this.din = din; 
        this.dout = dout; 
        this.conf = c;
        this.dos = null;
    }
    

    
    public void run(){
        try {
            this.dos = new DataOutputStream(this.socket.getOutputStream());//no se eu
            // Receive the client message
            String recibido = din.readLine();
            //si la peticion es valida ejecuta
            if (recibido != null){
                String[] request = recibido.split(" "); 
                if(request != null || request.length > 1){
                    System.out.println("SERV: Recived "+recibido);
                    HttpResponse resp = new HttpResponse(recibido,this.conf,this.socket);
                    // Send response to the client
                    if (resp.isDirRequest()){
                        //es un dir 
                        resp.dirresource = resp.resource;
                        resp.resource = request[1]+this.conf.getDirectoryIndex();
                        resp.isDir = true;
                        String header = resp.getHeader();
                        dout.println(header);
                        if(resp.FileExists()){
                            resp.doGet();                                     
                        } else {
                            //no existe
                            String listadir = resp.getDirAllow();
                            dout.println(listadir);
                        }
                    } else {
                        //no es un dir 
                        String[] ext = request[1].split("[.]");
                        if(ext == null || ext.length == 1){
                            //no tiene extension
                            resp.setStatusCode(400);
                            String header = resp.doHead();
                            dout.println(header);
                            String response = resp.getInvalid();
                            dout.println(response);
                        } else {
                            //tiene extension
                             if (ext[1].startsWith("do")){
                                //es peticion dinamica -> do head + get dinamico
                                String classname = ext[0].substring(1);
                                int index = request[1].lastIndexOf("?");
                                String query = request[1].substring(index+1);
                                System.out.println(query);
                                String response = resp.servlet(classname,query);
                                dout.println(response);
                             }else {
                                 //no es peticion dinamica
                                 String header = resp.getHeader();
                                 dout.println(header);
                                 //miramos si existe el recurso solitado
                                 if(resp.FileExists()){
                                     resp.doGet();                                     
                                 } else {
                                     //no existe y no es un directorio
                                     String notfound = resp.getNotFound();
                                     dout.println(notfound);
                                 }
                             }
                        }                    
                    }                
                    logger loging = new logger();
                    loging.log(this.socket,resp,recibido,conf);
                }
            }           
            // Close the streams
            dout.close();
            din.close();
        } catch (SocketTimeoutException e){
            System.err.println("Nothing recived in 300 secs");
        } catch (Exception e){
            System.err.println("Error"+e.getMessage());
        } finally{
            try{
                socket.close();
            } catch (IOException ex){
                Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE,null,ex);
            }
        } 
    }
}

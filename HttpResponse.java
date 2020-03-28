/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.udc.redes.webserver;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * 
 * this class proccess the client request and generate the head and the body
 * request for static and dynamic pages
 */
public class HttpResponse {
    private final String method;
    private final String protocol;
    private final config conf;
    private final Socket socket;
    protected String resource;
    protected String dirresource;
    private int statusCode;
    public boolean isDir;
    private String request;
    private final SimpleDateFormat time = new SimpleDateFormat("E d MMM YYYY hh:mm:ss");
    //constructor
    public HttpResponse(String request,config c,Socket s){
        String[] peticion = request.split(" ");
        this.isDir = false;
        this.method = peticion[0];
        this.resource = peticion[1];
        this.protocol = peticion[2];
        this.conf = c;
        this.socket = s;
        this.request = request;
    
    }
    
    public String getdynhead(long size){
        /**
         * @throws dont throw any exception
         * @params recive the size of the dynamic response
         * @return this return a string with the header response of dynamic pages
         */
        String cabezera = "";
        this.setStatusCode(200);
        cabezera += this.protocol+" "+this.getStatusCode()+" "+this.getStatusMessage(this.getStatusCode())+"\n";
        cabezera += "Date: "+this.getDate()+"\n";
        cabezera += "Server: "+this.conf.getHostname()+"\n";
        cabezera += "Content-type: text/html \n";
        cabezera += "Content-Length: "+size+"\n";
        return cabezera;
    }
    
    public String servlet(String classname,String query) {
      /**        
        * @throws dont throw any exception
        * @params recive the classname, and the query with the parameters
        * @return a string with the dynamic response
        */
        Map<String, String> parameters = new HashMap<>();
        String cabecera = "";
        String[] parametros = query.split("&");
        for (String para : parametros) {
            String value = "";
            String name = para.split("=")[0];
            if (para.split("=").length > 1){
                value = para.split("=")[1];   
            }
            parameters.put(name, value);
        }
        try {
            String result = ServerUtils.processDynRequest(this.getClass()
                    .getPackage().getName()+"."+classname,parameters);
            long size = result.getBytes().length;
            cabecera = getdynhead(size);
            cabecera += "\n"+result;
        } catch (Exception ex) {
            System.out.println("EXCEPTION: "+ex);
        }
        return cabecera;
    }
    
    //fileExist method
    public boolean FileExists(){
        /**
         * @throws dont throw any exceptions
         * @params dont recive any parameters
         * @return a boolean value if the file exists in the server directory
         */
        int index = this.resource.lastIndexOf("/");
        String filename = this.resource;
        File dir = new File(this.conf.getDirectory()+this.resource.substring(0,index+1));
        String[] ficheros = dir.list();
        if (ficheros != null){
            for (String fichero : ficheros) {
                if(fichero.equals(filename.substring(index+1))){
                    this.statusCode = 200;
                    return true;
                }
            }
        }
        this.statusCode = 404;
        return false;
    }
    
    
    public boolean isDirRequest(){
        /**        
        * @throws dont throw any exception
        * @params dont recive any parameters
        * @return a boolean value if the client request a directory
        */
        return "/".equals(this.resource.substring(this.resource.length()-1));
    }
    
    public String getDirAllow(){
        /**        
        * @throws dont throws any exception
        * @params dont recive any parameters
        * @return the list of files and subdirectories for the request directory
        * if the allow directive is true
        */
        String response = "";
        if(this.conf.getAllow()){
            File dir = new File(this.conf.getDirectory()+this.dirresource);
            System.out.println(this.conf.getDirectory()+this.dirresource);
            if (dir.isDirectory()){
                File[] ficheros = dir.listFiles();
                response += "<h2>Lista de ficheros</h2><br>";
                for (File fichero : ficheros) {
                    int index = fichero.toString().lastIndexOf("\\");
                    if(fichero.isDirectory()){
                       response += "<a href='"+this.dirresource
                               +fichero.toString().substring(index+1)+"/"+"'>"
                               +fichero.toString().substring(index)+"</a></p>\n";
                    } else {                    
                        response += "<a href='"+this.dirresource
                                +fichero.toString().substring(index+1)+"'>"
                               +fichero.toString().substring(index+1)+"</a></p>\n";
                    }
                }
                return response;
            } else {
                 response += this.getNotFound();
            }
        }else {
            response += this.getForbidden();
        }
        return response;
    }
    public boolean containsIMS(){
      /**        
        * @throws dont throws any exception
        * @params dont recive any parameters
        * @return a boolean value if the client request contains
        * the string "If-Modified-Sice"
        */
        boolean value = false;
        String ifmodsince = "If-Modified-Since: ";
        if(this.request.contains(ifmodsince)){ value = true;}
        return value;
    }
    public void checkStatusCode() throws ParseException{
         /**
         * @throws an exception if there was a problem formating the date
         * @params dont recive any parameters
         * @return dont return anything
         */
        Date lastmod = this.time.parse(this.getLastModified());
        Date date = this.time.parse(this.getDate());
        if (!"GET".equals(this.method) && !"HEAD".equals(this.method)){
            this.statusCode = 400;
        } else if (this.method.equals("GET")){
            if(this.FileExists()){
                this.statusCode = 200;
                if(this.containsIMS()){
                    if (lastmod.compareTo(date) < 0){
                        this.statusCode = 304;
                    }
                }
            } else {              
                this.statusCode = 404;  
            }
        } else if (this.method.equals("HEAD")){
            if(this.FileExists()){
                this.statusCode = 200;
                if(this.containsIMS()){
                    if (lastmod.compareTo(date) < 0){
                        this.statusCode = 304;
                    }
                }
            }else{
                this.statusCode = 404;
            }
        }
    }
    
    public String getDate(){
       /**        
        * @throws dont throws any exception
        * @params dont recive any parameters
        * @return a string with the current time
        */
         
        String date = "";
        date = this.time.format(new Date());
        return date;
    }   
    
    public String getNotFound(){
      /**        
        * @throws dont throws any exception
        * @params dont recive any parameters
        * @return a string with the response in the case of file are not found
        */
         
        return "<html><body><h1>404 File Not found</h1><body></html>";
    }
    
    public String getInvalid(){
       /**        
        * @trhows dont trhows any exception
        * @params dont recive any parameters
        * @return a string whit the response in case of the request are invalid
        */
        return "<html><body><h1>400 Invalid Request</h1><body></html>";
    }
    
    public String getForbidden(){
        /**        
        * @throws dont throws any exception
        * @params dont recive any parameters
        * @return a string with the response in case of the client requesting a 
        * directory and the directive allow is false
        */
        return "<h2>403 Access Forbidden</h2>";
    }
    public String doHead() throws IOException{
       /**        
        * @throws an exception if there was a problem to get the content lenght
        * of the request file
        * @params dont recive any parameters
        * @return a string with the header of the response
        */
        String cabezera = "";
        cabezera += this.protocol+" "+this.getStatusCode()+" "+this.getStatusMessage(this.getStatusCode())+"\n";
        cabezera += "Date: "+this.getDate()+"\n";
        cabezera += "Server: "+this.conf.getHostname()+"\n";
        cabezera += "Last-Modified: "+this.getLastModified()+"\n";
        cabezera += "Content-type: "+ this.getContentType()+"\n";
        cabezera += "Content-Length: "+this.getContentLenght()+"\n";
        return cabezera;
    }
    
    public void doGet() throws IOException{
       /**        
        * @throws an exception if there was a problem to get the content of the request file
        * @params dont recive any parameters
        * @return a string with the header of the response
        */
        DataOutputStream in = new DataOutputStream(this.socket.getOutputStream());
        File body = new File(this.conf.getDirectory()+this.resource);
        FileInputStream input = new FileInputStream(body);
        byte[] buffer = new byte[(int)this.getContentLenght()];
        int count = 0;
        while ((count = input.read(buffer)) > 0){
            in.write(buffer, 0, count);
        }
    }
             

    public String getHeader() throws FileNotFoundException, IOException, ParseException{
       /**        
        * @throws dont throws any exception
        * @params dont recive any parameters
        * @return the header of the server response
        */
        this.checkStatusCode();
        String response = doHead();
        return response;
    }
    
    public String getLastModified(){
       /**        
        * @throws dont throws any exception
        * @params dont recive any parameters
        * @return  the date of the last modification of the requested file
        */
        File f = new File(this.conf.getDirectory()+this.resource);
        String fecha = this.time.format(f.lastModified());
        return fecha;
       
    }
    public long getContentLenght() throws IOException{
       /**        
        * @trhows an Exception if there was a problem with the request file
        * @params dont recive any parameters
        * @return the lenght of the request file
        */
        long longitud = 0;
        if(FileExists()){
            File fichero = new File(this.conf.getDirectory()+this.resource);
            longitud = (int)fichero.length();
        }else{
            if(this.isDir){
                String data = this.getDirAllow();
                longitud = data.length();
            } else {
                String data = this.getNotFound();
                longitud = data.length();
            }
        }
        return longitud;        
    }
    
    public String getStatusMessage(int status){
       /**        
        * @throws dont throws any exception
        * @params recive the status code
        * @return  the message corresponding to a given status code
        */
        switch(status) {
        case 100: return "Continue";
        case 101: return "Switching Protocols";
        case 103: return "Checkpoint";
        case 200: return "OK";
        case 201: return "Created";
        case 202: return "Accepted";
        case 203: return "Non-Authoritative Information";
        case 204: return "No Content";
        case 300: return "Multiple Choices";
        case 301: return "Moved Permanently";
        case 302: return "Found";
        case 400: return "Bad Request";
        case 401: return "Unauthorized";
        case 403: return "Forbidden";
        case 404: return "Not Found";
        case 405: return "Method Not Allowed";
        case 500: return "Internal Server Error";
        case 501: return "Not Implemented";
        case 502: return "Bad Gateway";
        case 503: return "Service Unavailable";
        default:
            return "Continue";
        }
    }
    
    public void setStatusCode(int code){
      /**        
        * @throws dont throws any exception
        * @params recive the status code
        * @return dont return anything
        */
        this.statusCode = code;
    }
    
    public int getStatusCode(){
      /**        
        * @throws dont throws any exception
        * @params dont recive anything
        * @return the status code
        */
        return this.statusCode;
    }
    
    public String getContentType(){
      /**        
        * @throws dont throws any exception
        * @params dont recive anything
        * @return an String with the MIME contentType 
        */
        String[] ext = this.resource.split("[.]");
        if (ext == null || ext.length == 1  ){return "text/html";}
        switch(ext[1]) {
        case "html":
          return "text/html";
        case "do":
          return "text/html";
        case "css":
          return "text/css";
        case "xml":
          return "text/xml";
        case "txt":
          return "text/plain";
        case "js":
          return "application/javascript";
        case "json":
          return "application/json";
        case "pdf":
          return "application/pdf";
        case "png":
          return "image/png";
        case "gif":
          return "image/gif";
        case "jpg":
          return "image/jpeg";
        default:
            return "application/octet-stream";
        }
    }
}

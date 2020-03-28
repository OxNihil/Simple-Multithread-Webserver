/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.udc.redes.webserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Daniel Gonzalez 
 * this class read the configuration of the web server, who is stored in the
 * file config.properties 
 */
public class config {
    InputStream inputStream;
    private String PORT; 
    private String ALLOW;
    private String HOSTNAME;
    private String DIRECTORY;
    private String DIRECTORY_INDEX;
    //Getters
    
    protected String getHostname(){
        /*
        * @throws dont throws any exception
        * @params dont recive any parameters
        * @return this method return the hostname value 
        */
        return this.HOSTNAME;
    }
    protected boolean getAllow(){
        /*
        * @throws dont throws any exception
        * @params dont recive any parameters
        * @return this method return a boolean value for the variable allow
        */
        String val = this.ALLOW;
        if (val.toLowerCase().equals("true")){
            return true;
        } else {
            return false;
        }
    }
    protected String getDirectory(){
        /*
        * @throws dont throws any exception
        * @params dont recive any parameters
        * @return this method return the directory path
        */
        return this.DIRECTORY;
    }
    protected String getDirectoryIndex(){
       /*
        * @throws dont throws any exception
        * @params dont recive any parameters
        * @return this method return the index file of the webserver
        */
        return this.DIRECTORY_INDEX;
    }
    protected int getPort(){
       /*
        * @throws dont throws any exception
        * @params dont recive any parameters
        * @return this method return the port 
        */
        return Integer.parseInt(this.PORT);
    }
    
    //Constructor 
    public config(){}
    
    public void getPropValues() throws IOException {
        /**
         * this method read the property file and set the values in the class
         * attributes
         *@throws this method throws an Input/Output exception in the case of
         * the file config.properties is not found, and handle another 
         * unexpected exceptions properly
        * @params dont recive any parameters
        * @return this method dont return anything
         */
        try {
            Properties prop = new Properties();
            String path = System.getProperty("user.dir") +"\\src\\es\\udc\\redes\\webserver\\resources\\";
	    String filename = "config.properties";
            inputStream = new FileInputStream(path+filename);
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
		throw new FileNotFoundException("property file '"+path + filename + "' not found");
            }
	    // get the property value and set in attributes
	    this.PORT = prop.getProperty("PORT");
            this.DIRECTORY_INDEX = prop.getProperty("DIRECTORY_INDEX");
            this.DIRECTORY = System.getProperty("user.dir")+ prop.getProperty("DIRECTORY");
            this.ALLOW = prop.getProperty("ALLOW");
            this.HOSTNAME = prop.getProperty("HOSTNAME");
            System.out.println();
        } catch (Exception e) {
		System.out.println("Exception: " + e);
	} finally {
		inputStream.close();
	}
    }

}


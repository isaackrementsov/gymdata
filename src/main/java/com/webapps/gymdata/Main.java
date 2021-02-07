package com.webapps.gymdata;

// Main application class
public class Main {
    
    public static void main(String[] args) {
        try {
            Server.initialize();            
        }catch(Exception e){
            System.out.println("There was an error starting the server...");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
}

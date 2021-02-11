/* 
GymData Web Application
Written by Isaac Krementsov, 2021 

See README.md in the root project directory for setup details
*/


package com.webapps.gymdata;

// Start the app from this file
public class Main {
    
    public static void main(String[] args) {
        try {
            // Start the application server
            Server.initialize();            
        }catch(Exception e){
            // Handle errors starting the server -- this is usually caused by an invalid config.json
            System.out.println("There was an error starting the server...");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
}

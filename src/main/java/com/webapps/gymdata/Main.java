/* 
GymData Web Application
Written by Isaac Krementsov, 2021 

See README.md in the root project directory for setup details
*/

// Start the app from this file
package com.webapps.gymdata;

import com.webapps.gymdata.models.Employee;
import com.webapps.gymdata.models.Member;
import com.webapps.gymdata.models.Scan;
import java.util.Date;

public class Main {
    
    public static void main(String[] args) {
        try {
            // Uncomment line below to generate dummy member/scan data
            //createData(20, 20);
            // Uncomment line below to generate dummy staff data
            //createStaffData(25);
            
            // Start the application server
            Server.initialize();            
        }catch(Exception e){
            // Handle errors starting the server -- this is usually caused by an invalid config.json
            System.out.println("There was an error starting the server...");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Randomly generate dummy member/scan data (n = number of members, s = minimum number of scans per member)
    public static void createData(int n, int s){
        // Constants for converting between milliseconds and hours/days
        double MS_PER_HOUR = 3.6e6;
        double MS_PER_DAY = MS_PER_HOUR*24;
        
        // Genders to be randomly selected
        String[] genders = {"male", "female"};
        
        for(int i = 0; i < n; i++){
            // Randomly select a gender (index is 1 or 0)
            String gender = genders[(int) Math.round(Math.random())];
            
            // Create a new member with a set name (since this doesn't really matter in testing)
            Member member = new Member("Jeff", "Smith", gender);
            // Save the member to the database
            member.save();
            
            // Create a bunch of scan-ins and scan-outs (start with a scan-in)
            boolean in = true;            
            Date lastDate = null;
            
            // Randomly scans between s and s + 30 times
            for(int j = 0; j < s + 30*Math.random(); j++){
                // Date when the scan occured
                Date newDate;
                
                // If this is a scan-in, generate a new date
                if(in){
                    // The date is 0-40 days and 0-24 hours before present
                    double present = (new Date()).getTime();
                    newDate = new Date((long) (present - 40*Math.random()*MS_PER_DAY - 24*Math.random()*MS_PER_HOUR));
                    // Store this in the lastDate variable for generating the scan-out date
                    lastDate = newDate;
                }else{
                    // If this is a scan-out, randomly generate a date 0-5 hours after the last scan-in
                    newDate = new Date((long) (lastDate.getTime() + 5*Math.random()*MS_PER_HOUR));
                }
                
                Scan scan = new Scan(newDate, in, member);
                scan.save();
                
                // Alternate between scan-ins and scan-outs
                in = !in;
            }
        }
    }
    
    // Randomly generate employee data (n = number of employees)
    public static void createStaffData(int n){        
        for(int i = 0; i < n; i++){
            // Randomly assign 20-40 hours per week, in 5 hour intervals
            int hours = (int) Math.round(20 + 5*Math.round(20*Math.random()/5));            
            
            // Create an employee with a set name, random hours, and a random wage between $10-20/hour
            Employee employee = new Employee("Jeff", "Smith", hours, 10 + 10*Math.random());
            // Persist the employee to the database
            employee.save();
        }
    }
}

package com.webapps.gymdata.controllers;

import com.webapps.gymdata.models.Employee;
import com.webapps.gymdata.models.Member;
import com.webapps.gymdata.models.Scan;
import com.webapps.gymdata.Server;

import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Controller for loading data reports
public class ReportsController {
    
    // Load a membership report
    public static void getMembership(Context ctx){
        // Get all scans in the gym records
        List<Scan> scans = Scan.getAll();
        // Show the 10 most recent scans
        Collections.sort(scans, new Scan.DateComparator());
        scans = scans.subList(0, Math.min(scans.size() - 1, 10));
        
        // Get all gym members
        List<Member> members = Member.getAll();
        // List to store time ranges where members were present
        List<Member.Timestamp> timestamps = new ArrayList<>();
        
        for(Member member : members){
            // Add each individual member's timestamps to the list
            timestamps.addAll(member.getTimestamps());
        }
        
        HashMap<String, Object> data = new HashMap<>();
        data.put("timestamps", timestamps);
        data.put("members", members);
        data.put("scans", scans);
        data.put("counter", Server.portRead.counter);
        
        // Load template with timestamps, members, and scans
        ctx.render("members.ftl", data);        
    }
    
    // Load staff report
    public static void getStaff(Context ctx){
        // Get all employees in the database
        List<Employee> staff = Employee.getAll();
        // Group employees by hour
        Map<String, Employee.HourGroup> hourGroups = new HashMap<>();
        
        // Keep track of aggregate cost
        double totalCost = 0.0;
        // Keep hour grouping map keys separately for displaying entries in a specific order
        List<Integer> hourGroupKeys = new ArrayList<>();
        
        for(Employee employee : staff){
            // Try to find an HourGroup to assign the employee
            Integer hours = employee.getHoursPerWeek();
            String hourKey = "hour_" + hours.toString();
            Employee.HourGroup current = hourGroups.get(hourKey);

            // If there is no existing group for this employee's hours, make one
            if(current == null){
                // Create a new HourGroup and add this employee's information
                current = new Employee.HourGroup(hours);
                current.addEmployee(employee);
                // Add this to the HashMap
                hourGroupKeys.add(hours);
                hourGroups.put(hourKey, current);
            }else{
                // If there is an existing group, add this employee's information to it
                hourGroups.get(hourKey).addEmployee(employee);
            }

            // Add each employee's cost to the total
            totalCost += employee.getWeeklyCost();            
        }
        
        // Sort the keys so that groups are listed in order of increasing hours
        hourGroupKeys.sort(Comparator.naturalOrder());
        
        HashMap<String, Object> data = new HashMap<>();
        data.put("totalCost", Math.round(totalCost*100.0)/100.0);
        data.put("staff", staff);
        data.put("hourGroups", hourGroups);
        data.put("hourGroupKeys", hourGroupKeys);
        
        // Render the staff report template and pass the total employee cost, staff, and hour groups as parameters
        ctx.render("staff.ftl", data);        
    }
    
}

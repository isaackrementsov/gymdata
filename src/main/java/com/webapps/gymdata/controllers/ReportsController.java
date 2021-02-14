package com.webapps.gymdata.controllers;

import com.webapps.gymdata.models.Employee;
import com.webapps.gymdata.models.Member;
import com.webapps.gymdata.models.Scan;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Controller for loading data reports
public class ReportsController {

    // Load a membership report
    public static void getMembership(Context ctx){
        // Get all scans in the gym records
        List<Scan> scans = Scan.getAll();
        // Get all gym members
        List<Member> members = Member.getAll();
        // List to store time ranges where members were present
        List<Member.Timestamp> timestamps = new ArrayList<>();
        
        for(Member member : members){
            // Add each individual member's timestamps to the list
            timestamps.addAll(member.getTimestamps());
        }
        
        // Load template with timestamps, members, and scans
        ctx.render("members.jte", Map.of("timestamps", timestamps, "members", members, "scans", scans));        
    }
    
    // Load staff report
    public static void getStaff(Context ctx){
        // Get all employees in the database
        List<Employee> staff = Employee.getAll();
        // Group employees by hour
        Map<Integer, Employee.HourGroup> hourGroups = new HashMap<>();
        
        // Keep track of aggregate cost
        double totalCost = 0.0;
        
        for(Employee employee : staff){
            // Try to find an HourGroup to assign the employee
            Integer hours = employee.getHoursPerWeek();
            Employee.HourGroup current = hourGroups.get(hours);
            
            // If there is no existing group for this employee's hours, make one
            if(current == null){
                // Create a new HourGroup and add this employee's information
                current = new Employee.HourGroup(hours);
                current.addEmployee(employee);
                // Add this to the HashMap
                hourGroups.put(hours, current);
            }else{
                // If there is an existing group, add this employee's information to it
                hourGroups.get(hours).addEmployee(employee);
            }

            // Add each employee's cost to the total
            totalCost += employee.getWeeklyCost();            
        }
        
        // Keep hour grouping map keys separately for displaying entries in a specific order
        List<Integer> hourGroupKeys = new ArrayList<>(hourGroups.keySet());
        // Sort the keys so that groups are listed in order of increasing hours
        hourGroupKeys.sort(Comparator.naturalOrder());
        
        // Render the staff report template and pass the total employee cost, staff, and hour groups as parameters
        ctx.render("staff.jte", Map.of("totalCost", totalCost, "staff", staff, "hourGroups", hourGroups, "hourGroupKeys", hourGroupKeys));        
    }
    
}

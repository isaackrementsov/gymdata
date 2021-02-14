package com.webapps.gymdata.models;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TypedQuery;

// JPA entity for storing Gym member data (MySQL table: members)
@Entity
@Table(name="members")
public class Member extends Model implements Serializable {
    
    // Required parameter for serialization version tracking
    private static final long serialVersionUId = 1L;
    
    // Automatically generated id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    // Member's personal information
    private String firstName;
    private String lastName;
    private String gender;  
    
    // Times the member has scanned in/out (related using join column in the Scan entity)
    @OneToMany(fetch=FetchType.EAGER, mappedBy="member")
    private List<Scan> scans = new ArrayList<>();
    
    // Getter and setter methods for each class attribute
    
    // Id has no setter method because it is automatically generated by MySQL
    public Integer getId(){
        return id;
    }
    
    // Add first and last name to get full
    public String getName(){
        return getFirstName() + " " + getLastName();
    }
    
    public String getFirstName(){
        return firstName;
    }
    
    public String getLastName(){
        return lastName;
    }
    
    public void setFirstName(String firstName){
        this.firstName = firstName;
    }
    
    public void setLastName(String lastName){
        this.lastName = lastName;
    }
    
    public String getGender(){
        return gender;
    }
    
    public void setGender(String gender){
        this.gender = gender;
    }
    
    // Get a member by a specific id (not used yet, but could be helpful in the future)
    // See com.webapps.gymdata.models.Model for information on the execute() method
    public static Member get(Integer id){
        Member member = (Member) Model.execute((Connection connection) -> {
            // Use JPA find method to get an entity matching the id
            return connection.em.find(Member.class, id);
        });
        
        return member;
    }
    
    // Get time ranges for when the member was present in the gym (local Timestamp class is at the bottom of this class body)
    public List<Timestamp> getTimestamps(){
        List<Timestamp> timestamps = new ArrayList<>();
        
        // Run through each of this member's scan-in/out times
        for(Scan scan : scans){
            if(scan.getScanIn()){
                // Create a new time range starting at the scan-in time
                timestamps.add(new Timestamp(scan.getDate(), this));
            }else{
                // If this is a scan out, get the last time range and end it with the scan-out time
                timestamps.get(timestamps.size() - 1).setEnd(scan.getDate());
            }
        }
        
        return timestamps;
    }
    
    // Get all members in the database
    public static List<Member> getAll(){
        List<Member> members = (List<Member>) Model.execute((Connection connection) -> {
            // HQL query to unconditionally select rows from the member (entity Member) table
            String queryString = "select m from Member m";
            // Turn string into actionable query
            TypedQuery<Member> query = connection.em.createQuery(queryString, Member.class);
            
            // Return all records found (this is passed to and returned by execute)
            return query.getResultList();
        });
        
        return members;
    }
   
    // Object to give a time range for when a member attends the gym
    public static class Timestamp {
        
        // Start and end dates for the range
        private Date start;
        private Date end;
        
        // The member that was present
        private Member member;
        
        // Getter and setter methods for class attributes
        
        public Date getStart(){
            return start;
        }
        
        public Date getEnd(){
            return end;
        }
        
        public void setEnd(Date end){
            this.end = end;
        }
        
        public Member getMember(){
            return member;
        }
        
        // Initialize with only the start date and member (usually using a scan-in)
        public Timestamp(Date start, Member member){
            this(start, null, member);
        }
        
        
        // Initialize with whole range and member
        public Timestamp(Date start, Date end, Member member){
            this.start = start;
            this.end = end;
            this.member = member;
        }
    }
    
    // Default constructor (needed for JPA)
    public Member(){  }
    
    // Create a member using personal information
    public Member(String firstName, String lastName, String gender){
        setFirstName(firstName);
        setLastName(lastName);
        setGender(gender);
    }
    
}

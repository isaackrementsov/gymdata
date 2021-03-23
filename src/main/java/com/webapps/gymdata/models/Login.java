// JPA Entity to store login credentials
package com.webapps.gymdata.models;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TypedQuery;

@Entity
@Table(name="login")
public class Login extends Model implements Serializable {
    
    // Required parameter for serialization version tracking
    private static final long serialVersionUId = 1L;
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)    
    private Integer id;
    
    private String username;
    private String password;
    
    public Integer getId(){
        return id;
    }
    
    public String getUsername(){
        return username;
    }
    
    public String getPassword(){
        return password;
    }
    
    public void setUsername(String username){
        this.username = username;
    }
    
    public void setPassword(String password){
        this.password = password;
    }
    
    // Check for user that matches input username and password
    public static boolean auth(String username, String password){
        List<Login> matched = (List<Login>) Model.execute((Connection connection) -> {
            String queryString = "select l from Login l where l.username = :username and l.password = :password";
            TypedQuery<Login> query = connection.em.createQuery(queryString, Login.class);
            
            query.setParameter("username", username);
            query.setParameter("password", password);
            
            return query.getResultList();
        });
        
        return matched.size() > 0;
    }
    
}

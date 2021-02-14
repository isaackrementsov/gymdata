package com.webapps.gymdata.models;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

// Base class for JPA entity objects
public class Model {

    // Use persistence library to create a factory for entity managers
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory("gymdata");
    
    // Execute a database action (Action interface is defined further down and functions as a callback method)
    public static Object execute(Action action){
        // Instantiate a manager from the factory
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction et = null;
        Object out = null;
        
        // Use a try block to handle database exceptions gracefully
        try {
            // Get & initialize database transaction from the manager
            et = em.getTransaction();
            et.begin();
            
            // Get return value from the Action callback (so that it can be returned outside of execute())
            out = action.run(new Connection(em, et));
            
            // Commit changes made in transaction
            et.commit();
        }catch(Exception e){
            handleError(e, et);
        }finally {
            // Always close the entity manager
            em.close();
            
            // Return result of the Action callback (or null if there was an error)
            return out;
        }
    }
    
    // Cleanly handle database exception 
    public static void handleError(Exception e, EntityTransaction et){
        if(et != null){
            // Rollback any changes made to the database
            et.rollback();
        }
        // Trace the exception in the console
        e.printStackTrace();
    }
    
    // Persist entity to the database
    public void save(){
        Model.execute((Connection connection) -> {
            // Use the JPA persist method to save this entity (equivalent to SQL INSERT/UPDATE SET) 
            connection.em.persist(this);
            
            // Return value from this callback is not needed
            return null;
        });
    }
    
        // Interface for executing database actions as callbacks
    static interface Action {        
        // Callback method; must return some object
        Object run(Connection connection);
    }
    
    // Object to store connection attributes
    static class Connection {
        
        // Keep instances of the entity manager and transaction
        EntityManager em;
        EntityTransaction et;
        
        public Connection(EntityManager em, EntityTransaction et){
            this.em = em;
            this.et = et;
        }
        
    }
}

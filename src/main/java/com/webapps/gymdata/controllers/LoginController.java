package com.webapps.gymdata.controllers;

import com.webapps.gymdata.Server;
import com.webapps.gymdata.models.Login;
import io.javalin.http.Context;

// Login system for securing the application
public class LoginController {
    
    // Render the index login page
    public static void get(Context ctx){
        // Render JTE template (https://javalin.io/tutorials/jte)
        ctx.render("login.ftl");
    }
    
    // Handle login form submit
    public static void post(Context ctx){
        // Get username and password from form data (<form action="/" method="POST"> in index.jte)
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");
                
        // Check whether the username and password entered match the admin credentials
        if(Login.auth(username, password)){
            // Add username to persistent session for validation on other pages
            ctx.sessionAttribute("username", username);
            // Redirect to membership reports page
            ctx.redirect("/reports/membership");
        }else{
            // If the credentials are incorrect, go back to the login page
            ctx.redirect("/login");
        }
    }
    
    // Log user out of a session
    public static void endSession(Context ctx){        
        // Destroy existing session
        ctx.req.getSession().invalidate();
        // Redirect to login page
        ctx.redirect("/login");
    }
    
}

package com.webapps.gymdata.controllers;

import com.webapps.gymdata.Server;
import io.javalin.http.Context;

// Login system for securing the application
public class LoginController {
    
    // Render the index login page
    public static void get(Context ctx){
        // Render JTE template (https://javalin.io/tutorials/jte)
        ctx.render("index.ftlh");
    }
    
    public static void post(Context ctx){
        // Get username and password from form data (<form action="/" method="POST"> in index.jte)
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");
        
        // Check whether the username and password entered match the admin credentials
        if(Server.validAdmin(username, password)){
            // Add username to persistent session for validation on other pages
            ctx.sessionAttribute("username", username);
            // Redirect to membership reports page
            ctx.redirect("/reports/membership");
        }else{
            // If the credentials are incorrect, go back to the login page
            ctx.redirect("/");
        }
    }
    
    public static void endSession(Context ctx){        
        ctx.req.getSession().invalidate();
        
        ctx.redirect("/");
    }
    
}

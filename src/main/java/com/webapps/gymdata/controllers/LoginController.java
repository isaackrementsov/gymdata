package com.webapps.gymdata.controllers;

import com.webapps.gymdata.Server;
import io.javalin.http.Context;

// Login system for securing the application
public class LoginController {
    
    public static void get(Context ctx){
        ctx.render("index.jte");
    }
    
    public static void post(Context ctx){
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");
        
        if(Server.validAdmin(username, password)){
            ctx.sessionAttribute("username", username);
            ctx.redirect("/dashboard");
        }else{
            ctx.redirect("/");
        }
    }
    
}

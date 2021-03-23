// Serve index page
package com.webapps.gymdata.controllers;

import io.javalin.http.Context;

public class HomeController {
    
    public static void get(Context ctx){
        ctx.render("index.ftl");
    }
    
}

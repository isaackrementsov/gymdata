package com.webapps.gymdata.controllers;

import io.javalin.http.Context;

public class ReportsController {

    public static void getMembership(Context ctx){
        ctx.render("members.jte");        
    }
    
    public static void getStaff(Context ctx){
        ctx.render("staff.jte");        
    }
    
}

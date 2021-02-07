package com.webapps.gymdata;

import com.webapps.gymdata.controllers.LoginController;
import com.webapps.gymdata.controllers.ReportsController;

// Uses lightweight Javalin framework - see https://javalin.io/documentation
import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import io.javalin.core.security.Role;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;

import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Server {
    
    // Host/IP for the web app to run on (127.0.0.1 = localhost)
    static String IP;
    // Port to run on
    static Integer PORT;
    // Directory where views are stored
    static String STATIC_DIRECTORY;
    
    // Predefined admin login credentials
    static String ADMIN_USERNAME;
    static String ADMIN_PASSWORD;
    
    enum UserRole implements Role {
        ADMIN, STD
    }
    
    public static void initialize() throws Exception {
        loadConfig("config.json");
        start();
    }
    
    public static void loadConfig(String path) throws Exception {
        JSONParser parser = new JSONParser();
        FileReader reader = new FileReader(path);
        JSONObject config = (JSONObject) parser.parse(reader);
        
        JSONObject server = (JSONObject) config.get("server");
        IP = (String) server.get("ip");
        PORT = ((Long) server.get("port")).intValue();
        STATIC_DIRECTORY = Paths.get("public").toAbsolutePath().toString();
        
        JSONObject admin = (JSONObject) config.get("admin");
        ADMIN_USERNAME = (String) admin.get("username");
        ADMIN_PASSWORD = (String) admin.get("password");
    }
    
    public static void start(){
        Javalin app = Javalin.create(config -> {
            config.addStaticFiles(STATIC_DIRECTORY, Location.EXTERNAL);
            
            config.accessManager((handler, ctx, permittedRoles) -> {
                UserRole role = Server.getRole(ctx);
                
                if(permittedRoles.contains(role)){
                    handler.handle(ctx);
                }else{
                    ctx.redirect("/");
                }
            });
        }).start(IP, PORT);
        
        app.routes(() -> {
            Set<Role> anyone = roles(UserRole.STD, UserRole.ADMIN);
            Set<Role> adminOnly = roles(UserRole.ADMIN);
            
            path("/", () -> {               
               get(LoginController::get, anyone);
               post(LoginController::post, anyone);
            });
            
            path("/reports", () -> {               
               get("activity", ReportsController::getActivity, adminOnly);
               get("demographics", ReportsController::getDemographics, adminOnly);
               get("membership", ReportsController::getMembership, adminOnly);
               get("staff", ReportsController::getStaff, adminOnly);
            });
        });
        
        System.out.println("Server running at http://" + IP + ":" + PORT);
    }
    
    public static boolean validAdmin(String username, String password){
        return (username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD));
    }    
    
    public static UserRole getRole(Context ctx){
        if(ctx.sessionAttribute("username") == null){
            return UserRole.STD;
        }else{
            return UserRole.ADMIN;
        }
    }
}

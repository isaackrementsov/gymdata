package com.webapps.gymdata;

// External controllers for endpoint handling
import com.webapps.gymdata.controllers.LoginController;
import com.webapps.gymdata.controllers.ReportsController;

// This app uses the lightweight Javalin framework - see https://javalin.io/documentation
import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.core.security.Role;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;

import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

// This class sets up the server for this web app to run on
public class Server {
    
    // Host/IP for the web app to run on (127.0.0.1 = localhost) [set in config.json]
    static String IP;
    // Port to run on [set in config.json]
    static Integer PORT;
    // Directory where views are stored [public folder in root project directory]
    static String STATIC_DIRECTORY;
    
    // Predefined credentials to login and see reports [set in config.json]
    static String ADMIN_USERNAME;
    static String ADMIN_PASSWORD;
    
    // Roles to determine whether a user has logged in and can access reports
    // UserRole.ADMIN = logged in, UserRole.STD = not logged in
    enum UserRole implements Role {
        ADMIN, STD
    }
    
    // Start the server
    public static void initialize() throws Exception {
        // Load environment parameters from the config.json file (see README for more details)
        loadConfig("config.json");
        // Configure routes and tell the server to listen on a designated port
        start();
    }
    
    // Load parameters from JSON file located at path argument
    public static void loadConfig(String path) throws Exception {
        // Read the file and parse it as a JSON object
        JSONParser parser = new JSONParser();
        FileReader reader = new FileReader(path);
        JSONObject config = (JSONObject) parser.parse(reader);
        
        // Get server configuration parameters
        JSONObject server = (JSONObject) config.get("server");
        // Set the IP address and network port for the server to run on
        IP = (String) server.get("ip");
        PORT = ((Long) server.get("port")).intValue();
        // Get the static directory's ("public" folder in root project directory) absolute path
        STATIC_DIRECTORY = Paths.get("public").toAbsolutePath().toString();
        
        // Get admin login values
        JSONObject admin = (JSONObject) config.get("admin");
        // Set the username and password needed to access reports
        ADMIN_USERNAME = (String) admin.get("username");
        ADMIN_PASSWORD = (String) admin.get("password");
    }
    
    public static void start(){
        // Create a new Javalin application
        Javalin app = Javalin.create(config -> {
            // Configure the app to serve static files from the public directory
            // The Location.EXTERNAL argument specifies that the location is absolute and not in the classpath
            config.addStaticFiles(STATIC_DIRECTORY, Location.EXTERNAL);
            
            // This manager runs before each request to make sure that only specified user groups can access certain URLs
            config.accessManager((handler, ctx, permittedRoles) -> {
                // Uses this class's getRole method to classify the user based on request context
                UserRole role = Server.getRole(ctx);
                
                // Check whether this role is in the list of roles permitted for each route
                if(permittedRoles.contains(role)){
                    // If the role is permitted, continue to the handler method
                    handler.handle(ctx);
                }else{
                    // If the role is not permitted, redirect to a different URL
                    if(role.equals(UserRole.ADMIN)){
                        // A user that is logged in (ADMIN) will be sent back to the membership reports
                        ctx.redirect("/reports/membership");
                    }else{
                        // A user that is not logged in (STD) will be sent to the login page
                        ctx.redirect("/");                        
                    }
                }
            });
        // Start the server on the IP/PORT from config.json
        }).start(IP, PORT);
        
        // Define handlers for specified URLs
        app.routes(() -> {
            // Users that aren't logged in
            Set<Role> anyone = roles(UserRole.STD);
            // Users that are logged in
            Set<Role> adminOnly = roles(UserRole.ADMIN);
            
            // Handle GET/POST requests to '/' with the LoginController
            // These routes are available to users that have not yet logged in
            path("/", () -> {               
               get(LoginController::get, anyone);
               post(LoginController::post, anyone);
               
               get("/logout", LoginController::endSession, adminOnly);
            });
            
            // Handle GET requests to '/reports/xxxx' with the ReportsController 
            // These routes are available only to logged in/admin users
            path("/reports", () -> {
               get("membership", ReportsController::getMembership, adminOnly);
               get("staff", ReportsController::getStaff, adminOnly);
            });
        });
        
        System.out.println("Server running at http://" + IP + ":" + PORT);
    }
    
    // Get a user's role based on request context
    public static UserRole getRole(Context ctx){
        // Check whether a username is stored in the user's session
        if(ADMIN_USERNAME.equals(ctx.sessionAttribute("username"))){
            // If the value matches the admin username, the user has logged in
            return UserRole.ADMIN;
        }else{
            // If the value is null or does not match, they have not logged in (properly)
            return UserRole.STD;
        }
    }
    
    // Check whether a given username and password match the admin credentials stored in this class
    public static boolean validAdmin(String username, String password){
        return (username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD));
    }    
}

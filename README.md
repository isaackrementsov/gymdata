# GymData
Web application to display gym usage data, written using the lightweight [Javalin framework](https://javalin.io/documentation)
## Requirements
- MySQL Database
- JDK 8+
## Setup
Environment variables for GymData should be configured by adding a `config.json` file to the root project directory. 
It should look something like this:
```javascript
{
    "server": {
        "ip": "127.0.0.1",
        "port": 8080
    },
    "admin": {
        "username": "myusername",
        "password": "password123"
    }
}
```
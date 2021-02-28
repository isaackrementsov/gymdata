# GymData
Web application to display gym usage data, written using the lightweight [Javalin framework](https://javalin.io/documentation)
## Requirements
- MySQL Database
- MySQL Connector/J (JAR must be added as a dependency)
- JDK 8+ (development and testing was done on JDK 11, so 10+ is preferable)
## Setup
When discussing setup details for GymData, `./` will reference the root project directory (for example, this file is located at `./README.md`)

### Database
This project uses JPA and Hibernate to translate SQL tables and rows to Java classes and objects. 
To use JPA, you must add configuration information to `./src/main/resources/META-INF/persistence.xml`. 
A `persistence.xml` file should follow the format below, with your database credentials in place of `root` and `myPasswd`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.0"
    xmlns="http://java.sun.com/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java,sun.com/xml/ns/persistence/peristence_2_0.xsd">
    
    <persistence-unit name="gymdata" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
        
        <class>com.webapps.gymdata.models.Member</class>
        <class>com.webapps.gymdata.models.Scan</class>
        <class>com.webapps.gymdata.models.Employee</class>
        <properties>
            <property name="javax.persistence.jdbc.driver"
            value="com.mysql.cj.jdbc.Driver"/>
            <property name="javax.persistence.jdbc.url"
            value="jdbc:mysql://localhost:3306/gymdata"/>
            <property name="javax.persistence.jdbc.user"
            value="root"/>
            <property name="javax.persistence.jdbc.password"
            value="myPasswd"/>
        </properties>
    </persistence-unit>
</persistence>
```
Make sure not to change the entities listed in the `<class>...</class>`.<br/>
So that JDBC can connect to MySQL, add the following dependency to `./pom.xml` (if it doesn't already exist):
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector</artifactId>
    <version>1.0</version>
</dependency>
```
Then, run a build and check the list under `Project -> Dependencies` for the newly added artifact. 
Right click the entry and select the `Manually install artifact` option that shows up. When prompted for a JAR, 
find and select the MySQL Connector/J file (if you don't already have the file, it can be downloaded [here](https://dev.mysql.com/downloads/connector/j/)). 
After you run another build, the `com.mysql` package should load properly.<br/>
To set up a database for this project, create and enter a MySQL database (make sure it matches the `javax.persistence.jdbc.url` in `persistence.xml`) 
before running the statements in `./sql/migrations.sql`.<br/>
From there, `createData` and `createStaffData` can be called in the `com.webapps.gymdata.Main.main` 
method to randomly generate sample data. 

### App configuration
Static information for the GymData app should be configured by adding a `./config.json` file. 
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
### Views and Static Files
Static files for this project are served from the `./public` directory and can be accessed through GET requests to `/path/to/file/under/public`. 
For example, `./public/css/main.css` is accessible at the URL `/css/main.css`.<br/>
Views are written for the [JTE templating engine](https://github.com/casid/jte/blob/master/DOCUMENTATION.md) under the `./src/main/jte` directory.
JTE syntax resembles that of Java, with some modifications to account for its embedded structure.
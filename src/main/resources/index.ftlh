<%-- index.jte provides a simple login system for the admin to access reports --%>

<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8"/> 
        <title>GymData Login</title>
        <link rel="stylesheet" type="text/css" href="/css/main.css"/>
    </head>
    <body>
        <div class="content">
            <div class="central-panel">
                <h1>Login</h1>
                <form action="/" method="POST">
                    <input type="text" name="username" placeholder="Username" required/>
                    <input type="password" name="password" placeholder="Password" required/>
                    <input type="submit" value="Continue"/>
                </form>
            </div>
        </div>
    </body>
</html>
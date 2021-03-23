<#-- staff.jte provides and overview of employee data and overall labor costs -->
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8"/> 
        <title>Membership Report</title>
        <script src="https://code.jquery.com/jquery-3.5.1.min.js" integrity="sha256-9/aliU8dGd2tb6OSsuzixeV4y/faTqgFtohetphbbj0="crossorigin="anonymous"></script>
        <script src="/js/chart.min.js"></script>
        <link rel="stylesheet" type="text/css" href="/css/chart.min.css"/>
        <link rel="stylesheet" type="text/css" href="/css/main.css"/>
    </head>
    <body>
        <div class="content flex-container">
            <ul class="side-nav">
                <a href="#"><h1>View Reports</h1></a>
                <a class="link" href="/reports/membership">Membership</a>
                <a class="active link" href="/reports/staff">Staff</a>
                <a class="link" href="/login/logout">Logout</a>
            </ul>
            <div class="graphs">
                <h1>Staff Report</h1>
                <div class="flex-container">
                    <div class="chart-container big-data">
                        <h1>&euro;${totalCost}</h1>
                        <h2>Weekly Labor Cost</h2>
                    </div>
                    <div class="chart-container" id="hour-groups">
                        <#-- Loop through the hourGroups in order of increasing hours-->
                        <#list hourGroupKeys as hour>
                            <#assign hourGroup = hourGroups["hour_" + hour]>
                            <#-- Show a report of this group's statistics -->
                            <div class="hour-group">
                                <h3>${hourGroup.numberOfEmployees} Employees working ${hour} hours/week</h3>
                                <p><span>&euro;${hourGroup.minWage}-${hourGroup.maxWage}/hour</span><span>&euro;${hourGroup.totalCost} weekly cost</span></p>
                            </div>
                        </#list>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>

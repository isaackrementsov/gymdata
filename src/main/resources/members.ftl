<#-- members.jte represents membership data across time periods and demographic groups -->

<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8"/> 
        <title>Membership Report</title>
        <script src="https://code.jquery.com/jquery-3.5.1.min.js" integrity="sha256-9/aliU8dGd2tb6OSsuzixeV4y/faTqgFtohetphbbj0=" crossorigin="anonymous"></script>
        <script src="/js/chart.min.js"></script>
        <link rel="stylesheet" type="text/css" href="/css/chart.min.css"/>
        <link rel="stylesheet" type="text/css" href="/css/main.css"/>
    </head> 
    <body>
        <div class="content flex-container">
            <ul class="side-nav">
                <a href="#"><h1>View Reports</h1></a>
                <a class="active link" href="/reports/membership">Membership</a>
                <a class="link" href="/reports/staff">Staff</a>
                <a class="link" href="/logout">Logout</a>
            </ul>
            <div class="graphs">
                <h1>Membership Report</h1>
                <div class="flex-container">
                    <div class="chart-container big-data">
                        <h1>${members?size}</h1>
                        <h2>Total Members</h2>
                    </div>
                    <div class="chart-container" style="text-align: center">
                        <h2 style="margin-top: 0">Recent Activity</h2>
                        <div id="recent-activity" class="activity-container">
                            <#list scans[0..10] as scan>
                                <p>${scan.member.name} scanned ${scan.scanIn?then("in", "out")} at ${scan.getPrettyDate()}</p>
                            </#list>
                        </div>
                    </div>
                    <div id="main-chart-container" class="chart-container">
                        <p id="main-select">
                            See data from the last 
                            <input type="number" value="1" min="1" id="main-chart-number"/>
                            <select id="main-chart-unit">
                                <option value="week">Week</option>
                                <option value="month">Month</option>                            
                                <option value="year">Year</option>
                            </select>
                        </p>
                        <canvas id="main-chart"></canvas>
                    </div>
                    <div id="details-container" class="chart-container" style="display: none">
                        <h2 id="date"></h2>
                        <p id="visitors"></p>
                        <canvas id="details"></canvas>
                        <button onclick="buildDayChart()" class="bottom-button" id="day-button">Detailed View</button>
                    </div>
                </div>
            </div>
        </div>
        <div style="display: none" id="timestamps-json">
            <#-- Serialize timestamp information as a JSON array-->
            <#-- This allows the data to be used in JavaScript -->
            [
            <#assign last = timestamps?size - 1>
            <#list 0..last as i>
                <#assign timestamp = timestamps[i]>
                {
                    "start": "${timestamp.start?long}", 
                    "end": "${(timestamp.end!timestamp.start)?long}", 
                    "gender": "${timestamp.member.gender}"
                }
                <#-- Do not put a comma after the last object (otherwise JSON.parse will fail) -->
                <#if i != last> 
                    ,
                </#if>
            </#list>
            ]
        </div>
        <script src="/js/members.js"></script>
    </body>
</html>
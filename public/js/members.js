/*
    This file handles charts and interactivity for the membership reporting page (members.jte)
    The ChartJS library loaded in the ./chart.min.js file and referenced with the "Chart" class
*/

// Set global chart font color to light grey
Chart.defaults.global.defaultFontColor = "whitesmoke";

// Parse Member.Timestamp data from the backend as JSON (this is loaded into div#timestamps-json in members.jte)
let timestamps = $('#timestamps-json').text();
timestamps = JSON.parse(timestamps);

// Get the number of time units to go back by when displaying data (ex. *3* weeks)
let number = $('#main-chart-number').val();
// Get the time unit to go back by when displaying data (ex. 3 *week*s)
let unit = $('#main-chart-unit').val();
// This will store the date selected on the chart
let focusedDate;

// Define constants for translating between different time units and milliseconds
const msPerDay = 8.64e7;

const goBackUnits = {
    'week': 7*msPerDay,
    'month': 30*msPerDay,
    'year': 365*msPerDay
}

// This will store the origin map of data used by the chart (before it is simplified to bar chart format)
let originalData = {};

// Setting to disable chart animation
const noAnimation = {duration: 0};
// Default bar chart color
const barColor = 'rgb(255,196,0,0.7)';
// Color to highlight a bar that has been selected
const activeColor =  '#FFD740';

// Configuration for the main bar chart (total visitors over a long period of time)
// See ChartJS docs for more details (https://www.chartjs.org/docs/latest/charts/bar.html)
const chartConfig = {
    type: 'bar',
    name: 'main',
    data: {
        labels: [],
        datasets: [{
            backgroundColor: [],
            label: 'Visitors',
            data: []
        }]
    },
    options: {
        legend: {
            display: false
        },
        scales: {
            xAxes: [{
                display: true,
                stacked: true,
                gridLines: {
                    display: true,
                    zeroLineColor: "#a6a6a6",
                    color: 'transparent'
                },
                ticks: {
                    autoSkip: true,
                    maxTicksLimit: 12
                }
            }],
            yAxes: [{
                display: true,
                stacked: true,
                gridLines: {
                    display: true,
                    zeroLineColor: "#a6a6a6",
                    color: 'transparent'
                },
                scaleLabel: {
                    display: true,
                    labelString: 'Visitors'
                },
                ticks: {
                    autoSkip: true,
                    maxTicksLimit: 6
                }
            }]
      }
  }
}

// Custom ChartJS plugin to update background colors in an array format
const colorArray = {
    // Set colors before updating or building a chart
    beforeUpdate: function(chart) {
        // Only do this for the main bar chart
        if(chart.config.name == 'main'){
            // Force the chart background color to match array added to configuration
            chart.config.data.datasets[0].backgroundColor = chart.config.options.backgroundColorArray;            
        }
    }
};
// Register the custom plugin globally
Chart.pluginService.register(colorArray);

// Build the main bar chart
function buildChart(goBackNumber, goBackUnit, config, animate){
    // Remove and re-insert the main canvas to reset the chart
    $('#main-chart').remove();
    $('#main-chart-container').append('<canvas id="main-chart" height="400" width="800"></canvas>');
    
    // This will store a map of data keyed by the day it is assigned to
    let byDate = {};

    // Get the current date in ms
    const now = (new Date()).valueOf();
    // Go back by the user-defined number of time units, converted to ms
    const goBackBy = goBackNumber*goBackUnits[goBackUnit];
    // Calculate the starting date of the selected range
    const startDate = new Date(now - goBackBy);
    // Set a date variable that will range from startDate => now
    let currentDate = startDate;
    
    // Keep generating data while the currentDate is before now
    while(currentDate.valueOf() <= now){
        // Create an entry in the byDate map for this particular day
        byDate["Day_" + totalDays(currentDate)] = {'male': 0, 'female': 0, date: currentDate.toString()};
        
        // Increment the currentDate by 1 day for each iteration
        currentDate = new Date(msPerDay + currentDate.valueOf());
    }
    
    // Loop through the Member.Timestamp data
    for(timestamp of timestamps){
        // Parse the start date in a JavaScript-usable format
        let start = new Date(parseInt(timestamp.start));
        // Conver this date to a total number of days
        let startDay = totalDays(start);
        
        // Only add this to the byDate map if it is after the selected startDate
        if(start.valueOf() > startDate.valueOf()){
            // Increment the number of visitors of this member's gender for the day their visit started
            byDate["Day_" + startDay][timestamp.gender]++;
        }
    }
    
    // Save the raw data to a global variable for later use
    originalData = byDate;
    // Convert the map to an array of bar data representing total visitors per day 
    const data = getBarData(byDate);
    
    // Add data to the chart config
    config.data.datasets[0].data = data;
    // Label the y-axis as "Visitors"
    config.options.scales.yAxes[0].scaleLabel.labelString = 'Visitors';
    
    // Replace the background color array with the default bar colors if it doesn't exist or doesn't match the data length
    if(!config.options.backgroundColorArray || config.options.backgroundColorArray.length != data.length){
        config.options.backgroundColorArray = fillArray(barColor, data.length);        
    }
    
    // Add labels for each day (labels will be skipped if the x-axis gets too crowded)
    config.data.labels = Object.keys(byDate).map(d => {
        // Convert the Day_#ofyears format to a date
        let date = new Date(parseInt(msPerDay*d.split('_')[1]));
        // Generate a date string and remove the year at the end
        let day = date.toDateString().split(' ').slice(1,3).join(' ');

        return day;
    });
    
    // If this chart should be animated, delete the noAnimation option (if it exists)
    if(animate){
        delete chartConfig.options.animation;
    }else{
        // If the chart should not be animated, set the noAnimation option
        chartConfig.options.animation = noAnimation;
    }
    
    // Create a new bar chart in the main canvas
    let barChart = new Chart($('#main-chart'), config);
    
    // Respond when the canvas is clicked
    $('#main-chart').on('click', function(event){
        // Get the elements on the canvas that were clicked
        let elements = barChart.getElementsAtEvent(event);
        
        if(elements.length > 0){
            // Get the index of the first element (if a bar was clicked, this will be the index of the bar in the data array)
            let idx = barChart.getElementsAtEvent(event)[0]['_index'];
            
            // Reset the background color of each bar (in case one was already clicked)
            chartConfig.options.backgroundColorArray = fillArray(barColor, chartConfig.data.datasets[0].data.length);
            // Highlight the bar that was clicked
            chartConfig.options.backgroundColorArray[idx] = activeColor;   
            // Rebuild the chart without an animation
            buildChart(number, unit, config, false);
            
            // Get the key in the raw data map that matches this bar
            let key = Object.keys(originalData)[idx];
            // Get the original data for this bar using the key
            let original = originalData[key];
            // Convert Day_#ofdays to a date and set this as the focused date
            let days = parseInt(key.replace('Day_', ''));
            focusedDate = new Date(days*msPerDay);
            // Set the clock in this date to 0 (otherwise it will be the current time)
            focusedDate.setHours(0);
            
            // If the details side-popup is hidden, show it
            if(!$('#details-container').is(':visible')){
                $('#details-container').show(400);
            }
            
            // Set the date header text (in #details-container) to the focused date string
            $('#date').text(focusedDate.toDateString());
            // Display the total number of visitors for this day
            $('#visitors').text(original.male + original.female + ' visitors');
            
            // Build the gender pie chart for this day
            buildPieChart(original.male, original.female);
            
        }
    });
}

// Build a chart showing visitors at each hour of the focused day (detailed view)
function buildDayChart(){
    // Change the button at the bottom of #details-container so that it will close the detailed view
    $('#day-button').addClass("inactive");
    $('#day-button').text('Close Detailed View');
    $('#day-button').removeAttr('onclick');
    $('#day-button').attr('onclick', 'closeDayChart()');
    
    // Hide/destroy the main bar chart and goBackBy inputs
    
    $('#main-select').hide();
    
    $('#main-chart').remove();
    // Insert a new canvas to build this chart
    $('#main-chart-container').append('<canvas id="main-chart" height="400" width="800"></canvas>');
    
    // Data map to be arranged similarly to byDate, except statistics are collected per hour
    let byHour = {};
    // Duplicate the focused date for modification while generating data
    let currentDate = new Date(focusedDate.getTime());
    
    // Create new entries in byHour for every hour of the day (in [0,24) )
    for(let i = 0; i < 24; i++){
        // Set the currentDate's clock to the current hour
        currentDate.setHours(i);
        // Create a new entry for this hour
        byHour["Time_" + i] = {'male': 0, 'female': 0, date: currentDate.toString()};
    }

    // Collect data from Member.Timestamps for the focusedDate
    for(timestamp of timestamps){
        // Parse the start and end scan times as JS Dates
        let start = new Date(parseInt(timestamp.start));
        let end = new Date(parseInt(timestamp.end));
        
        // Check whether the timestamp's start matches the focused date (date strings do not include time)
        if(start.toDateString() == focusedDate.toDateString()){
            // Get the scan-in and scan-out hours
            let startHours = start.getHours();
            let endHours = end.getHours();

            // Increment data for each hour between the scan-in and scan-out times
            for(let i = startHours; i <= endHours; i++){
                // Increment this hour's visitors for this member's gender
                byHour['Time_' + i][timestamp.gender]++;
            }                    
        }
    }
    
    // Get separate bar data for each gender
    const maleData = getGenderData(byHour, 'male');
    const femaleData = getGenderData(byHour, 'female');
    
    // Configure scales and set the y-axis label to "Members present"
    const scales = chartConfig.options.scales;
    scales.yAxes[0].scaleLabel.labelString = 'Members present';
    
    // Initialize a new bar chart with male and female datasets
    new Chart($('#main-chart'), {
        type: 'bar',
        data: {
            labels: Object.keys(byHour).map(d => {
                // Parse each time in the dataset in a "pretty" 12-hour format
                let time = parseInt(d.replace('Time_', '')) + 1;
                let end = 'am';
                
                if(time > 12){
                    time -= 12;
                    end = 'pm';
                }
                
                return time + ':00' + end;
            }),
            datasets: [{
                data: maleData,
                backgroundColor: 'rgba(255,215,64,0.5)',
                borderColor: 'rgb(255,215,64)',
                borderWidth: 0,
                barPercentage: 1.25,
                label: 'Male'
            }, {
                data: femaleData,
                backgroundColor: 'rgba(209, 196, 233, 0.5)',
                borderColor: 'rgb(209, 196, 233)',
                borderWidth: 0,
                barPercentage: 1.25,
                label: 'Female'
            }]
        },
        options: {
            legend: {
                display: true
            },
            scales: chartConfig.options.scales
        }
    });
}

// Close the detailed view
function closeDayChart(){
    // Set the #details-container button back to showing the detailed view
    $('#day-button').removeClass("inactive");
    $('#day-button').text('Detailed View');
    $('#day-button').removeAttr('onclick');
    $('#day-button').attr('onclick', 'buildDayChart()');
    
    // Show the goBackBy inputs again
    $('#main-select').show();

    // Rebuild the main chart
    buildChart(number, unit, chartConfig, true);
}

// Build a pie chart for showing each day's gender ratio
function buildPieChart(male, female){
    // Reset the details canvas
    $('#details').remove();
    // Wait for the canvas to actually disappear before recreating the chart (otherwise ChartJS will override the canvas size defined in main.css)
    $('#details-container').append('<canvas id="details"></canvas>').ready(function(){
        // Create a new chart in the details container
        let ctx = $('#details');
        
        let pieChart = new Chart(ctx, {
            type: 'pie',
            data: {
                labels: ['Male', 'Female'],
                datasets: [{
                    // The actual male vs female data is passed to this function when a date is focused (see the #main-chart listener in buildChart)
                    data: [male, female],
                    // Set different border and background colors for both pieces of the pie chart
                    backgroundColor: ['rgba(255,215,64,0.5)', 'rgba(209, 196, 233, 0.5)'],
                    borderColor: ['rgb(255,215,64)', 'rgb(209, 196, 233)']
                }]
            }
        });  
    });
}

// Get the total number of days associated with a date (days since the Unix epoch)
function totalDays(date){
    // Divide the ms value of the date by the number of ms in a day and round
    return Math.round(date.valueOf()/msPerDay);
}

// Convert raw data map to bar chart format
function getBarData(datapoints){
    // Bar data is stored as a simple list
    let data = [];
    
    // Get data for each key in the data map
    for(key of Object.keys(datapoints)){
        let record = datapoints[key]; 
        // Add the total number of visitors to the data array
        data.push(record.male + record.female);
    }
    
    return data;
}

// Get bar data for a particular gender
function getGenderData(datapoints, gender){
    let data = [];
    
    // Do the same thing as getBarData but only use data for the selected gender
    for(key of Object.keys(datapoints)){
        let record = datapoints[key]; 
        data.push(record[gender]);
    }
    
    return data;
}

// Fill an array with n of the same element (equivalent to [value]*len in Python)
function fillArray(value, len) {
    var arr = [];
    for (var i = 0; i < len; i++) {
        arr.push(value);
    }
    return arr;
}

// Listen for changes to the go back by number input
$('#main-chart-number').on('input', function(event){
    // Get the integer value in the input and set to the global number variable
    number = parseInt($(event.target).val());
    
    // If the number is greater than one, make the units plural
    if(number > 1){
        $('#main-chart-unit option').each(function(){
            let current = $(this).text();
            // Check whether the unit is not plural yet
            if(current[current.length - 1] != 's'){
                $(this).text($(this).text() + 's');                
            }
        });
    // If the number is equal to one, remove the plural from the units
    }else{
        $('#main-chart-unit option').each(function(){
            let current = $(this).text();
            // Check whether the unit is already plural
            if(current[current.length - 1] == 's'){
                $(this).text($(this).text().slice(0, current.length - 1));                
            }
        });
    }
    
    // Rebuild the chart and go back by the updated number of time units
    buildChart(number, unit, chartConfig, true);
});

// Listen for changes to the go back by unit select
$('#main-chart-unit').on('input', function(event){
    // Set the global unit variable to the select's new value
    unit = $(event.target).val();
    // Build the chart and go back by the updated time unit
    buildChart(number, unit, chartConfig, true);
    
});

// Build the main chart when the members.jte page first loads
buildChart(number, unit, chartConfig, true);

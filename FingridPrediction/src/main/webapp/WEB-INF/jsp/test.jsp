<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="//code.jquery.com/jquery-1.12.0.min.js"></script>
        <script src="https://code.highcharts.com/highcharts.js"></script>
        <title>Electricity consumption forecast</title>
    </head>
    <body>
        <div id="container" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
        <div id="container2" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
        <script>
            $(function () {
               // $.getJSON('https://www.highcharts.com/samples/data/jsonp.php?filename=usdeur.json&callback=?', function (data) {
               $.getJSON('forecast_electricity', function (data) {
                    $('#container').highcharts({
                        chart: {
                            zoomType: 'x',
                            marginLeft: 70,
                            events: {
                                load: function () {

                                    // set up the updating of the chart each second
                                    var axis = this.xAxis[0];
                                    setInterval(function () {
                                        axis.removePlotLine('current-time');
                                        var plotLine = {
                                            value: new Date().getTime(),
                                            color: 'green',
                                            id: 'current-time',
                                            dashStyle: 'shortdash',
                                            width: 2,
                                            label: {
                                                text: 'Current time'
                                            }
                                        }
                                        axis.addPlotLine(plotLine);
                                    }, 10000);
                                }
                            }
                        },
                        title: {
                            text: '36 hour electricity consumption forecast for Finland'
                        },
                        xAxis: {
                            title: {
                                text: 'Time (UTC)'
                            },
                            plotLines: [{
                                value: new Date().getTime(),
                                color: 'green',
                                id: 'current-time',
                                dashStyle: 'shortdash',
                                width: 2,
                                label: {
                                    text: 'Current time'
                                }
                            }],
                            type: 'datetime'
                        },
                        yAxis: {
                            title: {
                                text: 'Electricity consumption (MWh/h)'
                            },
                            labels: {
                                formatter: function () {
                                    return this.value;
                                }
                            }
                        },
                        legend: {
                            enabled: false
                        },
                        plotOptions: {
                            area: {
                                fillColor: {
                                    linearGradient: {
                                        x1: 0,
                                        y1: 0,
                                        x2: 0,
                                        y2: 1
                                    },
                                    stops: [
                                        [0, Highcharts.getOptions().colors[0]],
                                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                                    ]
                                },
                                marker: {
                                    radius: 2
                                },
                                lineWidth: 1,
                                states: {
                                    hover: {
                                        lineWidth: 1
                                    }
                                },
                                threshold: null
                            }
                        },

                        series: [{
                            type: 'area',
                            name: 'Electricity consumption',
                            data: data
                        }]
                    });
                });
            });
            
            $(function () {
               // $.getJSON('https://www.highcharts.com/samples/data/jsonp.php?filename=usdeur.json&callback=?', function (data) {
               $.getJSON('weather_forecast', function (data) {
                    $('#container2').highcharts({
                        chart: {
                            zoomType: 'x',
                            marginLeft: 70,
                            events: {
                                load: function () {

                                    // set up the updating of the chart each second
                                    var axis = this.xAxis[0];
                                    setInterval(function () {
                                        axis.removePlotLine('current-time');
                                        var plotLine = {
                                            value: new Date().getTime(),
                                            color: 'green',
                                            id: 'current-time',
                                            dashStyle: 'shortdash',
                                            width: 2,
                                            label: {
                                                text: 'Current time'
                                            }
                                        }
                                        axis.addPlotLine(plotLine);
                                    }, 10000);
                                }
                            }
                        },
                        title: {
                            text: '36 hour air temperature forecast'
                        },
                        subtitle: {
                            text: 'Source: Finnish Meteorological Institute'
                        },
                        xAxis: {
                            title: {
                                text: 'Time (UTC)'
                            },
                            plotLines: [{
                                value: new Date().getTime(),
                                color: 'green',
                                id: 'current-time',
                                dashStyle: 'shortdash',
                                width: 2,
                                label: {
                                    text: 'Current time'
                                }
                            }],
                            type: 'datetime'
                        },
                        yAxis: {
                            title: {
                                text: 'Temperature (°C)'
                            },
                            
                        },
                        tooltip: {
                            valueSuffix: '°C'
                        },
                        legend: {
                            layout: 'vertical',
                            align: 'right',
                            floating: true,
                            verticalAlign: 'middle',
                            borderWidth: 0
                        },
                        

                        series: [{
                            type: 'line',
                            name: 'Helsinki kaisaniemi',
                            data: data[0]
                        },
                        {
                            type: 'line',
                            name: 'Kuopio ritoniemi',
                            data: data[1]
                        },
                        {
                            type: 'line',
                            name: 'Rovaniemi railway station',
                            data: data[2]
                        }]
                    });
                });
            });
        </script>
    </body>
</html>

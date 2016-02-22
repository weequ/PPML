<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="//code.jquery.com/jquery-1.12.0.min.js"></script>
        <script src="https://code.highcharts.com/highcharts.js"></script>
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Hello Worldddddddd!</h1>
        ${asd}
        <div id="container" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
        <script>
            $(function () {
                $.getJSON('https://www.highcharts.com/samples/data/jsonp.php?filename=usdeur.json&callback=?', function (data) {

                    $('#container').highcharts({
                        chart: {
                            zoomType: 'x'
                        },
                        title: {
                            text: 'Predicted energy consumption over time'
                        },
                        xAxis: {
                            type: 'datetime'
                        },
                        yAxis: {
                            title: {
                                text: 'Energy consumption (MWh/h)'
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
                            name: 'USD to EUR',
                            data: data
                        }]
                    });
                });
            });
        </script>
    </body>
</html>

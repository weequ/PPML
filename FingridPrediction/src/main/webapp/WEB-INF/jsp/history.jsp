<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<c:import url="header.jsp"></c:import>
<div id="container" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
<div id="errorContainer" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
<script>
    $(function () {
        // $.getJSON('https://www.highcharts.com/samples/data/jsonp.php?filename=usdeur.json&callback=?', function (data) {
        $.getJSON('historyCompareData', function (data) {
             $('#container').highcharts({
                 chart: {
                     zoomType: 'x',
                     marginLeft: 70,
                     events: {
                         load: function () {
                             return;
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
                     text: '24 hour forecast history'
                 },
                 xAxis: {
                     title: {
                         text: 'Time (UTC)'
                     },
                     
                     type: 'datetime'
                 },
                 yAxis: {
                     title: {
                         text: 'Electricity consumption (MWh/h)'
                     },
                     labels: {
                        formatter: function () {
                            return Highcharts.numberFormat(this.value,0);//this.value;
                        }
                    }

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
                     name: '24 hour electricity consumption forecast',
                     data: data[0]
                 },
                 {
                     type: 'line',
                     name: 'actual electricity consumption',
                     data: data[1]
                 }]
             });
         });
     });

 </script>
    
<c:import url="footer.jsp"></c:import>

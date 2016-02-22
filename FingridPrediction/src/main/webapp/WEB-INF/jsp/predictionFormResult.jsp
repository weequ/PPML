<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<c:import url="header.jsp"></c:import>
    Input: <br>
    helsinki:<c:out value="${predictionForm.helsinkiTemp}"/><br>
    kuopio:<c:out value="${predictionForm.kuopioTemp}"/><br>
    rovaniemi:<c:out value="${predictionForm.rovaniemiTemp}"/><br>
    dayOfWeek:<c:out value="${predictionForm.dayOfWeek}"/><br>
    hourOfDay:<c:out value="${predictionForm.hourOfDay}"/><br>
    Prediction result: <c:out value="${predictionResult}"/>
<c:import url="footer.jsp"></c:import>

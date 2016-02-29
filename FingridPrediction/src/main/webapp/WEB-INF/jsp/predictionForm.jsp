<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<c:import url="header.jsp"></c:import>
<c:url var="actionUrl" value="/custom"/>
<form:form action="${actionUrl}" commandName="predictionForm">
    <table>
        <tr>
            <td align="left" width="20%">helsinkiTemp (°C): </td>
            <td align="left" width="40%"><form:input path="helsinkiTemp" size="30" required="required"/></td>
            <td align="left"><form:errors path="helsinkiTemp" cssClass="error"/></td>
        </tr>
        <tr>
            <td align="left" width="20%">kuopioTemp (°C): </td>
            <td align="left" width="40%"><form:input path="kuopioTemp" size="30" required="required"/></td>
            <td align="left"><form:errors path="kuopioTemp" cssClass="error"/></td>
        </tr>
        <tr>
            <td align="left" width="20%">rovaniemiTemp (°C): </td>
            <td align="left" width="40%"><form:input path="rovaniemiTemp" size="30" required="required"/></td>
            <td align="left"><form:errors path="rovaniemiTemp" cssClass="error"/></td>
        </tr>
        <tr>
            <td align="left" width="20%">dayOfWeek: </td>
            <td align="left" width="40%">
                <form:select path="dayOfWeek">
                    <form:option value="1">Monday</form:option>
                    <form:option value="2">Tuesday</form:option>
                    <form:option value="3">Wednesday</form:option>
                    <form:option value="4">Thursday</form:option>
                    <form:option value="5">Friday</form:option>
                    <form:option value="6">Saturday</form:option>
                    <form:option value="7">Sunday</form:option>
                </form:select>
                
            </td>
            <td align="left"><form:errors path="dayOfWeek" cssClass="error"/></td>
        </tr>
        <tr>
            <td align="left" width="20%">hourOfDay: (0-23)</td>
            <td align="left" width="40%"><form:input type="number" step="1" min="0" max="23" path="hourOfDay" size="30"/></td>
            <td align="left"><form:errors path="hourOfDay" cssClass="error"/></td>
        </tr>
    </table>
    <input type="submit" value="Predict electricity consumption"/>
</form:form>
<c:import url="footer.jsp"></c:import>

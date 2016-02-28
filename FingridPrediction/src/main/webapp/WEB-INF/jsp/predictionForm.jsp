<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<c:import url="header.jsp"></c:import>
<c:url var="actionUrl" value="/custom"/>
<form:form action="${actionUrl}" commandName="predictionForm">
    <table>
        <tr>
            <td align="left" width="20%">helsinkiTemp: </td>
            <td align="left" width="40%"><form:input path="helsinkiTemp" size="30"/></td>
            <td align="left"><form:errors path="helsinkiTemp" cssClass="error"/></td>
        </tr>
        <tr>
            <td align="left" width="20%">kuopioTemp: </td>
            <td align="left" width="40%"><form:input path="kuopioTemp" size="30"/></td>
            <td align="left"><form:errors path="kuopioTemp" cssClass="error"/></td>
        </tr>
        <tr>
            <td align="left" width="20%">rovaniemiTemp: </td>
            <td align="left" width="40%"><form:input path="rovaniemiTemp" size="30"/></td>
            <td align="left"><form:errors path="rovaniemiTemp" cssClass="error"/></td>
        </tr>
        <tr>
            <td align="left" width="20%">dayOfWeek: </td>
            <td align="left" width="40%"><form:input path="dayOfWeek" size="30"/></td>
            <td align="left"><form:errors path="dayOfWeek" cssClass="error"/></td>
        </tr>
        <tr>
            <td align="left" width="20%">hourOfDay: </td>
            <td align="left" width="40%"><form:input path="hourOfDay" size="30"/></td>
            <td align="left"><form:errors path="hourOfDay" cssClass="error"/></td>
        </tr>
    </table>
    <input type="submit" value="Predict energy consumption"/>
</form:form>
<c:import url="footer.jsp"></c:import>

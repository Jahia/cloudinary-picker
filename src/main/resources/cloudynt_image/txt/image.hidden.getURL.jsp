<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>


<c:set var="baseUrl" value="${currentNode.properties['cloudy:baseUrl'].string}"/>
<c:set var="endUrl" value="${currentNode.properties['cloudy:endUrl'].string}"/>

<utility:logger level="DEBUG" value="***[cloudinaryImage] cloudinaryNode width: ${currentResource.moduleParams.width}"/>
<utility:logger level="DEBUG" value="***[cloudinaryImage] cloudinaryNode height: ${currentResource.moduleParams.height}"/>

<c:set var="h_" value="h_"/>
<c:set var="w_" value="w_"/>

<c:set var="height" value="${not empty currentResource.moduleParams.height ?
    'h_'.concat(currentResource.moduleParams.height) : ''}"/>
<c:set var="width" value="${not empty currentResource.moduleParams.width ?
    'w_'.concat(currentResource.moduleParams.width) : ''}"/>

<c:set var="urlParams" value=""/>
<c:choose>
    <c:when test="${not empty height}">
        <c:set var="urlParams" value="/${height}"/>
        <c:if test="${not empty width}">
            <c:set var="urlParams" value="${urlParams},${width}"/>
        </c:if>
    </c:when>
    <c:otherwise>
        <c:if test="${not empty width}">
            <c:set var="urlParams" value="/${width}"/>
        </c:if>
    </c:otherwise>
</c:choose>

<c:url value="${baseUrl}${urlParams}/${endUrl}"/>
<%--<c:out value="${url}" />--%>

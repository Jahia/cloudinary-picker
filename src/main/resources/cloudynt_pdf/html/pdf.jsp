<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>

<%--<c:set var="pdfUrl" value="${currentNode.properties['cloudy:url'].string}"/>--%>
<c:set var="pdfMinHeight" value="${not empty currentResource.moduleParams.pdfMinHeight ? currentResource.moduleParams.pdfMinHeight :
    '512'}"/>
<iframe
        src="${currentNode.getUrl()}"
        webkitallowfullscreen
        mozallowfullscreen
        allowfullscreen
        frameborder="0"
        allowtransparency="true"
        style="width:100%;min-height:${pdfMinHeight}px">
</iframe>
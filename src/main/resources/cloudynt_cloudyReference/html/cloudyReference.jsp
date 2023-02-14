<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>

<c:set var="cloudyNode" value="${currentNode.properties['j:node'].node}"/>
<c:set var="referenceView" value="${not empty currentNode.properties['j:referenceView'] ?
    currentNode.properties['j:referenceView'].string :
    'default'}"/>

<c:set var="width" value="${currentNode.properties['cloudy:width']}"/>
<c:set var="height" value="${currentNode.properties['cloudy:height']}"/>

<%--<c:set var="_widths_" value="${currentNode.properties['wden:imageSizes']}"/>--%>
<%--<c:set var="pdfMinHeight" value="${currentNode.properties['wden:pdfMinHeight'].long}"/>--%>

<%--<c:set var="defaultWidth" value="${not empty currentNode.properties['wden:defaultImageSize'] ?--%>
<%--    currentNode.properties['wden:defaultImageSize'].long :--%>
<%--    768}"/>--%>

<%--<c:choose>--%>
<%--    <c:when test="${fn:length(_widths_) > 0}">--%>
<%--        <c:set var="widths" value="${_widths_[0]}"/>--%>
<%--        <c:forEach var="width" items="${_widths_}" begin="1">--%>
<%--            <c:set var="widths" value="${widths},${width}"/>--%>
<%--            &lt;%&ndash;            <utility:logger level="INFO" value="***[widenReference] cloudyNode widths: ${widths}"/>&ndash;%&gt;--%>
<%--        </c:forEach>--%>
<%--    </c:when>--%>
<%--    <c:otherwise>--%>
<%--        <c:set var="widths" value="256,512,768,1280"/>--%>
<%--    </c:otherwise>--%>
<%--</c:choose>--%>

<c:if test="${renderContext.editMode}" >
    <div>
    <span style="color:#ccc;">Edit cloudinary media</span>
</c:if>

<template:module node="${cloudyNode}" editable="false" view="${referenceView}">
    <template:param name="width" value="${width}"/>
    <template:param name="height" value="${height}"/>
</template:module>

<c:if test="${renderContext.editMode}" >
    </div>
</c:if>

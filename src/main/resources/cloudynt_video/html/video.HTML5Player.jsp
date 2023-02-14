<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cloudinary" uri="http://www.jahia.org/se/cloudinary" %>

<c:set var="stream" value="${currentNode.properties['cloudy:url'].string}"/>
<c:set var="baseUrl" value="${currentNode.properties['cloudy:baseUrl'].string}"/>
<c:set var="poster" value="${currentNode.properties['cloudy:poster'].string}"/>

<video poster="${baseUrl}/${poster}" controls muted width="100%">
    <source src="${stream}">
</video>


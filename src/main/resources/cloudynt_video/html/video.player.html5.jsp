<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="stream" value="${currentNode.properties['cloudy:url'].string}"/>
<c:set var="baseUrl" value="${currentNode.properties['cloudy:baseUrl'].string}"/>
<c:set var="poster" value="${currentNode.properties['cloudy:poster'].string}"/>

<video poster="${currentNode.getThumbnailUrl("poster")}" controls muted width="100%">
    <source src="${currentNode.getUrl()}">
</video>
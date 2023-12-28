<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="cloudyNode" value="${currentNode.properties['source'].node}"/>

<video poster="${cloudyNode.getThumbnailUrl("poster")}" controls muted width="100%">
    <source src="${cloudyNode.getUrl()}">
</video>
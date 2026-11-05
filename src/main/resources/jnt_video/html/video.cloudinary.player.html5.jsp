<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="cloudyNode" value="${currentNode.properties['source'].node}"/>

<video poster="${fn:escapeXml(cloudyNode.getThumbnailUrl("poster"))}" controls muted width="100%">
    <source src="${fn:escapeXml(cloudyNode.getUrl())}">
</video>
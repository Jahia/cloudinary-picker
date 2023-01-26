<%@ page language="java" contentType="text/javascript" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="cloudinary" uri="http://www.jahia.org/se/cloudinary" %>

<c:set var="cloudinaryConfig" value="${cloudinary:config()}"/>

<c:choose>
    <c:when test="${! empty cloudinaryConfig}">
        window.contextJsParameters.config.cloudinary={
            <c:if test="${! empty cloudinaryConfig['cloudinary_provider.cloudName']}">
                cloudName:"${cloudinaryConfig['cloudinary_provider.cloudName']}",
            </c:if>
            <c:if test="${! empty cloudinaryConfig['cloudinary_provider.apiKey']}">
                apiKey:"${cloudinaryConfig['cloudinary_provider.apiKey']}",
            </c:if>
            <%--mountPoint:"/sites/systemsite/contents/dam-cloudinary"--%>
        }
        console.debug("%c Cloudinary config is added to contextJsParameters.config", 'color: #3c8cba');
    </c:when>
    <c:otherwise>
        <utility:logger level="warn" value="no content of cloudinarynt:mountPoint available"/>
        console.warn("no content of cloudinarynt:mountPoint available");
    </c:otherwise>
</c:choose>

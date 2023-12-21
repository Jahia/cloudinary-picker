<%@ page language="java" contentType="text/javascript" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>

<c:set var="cloudinaryConfig" value="${functions:getConfigValues('org.jahia.se.modules.cloudinary_picker_credentials')}"/>

<c:choose>
    <c:when test="${! empty cloudinaryConfig}">
        window.contextJsParameters.config.cloudinary={
            apiKey:"${cloudinaryConfig['cloudinary_provider.apiKey']}",
            cloudName:"${cloudinaryConfig['cloudinary_provider.cloudName']}",
            mountPoint:"/sites/systemsite/contents/dam-cloudinary",
            applyOnPickers:"${cloudinaryConfig['cloudinary_provider.applyOnPickers']}",
        }
        console.debug("%c Cloudinary config is added to contextJsParameters.config", 'color: #3c8cba');
    </c:when>
    <c:otherwise>
        <utility:logger level="warn" value="no content of cloudinarynt:mountPoint available"/>
        console.warn("no content of cloudinarynt:mountPoint available");
    </c:otherwise>
</c:choose>

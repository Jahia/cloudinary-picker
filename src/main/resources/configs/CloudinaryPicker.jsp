<%@ page language="java" contentType="text/javascript" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>

<c:set var="cloudyConfig" value="${functions:getConfigValues('org.jahia.se.modules.dam.cloudinary.provider.config')}"/>

<c:choose>
    <c:when test="${! empty cloudyConfig}">
        window.contextJsParameters.config.cloudinary={
            apiKey:"${cloudyConfig['apiKey']}",
            cloudName:"${cloudyConfig['cloudName']}",
            applyOnPickers:"${cloudyConfig['frontApplyOnPickers']}",
            mountPoint:"${cloudyConfig['edpMountPath']}"
        }
        console.debug("%c Cloudinary config is added to contextJsParameters.config", 'color: #3c8cba');
    </c:when>
    <c:otherwise>
        <utility:logger level="warn" value="Cloudinary provider configuration is not available"/>
        console.warn("Cloudinary provider configuration is not available");
    </c:otherwise>
</c:choose>

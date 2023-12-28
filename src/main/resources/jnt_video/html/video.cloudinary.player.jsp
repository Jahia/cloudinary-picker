<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>


<c:set var="cloudName" value="${functions:getConfigValue('org.jahia.se.modules.cloudinary_picker_credentials','cloudinary_provider.cloudName')}"/>
<c:set var="cloudyNode" value="${currentNode.properties['source'].node}"/>
<c:set var="publicId" value="${cloudyNode.properties['cloudy:publicId'].string}"/>

<link href="https://unpkg.com/cloudinary-video-player@latest/dist/cld-video-player.min.css" rel="stylesheet">
<script src="https://unpkg.com/cloudinary-video-player@latest/dist/cld-video-player.min.js" type="text/javascript"></script>

<video
        id="example-player"
        controls
        muted
        class="cld-video-player cld-video-player-skin-dark"
        width="100%"
        data-cld-public-id="${publicId}">
</video>

<script>
    const cld = cloudinary.videoPlayer('example-player',{ fluid:true, cloud_name: '${cloudName}',showLogo:false});
</script>
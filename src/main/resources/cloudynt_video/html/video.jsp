<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="cloudinary" uri="http://www.jahia.org/se/cloudinary" %>

<c:set var="cloudinaryConfig" value="${cloudinary:config()}"/>
<%--<c:set var="stream" value="${currentNode.properties['cloudy:url'].string}"/>--%>
<c:set var="publicId" value="${currentNode.properties['cloudy:publicId'].string}"/>

<link href="https://unpkg.com/cloudinary-video-player@1.9.5/dist/cld-video-player.min.css" rel="stylesheet">
<script src="https://unpkg.com/cloudinary-video-player@1.9.5/dist/cld-video-player.min.js" type="text/javascript"></script>

<video
    id="example-player"
    controls
    muted
    class="cld-video-player cld-video-player-skin-dark"
    data-cld-public-id="${publicId}">
</video>

<script>
    const cld = cloudinary.videoPlayer('example-player',{ cloud_name: '${cloudinaryConfig['cloudinary_provider.cloudName']}' }).width(500);
</script>


<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--
    Renders the model without the Cloudinary Product Gallery, for a site whose Cloudinary plan does
    not carry it. The viewer reads glTF only, so Cloudinary converts the asset on delivery: f_glb for
    the web, f_usdz for the iOS AR handover.
--%>
<c:set var="baseUrl" value="${currentNode.properties['cloudy:baseUrl'].string}"/>
<c:set var="endUrl" value="${currentNode.properties['cloudy:endUrl'].string}"/>
<c:set var="minHeight" value="${not empty currentResource.moduleParams.minHeight ?
    currentResource.moduleParams.minHeight : '512'}"/>
<c:set var="viewerVersion" value="${not empty currentResource.moduleParams.viewerVersion ?
    currentResource.moduleParams.viewerVersion : '4.0.0'}"/>

<script type="module" src="https://unpkg.com/@google/model-viewer@${fn:escapeXml(viewerVersion)}/dist/model-viewer.min.js"></script>

<model-viewer src="${baseUrl}/f_glb/${endUrl}"
              ios-src="${baseUrl}/f_usdz/${endUrl}"
              poster="${currentNode.getThumbnailUrl('thumbnail2')}"
              alt="${fn:escapeXml(currentNode.displayableName)}"
              camera-controls
              auto-rotate
              ar
              ar-modes="webxr scene-viewer quick-look"
              style="width:100%;min-height:${fn:escapeXml(minHeight)}px"></model-viewer>

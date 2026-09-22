<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<c:set var="cloudName" value="${functions:getConfigValue('org.jahia.se.modules.dam.cloudinary.provider.config','cloudName')}"/>
<c:set var="publicId" value="${currentNode.properties['cloudy:publicId'].string}"/>

<%-- Cloudinary documents "latest" as a development channel, so the gallery is pinned. --%>
<c:set var="galleryVersion" value="${not empty currentResource.moduleParams.galleryVersion ?
    currentResource.moduleParams.galleryVersion : '1.2.2'}"/>
<%-- The gallery sizes itself from its container's width, so an unbounded container fills the page. --%>
<c:set var="maxWidth" value="${not empty currentResource.moduleParams.maxWidth ?
    currentResource.moduleParams.maxWidth : '800'}"/>
<c:set var="aspectRatio" value="${not empty currentResource.moduleParams.aspectRatio ?
    currentResource.moduleParams.aspectRatio : 'square'}"/>
<c:set var="showAR" value="${currentResource.moduleParams.showAR ne 'false'}"/>
<c:set var="autoRotate" value="${currentResource.moduleParams.autoRotate ne 'false'}"/>

<template:addResources type="javascript" resources="https://product-gallery.cloudinary.com/${fn:escapeXml(galleryVersion)}/all.js"/>
<template:addResources type="javascript" resources="cloudinary-model3d.js"/>

<%-- The gallery addresses its container by CSS selector, so the script gives each one an id. --%>
<div class="cloudy-model3d"
     data-cloud-name="${fn:escapeXml(cloudName)}"
     data-public-id="${fn:escapeXml(publicId)}"
     data-show-ar="${showAR}"
     data-auto-rotate="${autoRotate}"
     data-aspect-ratio="${fn:escapeXml(aspectRatio)}"
     style="max-width:${fn:escapeXml(maxWidth)}px;margin:0 auto"></div>

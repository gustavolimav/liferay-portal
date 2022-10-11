<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ include file="/init.jsp" %>

<%@ page import="com.liferay.asset.kernel.service.AssetCategoryLocalServiceUtil" %><%@
page import="com.liferay.portal.search.web.internal.facet.category.builder.AssetCategoriesSearchFacetDisplayContextBuilder" %><%@
page import="com.liferay.portal.search.web.internal.facet.category.builder.AssetCategoryPermissionCheckerImpl" %><%@
page import="com.liferay.portal.search.web.internal.facet.category.display.context.AssetCategoriesSearchFacetDisplayContext" %><%@
page import="com.liferay.portal.search.web.internal.facet.category.display.context.AssetCategoriesSearchFacetTermDisplayContext" %><%@
page import="com.liferay.portal.search.web.internal.facet.folder.FolderSearcher" %><%@
page import="com.liferay.portal.search.web.internal.facet.folder.FolderTitleLookupImpl" %><%@
page import="com.liferay.portal.search.web.internal.facet.folder.display.context.FolderSearchFacetDisplayContext" %><%@
page import="com.liferay.portal.search.web.internal.facet.folder.display.context.FolderSearchFacetTermDisplayContext" %><%@
page import="com.liferay.portal.search.web.internal.facet.folder.display.context.builder.FolderSearchFacetDisplayContextBuilder" %><%@
page import="com.liferay.portal.search.web.internal.facet.site.builder.ScopeSearchFacetDisplayContextBuilder" %><%@
page import="com.liferay.portal.search.web.internal.facet.site.display.context.ScopeSearchFacetDisplayContext" %><%@
page import="com.liferay.portal.search.web.internal.facet.site.display.context.ScopeSearchFacetTermDisplayContext" %><%@
page import="com.liferay.portal.search.web.internal.facet.tag.builder.AssetTagsSearchFacetDisplayContextBuilder" %><%@
page import="com.liferay.portal.search.web.internal.facet.tag.display.context.AssetTagsSearchFacetDisplayContext" %><%@
page import="com.liferay.portal.search.web.internal.facet.tag.display.context.AssetTagsSearchFacetTermDisplayContext" %><%@
page import="com.liferay.portal.search.web.internal.facet.type.builder.AssetEntriesSearchFacetDisplayContextBuilder" %><%@
page import="com.liferay.portal.search.web.internal.facet.type.display.context.AssetEntriesSearchFacetDisplayContext" %><%@
page import="com.liferay.portal.search.web.internal.facet.type.display.context.AssetEntriesSearchFacetTermDisplayContext" %><%@
page import="com.liferay.portal.search.web.internal.facet.user.builder.UserSearchFacetDisplayContextBuilder" %><%@
page import="com.liferay.portal.search.web.internal.facet.user.display.context.UserSearchFacetDisplayContext" %><%@
page import="com.liferay.portal.search.web.internal.facet.user.display.context.UserSearchFacetTermDisplayContext" %>

<%
String randomNamespace = PortalUtil.generateRandomKey(request, _RANDOM_KEY_INPUT) + StringPool.UNDERLINE;

Facet facet = (Facet)request.getAttribute("search.jsp-facet");

String fieldParam = ParamUtil.getString(request, facet.getFieldId());

FacetConfiguration facetConfiguration = facet.getFacetConfiguration();

JSONObject dataJSONObject = facetConfiguration.getData();

FacetCollector facetCollector = facet.getFacetCollector();

String cssClass = "search-facet search-";
%>

<%!
private static final String _RANDOM_KEY_INPUT = "portlet_search_facets_" + StringUtil.randomString();
%>
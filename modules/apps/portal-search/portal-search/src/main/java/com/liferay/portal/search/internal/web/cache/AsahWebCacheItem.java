/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.internal.web.cache;

import com.liferay.analytics.settings.configuration.AnalyticsConfiguration;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.webcache.WebCacheItem;
import com.liferay.portal.kernel.webcache.WebCachePoolUtil;
import com.liferay.portal.search.internal.configuration.AsahIndividualsConfiguration;
import com.liferay.portal.search.internal.configuration.AsahSearchKeywordsConfiguration;

import java.net.HttpURLConnection;

/**
 * @author Petteri Karttunen
 */
public class AsahWebCacheItem implements WebCacheItem {

	public static JSONObject get(
		AnalyticsConfiguration analyticsConfiguration,
		AsahSearchKeywordsConfiguration asahSearchKeywordsConfiguration,
		AsahIndividualsConfiguration asahIndividualsConfiguration,
		String contentType, long companyId, String displayLanguageId,
		long groupId, int minCounts, int rangeKey, int size, String sort,
		String endPointUsage, String endPointName) {

		try {
			return (JSONObject)WebCachePoolUtil.get(
				StringBundler.concat(
					AsahWebCacheItem.class.getName(), StringPool.POUND,
					companyId, StringPool.POUND, minCounts, StringPool.POUND,
					displayLanguageId, StringPool.POUND, groupId,
					StringPool.POUND, sort),
				new AsahWebCacheItem(
					analyticsConfiguration, asahSearchKeywordsConfiguration,
					asahIndividualsConfiguration, contentType,
					displayLanguageId, groupId, minCounts, rangeKey, size, sort,
					endPointUsage, endPointName));
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			return JSONFactoryUtil.createJSONObject();
		}
	}

	public AsahWebCacheItem(
		AnalyticsConfiguration analyticsConfiguration,
		AsahSearchKeywordsConfiguration asahSearchKeywordsConfiguration,
		AsahIndividualsConfiguration asahIndividualsConfiguration,
		String contentType, String displayLanguageId, long groupId,
		int minCounts, int rangeKey, int size, String sort,
		String endPointUsage, String endPointName) {

		_analyticsConfiguration = analyticsConfiguration;
		_asahSearchKeywordsConfiguration = asahSearchKeywordsConfiguration;
		_asahIndividualsConfiguration = asahIndividualsConfiguration;
		_contentType = contentType;
		_displayLanguageId = displayLanguageId;
		_groupId = groupId;
		_minCounts = minCounts;
		_rangeKey = rangeKey;
		_size = size;
		_sort = sort;
		_endPointUsage = endPointUsage;
		_endPointName = endPointName;
	}

	@Override
	public JSONObject convert(String key) {
		try {
			Http.Options options = new Http.Options();

			options.addHeader(
				"OSB-Asah-Faro-Backend-Security-Signature",
				_analyticsConfiguration.
					liferayAnalyticsFaroBackendSecuritySignature());
			options.addHeader(
				"OSB-Asah-Project-ID",
				_analyticsConfiguration.liferayAnalyticsProjectId());

			String url = _getURL();

			if (_log.isDebugEnabled()) {
				_log.debug("Reading " + url);
			}

			options.setLocation(url);

			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
				HttpUtil.URLtoString(options));

			_validateResponse(jsonObject, options.getResponse());

			return jsonObject;
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public long getRefreshTime() {
		return _asahSearchKeywordsConfiguration.cacheTimeout();
	}

	private String _getHashedEmail() {
		return "47ff64395860b1d498241d907069f649b98c198a95b3ba5303b87094058590c1";
	}

	private String _getURL() {
		StringBundler sb = new StringBundler(22);

		sb.append(_analyticsConfiguration.liferayAnalyticsFaroBackendURL());
		sb.append("/api/1.0/");
		sb.append(_endPointName);

		if (_endPointName.equals("individuals")) {
			sb.append("/");
			sb.append(_getHashedEmail());
		}

		sb.append("/");
		sb.append(_endPointUsage);
		sb.append("?");

		if (_minCounts > 0) {
			if (_endPointName.equals("individuals")) {
				sb.append("counts=");
			}
			else {
				sb.append("minCounts=");
			}

			sb.append(_minCounts);
		}

		if (_rangeKey > 0) {
			sb.append("&rangeKey=");
			sb.append(_rangeKey);
		}

		if (!Validator.isBlank(_contentType)) {
			sb.append("&contentType=");
			sb.append(_contentType);
		}

		if (!Validator.isBlank(_displayLanguageId)) {
			sb.append("&displayLanguageId=");
			sb.append(_displayLanguageId);
		}

		if (_groupId > 0) {
			sb.append("&groupId=");
			sb.append(_groupId);
		}

		sb.append("&size=");
		sb.append(_size);
		sb.append("&sort=");
		sb.append(_sort);

		return sb.toString();
	}

	private void _validateResponse(
		JSONObject jsonObject, Http.Response response) {

		if ((response.getResponseCode() == HttpURLConnection.HTTP_OK) &&
			jsonObject.has("_embedded")) {

			return;
		}

		throw new RuntimeException(
			StringBundler.concat(
				"Response body: ", jsonObject, "\nResponse code: ",
				response.getResponseCode()));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AsahWebCacheItem.class);

	private final AnalyticsConfiguration _analyticsConfiguration;
	private final AsahIndividualsConfiguration _asahIndividualsConfiguration;
	private final AsahSearchKeywordsConfiguration
		_asahSearchKeywordsConfiguration;
	private final String _contentType;
	private final String _displayLanguageId;
	private final String _endPointName;
	private final String _endPointUsage;
	private final long _groupId;
	private final int _minCounts;
	private final int _rangeKey;
	private final int _size;
	private final String _sort;

}
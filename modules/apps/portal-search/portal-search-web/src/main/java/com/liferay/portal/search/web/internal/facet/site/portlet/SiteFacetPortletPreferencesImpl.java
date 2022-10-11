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

package com.liferay.portal.search.web.internal.facet.site.portlet;

import com.liferay.portal.search.web.internal.helper.PortletPreferencesHelper;

import java.util.Optional;

import javax.portlet.PortletPreferences;

/**
 * @author André de Oliveira
 */
public class SiteFacetPortletPreferencesImpl
	implements SiteFacetPortletPreferences {

	public SiteFacetPortletPreferencesImpl(
		Optional<PortletPreferences> portletPreferencesOptional) {

		_portletPreferencesHelper = new PortletPreferencesHelper(
			portletPreferencesOptional);
	}

	@Override
	public int getFrequencyThreshold() {
		return _portletPreferencesHelper.getInteger(
			PREFERENCE_KEY_FREQUENCY_THRESHOLD, 1);
	}

	@Override
	public int getMaxTerms() {
		return _portletPreferencesHelper.getInteger(
			PREFERENCE_KEY_MAX_TERMS, 10);
	}

	@Override
	public String getParameterName() {
		return _portletPreferencesHelper.getString(
			PREFERENCE_KEY_PARAMETER_NAME, "site");
	}

	@Override
	public boolean isFrequenciesVisible() {
		return _portletPreferencesHelper.getBoolean(
			PREFERENCE_KEY_FREQUENCIES_VISIBLE, true);
	}

	private final PortletPreferencesHelper _portletPreferencesHelper;

}
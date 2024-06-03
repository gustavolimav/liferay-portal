/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.internal.facet;

import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.RangeFacet;
import com.liferay.portal.kernel.search.facet.util.RangeParserUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.facet.nested.NestedFacet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bryan Engler
 * @author André de Oliveira
 */
public class FacetBucketUtil {

	public static boolean isFieldInBucket(
		Field field, String term, Facet facet) {

		if (facet instanceof NestedFacet) {
			String[] values = field.getValues();

			for (String value : values) {
				value = value.substring(1, value.length() - 1); // tirar as chaves

				String[] pairs = value.split(", "); // fazer os pares

				Map<String, String> map = new HashMap<>();

				for (String pair : pairs) {
					String[] keyValue = pair.split("=");
					String key = keyValue[0].trim();
					String value2 = keyValue[1].trim();

					// se key for aggregation key
					map.put(key, value2); // popular o hashmap
				}

				if (map.containsValue(term)) { // verificar logica
					return true;
				}
			}

			return false;
		}

		if (facet instanceof RangeFacet) {
			String[] range = RangeParserUtil.parserRange(term);

			String lower = range[0];
			String upper = range[1];

			String value = field.getValue();

			if (Validator.isNotNull(lower) && (lower.compareTo(value) <= 0) &&
				Validator.isNotNull(upper) && (value.compareTo(upper) <= 0)) {

				return true;
			}

			return false;
		}

		if (ArrayUtil.contains(field.getValues(), term, false)) {
			return true;
		}

		return false;
	}

}
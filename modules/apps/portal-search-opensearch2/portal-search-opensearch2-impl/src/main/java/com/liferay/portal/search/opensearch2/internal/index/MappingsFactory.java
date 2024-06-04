/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.index;

import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.opensearch2.internal.configuration.OpenSearchConfigurationWrapper;
import com.liferay.portal.search.opensearch2.internal.index.constants.MappingsConstants;
import com.liferay.portal.search.opensearch2.internal.util.IndexUtil;
import com.liferay.portal.search.opensearch2.internal.util.JsonpUtil;
import com.liferay.portal.search.opensearch2.internal.util.ResourceUtil;
import com.liferay.portal.search.spi.index.configuration.contributor.helper.TypeMappingsHelper;

import java.io.IOException;

import java.util.List;
import java.util.Map;

import org.opensearch.client.opensearch._types.mapping.DynamicTemplate;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch.indices.GetMappingRequest;
import org.opensearch.client.opensearch.indices.GetMappingResponse;
import org.opensearch.client.opensearch.indices.OpenSearchIndicesClient;
import org.opensearch.client.opensearch.indices.PutMappingRequest;
import org.opensearch.client.opensearch.indices.PutMappingResponse;
import org.opensearch.client.opensearch.indices.get_mapping.IndexMappingRecord;

/**
 * @author André de Oliveira
 * @author Petteri Karttunen
 */
public class MappingsFactory implements TypeMappingsHelper {

	public MappingsFactory(
		String indexName, JSONFactory jsonFactory,
		OpenSearchIndicesClient openSearchIndicesClient,
		OpenSearchConfigurationWrapper openSearchConfigurationWrapper) {

		_indexName = indexName;
		_jsonFactory = jsonFactory;
		_openSearchIndicesClient = openSearchIndicesClient;
		_openSearchConfigurationWrapper = openSearchConfigurationWrapper;
	}

	public String getCurrentMappings(String indexName) {
		try {
			GetMappingResponse getMappingResponse =
				_openSearchIndicesClient.getMapping(
					GetMappingRequest.of(
						getMappingRequest -> getMappingRequest.index(
							indexName)));

			Map<String, IndexMappingRecord> indexMappingRecords =
				getMappingResponse.result();

			IndexMappingRecord indexMappingRecord = indexMappingRecords.get(
				indexName);

			return JsonpUtil.toString(indexMappingRecord.mappings());
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	public JSONObject getMappingsJSONObject(String overrideMappings) {
		if (Validator.isNotNull(overrideMappings)) {
			return _removeLegacyDocumentType(overrideMappings);
		}

		String defaultMappings = ResourceUtil.getResourceAsString(
			getClass(), MappingsConstants.LIFERAY_MAPPING_FILE_NAME);
		String defaultMappingTemplate = ResourceUtil.getResourceAsString(
			getClass(),
			MappingsConstants.LIFERAY_MAPPING_FILE_NAME_OPTIONAL_DEFAULTS);

		return _createJSONObject(
			_getMappingsToPutWithMergedDynamicTemplates(
				defaultMappingTemplate, defaultMappings));
	}

	@Override
	public void putTypeMappings(String source) {
		PutMappingRequest.Builder builder = new PutMappingRequest.Builder();

		builder.index(_indexName);

		JSONObject mappingsJSONObject = _createJSONObject(
			_getMappingsToPutWithMergedDynamicTemplates(
				getCurrentMappings(_indexName), source));

		List<Map<String, DynamicTemplate>> dynamicTemplates =
			IndexUtil.getDynamicTemplatesMap(mappingsJSONObject);

		if (dynamicTemplates != null) {
			builder.dynamicTemplates(dynamicTemplates);
		}

		Map<String, Property> properties = IndexUtil.getPropertiesMap(
			mappingsJSONObject);

		if (properties != null) {
			builder.properties(properties);
		}

		try {
			PutMappingResponse putMappingResponse =
				_openSearchIndicesClient.putMapping(builder.build());

			JsonpUtil.logInfoResponse(putMappingResponse, _log);
		}
		catch (Exception exception) {
			_log.error(
				"Unable to put mappings for index " + _indexName, exception);
		}
	}

	private JSONObject _createJSONObject(String jsonString) {
		try {
			return _jsonFactory.createJSONObject(jsonString);
		}
		catch (JSONException jsonException) {
			throw new RuntimeException(jsonException);
		}
	}

	private String _getMappingsToPutWithMergedDynamicTemplates(
		String currentMappings, String mappingsToPut) {

		JSONObject currentMappingsJSONObject = _removeLegacyDocumentType(
			currentMappings);
		JSONObject mappingsToPutJSONObject = _removeLegacyDocumentType(
			mappingsToPut);

		mappingsToPutJSONObject.put(
			"dynamic_templates",
			IndexUtil.mergeDynamicTemplates(
				currentMappingsJSONObject.getJSONArray("dynamic_templates"),
				mappingsToPutJSONObject.getJSONArray("dynamic_templates")));

		return mappingsToPutJSONObject.toString();
	}

	private JSONObject _removeLegacyDocumentType(String source) {
		JSONObject jsonObject = _createJSONObject(source);

		if (jsonObject.has(MappingsConstants.LEGACY_LIFERAY_DOCUMENT_TYPE)) {
			return jsonObject.getJSONObject(
				MappingsConstants.LEGACY_LIFERAY_DOCUMENT_TYPE);
		}

		return jsonObject;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		MappingsFactory.class);

	private final String _indexName;
	private final JSONFactory _jsonFactory;
	private final OpenSearchConfigurationWrapper
		_openSearchConfigurationWrapper;
	private final OpenSearchIndicesClient _openSearchIndicesClient;

}
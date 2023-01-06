/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.search.experiences.internal.ml.text.embedding;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.UpdateDocumentRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.index.TextEmbeddingHelper;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.script.ScriptBuilder;
import com.liferay.portal.search.script.ScriptType;
import com.liferay.portal.search.script.Scripts;
import com.liferay.search.experiences.configuration.SemanticSearchConfiguration;
import com.liferay.search.experiences.internal.search.spi.model.index.contributor.JournalArticleTextEmbeddingModelDocumentContributor;

import java.io.IOException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Gustavo Lima
 */
@Component(
	enabled = false, immediate = true,
	property = "background.task.executor.class.name=com.liferay.search.experiences.internal.ml.text.embedding.TextEmbeddingBackgroundTaskExecutor", // use that string to call background task
	service = {BackgroundTaskExecutor.class, TextEmbeddingHelper.class}
)
public class TextEmbeddingBackgroundTaskExecutor
	extends BaseBackgroundTaskExecutor implements TextEmbeddingHelper {

	@Override
	public BackgroundTaskExecutor clone() {
		return this;
	}

	@Override
	public BackgroundTaskResult execute(BackgroundTask backgroundTask)
		throws Exception {

		return null; // implement
	}

	@Override
	public BackgroundTaskDisplay getBackgroundTaskDisplay(
		BackgroundTask backgroundTask) {

		return null;
	}

	public void index(long[] companyIds) { // created to dosent need to use background task
		String[] indexNames = _getIndexNames(companyIds);

		for (int i = 0; i < companyIds.length; i++) {
			String indexName = indexNames[i];

			try {
				_indexTextEmbbeding(companyIds[i], indexName);
			}
			catch (IOException ioException) {
				_log.error(
					StringBundler.concat(
						"Unable to index assetVocabularyCategoryIds values in ",
						"index ", indexName,
						". A full reindex may be necessary."),
					ioException);
			}
		}
	}

	private BooleanQuery _createQuery(long companyId) {
		BooleanQuery booleanQueryLang = _queries.booleanQuery();

		if (false) {
			_realBooleanQueryLang(booleanQueryLang);
		}

		booleanQueryLang.addShouldQueryClauses(_queries.exists("title_en_US"));

		BooleanQuery booleanQueryClassName = _queries.booleanQuery();

		for (String name : _getClassNames(companyId)) {
			booleanQueryClassName.addShouldQueryClauses(
				_queries.term("entryClassName", name));
		}

		BooleanQuery finalBooleanQuery = _queries.booleanQuery();

		finalBooleanQuery.addMustQueryClauses(booleanQueryLang);

		finalBooleanQuery.addFilterQueryClauses(booleanQueryClassName);

		return finalBooleanQuery;
	}

	private SearchSearchRequest _createSearchRequest(
		long companyId, String indexName, int start) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setIndexNames(indexName);
		searchSearchRequest.setQuery(_createQuery(companyId));
		searchSearchRequest.setSize(100);
		searchSearchRequest.setSelectedFieldNames(
			Field.UID, Field.ENTRY_CLASS_NAME, Field.ENTRY_CLASS_PK);
		searchSearchRequest.setStart(start);

		return searchSearchRequest;
	}

	private String[] _getClassNames(long companyId) {
		_semanticSearchConfiguration = _getSemanticSearchConfiguration(
			companyId);

		return _semanticSearchConfiguration.assetEntryClassNames();
	}

	private String _getIndexName(long companyId) {
		return "liferay-" + companyId;
	}

	private String[] _getIndexNames(long[] companyIds) {
		String[] indexNames = new String[companyIds.length];

		for (int i = 0; i < companyIds.length; i++) {
			indexNames[i] = _getIndexName(companyIds[i]);
		}

		return indexNames;
	}

	private SemanticSearchConfiguration _getSemanticSearchConfiguration(
		long companyId) {

		try {
			return _configurationProvider.getCompanyConfiguration(
				SemanticSearchConfiguration.class, companyId);
		}
		catch (ConfigurationException configurationException) {
			return ReflectionUtil.throwException(configurationException);
		}
	}

	private UpdateDocumentRequest _getUpdateDocumentRequest(
		String indexName, DocumentBuilder documentBuilder,
		String uidFieldValueString) {

		UpdateDocumentRequest updateDocumentRequest = new UpdateDocumentRequest(
			indexName, uidFieldValueString, documentBuilder.build());

		ScriptBuilder scriptBuilder = _scripts.builder();

		updateDocumentRequest.setScript(
			scriptBuilder.idOrCode(
				"ctx._source.title_en_US = 'testing'"
			).language(
				"painless"
			).scriptType(
				ScriptType.INLINE
			).build());

		return updateDocumentRequest;
	}

	private void _indexTextEmbbeding(long companyId, String indexName)
		throws IOException {

		if (_log.isInfoEnabled()) {
			_log.info(
				"Started indexing of the Text Embbeding field for index " +
					indexName);
		}

		int start = 0;

		while (true) { // implement searchAfter following kibana annotation
			SearchSearchRequest searchSearchRequest = _createSearchRequest(
				companyId, indexName, start);

			SearchSearchResponse searchSearchResponse =
				_searchEngineAdapter.execute(searchSearchRequest);

			Hits hits = searchSearchResponse.getHits();

			Document[] documents = hits.getDocs();

			if (documents.length == 0) {
				break;
			}

			_updateDocuments(indexName, searchSearchResponse);

			// Atualizar com
			// https://github.com/BryanEngler/liferay-portal/commit/a0f92fcd2a6411d344e7df3a7914a0959d5afe23#diff-0da039c1beb53f09192459b461092e1993275ad0a3a933e6cddb1150e9c65000

			start += searchSearchRequest.getSize();
		}
	}

	private void _journalArticle(
		com.liferay.portal.search.document.Field entryClassPKField,
		Object entryClassNameDocumentFieldValue) {

		if (entryClassNameDocumentFieldValue.equals(
				JournalArticle.class.getName())) {

			Document portalKernelDocument = new DocumentImpl();

			List<JournalArticle> journalArticles =
				_journalArticleLocalService.getArticlesByResourcePrimKey(
					GetterUtil.getLong(entryClassPKField.getValue()));

			for (JournalArticle journalArticle : journalArticles) {
				String articleId = journalArticle.getArticleId();

				if (articleId.equals(entryClassPKField.getValue())) {
					_journalArticleTextEmbeddingModelDocumentContributor.
						contribute(portalKernelDocument, journalArticle);
				}
			}
		}
	}

	private void _realBooleanQueryLang(BooleanQuery booleanQueryLang) {
		List<String> languageIds = Arrays.asList(
			_semanticSearchConfiguration.languageIds());

		for (String lang : languageIds) {
			booleanQueryLang.addShouldQueryClauses(
				_queries.exists("text_embedding_256_" + lang));
			booleanQueryLang.addShouldQueryClauses(
				_queries.exists("text_embedding_512_" + lang));
			booleanQueryLang.addShouldQueryClauses(
				_queries.exists("text_embedding_768_" + lang));
		}
	}

	private void _updateDocuments(
		String indexName, SearchSearchResponse searchSearchResponse) {

		SearchHits searchHits = searchSearchResponse.getSearchHits();

		List<SearchHit> searchHitsList = searchHits.getSearchHits();

		BulkDocumentRequest bulkDocumentRequest = new BulkDocumentRequest();

		for (SearchHit hit : searchHitsList) {
			com.liferay.portal.search.document.Document portalSearchDocument =
				hit.getDocument();

			Map<String, com.liferay.portal.search.document.Field> fields =
				portalSearchDocument.getFields();

			com.liferay.portal.search.document.Field entryClassNameField =
				fields.get(Field.ENTRY_CLASS_NAME);
			com.liferay.portal.search.document.Field entryClassPKField =
				fields.get(Field.ENTRY_CLASS_PK);
			com.liferay.portal.search.document.Field uidField = fields.get(
				Field.UID);

			_journalArticle(entryClassPKField, entryClassNameField.getValue());

			DocumentBuilder documentBuilder = _documentBuilderFactory.builder();

			String uidFieldValueString = GetterUtil.getString(
				uidField.getValue());

			bulkDocumentRequest.addBulkableDocumentRequest(
				_getUpdateDocumentRequest(
					indexName, documentBuilder, uidFieldValueString));
		}

		_searchEngineAdapter.execute(bulkDocumentRequest);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		TextEmbeddingBackgroundTaskExecutor.class);

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference
	private DocumentBuilderFactory _documentBuilderFactory;

	@Reference
	private JournalArticleLocalService _journalArticleLocalService;

	@Reference
	private Queries _queries;

	@Reference
	private Scripts _scripts;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

	@Reference
	private JournalArticleTextEmbeddingModelDocumentContributor
		_journalArticleTextEmbeddingModelDocumentContributor; // test it

	private volatile SemanticSearchConfiguration _semanticSearchConfiguration;

}
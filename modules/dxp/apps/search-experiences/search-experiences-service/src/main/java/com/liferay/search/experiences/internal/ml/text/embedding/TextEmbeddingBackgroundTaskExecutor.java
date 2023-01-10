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

import com.liferay.blogs.model.BlogsEntry;
import com.liferay.blogs.service.BlogsEntryLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.knowledge.base.model.KBArticle;
import com.liferay.knowledge.base.service.KBArticleLocalService;
import com.liferay.message.boards.model.MBMessage;
import com.liferay.message.boards.service.MBMessageLocalService;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
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
import com.liferay.portal.search.script.Script;
import com.liferay.portal.search.script.ScriptBuilder;
import com.liferay.portal.search.script.ScriptType;
import com.liferay.portal.search.script.Scripts;
import com.liferay.search.experiences.configuration.SemanticSearchConfiguration;
import com.liferay.search.experiences.internal.search.spi.model.index.contributor.BlogsEntryTextEmbeddingModelDocumentContributor;
import com.liferay.search.experiences.internal.search.spi.model.index.contributor.JournalArticleTextEmbeddingModelDocumentContributor;
import com.liferay.search.experiences.internal.search.spi.model.index.contributor.KBArticleTextEmbeddingModelDocumentContributor;
import com.liferay.search.experiences.internal.search.spi.model.index.contributor.MBMessageTextEmbeddingModelDocumentContributor;
import com.liferay.search.experiences.internal.search.spi.model.index.contributor.WikiPageTextEmbeddingModelDocumentContributor;
import com.liferay.wiki.model.WikiPage;
import com.liferay.wiki.service.WikiPageLocalService;

import java.io.IOException;
import java.io.Serializable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Gustavo Lima
 */
@Component(
	enabled = false, immediate = true,
	property = "background.task.executor.class.name=com.liferay.search.experiences.internal.ml.text.embedding.TextEmbeddingBackgroundTaskExecutor",
	service = {BackgroundTaskExecutor.class, TextEmbeddingHelper.class}
)
public class TextEmbeddingBackgroundTaskExecutor
	extends BaseBackgroundTaskExecutor implements TextEmbeddingHelper {

	// com.liferay.search.experiences.internal.
	// ml.text.embedding.TextEmbeddingBackgroundTaskExecutor
	// use that string to call background task

	@Override
	public BackgroundTaskExecutor clone() {
		return this;
	}

	@Override
	public BackgroundTaskResult execute(BackgroundTask backgroundTask)
		throws Exception {

		Map<String, Serializable> taskContextMap =
			backgroundTask.getTaskContextMap();

		String indexName = (String)taskContextMap.get("indexName");
		long companyId = GetterUtil.getLong(taskContextMap.get("companyId"));

		// companyId is needed to get the configuration

		try {
			_indexTextEmbbeding(companyId, indexName);
		}
		catch (IOException ioException) {
			_log.error(
				StringBundler.concat(
					"Unable to index textEmbedding values in index ", indexName,
					". A full reindex may be necessary."),
				ioException);
		}

		return BackgroundTaskResult.SUCCESS;
	}

	@Override
	public BackgroundTaskDisplay getBackgroundTaskDisplay(
		BackgroundTask backgroundTask) {

		return null;
	}

	public void index(long[] companyIds) {
		String[] indexNames = _getIndexNames(companyIds);

		for (int i = 0; i < companyIds.length; i++) {
			String indexName = indexNames[i];

			try {
				_indexTextEmbbeding(companyIds[i], indexName);
			}
			catch (IOException ioException) {
				_log.error(
					StringBundler.concat(
						"Unable to index textEmbedding values in index ",
						indexName, ". A full reindex may be necessary."),
					ioException);
			}
		}
	}

	private void _contribute(
		String entryClassName, long entryClassPK, String uid, Long groupId,
		Document portalKernelDocument) {

		if (entryClassName.equals(BlogsEntry.class.getName())) {
			try {
				BlogsEntry blogsEntry =
					_blogsEntryLocalService.getBlogsEntryByUuidAndGroupId(
						uid, groupId);

				if (blogsEntry != null) {
					_blogsEntryTextEmbeddingModelDocumentContributor.contribute(
						portalKernelDocument, blogsEntry);
				}
			}
			catch (PortalException portalException) {
				_log.error(portalException);
			}
		}
		else if (entryClassName.equals(JournalArticle.class.getName())) {
			try {
				JournalArticle journalArticle =
					_journalArticleLocalService.getLatestArticle(entryClassPK);

				if (journalArticle != null) {
					_journalArticleTextEmbeddingModelDocumentContributor.
						contribute(portalKernelDocument, journalArticle);
				}
			}
			catch (PortalException portalException) {
				_log.error(portalException);
			}
		}
		else if (entryClassName.equals(KBArticle.class.getName())) {
			KBArticle kbArticle = _kbArticleLocalService.fetchLatestKBArticle(
				entryClassPK, WorkflowConstants.STATUS_APPROVED);

			if (kbArticle != null) {
				_kbArticleTextEmbeddingModelDocumentContributor.contribute(
					portalKernelDocument, kbArticle);
			}
		}
		else if (entryClassName.equals(MBMessage.class.getName())) {
			MBMessage mbMessage = _mbMessageLocalService.fetchMBMessage(
				entryClassPK);

			if (mbMessage != null) {
				_mbMessageTextEmbeddingModelDocumentContributor.contribute(
					portalKernelDocument, mbMessage);
			}
		}
		else if (entryClassName.equals(WikiPage.class.getName())) {
			WikiPage wikiPage = _wikiPageLocalService.fetchWikiPage(
				entryClassPK);

			if (wikiPage != null) {
				_wikiPageTextEmbeddingModelDocumentContributor.contribute(
					portalKernelDocument, wikiPage);
			}
		}
	}

	private BooleanQuery _createQuery(long companyId) {
		BooleanQuery booleanQueryLang = _queries.booleanQuery();

		if (false) {
			_realBooleanQueryLang(booleanQueryLang); // remove if false
		}

		// just to test

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
		searchSearchRequest.setSize(10000);
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

	private HashMap<String, String> _getFieldsToReindexHashMap(
		Document portalKernelDocument) {

		List<String> languageIds = Arrays.asList(
			_semanticSearchConfiguration.languageIds());

		HashMap<String, String> fieldsToReindexHashMap = new HashMap<>();

		for (String lang : languageIds) {
			String textEmbedding256 = "text_embedding_256_" + lang;
			String textEmbedding512 = "text_embedding_512_" + lang;
			String textEmbedding768 = "text_embedding_768_" + lang;

			fieldsToReindexHashMap.put(
				textEmbedding256, portalKernelDocument.get(textEmbedding256));
			fieldsToReindexHashMap.put(
				textEmbedding512, portalKernelDocument.get(textEmbedding512));
			fieldsToReindexHashMap.put(
				textEmbedding768, portalKernelDocument.get(textEmbedding768));
		}

		return fieldsToReindexHashMap;
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

	private Script _getScript(ScriptBuilder scriptBuilder, Document document) {
		HashMap<String, String> fieldsToReindexHashMap =
			_getFieldsToReindexHashMap(document);

		StringBuilder irOrCode = new StringBuilder();

		for (Map.Entry<String, String> entry :
				fieldsToReindexHashMap.entrySet()) {

			if (entry.getValue() != null) {
				irOrCode.append("ctx._source.");
				irOrCode.append(entry.getKey());
				irOrCode.append(" = ");
				irOrCode.append("'");
				irOrCode.append(entry.getValue());
				irOrCode.append("'");
				irOrCode.append(";");
			}
		}

		return scriptBuilder.idOrCode(
			irOrCode.toString()
		).language(
			"painless"
		).scriptType(
			ScriptType.INLINE
		).build();
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
		String indexName, String uid, DocumentBuilder documentBuilder,
		Document document) {

		UpdateDocumentRequest updateDocumentRequest = new UpdateDocumentRequest(
			indexName, uid, documentBuilder.build());

		ScriptBuilder scriptBuilder = _scripts.builder();

		updateDocumentRequest.setScript(_getScript(scriptBuilder, document));

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

			start += searchSearchRequest.getSize();
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

			String entryClassName = portalSearchDocument.getString(
				Field.ENTRY_CLASS_NAME);
			long entryClassPK = portalSearchDocument.getLong(
				Field.ENTRY_CLASS_PK);
			String uid = portalSearchDocument.getString(Field.UID);
			Long groupId = portalSearchDocument.getLong(Field.GROUP_ID);

			Document portalKernelDocument = new DocumentImpl();

			_contribute(
				entryClassName, entryClassPK, uid, groupId,
				portalKernelDocument);

			// it just accepts portalSearchDocument and not portalKernelDocument
			// is that the expected?

			DocumentBuilder documentBuilder = _documentBuilderFactory.builder(
				portalSearchDocument);

			bulkDocumentRequest.addBulkableDocumentRequest(
				_getUpdateDocumentRequest(
					indexName, uid, documentBuilder, portalKernelDocument));
		}

		_searchEngineAdapter.execute(bulkDocumentRequest);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		TextEmbeddingBackgroundTaskExecutor.class);

	@Reference
	private BlogsEntryLocalService _blogsEntryLocalService;

	@Reference
	private BlogsEntryTextEmbeddingModelDocumentContributor
		_blogsEntryTextEmbeddingModelDocumentContributor;

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference
	private DocumentBuilderFactory _documentBuilderFactory;

	@Reference
	private JournalArticleLocalService _journalArticleLocalService;

	@Reference
	private JournalArticleTextEmbeddingModelDocumentContributor
		_journalArticleTextEmbeddingModelDocumentContributor;

	@Reference
	private KBArticleLocalService _kbArticleLocalService;

	@Reference
	private KBArticleTextEmbeddingModelDocumentContributor
		_kbArticleTextEmbeddingModelDocumentContributor;

	@Reference
	private MBMessageLocalService _mbMessageLocalService;

	@Reference
	private MBMessageTextEmbeddingModelDocumentContributor
		_mbMessageTextEmbeddingModelDocumentContributor;

	@Reference
	private Queries _queries;

	@Reference
	private Scripts _scripts;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

	private volatile SemanticSearchConfiguration _semanticSearchConfiguration;

	@Reference
	private WikiPageLocalService _wikiPageLocalService;

	@Reference
	private WikiPageTextEmbeddingModelDocumentContributor
		_wikiPageTextEmbeddingModelDocumentContributor;

}
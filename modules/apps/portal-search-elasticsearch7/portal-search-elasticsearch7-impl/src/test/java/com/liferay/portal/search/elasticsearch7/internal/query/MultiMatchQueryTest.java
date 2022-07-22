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

package com.liferay.portal.search.elasticsearch7.internal.query;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.IndexWriter;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.search.elasticsearch7.internal.LiferayElasticsearchIndexingFixtureFactory;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.MultiMatchQuery;
import com.liferay.portal.search.query.Operator;
import com.liferay.portal.search.test.util.DocumentsAssert;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;
import com.liferay.portal.search.test.util.indexing.DocumentFixture;
import com.liferay.portal.search.test.util.indexing.IndexingFixture;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Wade Cao
 */
public class MultiMatchQueryTest extends BaseIndexingTestCase {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_indexWriter = getIndexWriter();
	}

	@After
	@Override
	public void tearDown() throws SearchException {
		Stream<Document> stream = _documents.stream();

		_indexWriter.deleteDocuments(
			_getSearchContext(),
			stream.map(
				document -> document.get(Field.UID)
			).collect(
				Collectors.toList()
			));

		_documents.clear();
	}

	@Test
	public void testMultiMatchQueryBoolPrefix() {
		_indexUserDocuments("delta", "omega", "userName1");

		_indexUserDocuments("omega", "delta", "userName2");

		_indexUserDocuments("omega", "alpha", "userName3");

		MultiMatchQuery multiMatchQuery = queries.multiMatch(
			"delta", "firstName", "lastName");

		multiMatchQuery.setType(MultiMatchQuery.Type.BOOL_PREFIX);

		List<String> expected = Arrays.asList("userName1", "userName2");

		_assertSearch(expected, multiMatchQuery);
	}

	@Test
	public void testMultiMatchQueryCrossField() {
		_indexUserDocuments("bravo", "alpha", "userName1");

		_indexUserDocuments("omega", "beta", "userName2");

		MultiMatchQuery multiMatchQuery = queries.multiMatch(
			"bravo alpha", "firstName", "lastName");

		multiMatchQuery.setOperator(Operator.AND);
		multiMatchQuery.setType(MultiMatchQuery.Type.CROSS_FIELDS);

		List<String> expected = Arrays.asList("userName1");

		_assertSearch(expected, multiMatchQuery);
	}

	@Test
	public void testMultiMatchQueryCrossField01() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField02() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField03() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField04() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField05() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField06() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField07() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField08() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField09() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField10() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField11() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField12() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField13() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField14() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField15() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField16() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField17() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField18() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField19() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField20() throws Exception {
		testMultiMatchQueryCrossField();
	}@Test
	public void testMultiMatchQueryCrossField21() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField22() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField23() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField24() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField25() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField26() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField27() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField28() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField29() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryCrossField30() throws Exception {
		testMultiMatchQueryCrossField();
	}

	@Test
	public void testMultiMatchQueryDefault() {
		_indexUserDocuments("alpha", "omega", "userName1");

		_indexUserDocuments("bravo", "alpha", "userName2");

		_indexUserDocuments("alpha", "zeta", "userName3");

		_indexUserDocuments("omega", "beta", "userName4");

		MultiMatchQuery multiMatchQuery = queries.multiMatch(
			"alpha", "firstName", "lastName");

		List<String> expected = Arrays.asList(
			"userName1", "userName2", "userName3");

		_assertSearch(expected, multiMatchQuery);
	}

	@Test
	public void testMultiMatchQueryPhrasePrefix() {
		_indexUserDocuments("bro charlie", "iota", "userName1");

		_indexUserDocuments("omega", "beta", "userName2");

		MultiMatchQuery multiMatchQuery = queries.multiMatch(
			"bro", "firstName", "lastName");

		multiMatchQuery.setType(MultiMatchQuery.Type.PHRASE_PREFIX);

		List<String> expected = Arrays.asList("userName1");

		_assertSearch(expected, multiMatchQuery);
	}

	@Test
	public void testMultiMatchQueryTieBreaker() {
		_indexUserDocuments("delta", "omega", "userName1");

		_indexUserDocuments("omega", "delta", "userName2");

		_indexUserDocuments("omega", "beta", "userName3");

		MultiMatchQuery multiMatchQuery = queries.multiMatch(
			"delta", "firstName", "lastName");

		multiMatchQuery.setTieBreaker(Float.valueOf(0.3F));

		List<String> expected = Arrays.asList("userName1", "userName2");

		_assertSearch(expected, multiMatchQuery);
	}

	@Test
	public void testMultiMatchQueryTieBreaker01() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker02() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker03() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker04() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker05() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker06() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker07() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker08() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker09() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker10() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker11() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker12() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker13() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker14() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker15() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker16() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker17() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker18() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker19() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker20() throws Exception {
		testMultiMatchQueryTieBreaker();
	}@Test
	public void testMultiMatchQueryTieBreaker21() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker22() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker23() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker24() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker25() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker26() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker27() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker28() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker29() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Test
	public void testMultiMatchQueryTieBreaker30() throws Exception {
		testMultiMatchQueryTieBreaker();
	}

	@Override
	protected IndexingFixture createIndexingFixture() throws Exception {
		return LiferayElasticsearchIndexingFixtureFactory.getInstance();
	}

	private void _assertSearch(
		List<String> expectedValues, MultiMatchQuery multiMatchQuery) {

		assertSearch(
			indexingTestHelper -> {
				SearchEngineAdapter searchEngineAdapter =
					getSearchEngineAdapter();

				SearchSearchResponse searchSearchResponse =
					searchEngineAdapter.execute(
						new SearchSearchRequest() {
							{
								setIndexNames("_all");
								setQuery(multiMatchQuery);
								setSize(30);
							}
						});

				SearchHits searchHits = searchSearchResponse.getSearchHits();

				List<SearchHit> searchHitsList = searchHits.getSearchHits();

				Hits hits = searchSearchResponse.getHits();

				DocumentsAssert.assertValuesIgnoreRelevance(
					"Retrieved hits ->", hits.getDocs(), Field.USER_NAME,
					expectedValues);

				Assert.assertEquals(
					"Retrieved hits", expectedValues.size(),
					searchHitsList.size());

				Assert.assertEquals(
					"Total hits", expectedValues.size(),
					searchHits.getTotalHits());
			});
	}

	private Document _createDocument(
		String firstName, String lastName, String userName) {

		Document document = DocumentFixture.newDocument(
			getCompanyId(), getGroupId(), getEntryClassName());

		document.addKeyword("firstName", firstName);
		document.addKeyword("lastName", lastName);
		document.addKeyword(Field.USER_NAME, userName);

		return document;
	}

	private SearchContext _getSearchContext() {
		SearchContext searchContext = new SearchContext();

		searchContext.setCompanyId(getCompanyId());

		return searchContext;
	}

	private void _indexUserDocuments(
		String firstName, String lastName, String userName) {

		Document document = _createDocument(firstName, lastName, userName);

		addDocument(document);

		_documents.add(document);
	}

	private final List<Document> _documents = new ArrayList<>();
	private IndexWriter _indexWriter;

}
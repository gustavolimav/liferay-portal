/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {mergeTests} from '@playwright/test';

import {featureFlagsTest} from '../../fixtures/featureFlagsTest';
import {loginTest} from '../../fixtures/loginTest';
import {searchPageTest} from '../../fixtures/searchPageTest';

export const test = mergeTests(
	loginTest(),
	featureFlagsTest({
		'LPS-122920': true,
	}),
	searchPageTest
);

test.describe('ViewSystemAndInstanceSettingsSaveUI', () => {
	test('Fill out required fields for Hugging Face Inference API and save in System Settings', async ({
		semanticSearchSettingsPage,
	}) => {
		await semanticSearchSettingsPage.navigateToSemanticSearchSystemSetting();

		await semanticSearchSettingsPage.checkTextEmbeddingsEnabled();
		await semanticSearchSettingsPage.fillAccessToken(
			'System Settings Token'
		);
		await semanticSearchSettingsPage.selectModel('facebook/bart-base');
		await semanticSearchSettingsPage.saveAndContinue();

		await semanticSearchSettingsPage.assertFieldNotPresent('Host Address');
	});

	// test('Assert form fields retain the values added earlier in System Settings', async ({ semanticSearchSettingsPage }) => {
	// 	await semanticSearchSettingsPage.navigateToSemanticSearchSystemSetting();

	// 	await semanticSearchSettingsPage.assertCheck('Text Embeddings Enabled', true);
	// 	await semanticSearchSettingsPage.assertAccessTokenValue('System Settings Token');
	// 	await semanticSearchSettingsPage.assertModelValue('facebook/bart-base');
	// });

	test('Refresh the page and assert form fields retain the values added earlier in System Settings', async ({
		semanticSearchSettingsPage,
	}) => {
		await semanticSearchSettingsPage.navigateToSemanticSearchSystemSetting();

		await semanticSearchSettingsPage.page.reload();

		await semanticSearchSettingsPage.assertCheck(
			'Text Embeddings Enabled',
			true
		);
		await semanticSearchSettingsPage.assertAccessTokenValue(
			'System Settings Token'
		);
		await semanticSearchSettingsPage.assertModelValue('facebook/bart-base');
	});

	test('Assert field values are reset when the form is reset to default in System Settings', async ({
		semanticSearchSettingsPage,
	}) => {
		await semanticSearchSettingsPage.navigateToSemanticSearchSystemSetting();

		await semanticSearchSettingsPage.resetConfiguration();

		await semanticSearchSettingsPage.assertFieldNotPresent('Host Address');
		await semanticSearchSettingsPage.assertCheck(
			'Text Embeddings Enabled',
			false
		);
		await semanticSearchSettingsPage.assertAccessTokenValue('');
		await semanticSearchSettingsPage.assertModelValue('');
	});

	test('Fill out required fields for Hugging Face Inference API and save in Instance Settings', async ({
		semanticSearchSettingsPage,
	}) => {
		await semanticSearchSettingsPage.navigateToSemanticSearchInstanceSetting();

		await semanticSearchSettingsPage.checkTextEmbeddingsEnabled();
		await semanticSearchSettingsPage.fillAccessToken(
			'Instance Settings Token'
		);
		await semanticSearchSettingsPage.selectModel('albert/albert-base-v1');

		await semanticSearchSettingsPage.saveAndContinue();

		await semanticSearchSettingsPage.assertFieldNotPresent('Host Address');
	});

	// test('Assert form fields retain the values added earlier in Instance Settings', async ({ semanticSearchSettingsPage }) => {
	// 	await semanticSearchSettingsPage.navigateToSemanticSearchInstanceSetting();

	// 	await semanticSearchSettingsPage.assertCheck('Text Embeddings Enabled', true);
	// 	await semanticSearchSettingsPage.assertAccessTokenValue('Instance Settings Token');
	// 	await semanticSearchSettingsPage.assertModelValue('albert/albert-base-v1');
	// });

	test('Refresh the page and assert form fields retain the values added earlier in Instance Settings', async ({
		semanticSearchSettingsPage,
	}) => {
		await semanticSearchSettingsPage.navigateToSemanticSearchInstanceSetting();

		await semanticSearchSettingsPage.page.reload();

		await semanticSearchSettingsPage.assertCheck(
			'Text Embeddings Enabled',
			true
		);
		await semanticSearchSettingsPage.assertAccessTokenValue(
			'Instance Settings Token'
		);
		await semanticSearchSettingsPage.assertModelValue(
			'albert/albert-base-v1'
		);
	});

	test('Assert Instance Settings field values remain unchanged after changes in System Settings', async ({
		semanticSearchSettingsPage,
	}) => {
		await semanticSearchSettingsPage.navigateToSemanticSearchSystemSetting();

		await semanticSearchSettingsPage.fillAccessToken(
			'System Settings Token'
		);
		await semanticSearchSettingsPage.selectModel('facebook/bart-base');

		await semanticSearchSettingsPage.saveAndContinue();

		await semanticSearchSettingsPage.navigateToSemanticSearchInstanceSetting();

		await semanticSearchSettingsPage.assertAccessTokenValue(
			'Instance Settings Token'
		);
		await semanticSearchSettingsPage.assertModelValue(
			'albert/albert-base-v1'
		);
	});

	test('Assert Instance Settings field values use the System Settings values when resetting to default', async ({
		semanticSearchSettingsPage,
	}) => {
		await semanticSearchSettingsPage.navigateToSemanticSearchInstanceSetting();

		await semanticSearchSettingsPage.resetConfiguration();

		await semanticSearchSettingsPage.assertCheck(
			'Text Embeddings Enabled',
			false
		);
		await semanticSearchSettingsPage.assertAccessTokenValue(
			'System Settings Token'
		);
		await semanticSearchSettingsPage.assertModelValue('facebook/bart-base');
	});
});

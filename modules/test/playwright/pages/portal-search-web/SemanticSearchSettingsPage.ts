/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import { Page, expect } from '@playwright/test';

import { InstanceSettingsPage } from '../configuration-admin-web/InstanceSettingsPage';
import {SystemSettingsPage} from '../configuration-admin-web/SystemSettingsPage';

export class SemanticSearchSettingsPage {
	readonly page: Page;
	readonly systemSettingsPage: SystemSettingsPage;
	readonly instanceSettingsPage: InstanceSettingsPage;

	constructor(page: Page) {
		this.page = page;
		this.systemSettingsPage = new SystemSettingsPage(page);
		this.instanceSettingsPage = new InstanceSettingsPage(page);
	}

	async navigateToSemanticSearchSystemSetting() {
		await this.systemSettingsPage.goToSystemSetting(
			'Search Experiences',
			'Semantic Search (Beta)'
		);
	}

	async navigateToSemanticSearchInstanceSetting() {
		await this.instanceSettingsPage.goToInstanceSetting(
			'Search Experiences',
			'Semantic Search (Beta)'
		);
	}

	async fillAccessToken(token: string) {
		await this.page.fill('label:has-text("Access Token")', token);
	}

	async fillModel(model: string) {
		await this.page.fill('label:has-text("Model")', model);
	}

	async selectModel(modelName: string) {
		await this.page.click(`text="${modelName}"`);
	}

	async saveAndContinue() {
		await this.page.click('button:has-text("Save")');
		await this.page.click('button:has-text("Continue to Save")');
	}

	async checkTextEmbeddingsEnabled() {
		await this.page.getByLabel('Text Embeddings Enabled').check();
	}

	async assertFieldNotPresent(fieldLabel: string) {
		await expect(this.page.locator(`label:has-text("${fieldLabel}")`)).not.toBeVisible();
	}

	async assertFieldValue(fieldLabel: string, fieldValue: string) {
		await expect(this.page.locator(`input[aria-label="${fieldLabel}"]`)).toHaveValue(fieldValue);
	}

	async resetConfiguration() {
		await this.page.getByRole('button', { name: 'Actions' }).click();
		await this.page.getByRole('link', { name: 'Reset Default Values' }).click();
	}

	async assertCheck(fieldLable: string, status: boolean) {
		if (status) {
			await expect(this.page.getByLabel(fieldLable)).toBeChecked();
		} else {
			await expect(this.page.getByLabel(fieldLable)).not.toBeChecked();
		}
	}
}

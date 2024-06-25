/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect} from '@playwright/test';

import {InstanceSettingsPage} from '../configuration-admin-web/InstanceSettingsPage';
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

	async fillAccessToken(acessToken: string) {
		await this.page.getByLabel('Access Token').fill(acessToken);
	}
	async selectModel(modelName: string) {
		const model = this.page.getByLabel('Model', {exact: true});

		await model.waitFor();
		await model.fill(modelName);
		await this.page.getByText(modelName).click();
	}

	async saveAndContinue() {
		await this.page.getByText('Save', {exact: true}).click();
		await this.page.getByRole('button', {name: 'Continue to Save'}).click();
	}

	async checkTextEmbeddingsEnabled() {
		await this.page.getByLabel('Text Embeddings Enabled').check();
	}

	async assertFieldNotPresent(fieldLabel: string) {
		await expect(this.page.getByLabel(fieldLabel)).not.toBeVisible();
	}

	async assertAccessTokenValue(fieldValue: string) {
		await expect(this.page.getByLabel('Access Token')).toHaveValue(
			fieldValue
		);
	}

	async assertModelValue(fieldValue: string) {
		await expect(this.page.getByLabel('Model', {exact: true})).toHaveValue(
			fieldValue
		);
	}

	async resetConfiguration() {
		await this.page.getByRole('button', {name: 'Actions'}).click();
		await this.page
			.getByRole('link', {name: 'Reset Default Values'})
			.click();
	}

	async assertCheck(fieldLable: string, status: boolean) {
		if (status) {
			await expect(this.page.getByLabel(fieldLable)).toBeChecked();
		}
		else {
			await expect(this.page.getByLabel(fieldLable)).not.toBeChecked();
		}
	}
}

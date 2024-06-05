/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {test} from '@playwright/test';

import {SearchPage} from '../pages/portal-search-web/SearchPage';
import { SemanticSearchSettingsPage } from '../pages/portal-search-web/SemanticSearchSettingsPage';

const searchPageTest = test.extend<{
	searchPage: SearchPage;
	semanticSearchSettingsPage: SemanticSearchSettingsPage;
}>({
	searchPage: async ({page}, use) => {
		await use(new SearchPage(page));
	},
	semanticSearchSettingsPage: async ({page}, use) => {
		await use(new SemanticSearchSettingsPage(page));
	},
});

export {searchPageTest};

/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.support.tomcat.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.catalina.ha.session.DeltaRequest;

/**
 * @author Shuyang Zhou
 */
public class LiferayDeltaRequest extends DeltaRequest {

	public LiferayDeltaRequest() {
	}

	public LiferayDeltaRequest(String sessionId, boolean recordAllActions) {
		super(sessionId, recordAllActions);
	}

	@Override
	public void readExternal(ObjectInput objectInput)
		throws ClassNotFoundException, IOException {

		byte[] bytes = new byte[objectInput.readInt()];

		objectInput.readFully(bytes);

		try (ObjectInput liferayObjectInput =
				LiferayDeltaManager.toObjectInputStream(
					new ByteArrayInputStream(bytes))) {

			super.readExternal(liferayObjectInput);
		}
	}

	@Override
	public void writeExternal(ObjectOutput objectOutput) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream =
			new ByteArrayOutputStream();

		try (ObjectOutput liferayObjectOutput =
				LiferayDeltaManager.toObjectOutputStream(
					byteArrayOutputStream)) {

			super.writeExternal(liferayObjectOutput);
		}

		byte[] bytes = byteArrayOutputStream.toByteArray();

		objectOutput.writeInt(bytes.length);
		objectOutput.write(bytes);
	}

}
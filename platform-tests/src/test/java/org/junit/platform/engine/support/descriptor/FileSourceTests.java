/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.PreconditionViolationException;

class FileSourceTests {

	@Test
	void creationWithNullFileShouldThrowPreconditionViolationException() {
		assertThrows(PreconditionViolationException.class, () -> new FileSource(null));
	}

	@Test
	void twoSameFileSourceObjectsShouldBeEqual() {
		assertEquals(new FileSource(new File("noPath")), new FileSource(new File("noPath")));
	}

	@Test
	void twoDifferentFileSourceObjectsShouldNotBeEqual() {
		assertNotEquals(new FileSource(new File("noPath")), new FileSource(new File("differentPath")));
	}

	@Test
	void twoSameFileSourceObjectsShouldHaveEqualHashCodes() {
		assertEquals(new FileSource(new File("noPath")).hashCode(), new FileSource(new File("noPath")).hashCode());
	}

	@Test
	void twoDifferentFileSourceObjectsShouldNotHaveEqualHashCodes() {
		assertNotEquals(new FileSource(new File("noPath")).hashCode(),
			new FileSource(new File("differentPath")).hashCode());
	}
}

/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.extension;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ContainerExtensionContext;

/**
 * Demo extension for auto-detection of extensions loaded via Java's
 * {@link java.util.ServiceLoader} mechanism.
 *
 * @since 5.0
 */
public class ServiceLoaderExtension implements BeforeAllCallback {

	@Override
	public void beforeAll(ContainerExtensionContext context) throws Exception {
	}

}

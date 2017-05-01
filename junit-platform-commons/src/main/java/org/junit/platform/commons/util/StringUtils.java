/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.util;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.util.Arrays;

import org.junit.platform.commons.meta.API;

/**
 * Collection of utilities for working with {@link String Strings},
 * {@link CharSequence CharSequences}, etc.
 *
 * <h3>DISCLAIMER</h3>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.0
 */
@API(Internal)
public final class StringUtils {

	///CLOVER:OFF
	private StringUtils() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	 * Determine if the supplied {@link String} is <em>blank</em> (i.e.,
	 * {@code null} or consisting only of whitespace characters).
	 *
	 * @param str the string to check
	 * @return {@code true} if the string is blank
	 * @see #isNotBlank(String)
	 */
	public static boolean isBlank(String str) {
		return (str == null || str.trim().isEmpty());
	}

	/**
	 * Determine if the supplied {@link String} is not {@linkplain #isBlank
	 * blank}.
	 *
	 * @param str the string to check
	 * @return {@code true} if the string is not blank
	 * @see #isBlank(String)
	 */
	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}

	/**
	 * Convert the supplied {@code Object} to a {@code String} using the
	 * following algorithm.
	 *
	 * <ul>
	 * <li>If the supplied object is {@code null}, this method returns {@code "null"}.</li>
	 * <li>If the supplied object is a primitive array, the appropriate
	 * {@code Arrays#toString(...)} variant will be used to convert it to a String.</li>
	 * <li>If the supplied object is an object array, {@code Arrays#deepToString(Object[])}
	 * will be used to convert it to a String.</li>
	 * <li>Otherwise, the result of invoking {@code toString()} on the object
	 * will be returned.</li>
	 * </ul>
	 *
	 * @param obj the object to convert to a String
	 * @return a String representation of the supplied object
	 * @see Arrays#deepToString(Object[])
	 * @see ClassUtils#nullSafeToString(Class...)
	 */
	public static String nullSafeToString(Object obj) {
		if (obj == null) {
			return "null";
		}
		else if (obj.getClass().isArray()) {
			if (obj.getClass().getComponentType().isPrimitive()) {
				if (obj instanceof boolean[]) {
					return Arrays.toString((boolean[]) obj);
				}
				if (obj instanceof char[]) {
					return Arrays.toString((char[]) obj);
				}
				if (obj instanceof short[]) {
					return Arrays.toString((short[]) obj);
				}
				if (obj instanceof byte[]) {
					return Arrays.toString((byte[]) obj);
				}
				if (obj instanceof int[]) {
					return Arrays.toString((int[]) obj);
				}
				if (obj instanceof long[]) {
					return Arrays.toString((long[]) obj);
				}
				if (obj instanceof float[]) {
					return Arrays.toString((float[]) obj);
				}
				if (obj instanceof double[]) {
					return Arrays.toString((double[]) obj);
				}
			}
			return Arrays.deepToString((Object[]) obj);
		}
		// else
		return obj.toString();
	}

}

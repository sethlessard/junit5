/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.discovery;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUri;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.extensions.TempDirectory;
import org.junit.jupiter.extensions.TempDirectory.Root;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * Unit tests for {@link DiscoverySelectors}.
 *
 * @since 1.0
 */
class DiscoverySelectorsTests {

	@Test
	void selectUriByName() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> selectUri((String) null));
		assertThrows(PreconditionViolationException.class, () -> selectUri("   "));
		assertThrows(PreconditionViolationException.class, () -> selectUri("foo:"));

		String uri = "http://junit.org";

		UriSelector selector = selectUri(uri);
		assertEquals(uri, selector.getUri().toString());
	}

	@Test
	void selectUriByURI() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> selectUri((URI) null));
		assertThrows(PreconditionViolationException.class, () -> selectUri("   "));

		URI uri = new URI("http://junit.org");

		UriSelector selector = selectUri(uri);
		assertEquals(uri, selector.getUri());
	}

	@Test
	void selectFileByName() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> selectFile((String) null));
		assertThrows(PreconditionViolationException.class, () -> selectFile("   "));

		String path = "src/test/resources/do_not_delete_me.txt";

		FileSelector selector = selectFile(path);
		assertEquals(path, selector.getRawPath());
		assertEquals(new File(path), selector.getFile());
		assertEquals(Paths.get(path), selector.getPath());
	}

	@Test
	void selectFileByFileReference() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> selectFile((File) null));
		assertThrows(PreconditionViolationException.class, () -> selectFile(new File("bogus/nonexistent.txt")));

		File currentDir = new File(".").getCanonicalFile();
		File relativeDir = new File("..", currentDir.getName());
		File file = new File(relativeDir, "src/test/resources/do_not_delete_me.txt");
		String path = file.getCanonicalFile().getPath();

		FileSelector selector = selectFile(file);
		assertEquals(path, selector.getRawPath());
		assertEquals(file.getCanonicalFile(), selector.getFile());
		assertEquals(Paths.get(path), selector.getPath());
	}

	@Test
	void selectDirectoryByName() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> selectDirectory((String) null));
		assertThrows(PreconditionViolationException.class, () -> selectDirectory("   "));

		String path = "src/test/resources";

		DirectorySelector selector = selectDirectory(path);
		assertEquals(path, selector.getRawPath());
		assertEquals(new File(path), selector.getDirectory());
		assertEquals(Paths.get(path), selector.getPath());
	}

	@Test
	void selectDirectoryByFileReference() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> selectDirectory((File) null));
		assertThrows(PreconditionViolationException.class, () -> selectDirectory(new File("bogus/nonexistent")));

		File currentDir = new File(".").getCanonicalFile();
		File relativeDir = new File("..", currentDir.getName());
		File directory = new File(relativeDir, "src/test/resources");
		String path = directory.getCanonicalFile().getPath();

		DirectorySelector selector = selectDirectory(directory);
		assertEquals(path, selector.getRawPath());
		assertEquals(directory.getCanonicalFile(), selector.getDirectory());
		assertEquals(Paths.get(path), selector.getPath());
	}

	@Test
	void selectClasspathResources() {
		assertThrows(PreconditionViolationException.class, () -> selectClasspathResource(null));
		assertThrows(PreconditionViolationException.class, () -> selectClasspathResource(""));
		assertThrows(PreconditionViolationException.class, () -> selectClasspathResource("    "));
		assertThrows(PreconditionViolationException.class, () -> selectClasspathResource("\t"));

		// with unnecessary "/" prefix
		ClasspathResourceSelector selector = selectClasspathResource("/foo/bar/spec.xml");
		assertEquals("foo/bar/spec.xml", selector.getClasspathResourceName());

		// standard use case
		selector = selectClasspathResource("A/B/C/spec.json");
		assertEquals("A/B/C/spec.json", selector.getClasspathResourceName());
	}

	@Test
	void selectPackageByName() {
		PackageSelector selector = selectPackage(getClass().getPackage().getName());
		assertEquals(getClass().getPackage().getName(), selector.getPackageName());
	}

	@Test
	void selectClassByName() {
		ClassSelector selector = selectClass(getClass().getName());
		assertEquals(getClass(), selector.getJavaClass());
	}

	@Test
	void selectMethodByFullyQualifiedName() throws Exception {
		Class<?> clazz = getClass();
		Method method = clazz.getDeclaredMethod("myTest");
		assertSelectMethodByFullyQualifiedName(clazz, method);
	}

	@Test
	void selectMethodByFullyQualifiedNameForDefaultMethodInInterface() throws Exception {
		Class<?> clazz = TestCaseWithDefaultMethod.class;
		Method method = clazz.getMethod("myTest");
		assertSelectMethodByFullyQualifiedName(clazz, method);
	}

	@Test
	void selectMethodByFullyQualifiedNameWithPrimitiveParameter() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", int.class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, int.class, "int");
	}

	@Test
	void selectMethodByFullyQualifiedNameWithPrimitiveParameterUsingSourceCodeSyntax() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", int.class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, "int", "int");
	}

	@Test
	void selectMethodByFullyQualifiedNameWithObjectParameter() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String.class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, String.class, String.class.getName());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithObjectParameterUsingSourceCodeSyntax() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String.class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, "java.lang.String", String.class.getName());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithPrimitiveArrayParameter() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", int[].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, int[].class, int[].class.getName());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithPrimitiveArrayParameterUsingSourceCodeSyntax() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", int[].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, "int[]", "int[]");
	}

	@Test
	void selectMethodByFullyQualifiedNameWithObjectArrayParameter() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String[].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, String[].class, String[].class.getName());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithObjectArrayParameterUsingSourceCodeSyntax() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String[].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, "java.lang.String[]", "java.lang.String[]");
	}

	@Test
	void selectMethodByFullyQualifiedNameWithTwoDimensionalPrimitiveArrayParameter() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", int[][].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, int[][].class, int[][].class.getName());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithTwoDimensionalPrimitiveArrayParameterUsingSourceCodeSyntax()
			throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", int[][].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, "int[][]", "int[][]");
	}

	@Test
	void selectMethodByFullyQualifiedNameWithTwoDimensionalObjectArrayParameter() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String[][].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, String[][].class, String[][].class.getName());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithTwoDimensionalObjectArrayParameterUsingSourceCodeSyntax()
			throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String[][].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, "java.lang.String[][]", "java.lang.String[][]");
	}

	@Test
	void selectMethodByFullyQualifiedNameWithMultidimensionalPrimitiveArrayParameter() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", int[][][][][].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, int[][][][][].class, int[][][][][].class.getName());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithMultidimensionalPrimitiveArrayParameterUsingSourceCodeSyntax()
			throws Exception {

		Method method = getClass().getDeclaredMethod("myTest", int[][][][][].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, "int[][][][][]", "int[][][][][]");
	}

	@Test
	void selectMethodByFullyQualifiedNameWithMultidimensionalObjectArrayParameter() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", Double[][][][][].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, Double[][][][][].class,
			Double[][][][][].class.getName());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithMultidimensionalObjectArrayParameterUsingSourceCodeSyntax()
			throws Exception {

		Method method = getClass().getDeclaredMethod("myTest", Double[][][][][].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, "java.lang.Double[][][][][]",
			"java.lang.Double[][][][][]");
	}

	private void assertSelectMethodByFullyQualifiedName(Class<?> clazz, Method method) {
		MethodSelector selector = selectMethod(fqmn(clazz, method.getName()));
		assertEquals(method, selector.getJavaMethod());
		assertEquals(clazz, selector.getJavaClass());
		assertEquals(clazz.getName(), selector.getClassName());
		assertEquals(method.getName(), selector.getMethodName());
		assertNull(selector.getMethodParameterTypes());
	}

	private void assertSelectMethodByFullyQualifiedName(Class<?> clazz, Method method, Class<?> parameterType,
			String expectedParameterTypes) {

		MethodSelector selector = selectMethod(fqmn(parameterType));
		assertEquals(method, selector.getJavaMethod());
		assertEquals(clazz, selector.getJavaClass());
		assertEquals(clazz.getName(), selector.getClassName());
		assertEquals(method.getName(), selector.getMethodName());
		assertEquals(expectedParameterTypes, selector.getMethodParameterTypes());
	}

	private void assertSelectMethodByFullyQualifiedName(Class<?> clazz, Method method, String parameterName,
			String expectedParameterTypes) {

		MethodSelector selector = selectMethod(fqmnWithParamNames(parameterName));
		assertEquals(method, selector.getJavaMethod());
		assertEquals(clazz, selector.getJavaClass());
		assertEquals(clazz.getName(), selector.getClassName());
		assertEquals(method.getName(), selector.getMethodName());
		assertEquals(expectedParameterTypes, selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodByClassAndMethodName() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest");

		MethodSelector selector = selectMethod(getClass(), "myTest");
		assertEquals(getClass(), selector.getJavaClass());
		assertEquals(getClass().getName(), selector.getClassName());
		assertEquals(method, selector.getJavaMethod());
		assertEquals("myTest", selector.getMethodName());
		assertNull(selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodByClassAndMethodNameWithParameterTypes() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String.class);

		MethodSelector selector = selectMethod(getClass(), "myTest", "java.lang.String");
		assertEquals(getClass(), selector.getJavaClass());
		assertEquals(getClass().getName(), selector.getClassName());
		assertEquals(method, selector.getJavaMethod());
		assertEquals("myTest", selector.getMethodName());
		assertEquals("java.lang.String", selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodWithParametersByMethodReference() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String.class);

		MethodSelector selector = selectMethod(getClass(), method);
		assertEquals(method, selector.getJavaMethod());
		assertEquals(getClass(), selector.getJavaClass());
		assertEquals(getClass().getName(), selector.getClassName());
		assertEquals("myTest", selector.getMethodName());
		assertNull(selector.getMethodParameterTypes());
	}

	@Test
	void selectClassByNameForSpockSpec() {
		String spockClassName = "org.example.CalculatorSpec";
		ClassSelector selector = selectClass(spockClassName);
		assertEquals(spockClassName, selector.getClassName());
	}

	@Test
	void selectMethodByClassAndNameForSpockSpec() {
		String spockClassName = "org.example.CalculatorSpec";
		String spockMethodName = "#a plus #b equals #c";

		MethodSelector selector = selectMethod(spockClassName, spockMethodName);
		assertEquals(spockClassName, selector.getClassName());
		assertEquals(spockMethodName, selector.getMethodName());
		assertNull(selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodByFullyQualifiedNameForSpockSpec() {
		String spockClassName = "org.example.CalculatorSpec";
		String spockMethodName = "#a plus #b equals #c";
		String spockFullyQualifiedMethodName = spockClassName + "#" + spockMethodName;

		MethodSelector selector = selectMethod(spockFullyQualifiedMethodName);
		assertEquals(spockClassName, selector.getClassName());
		assertEquals(spockMethodName, selector.getMethodName());
		assertNull(selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodByFullyQualifiedNameForSpockSpecWithParameters() {
		String className = "org.example.CalculatorSpec";
		String methodName = "#a plus #b equals #c";
		String methodParameters = "int, int, int";
		String spockFullyQualifiedMethodName = String.format("%s#%s(%s)", className, methodName, methodParameters);

		MethodSelector selector = selectMethod(spockFullyQualifiedMethodName);
		assertEquals(className, selector.getClassName());
		assertEquals(methodName, selector.getMethodName());
		assertEquals(methodParameters, selector.getMethodParameterTypes());
	}

	@Test
	public void selectClasspathRootsWithNonExistingDirectory() throws Exception {
		List<ClasspathRootSelector> selectors = selectClasspathRoots(singleton(Paths.get("some", "local", "path")));

		assertThat(selectors).isEmpty();
	}

	@Test
	public void selectClasspathRootsWithNonExistingJarFile() throws Exception {
		List<ClasspathRootSelector> selectors = selectClasspathRoots(singleton(Paths.get("some.jar")));

		assertThat(selectors).isEmpty();
	}

	@Test
	@ExtendWith(TempDirectory.class)
	public void selectClasspathRootsWithExistingDirectory(@Root Path tempDir) throws Exception {
		List<ClasspathRootSelector> selectors = selectClasspathRoots(singleton(tempDir));

		assertThat(selectors).extracting(ClasspathRootSelector::getClasspathRoot).containsExactly(tempDir.toUri());
	}

	@Test
	public void selectClasspathRootsWithExistingJarFile() throws Exception {
		URI jarUri = getClass().getResource("/jartest.jar").toURI();
		Path jarFile = Paths.get(jarUri);

		List<ClasspathRootSelector> selectors = selectClasspathRoots(singleton(jarFile));

		assertThat(selectors).extracting(ClasspathRootSelector::getClasspathRoot).containsExactly(jarUri);
	}

	// -------------------------------------------------------------------------

	private static String fqmn(Class<?>... params) {
		return fqmn(DiscoverySelectorsTests.class, "myTest", params);
	}

	private static String fqmn(Class<?> clazz, String methodName, Class<?>... params) {
		return ReflectionUtils.getFullyQualifiedMethodName(clazz, methodName, params);
	}

	private static String fqmnWithParamNames(String... params) {
		return String.format("%s#%s(%s)", DiscoverySelectorsTests.class.getName(), "myTest", String.join(", ", params));
	}

	interface TestInterface {

		@Test
		default void myTest() {
		}
	}

	static class TestCaseWithDefaultMethod implements TestInterface {
	}

	void myTest() {
	}

	void myTest(int num) {
	}

	void myTest(int[] nums) {
	}

	void myTest(int[][] grid) {
	}

	void myTest(int[][][][][] grid) {
	}

	void myTest(String info) {
	}

	void myTest(String[] info) {
	}

	void myTest(String[][] info) {
	}

	void myTest(Double[][][][][] data) {
	}

}

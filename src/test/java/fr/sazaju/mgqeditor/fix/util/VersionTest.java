package fr.sazaju.mgqeditor.fix.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class VersionTest {

	@Test
	public void testInitialVersionHasSpecifiedSize() {
		Version version = new Version((i) -> "", 5);
		assertEquals(5, version.size());
	}

	@Test
	public void testInitialVersionHasCorrectLines() {
		List<String> content = Arrays.asList("a", "b", "c", "d");

		Version version = new Version((i) -> content.get(i), content.size());
		assertEquals("a", version.get(0));
		assertEquals("b", version.get(1));
		assertEquals("c", version.get(2));
		assertEquals("d", version.get(3));
	}

	@Test
	public void testInitialVersionIteratorHasCorrectLines() {
		List<String> content = Arrays.asList("a", "b", "c", "d");

		Version version = new Version((i) -> content.get(i), content.size());
		Iterator<String> iterator = version.iterator();
		assertTrue(iterator.hasNext());
		assertEquals("a", iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals("b", iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals("c", iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals("d", iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testRevisedVersionHasCorrectSize() {
		List<String> content = Arrays.asList("a", "b", "c", "d");
		Version parent = new Version((i) -> content.get(i), content.size());

		Version version = new Version(parent, (src, i) -> src.get(i + 1));
		assertEquals(3, version.size());
	}

	@Test
	public void testRevisedVersionHasCorrectLines() {
		List<String> content = Arrays.asList("a", "b", "c", "d");
		Version parent = new Version((i) -> content.get(i), content.size());

		Version version = new Version(parent, (src, i) -> src.get(i + 1));
		assertEquals("b", version.get(0));
		assertEquals("c", version.get(1));
		assertEquals("d", version.get(2));
	}

	@Test
	public void testRevisedVersionIteratorHasCorrectLines() {
		List<String> content = Arrays.asList("a", "b", "c", "d");
		Version parent = new Version((i) -> content.get(i), content.size());

		Version version = new Version(parent, (src, i) -> src.get(i + 1));
		Iterator<String> iterator = version.iterator();
		assertTrue(iterator.hasNext());
		assertEquals("b", iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals("c", iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals("d", iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testVersionFromListHasCorrectSize() {
		Version version = Version.from(Arrays.asList("a", "b", "c", "d"));
		assertEquals(4, version.size());
	}

	@Test
	public void testVersionFromListHasCorrectLines() {
		Version version = Version.from(Arrays.asList("a", "b", "c", "d"));
		assertEquals("a", version.get(0));
		assertEquals("b", version.get(1));
		assertEquals("c", version.get(2));
		assertEquals("d", version.get(3));
	}

	@Test
	public void testVersionFromListIteratorHasCorrectLines() {
		Version version = Version.from(Arrays.asList("a", "b", "c", "d"));

		Iterator<String> iterator = version.iterator();
		assertTrue(iterator.hasNext());
		assertEquals("a", iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals("b", iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals("c", iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals("d", iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testVersionFromListGeneratesDifferentInstancesFromSameContent() {
		List<String> list = Arrays.asList("a", "b", "c", "d");
		Version version1 = Version.from(list);
		Version version2 = Version.from(list);

		assertFalse(version1 == version2);
	}

	@Test
	public void testVersionFromListIsIndependent() {
		List<String> list = new LinkedList<>(Arrays.asList("a", "b", "c", "d"));
		Version version = Version.from(list);
		list.set(0, "x");
		assertEquals("a", version.get(0));
	}

	@Test
	public void testVersionEqualsIfSameContent() {
		List<String> list = Arrays.asList("a", "b", "c", "d");
		Version version1 = Version.from(list);
		Version version2 = Version.from(list);

		assertEquals(version1, version2);
	}

	@Test
	public void testVersionToListProvidesCorrectStrings() {
		List<String> expected = Arrays.asList("a", "b", "c", "d");
		Version version = Version.from(expected);
		List<String> actual = version.toList();

		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}

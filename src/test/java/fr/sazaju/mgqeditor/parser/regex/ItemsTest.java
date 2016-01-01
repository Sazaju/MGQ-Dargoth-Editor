package fr.sazaju.mgqeditor.parser.regex;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import fr.sazaju.mgqeditor.parser.regex.Items.Item;
import fr.sazaju.mgqeditor.parser.regex.Items.ItemField;
import fr.vergne.ioutils.FileUtils;

public class ItemsTest {

	private final File testFolder = new File("src/test/resources");

	@Test
	public void testItemFieldFitsNameLineWithUnixNewline() throws IOException {
		ItemField field = new ItemField();
		field.setContent(" Name \"Herb\"\n");
		assertEquals("Name", field.getName());
		assertEquals("Herb", field.getValue());
	}

	@Test
	public void testItemFieldFitsNameLineWithWindowsNewline()
			throws IOException {
		ItemField field = new ItemField();
		field.setContent(" Name \"Herb\"\r\n");
		assertEquals("Name", field.getName());
		assertEquals("Herb", field.getValue());
	}

	@Test
	public void testItemFitsDummyWithUnixLine() throws IOException {
		Item item = new Item();
		item.setContent("Item 244\n Name \"***ダミー\"\n\n");
		assertEquals(244, item.getItemID());
		assertEquals("***ダミー", item.getField("Name"));
	}

	@Test
	public void testItemFitsDummyWithWindowsLine() throws IOException {
		Item item = new Item();
		item.setContent("Item 244\r\n Name \"***ダミー\"\r\n\r\n");
		assertEquals(244, item.getItemID());
		assertEquals("***ダミー", item.getField("Name"));
	}

	@Test
	public void testItemFitsFullEntryWithUnixLine() throws IOException {
		Item item = new Item();
		item.setContent("Item 2\n Name \"High-Quality Herb\"\n Description \"[Item] Target:Ally　Effect:300 HP Recovery\r\nMedicinal herb that recovers 300 of an ally's HP.\"\n\n");
		assertEquals(2, item.getItemID());
		assertEquals("High-Quality Herb", item.getField("Name"));
		assertEquals(
				"[Item] Target:Ally　Effect:300 HP Recovery\r\nMedicinal herb that recovers 300 of an ally's HP.",
				item.getField("Description"));
	}

	@Test
	public void testItemTypeRetrieved() throws IOException {
		Item item = new Item();

		item.setContent("Item 1\n Name \"Dummy Item\"\n\n");
		assertEquals("Item", item.getItemType());

		item.setContent("Weapon 1\n Name \"Dummy Weapon\"\n\n");
		assertEquals("Weapon", item.getItemType());

		item.setContent("Armor 1\n Name \"Dummy Armor\"\n\n");
		assertEquals("Armor", item.getItemType());
	}

	@Test
	public void testItemsFitsWithBOM() throws IOException {
		Items items = new Items();
		items.setContent("\uFEFFItem 1\n Name \"Herb\"\n\n");
	}

	@Test
	public void testItemsParseFullFile() throws IOException {
		File file = new File(testFolder, "Items.txt");
		Items items = new Items();
		items.setContent(FileUtils.readFileToString(file));
		assertEquals(9, items.size());
	}

}

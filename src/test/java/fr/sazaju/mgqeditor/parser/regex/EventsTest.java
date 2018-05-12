package fr.sazaju.mgqeditor.parser.regex;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import fr.sazaju.mgqeditor.parser.regex.Events.ChoiceLine;
import fr.sazaju.mgqeditor.parser.regex.Events.CommentLine;
import fr.sazaju.mgqeditor.parser.regex.Events.EmptyLine;
import fr.sazaju.mgqeditor.parser.regex.Events.Event;
import fr.sazaju.mgqeditor.parser.regex.Events.EventContentLine;
import fr.sazaju.mgqeditor.parser.regex.Events.Function355And655Line;
import fr.sazaju.mgqeditor.parser.regex.Events.FunctionLine;
import fr.sazaju.mgqeditor.parser.regex.Events.MessageLine;
import fr.sazaju.mgqeditor.parser.regex.Events.PageLine;
import fr.sazaju.mgqeditor.parser.regex.Events.StartLine;
import fr.vergne.parsing.layer.exception.ParsingException;

public class EventsTest {

	private static final String CHARSET = "UTF8";
	private final File testFolder = new File("src/test/resources");

	/* START LINE */

	@Test
	public void testStartLineAcceptsCorrectPattern() {
		StartLine line = new StartLine();
		line.setContent("EVENT 1");
		line.setContent("EVENT    10");
		line.setContent("   EVENT    2");
	}

	/* PAGE LINE */

	@Test
	public void testPageLineAcceptsCorrectPattern() {
		PageLine line = new PageLine();
		line.setContent("PAGE 1");
		line.setContent("PAGE    10");
		line.setContent("   PAGE    2");
	}

	/* COMMENT */

	@Test
	public void testCommentLineAcceptsCorrectPattern() {
		CommentLine line = new CommentLine();
		line.setContent("// condition: switch 2016 is ON");
		line.setContent("    // condition: switch 2016 is ON");
		line.setContent("    //");
	}

	/* MESSAGE LINE */

	@Test
	public void testMessageLineAcceptsCorrectPattern() {
		MessageLine line = new MessageLine();
		line.setContent("ShowMessage(\"abcd\")");
		line.setContent("    ShowMessage(\"abcd\")");
		line.setContent("    ShowMessage(\"\")");
	}

	@Test
	public void testMessageLineReturnsCorrectMessage() {
		MessageLine line = new MessageLine();
		line.setContent("ShowMessage(\"abcd\")");
		assertEquals("abcd", line.getMessage());
		line.setContent("    ShowMessage(\"abcd\")");
		assertEquals("abcd", line.getMessage());
		line.setContent("    ShowMessage(\"\")");
		assertEquals("", line.getMessage());
	}

	@Test
	public void testMessageLineSetMessageChangesMessageCorrectly() {
		MessageLine line = new MessageLine();
		line.setContent("    ShowMessage(\"\")");
		line.setMessage("test");
		assertEquals("test", line.getMessage());
	}

	@Test
	public void testMessageLineSetMessageChangesContentCorrectly() {
		MessageLine line = new MessageLine();
		line.setContent("    ShowMessage(\"\")");
		line.setMessage("test");
		assertEquals("    ShowMessage(\"test\")", line.getContent());
	}

	/* CHOICE LINE */

	@Test
	public void testChoiceLineAcceptsCorrectPattern() {
		ChoiceLine line = new ChoiceLine();
		line.setContent("ShowChoices(strings(\"A\",\"B\"),2)");
		line.setContent("    ShowChoices(strings(\"A\",\"B\"),2)");
	}

	@Test
	public void testChoiceLineReturnsCorrectSize() {
		ChoiceLine line = new ChoiceLine();
		line.setContent("ShowChoices(strings(\"A\",\"B\"),2)");
		assertEquals(2, line.size());
		line.setContent("ShowChoices(strings(\"A\",\"B\",\"C\"),2)");
		assertEquals(3, line.size());
	}

	@Test
	public void testChoiceLineReturnsCorrectChoiceThroughIndex() {
		ChoiceLine line = new ChoiceLine();
		line.setContent("ShowChoices(strings(\"A\",\"B\",\"C\"),2)");
		assertEquals("A", line.getChoice(0));
		assertEquals("B", line.getChoice(1));
		assertEquals("C", line.getChoice(2));
	}

	@Test
	public void testChoiceLineSetChoiceChangesChoiceCorrectly() {
		ChoiceLine line = new ChoiceLine();
		line.setContent("ShowChoices(strings(\"A\",\"B\",\"C\"),2)");
		line.setChoice(0, "1");
		assertEquals("1", line.getChoice(0));
		line.setChoice(1, "2");
		assertEquals("2", line.getChoice(1));
		line.setChoice(2, "3");
		assertEquals("3", line.getChoice(2));
	}

	@Test
	public void testChoiceLineSetChoiceChangesContentCorrectly() {
		ChoiceLine line = new ChoiceLine();
		line.setContent("ShowChoices(strings(\"A\",\"B\",\"C\"),2)");
		line.setChoice(0, "1");
		line.setChoice(1, "2");
		line.setChoice(2, "3");
		assertEquals("ShowChoices(strings(\"1\",\"2\",\"3\"),2)", line.getContent());
	}

	@Test
	public void testChoiceLineReturnsCorrectChoices() {
		ChoiceLine line = new ChoiceLine();
		line.setContent("ShowChoices(strings(\"A\",\"B\",\"C\"),2)");
		assertEquals(Arrays.asList("A", "B", "C"), line.getChoices());
	}

	@Test
	public void testChoiceLineSetChoicesChangesChoicesCorrectly() {
		ChoiceLine line = new ChoiceLine();
		line.setContent("ShowChoices(strings(\"A\",\"B\",\"C\"),2)");
		line.setChoices(Arrays.asList("1", "2", "3"));
		assertEquals("1", line.getChoice(0));
		assertEquals("2", line.getChoice(1));
		assertEquals("3", line.getChoice(2));
	}

	@Test
	public void testChoiceLineSetChoicesChangesContentCorrectly() {
		ChoiceLine line = new ChoiceLine();
		line.setContent("ShowChoices(strings(\"A\",\"B\",\"C\"),2)");
		line.setChoices(Arrays.asList("1", "2", "3"));
		assertEquals("ShowChoices(strings(\"1\",\"2\",\"3\"),2)", line.getContent());
	}

	/* 355() + 655() */

	@Test
	public void testFunction355And655LineAcceptsCorrectPattern() {
		Function355And655Line line = new Function355And655Line();
		line.setContent("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")\n"
				+ "   655(\"\\\"Add to party\\\",\")\n" + "   655(\"\\\"View present list\\\",\\\"Request\\\",\")\n"
				+ "   655(\"\\\"Never mind\\\"])\")");
		line.setContent("355(\"ex_choice_add(\\\"Change difficulty\\\",1)\")\n"
				+ "655(\"ex_choice_add(\\\"Challenge to a fight\\\",2)\")\n"
				+ "655(\"ex_choice_add(\\\"New Game+\\\",3,\")\n" + "655(\"\\\"v[1001]>18\\\",0)\")\n"
				+ "655(\"ex_choice_add(\\\"Remove male characters from companions\\\",4)\")\n"
				+ "655(\"ex_choice_add(\\\"Never mind\\\",5)\")");
	}

	@Test
	public void testFunction355And655LineReturnsCorrectSize() {
		Function355And655Line line = new Function355And655Line();
		line.setContent("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")\n"
				+ "   655(\"\\\"Add to party\\\",\")\n" + "   655(\"\\\"View present list\\\",\\\"Request\\\",\")\n"
				+ "   655(\"\\\"Never mind\\\"])\")");

		assertEquals(5, line.size());
	}

	@Test
	public void testFunction355And655LineReturnsCorrectFunctionString() {
		Function355And655Line line = new Function355And655Line();
		line.setContent("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")\n"
				+ "   655(\"\\\"Add to party\\\",\")\n" + "   655(\"\\\"View present list\\\",\\\"Request\\\",\")\n"
				+ "   655(\"\\\"Never mind\\\"])\")");

		assertEquals("Give presents", line.getFunctionString(0));
		assertEquals("Add to party", line.getFunctionString(1));
		assertEquals("View present list", line.getFunctionString(2));
		assertEquals("Request", line.getFunctionString(3));
		assertEquals("Never mind", line.getFunctionString(4));
	}

	@Test
	public void testFunction355And655LineReturnsCorrectFunctionStrings() {
		Function355And655Line line = new Function355And655Line();
		line.setContent("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")\n"
				+ "   655(\"\\\"Add to party\\\",\")\n" + "   655(\"\\\"View present list\\\",\\\"Request\\\",\")\n"
				+ "   655(\"\\\"Never mind\\\"])\")");

		List<String> list = line.getFunctionStrings();
		assertEquals("" + list, 5, list.size());
		assertEquals("Give presents", list.get(0));
		assertEquals("Add to party", list.get(1));
		assertEquals("View present list", list.get(2));
		assertEquals("Request", list.get(3));
		assertEquals("Never mind", list.get(4));
	}
	
	@Test
	public void testFunction355And655LineReturnsCorrectContentSize() {
		Function355And655Line line = new Function355And655Line();
		line.setContent("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")\n"
				+ "   655(\"\\\"Add to party\\\",\")\n" + "   655(\"\\\"View present list\\\",\\\"Request\\\",\")\n"
				+ "   655(\"\\\"Never mind\\\"])\")");

		assertEquals(4, line.getLinesCount());
	}

	@Test
	public void testFunction355And655LineReturnsCorrectContentLine() {
		Function355And655Line line = new Function355And655Line();
		line.setContent("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")\n"
				+ "   655(\"\\\"Add to party\\\",\")\n" + "   655(\"\\\"View present list\\\",\\\"Request\\\",\")\n"
				+ "   655(\"\\\"Never mind\\\"])\")");

		assertEquals("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")", line.getLineContent(0));
		assertEquals("   655(\"\\\"Add to party\\\",\")", line.getLineContent(1));
		assertEquals("   655(\"\\\"View present list\\\",\\\"Request\\\",\")", line.getLineContent(2));
		assertEquals("   655(\"\\\"Never mind\\\"])\")", line.getLineContent(3));
	}

	@Test
	public void testFunction355And655LineThrowsExceptionOnTooLowLineIndex() {
		Function355And655Line line = new Function355And655Line();
		line.setContent("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")\n"
				+ "   655(\"\\\"Add to party\\\",\")\n" + "   655(\"\\\"View present list\\\",\\\"Request\\\",\")\n"
				+ "   655(\"\\\"Never mind\\\"])\")");

		try {
			line.getLineContent(-1);
			fail("No exception thrown");
		} catch (IndexOutOfBoundsException e) {
			// OK
		}
	}
	
	@Test
	public void testFunction355And655LineThrowsExceptionOnTooHighLineIndex() {
		Function355And655Line line = new Function355And655Line();
		line.setContent("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")\n"
				+ "   655(\"\\\"Add to party\\\",\")\n" + "   655(\"\\\"View present list\\\",\\\"Request\\\",\")\n"
				+ "   655(\"\\\"Never mind\\\"])\")");

		try {
			line.getLineContent(4);
			fail("No exception thrown");
		} catch (IndexOutOfBoundsException e) {
			// OK
		}
	}

	@Test
	public void testFunction355And655LineUpdateFunctionStringCorrectly() {
		Function355And655Line line = new Function355And655Line();
		line.setContent("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")\n"
				+ "   655(\"\\\"Add to party\\\",\")\n" + "   655(\"\\\"View present list\\\",\\\"Request\\\",\")\n"
				+ "   655(\"\\\"Never mind\\\"])\")");

		line.setFunctionString(0, "test 1");
		line.setFunctionString(1, "test 2");
		line.setFunctionString(2, "test 3");
		line.setFunctionString(3, "test 4");
		line.setFunctionString(4, "test 5");

		assertEquals("test 1", line.getFunctionString(0));
		assertEquals("test 2", line.getFunctionString(1));
		assertEquals("test 3", line.getFunctionString(2));
		assertEquals("test 4", line.getFunctionString(3));
		assertEquals("test 5", line.getFunctionString(4));

		assertEquals(
				"   355(\"unlimited_choices(11, [\\\"test 1\\\",\")\n" + "   655(\"\\\"test 2\\\",\")\n"
						+ "   655(\"\\\"test 3\\\",\\\"test 4\\\",\")\n" + "   655(\"\\\"test 5\\\"])\")",
				line.getContent());
	}

	@Test
	public void testFunction355And655LineUpdateAllFunctionStringsCorrectly() {
		Function355And655Line line = new Function355And655Line();
		line.setContent("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")\n"
				+ "   655(\"\\\"Add to party\\\",\")\n" + "   655(\"\\\"View present list\\\",\\\"Request\\\",\")\n"
				+ "   655(\"\\\"Never mind\\\"])\")");

		line.setFunctionStrings(Arrays.asList("test 1", "test 2", "test 3", "test 4", "test 5"));

		assertEquals("test 1", line.getFunctionString(0));
		assertEquals("test 2", line.getFunctionString(1));
		assertEquals("test 3", line.getFunctionString(2));
		assertEquals("test 4", line.getFunctionString(3));
		assertEquals("test 5", line.getFunctionString(4));

		assertEquals(
				"   355(\"unlimited_choices(11, [\\\"test 1\\\",\")\n" + "   655(\"\\\"test 2\\\",\")\n"
						+ "   655(\"\\\"test 3\\\",\\\"test 4\\\",\")\n" + "   655(\"\\\"test 5\\\"])\")",
				line.getContent());
	}

	@Test
	public void testFunction355And655LineThrowsExceptionOnGettingTooLowIndex() {
		Function355And655Line line = new Function355And655Line();
		line.setContent("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")\n"
				+ "   655(\"\\\"Add to party\\\",\")\n" + "   655(\"\\\"View present list\\\",\\\"Request\\\",\")\n"
				+ "   655(\"\\\"Never mind\\\"])\")");

		try {
			line.getFunctionString(-1);
			fail("No exception thrown");
		} catch (IndexOutOfBoundsException e) {
			// OK
		}
	}

	@Test
	public void testFunction355And655LineThrowsExceptionOnGettingTooHighIndex() {
		Function355And655Line line = new Function355And655Line();
		line.setContent("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")\n"
				+ "   655(\"\\\"Add to party\\\",\")\n" + "   655(\"\\\"View present list\\\",\\\"Request\\\",\")\n"
				+ "   655(\"\\\"Never mind\\\"])\")");

		try {
			line.getFunctionString(5);
			fail("No exception thrown");
		} catch (IndexOutOfBoundsException e) {
			// OK
		}
	}

	@Test
	public void testFunction355And655LineThrowsExceptionOnSettingTooLowIndex() {
		Function355And655Line line = new Function355And655Line();
		line.setContent("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")\n"
				+ "   655(\"\\\"Add to party\\\",\")\n" + "   655(\"\\\"View present list\\\",\\\"Request\\\",\")\n"
				+ "   655(\"\\\"Never mind\\\"])\")");

		try {
			line.setFunctionString(-1, "test");
			fail("No exception thrown");
		} catch (IndexOutOfBoundsException e) {
			// OK
		}
	}

	@Test
	public void testFunction355And655LineThrowsExceptionOnSettingTooHighIndex() {
		Function355And655Line line = new Function355And655Line();
		line.setContent("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")\n"
				+ "   655(\"\\\"Add to party\\\",\")\n" + "   655(\"\\\"View present list\\\",\\\"Request\\\",\")\n"
				+ "   655(\"\\\"Never mind\\\"])\")");

		try {
			line.setFunctionString(5, "test");
			fail("No exception thrown");
		} catch (IndexOutOfBoundsException e) {
			// OK
		}
	}

	@Test
	public void testFunction355And655LineThrowsExceptionOnSettingNullFunctionString() {
		Function355And655Line line = new Function355And655Line();
		line.setContent("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")\n"
				+ "   655(\"\\\"Add to party\\\",\")\n" + "   655(\"\\\"View present list\\\",\\\"Request\\\",\")\n"
				+ "   655(\"\\\"Never mind\\\"])\")");

		try {
			line.setFunctionString(1, null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			// OK
		}
	}

	@Test
	public void testFunction355And655LineThrowsExceptionOnSettingNullList() {
		Function355And655Line line = new Function355And655Line();
		line.setContent("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")\n"
				+ "   655(\"\\\"Add to party\\\",\")\n" + "   655(\"\\\"View present list\\\",\\\"Request\\\",\")\n"
				+ "   655(\"\\\"Never mind\\\"])\")");

		try {
			line.setFunctionStrings(null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			// OK
		}
	}

	@Test
	public void testFunction355And655LineThrowsExceptionOnSettingListOfWrongSize() {
		Function355And655Line line = new Function355And655Line();
		line.setContent("   355(\"unlimited_choices(11, [\\\"Give presents\\\",\")\n"
				+ "   655(\"\\\"Add to party\\\",\")\n" + "   655(\"\\\"View present list\\\",\\\"Request\\\",\")\n"
				+ "   655(\"\\\"Never mind\\\"])\")");

		try {
			line.setFunctionStrings(Arrays.asList("test 1", "test 2"));
			fail("No exception thrown");
		} catch (IllegalArgumentException e) {
			// OK
		}
	}

	/* EMPTY */

	@Test
	public void testEmptyLineAcceptsCorrectPattern() {
		EmptyLine line = new EmptyLine();
		line.setContent("");
		line.setContent("    ");
	}

	/* FUNCTION */

	@Test
	public void testFunctionLineAcceptsCorrectPattern() {
		FunctionLine line = new FunctionLine();
		line.setContent("If(0,2001,0)");
		line.setContent("If(2,\"A\",1)");
		line.setContent("Else()");
		line.setContent("EndIf()");
		line.setContent("DefineLabel(\"買い物\")");
		line.setContent("JumpToLabel(\"買い物\")");
		line.setContent("TeleportPlayer(0,2,296,355,0,0)");
		line.setContent("TeleportEvent(56,0,1,20,0)");
		line.setContent("ChangeInventory_Item(503,0,0,1)");
		line.setContent("ChangeInventoryWeapon(1,0,0,1)");
		line.setContent("ChangeInventoryWeapon(362,0,0,1,false)");
		line.setContent("ChangeInventoryArmor(279,0,0,1)");
		line.setContent("ChangeInventoryArmor(176,0,0,1,false)");
		line.setContent("ChangeInventoryWeapon(1,0,0,1)");
		line.setContent("ChangeInventoryWeapon(1,0,0,1)");
		line.setContent("PictureDisplay(5,\"80_sonia_m1_st01\",0,0,0,0,100,100,0,0)");
		line.setContent("PictureDisplay(6,\"80_sonia_m1_st01\",0,0,-170,0,100,100,255,0)");
		line.setContent("PictureMove(5,null,0,0,0,0,100,100,0,0,30,true)");
		line.setContent("PictureClear(6)");
		line.setContent("0()");
		line.setContent("404()");
		line.setContent("355(\"actor_label_jump\")");
		line.setContent("355(\"unlimited_choices(11, [\\\"Inn\\\",\")");
		line.setContent("655(\"\\\"Weapon Shop\\\",\\\"Armor Shop\\\",\")");
		line.setContent("605(0,321,0,0)");
		line.setContent(
				"250(bytes(0x04,0x08,0x6f,0x3a,0x0c,0x52,0x50,0x47,0x3a,0x3a,0x53,0x45,0x08,0x3a,0x0a,0x40,0x6e,0x61,0x6d,0x65,0x49,0x22,0x09,0x4d,0x6f,0x76,0x65,0x06,0x3a,0x06,0x45,0x54,0x3a,0x0b,0x40,0x70,0x69,0x74,0x63,0x68,0x69,0x69,0x3a,0x0c,0x40,0x76,0x6f,0x6c,0x75,0x6d,0x65,0x69,0x55))");
		line.setContent(
				"205(0,bytes(0x04,0x08,0x6f,0x3a,0x13,0x52,0x50,0x47,0x3a,0x3a,0x4d,0x6f,0x76,0x65,0x52,0x6f,0x75,0x74,0x65,0x09,0x3a,0x0c,0x40,0x72,0x65,0x70,0x65,0x61,0x74,0x46,0x3a,0x0f,0x40,0x73,0x6b,0x69,0x70,0x70,0x61,0x62,0x6c,0x65,0x46,0x3a,0x0a,0x40,0x77,0x61,0x69,0x74,0x54,0x3a,0x0a,0x40,0x6c,0x69,0x73,0x74,0x5b,0x0c,0x6f,0x3a,0x15,0x52,0x50,0x47,0x3a,0x3a,0x4d,0x6f,0x76,0x65,0x43,0x6f,0x6d,0x6d,0x61,0x6e,0x64,0x07,0x3a,0x0a,0x40,0x63,0x6f,0x64,0x65,0x69,0x16,0x3a,0x10,0x40,0x70,0x61,0x72,0x61,0x6d,0x65,0x74,0x65,0x72,0x73,0x5b,0x00,0x6f,0x3b,0x0a,0x07,0x3b,0x0b,0x69,0x14,0x3b,0x0c,0x5b,0x06,0x69,0x08,0x6f,0x3b,0x0a,0x07,0x3b,0x0b,0x69,0x17,0x3b,0x0c,0x5b,0x00,0x6f,0x3b,0x0a,0x07,0x3b,0x0b,0x69,0x14,0x3b,0x0c,0x5b,0x06,0x69,0x08,0x6f,0x3b,0x0a,0x07,0x3b,0x0b,0x69,0x18,0x3b,0x0c,0x5b,0x00,0x6f,0x3b,0x0a,0x07,0x3b,0x0b,0x69,0x2a,0x3b,0x0c,0x5b,0x00,0x6f,0x3b,0x0a,0x07,0x3b,0x0b,0x69,0x00,0x3b,0x0c,0x5b,0x00))");
		line.setContent(
				"223(bytes(0x04,0x08,0x75,0x3a,0x09,0x54,0x6f,0x6e,0x65,0x25,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00),30,true)");
		line.setContent("IfPlayerPicksChoice(1,null)");
		line.setContent("EndEventProcessing()");
		line.setContent("ShowMessageFace(\"alice_fc5\",1,0,2,6)");
		line.setContent("ShowMessageFace(\"\",0,0,2,5)");
		line.setContent("ShowMessage(\"Let's go!\")");
		line.setContent("ShowChoices(strings(\"We will go\",\"We'll leave it to you\"),2)");
		line.setContent("RunCommonEvent(303)");
		line.setContent("Wait(30)");
		line.setContent("ScrollMap(2,4,4)");
		line.setContent("ShowSpeechBalloon(-1,8,true)");
		line.setContent("ChangeVariable(1002,1002,0,0,3)");
		line.setContent("ChangeSwitch(2004,2004,1)");
		line.setContent("ChangeSelfSwitch(\"A\",0)");
		line.setContent("Shop(0,306,0,0,true)");
		line.setContent("ChangeSelfSwitch(\"A\",1)");
		line.setContent("236(symbol\"storm\",9,60,true)");
	}
	
	@Test
	public void testFunctionLineRejectsUnwantedPatterns() {
		FunctionLine line = new FunctionLine();
		
		try {
			line.setContent("ChangeSelfSwitch(binary\"A\",1)");
			fail("No exception thrown");
		} catch (ParsingException e) {
			// OK
		}
	}

	@Test
	public void testFunctionLineRejectsNewlineBefore() {
		FunctionLine line = new FunctionLine();
		String content = "ShowMessageFace(\"alice_fc5\",1,0,2,6)";

		line.setContent(content);
		try {
			line.setContent("\n" + content);
			fail("No exception thrown");
		} catch (ParsingException e) {
			// OK
		}
	}

	@Test
	public void testFunctionLineRejectsNewlineAfter() {
		FunctionLine line = new FunctionLine();
		String content = "ShowMessageFace(\"alice_fc5\",1,0,2,6)";

		line.setContent(content);
		try {
			line.setContent(content + "\n");
			fail("No exception thrown");
		} catch (ParsingException e) {
			// OK
		}
	}

	@Test
	public void testFunctionLineRejectsMultipleLines() {
		FunctionLine line = new FunctionLine();
		String content1 = "ShowMessageFace(\"alice_fc5\",1,0,2,6)";
		String content2 = "ShowMessage(\"test\")";

		line.setContent(content1);
		line.setContent(content2);
		try {
			line.setContent(content1 + "\n" + content2);
			fail("No exception thrown");
		} catch (ParsingException e) {
			// OK
		}
	}

	/* EVENT */

	@Test
	public void testEventParsesSuccessfully() throws IOException {
		new Event().setContent(FileUtils.readFileToString(new File(testFolder, "Event1.txt"), CHARSET));
		new Event().setContent(FileUtils.readFileToString(new File(testFolder, "Event2.txt"), CHARSET));
		new Event().setContent(FileUtils.readFileToString(new File(testFolder, "Event3.txt"), CHARSET));
		new Event().setContent(FileUtils.readFileToString(new File(testFolder, "Event4.txt"), CHARSET));
		new Event().setContent(FileUtils.readFileToString(new File(testFolder, "Event5.txt"), CHARSET));
	}

	@Test
	public void testEventIDProperlyRetrieved() throws IOException {
		Event event = new Event();
		event.setContent(FileUtils.readFileToString(new File(testFolder, "Event1.txt"), CHARSET));
		assertEquals(12, event.getEventID());
	}

	@Test
	public void testEventSizeProperlyCountAllButFirstLine() throws IOException {
		Event event = new Event();
		event.setContent(FileUtils.readFileToString(new File(testFolder, "Event1.txt"), CHARSET));
		assertEquals(11, event.size());
	}

	@Test
	public void testEventProvidesAsMuchLinesThanSize() throws IOException {
		Event event = new Event();
		event.setContent(FileUtils.readFileToString(new File(testFolder, "Event1.txt"), CHARSET));
		int count = 0;
		for (@SuppressWarnings("unused")
		EventContentLine line : event) {
			count++;
		}
		assertEquals(event.size(), count);
	}

	@Test
	public void testEventIteratorProvidesCorrectLines() throws IOException {
		Event event = new Event();
		event.setContent(FileUtils.readFileToString(new File(testFolder, "Event1.txt"), CHARSET));
		Iterator<EventContentLine> iterator = event.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(" PAGE   1", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("  ShowMessageFace(\"\",0,0,2,1)", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("  ShowMessage(\"-Lost Woods-\")", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("  ShowMessage(\"Extremely Dangerous\")", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("  ShowMessage(\"Novice adventurers DO NOT ENTER\")", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("  If(0,6,0)", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("   ShowMessageFace(\"sonia_fc2\",2,0,2,2)", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals(
				"   ShowMessage(\"\\\\n<Sonya>I was always told never to step foot into this forest. If we don't have any business inside, perhaps we'd better not enter...\")",
				iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("   0()", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("  EndIf()", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("  0()", iterator.next().getContent());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testEventGetLineCompliesWithIterator() throws IOException {
		Event event = new Event();
		event.setContent(FileUtils.readFileToString(new File(testFolder, "Event1.txt"), CHARSET));
		int index = 0;
		for (EventContentLine line : event) {
			assertEquals(line, event.getLine(index));
			index++;
		}
	}

	@Test
	public void testEventRemoveLineProvidesCorrectLine() throws IOException {
		Event event = new Event();
		event.setContent(FileUtils.readFileToString(new File(testFolder, "Event1.txt"), CHARSET));
		assertEquals("  ShowMessage(\"-Lost Woods-\")", event.removeLine(2).getContent());
	}

	@Test
	public void testEventRemoveLineProperlyReducesEvent() throws IOException {
		Event event = new Event();
		event.setContent(FileUtils.readFileToString(new File(testFolder, "Event1.txt"), CHARSET));
		EventContentLine removedLine = event.removeLine(2);
		for (EventContentLine line : event) {
			assertFalse(line.equals(removedLine));
		}
		assertEquals(10, event.size());
	}

	@Test
	public void testEventAddLineProperlyIncreasesEvent() throws IOException {
		Event event = new Event();
		event.setContent(FileUtils.readFileToString(new File(testFolder, "Event1.txt"), CHARSET));
		EventContentLine addedLine = new EventContentLine();
		addedLine.setContent(" // test");
		event.addLine(5, addedLine);
		assertEquals(addedLine, event.getLine(5));
		assertEquals(12, event.size());
	}

	@Test
	public void testEventsParsesSuccessfully() throws IOException {
		new Events().setContent(FileUtils.readFileToString(new File(testFolder, "Events1.txt"), CHARSET));
		new Events().setContent(FileUtils.readFileToString(new File(testFolder, "Events2.txt"), CHARSET));
	}

	@Test
	public void testEventsRetrieveCorrectNumberOfEvents() throws IOException {
		Events events = new Events();
		events.setContent(FileUtils.readFileToString(new File(testFolder, "Events2.txt"), CHARSET));
		assertEquals(5, events.size());
	}

	@Test
	public void testEventsRetrieveCorrectEvents() throws IOException {
		Events events = new Events();
		events.setContent(FileUtils.readFileToString(new File(testFolder, "Events2.txt"), CHARSET));
		Iterator<Event> iterator = events.getEvents().iterator();
		assertTrue(iterator.hasNext());
		assertEquals(1, iterator.next().getEventID());
		assertTrue(iterator.hasNext());
		assertEquals(2, iterator.next().getEventID());
		assertTrue(iterator.hasNext());
		assertEquals(18, iterator.next().getEventID());
		assertTrue(iterator.hasNext());
		assertEquals(22, iterator.next().getEventID());
		assertTrue(iterator.hasNext());
		assertEquals(25, iterator.next().getEventID());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testEventsSortingImpactIterator() throws IOException {
		Events events = new Events();
		events.setContent(FileUtils.readFileToString(new File(testFolder, "Events2.txt"), CHARSET));
		events.sort((ev1, ev2) -> -Integer.compare(ev1.getEventID(), ev2.getEventID()));
		Iterator<Event> iterator = events.getEvents().iterator();
		assertTrue(iterator.hasNext());
		assertEquals(25, iterator.next().getEventID());
		assertTrue(iterator.hasNext());
		assertEquals(22, iterator.next().getEventID());
		assertTrue(iterator.hasNext());
		assertEquals(18, iterator.next().getEventID());
		assertTrue(iterator.hasNext());
		assertEquals(2, iterator.next().getEventID());
		assertTrue(iterator.hasNext());
		assertEquals(1, iterator.next().getEventID());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testEventsSortingImpactContent() throws IOException {
		Events events = new Events();
		events.setContent(FileUtils.readFileToString(new File(testFolder, "Events2.txt"), CHARSET));
		events.sort((ev1, ev2) -> -Integer.compare(ev1.getEventID(), ev2.getEventID()));
		String content = events.getContent();
		assertTrue(content.indexOf("\nEVENT   2\n") < content.indexOf("\nEVENT   1\n"));
		assertTrue(content.indexOf("\nEVENT   18\n") < content.indexOf("\nEVENT   2\n"));
		assertTrue(content.indexOf("\nEVENT   22\n") < content.indexOf("\nEVENT   18\n"));
		assertTrue(content.indexOf("\nEVENT   25\n") < content.indexOf("\nEVENT   22\n"));
	}

	@Test
	public void testEventsRetrieveCorrectEventThroughID() throws IOException {
		Events events = new Events();
		events.setContent(FileUtils.readFileToString(new File(testFolder, "Events2.txt"), CHARSET));
		assertEquals(1, events.getEvent(1).getEventID());
		assertEquals(2, events.getEvent(2).getEventID());
		assertEquals(18, events.getEvent(18).getEventID());
		assertEquals(22, events.getEvent(22).getEventID());
		assertEquals(25, events.getEvent(25).getEventID());
	}

	@Test
	public void testEventsSizeIsCorrect() throws IOException {
		Events events = new Events();
		events.setContent(FileUtils.readFileToString(new File(testFolder, "Events2.txt"), CHARSET));
		assertEquals(5, events.size());
	}

	@Test
	public void testEventsRetrieveAllSentenceIDs() throws IOException {
		Events events = new Events();
		events.setContent(FileUtils.readFileToString(new File(testFolder, "Events2.txt"), CHARSET));
		Iterator<Integer> iterator = events.iterator();
		for (int i = 0; i < 52; i++) {
			assertTrue("Index " + i, iterator.hasNext());
			iterator.next();
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testEventsRetrieveCorrectSentenceThroughID() throws IOException {
		Events events = new Events();
		events.setContent(FileUtils.readFileToString(new File(testFolder, "Events2.txt"), CHARSET));
		assertEquals("The chest is locked.", events.getSentence(0).getContent());
		assertEquals(
				"\\\\n<Salaan>According to the information I have gathered, yes. But all we know is that a monster is behind it...",
				events.getSentence(20).getContent());
		assertEquals(
				"\\\\n<Salaan>Meet with Sphinx and ask her if it's possible to return Her Majesty back to normal. Please, I leave this task in your hands.",
				events.getSentence(51).getContent());
	}

}

package fr.sazaju.mgqeditor.parser.regex;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import fr.sazaju.mgqeditor.parser.regex.Scripts.ArrayEntry;
import fr.sazaju.mgqeditor.parser.regex.Scripts.Attack;
import fr.sazaju.mgqeditor.parser.regex.Scripts.AttackIDs;
import fr.sazaju.mgqeditor.parser.regex.Scripts.Blank;
import fr.sazaju.mgqeditor.parser.regex.Scripts.Monster;
import fr.sazaju.mgqeditor.parser.regex.Scripts.ScriptSentence;
import fr.vergne.ioutils.FileUtils;

public class ScriptsTest {

	private final File testFolder = new File("src/test/resources");

	@Test
	public void testBlankFitsContent() {
		Blank blank = new Scripts.Blank();
		blank.setContent("\n      ");
	}

	@Test
	public void testArrayEntryFitsContent() {
		ArrayEntry arrayEntry = new Scripts.ArrayEntry();
		arrayEntry
				.setContent(":word_1 => [\"【スキュラ】\\nほぉら、触手を巻き付かせてあげるわ……\", \"scylla_fc1\", 0],");
	}

	@Test
	public void testArrayEntryProvidesID() {
		ArrayEntry arrayEntry = new Scripts.ArrayEntry();
		arrayEntry
				.setContent(":word_1 => [\"【スキュラ】\\nほぉら、触手を巻き付かせてあげるわ……\", \"scylla_fc1\", 0],");

		assertEquals("word_1", arrayEntry.getArrayEntryID());
	}

	@Test
	public void testSentenceProvidesMessage() {
		ArrayEntry arrayEntry = new Scripts.ArrayEntry();
		{
			arrayEntry
					.setContent(":word_1 => [\"【スキュラ】\\nほぉら、触手を巻き付かせてあげるわ……\", \"scylla_fc1\", 0],");
			ScriptSentence sentence = new ScriptSentence(arrayEntry);
			assertEquals("【スキュラ】\\nほぉら、触手を巻き付かせてあげるわ……", sentence.getContent());
		}

		{
			arrayEntry
					.setContent(":word_1 => [\"【ラティ】\\nこのパン、すごい！\\nむしゃむしゃ、むしゃむしゃ……\", \"nezumi_fc1\",1],");
			ScriptSentence sentence = new ScriptSentence(arrayEntry);
			assertEquals("【ラティ】\\nこのパン、すごい！\\nむしゃむしゃ、むしゃむしゃ……",
					sentence.getContent());
		}
	}

	@Test
	public void testAttackIDsFitsContent() {
		AttackIDs attackIDs = new Scripts.AttackIDs();
		attackIDs.setContent("3520");
		attackIDs.setContent("[3520]");
		attackIDs.setContent("[3520,3523]");
		attackIDs.setContent("3520..3526");
	}

	@Test
	public void testAttackIDsProvideIDs() {
		AttackIDs attackIDs = new Scripts.AttackIDs();

		{
			attackIDs.setContent("[3520]");
			List<Integer> actual = new LinkedList<>();
			for (Integer id : attackIDs) {
				actual.add(id);
			}
			List<Integer> expected = Arrays.asList(3520);
			assertTrue("There is missing IDs: " + actual,
					actual.containsAll(expected));
			assertTrue("There is extra IDs: " + actual,
					expected.containsAll(actual));
		}
		{
			attackIDs.setContent("[3520,3523]");
			List<Integer> actual = new LinkedList<>();
			for (Integer id : attackIDs) {
				actual.add(id);
			}
			List<Integer> expected = Arrays.asList(3520, 3523);
			assertTrue("There is missing IDs: " + actual,
					actual.containsAll(expected));
			assertTrue("There is extra IDs: " + actual,
					expected.containsAll(actual));
		}
		{
			attackIDs.setContent("3520..3526");
			List<Integer> actual = new LinkedList<>();
			for (Integer id : attackIDs) {
				actual.add(id);
			}
			List<Integer> expected = Arrays.asList(3520, 3521, 3522, 3523,
					3524, 3525, 3526);
			assertTrue("There is missing IDs: " + actual,
					actual.containsAll(expected));
			assertTrue("There is extra IDs: " + actual,
					expected.containsAll(actual));
		}
	}

	@Test
	public void testAttackIDsProvideCorrectMainID() {
		AttackIDs attackIDs = new Scripts.AttackIDs();

		{
			attackIDs.setContent("[3520]");
			assertEquals(3520, attackIDs.getMainID());
		}
		{
			attackIDs.setContent("[3520,3523]");
			assertEquals(3520, attackIDs.getMainID());
		}
		{
			attackIDs.setContent("3520..3526");
			assertEquals(3520, attackIDs.getMainID());
		}
	}

	@Test
	public void testAttackFitsContent() {
		Attack attack = new Scripts.Attack();
		attack.setContent("[3520] => { # 小悪魔パイズリ\n        :word_1 => [\"【スキュラ】\\n邪魔よ、あなた達……！\", \"scylla_fc1\", 0],\n        :word_2 => [\"【スキュラ】\\n私の触手で薙ぎ払ってあげるわ！\", \"scylla_fc1\", 0],\n        :word_3 => [\"【スキュラ】\\nこの触手、見切れるかしら……？\", \"scylla_fc1\", 0],\n      },");
	}

	@Test
	public void testAttackProvidesArrayEntrys() {
		Attack attack = new Scripts.Attack();
		attack.setContent("[3520] => { # 小悪魔パイズリ\n        :word_1 => [\"【スキュラ】\\n邪魔よ、あなた達……！\", \"scylla_fc1\", 0],\n        :word_2 => [\"【スキュラ】\\n私の触手で薙ぎ払ってあげるわ！\", \"scylla_fc1\", 0],\n        :word_3 => [\"【スキュラ】\\nこの触手、見切れるかしら……？\", \"scylla_fc1\", 0],\n      },");

		Iterator<ArrayEntry> iterator = attack.iterator();
		{
			assertTrue(iterator.hasNext());
			ArrayEntry arrayEntry = iterator.next();
			assertNotNull(arrayEntry);
			assertEquals("word_1", arrayEntry.getArrayEntryID());
		}
		{
			assertTrue(iterator.hasNext());
			ArrayEntry arrayEntry = iterator.next();
			assertNotNull(arrayEntry);
			assertEquals("word_2", arrayEntry.getArrayEntryID());
		}
		{
			assertTrue(iterator.hasNext());
			ArrayEntry arrayEntry = iterator.next();
			assertNotNull(arrayEntry);
			assertEquals("word_3", arrayEntry.getArrayEntryID());
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testAttackProvidesAttackComment() {
		Attack attack = new Scripts.Attack();
		attack.setContent("[3520] => { # 小悪魔パイズリ\n        :word_1 => [\"【スキュラ】\\n邪魔よ、あなた達……！\", \"scylla_fc1\", 0],\n        :word_2 => [\"【スキュラ】\\n私の触手で薙ぎ払ってあげるわ！\", \"scylla_fc1\", 0],\n        :word_3 => [\"【スキュラ】\\nこの触手、見切れるかしら……？\", \"scylla_fc1\", 0],\n      },");

		assertEquals("小悪魔パイズリ", attack.getAttackComment());
	}

	@Test
	public void testAttackProvidesAttackID() {
		Attack attack = new Scripts.Attack();
		attack.setContent("[3520] => { # 小悪魔パイズリ\n        :word_1 => [\"【スキュラ】\\n邪魔よ、あなた達……！\", \"scylla_fc1\", 0],\n        :word_2 => [\"【スキュラ】\\n私の触手で薙ぎ払ってあげるわ！\", \"scylla_fc1\", 0],\n        :word_3 => [\"【スキュラ】\\nこの触手、見切れるかしら……？\", \"scylla_fc1\", 0],\n      },");

		assertEquals(3520, attack.getAttackID());
	}

	@Test
	public void testMonsterFitsContent() {
		Monster monster = new Scripts.Monster();
		monster.setContent("1140 => { # キメラホムンクルス\n      [3500] => { # 攻撃\n        :word_1 => [\"[Chimera Homunculus]\\n食らえ……\", \"c_homunculus_fc1\", 0],\n      },\n      [4296] => { # イクステンタクル\n        :word_1 => [\"[Chimera Homunculus]\\nEven these tentacles can give men pleasure...\", \"c_homunculus_fc1\", 0],\n        :word_2 => [\"[Chimera Homunculus]\\nI'll squeeze out the male's semen with this tentacle.\", \"c_homunculus_fc1\", 0],\n      },\n      [2360] => { # 屍は屍に\n        :word_1 => [\"[Chimera Homunculus]\\n塵は塵に、屍は屍に……\", \"c_homunculus_fc1\", 0],\n      },\n    },");
	}

	@Test
	public void testMonsterProvidesAttacks() {
		Monster monster = new Scripts.Monster();
		monster.setContent("1140 => { # キメラホムンクルス\n      [3500] => { # 攻撃\n        :word_1 => [\"[Chimera Homunculus]\\n食らえ……\", \"c_homunculus_fc1\", 0],\n      },\n      [4296] => { # イクステンタクル\n        :word_1 => [\"[Chimera Homunculus]\\nEven these tentacles can give men pleasure...\", \"c_homunculus_fc1\", 0],\n        :word_2 => [\"[Chimera Homunculus]\\nI'll squeeze out the male's semen with this tentacle.\", \"c_homunculus_fc1\", 0],\n      },\n      [2360] => { # 屍は屍に\n        :word_1 => [\"[Chimera Homunculus]\\n塵は塵に、屍は屍に……\", \"c_homunculus_fc1\", 0],\n      },\n    },");

		Iterator<Attack> iterator = monster.iterator();
		{
			assertTrue(iterator.hasNext());
			Attack attack = iterator.next();
			assertNotNull(attack);
			assertEquals(3500, attack.getAttackID());
		}
		{
			assertTrue(iterator.hasNext());
			Attack attack = iterator.next();
			assertNotNull(attack);
			assertEquals(4296, attack.getAttackID());
		}
		{
			assertTrue(iterator.hasNext());
			Attack attack = iterator.next();
			assertNotNull(attack);
			assertEquals(2360, attack.getAttackID());
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testMonsterProvidesComment() {
		Monster monster = new Scripts.Monster();
		monster.setContent("1140 => { # キメラホムンクルス\n      [3500] => { # 攻撃\n        :word_1 => [\"[Chimera Homunculus]\\n食らえ……\", \"c_homunculus_fc1\", 0],\n      },\n      [4296] => { # イクステンタクル\n        :word_1 => [\"[Chimera Homunculus]\\nEven these tentacles can give men pleasure...\", \"c_homunculus_fc1\", 0],\n        :word_2 => [\"[Chimera Homunculus]\\nI'll squeeze out the male's semen with this tentacle.\", \"c_homunculus_fc1\", 0],\n      },\n      [2360] => { # 屍は屍に\n        :word_1 => [\"[Chimera Homunculus]\\n塵は塵に、屍は屍に……\", \"c_homunculus_fc1\", 0],\n      },\n    },");

		assertEquals("キメラホムンクルス", monster.getMonsterComment());
	}

	@Test
	public void testMonsterProvidesID() {
		Monster monster = new Scripts.Monster();
		monster.setContent("1140 => { # キメラホムンクルス\n      [3500] => { # 攻撃\n        :word_1 => [\"[Chimera Homunculus]\\n食らえ……\", \"c_homunculus_fc1\", 0],\n      },\n      [4296] => { # イクステンタクル\n        :word_1 => [\"[Chimera Homunculus]\\nEven these tentacles can give men pleasure...\", \"c_homunculus_fc1\", 0],\n        :word_2 => [\"[Chimera Homunculus]\\nI'll squeeze out the male's semen with this tentacle.\", \"c_homunculus_fc1\", 0],\n      },\n      [2360] => { # 屍は屍に\n        :word_1 => [\"[Chimera Homunculus]\\n塵は塵に、屍は屍に……\", \"c_homunculus_fc1\", 0],\n      },\n    },");

		assertEquals(1140, monster.getMonsterID());
	}

	@Test
	public void testScriptFitContents() throws IOException {
		Scripts scripts = new Scripts();
		File file = new File(testFolder, "ScriptsVarious.txt");
		scripts.setContent(FileUtils.readFileToString(file));
	}

	@Test
	public void testScriptProvidesMonsters() throws IOException {
		Scripts scripts = new Scripts();
		File file = new File(testFolder, "Scripts.txt");
		scripts.setContent(FileUtils.readFileToString(file));

		Iterator<Monster> iterator = scripts.getMonsters().iterator();
		for (int i = 1; i <= 10; i++) {
			assertTrue(iterator.hasNext());
			Monster monster = iterator.next();
			assertNotNull(monster);
			assertEquals(i, monster.getMonsterID());
		}
		assertFalse(iterator.hasNext());
	}
}

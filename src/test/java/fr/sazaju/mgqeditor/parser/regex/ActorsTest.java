package fr.sazaju.mgqeditor.parser.regex;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import fr.sazaju.mgqeditor.parser.regex.Actors.Actor;
import fr.sazaju.mgqeditor.parser.regex.Actors.ActorField;

public class ActorsTest {

	private final File testFolder = new File("src/test/resources");

	@Test
	public void testActorFieldFitsNameLineWithUnixNewline() throws IOException {
		ActorField field = new ActorField();
		field.setContent(" Name \"Luka\"\n");
		assertEquals("Name", field.getName());
		assertEquals("Luka", field.getValue());
	}

	@Test
	public void testActorFieldFitsNameLineWithWindowsNewline()
			throws IOException {
		ActorField field = new ActorField();
		field.setContent(" Name \"Luka\"\r\n");
		assertEquals("Name", field.getName());
		assertEquals("Luka", field.getValue());
	}

	@Test
	public void testActorFitsDummyWithUnixLine() throws IOException {
		Actor actor = new Actor();
		actor.setContent("Actor 002\n Name \"**ダミー\"\n");
		assertEquals(2, actor.getActorID());
		assertEquals("**ダミー", actor.getField("Name"));
	}

	@Test
	public void testActorFitsDummyWithWindowsLine() throws IOException {
		Actor actor = new Actor();
		actor.setContent("Actor 002\r\n Name \"**ダミー\"\r\n");
		assertEquals(2, actor.getActorID());
		assertEquals("**ダミー", actor.getField("Name"));
	}

	@Test
	public void testActorFitsMonsterWithUnixLine() throws IOException {
		Actor actor = new Actor();
		actor.setContent("Actor 064\n Name \"Teeny\"\n Nickname \"Tiny Lamia\"\n Notes \"<カテゴリー：ラミア>\r\n<ナワバリ:228-21-26>\r\n<初期サブクラス 254>\r\n<TP基本値 5>\r\n<TPLv補正 30>\r\n<開始時TP 50%>\r\n<経験値曲線 383>\r\n<スキル変化 3147-3277>\r\n<誘惑時使用スキル:3555>\r\n<経験済職業 62-3,123-5,129-6,254-5>\r\n<人間時追加特徴 391>\r\n<初期装備0:845>\r\n<初期装備1:283>\r\n<初期装備2:179>\r\n<初期装備3:56>\r\n<スキルタイプ強化 46-15>\r\n<主人格>\r\n<イラスト:人外モドキ>\"\n");
		assertEquals(64, actor.getActorID());
		assertEquals("Teeny", actor.getField("Name"));
		assertEquals("Tiny Lamia", actor.getField("Nickname"));
		assertEquals(
				"<カテゴリー：ラミア>\r\n<ナワバリ:228-21-26>\r\n<初期サブクラス 254>\r\n<TP基本値 5>\r\n<TPLv補正 30>\r\n<開始時TP 50%>\r\n<経験値曲線 383>\r\n<スキル変化 3147-3277>\r\n<誘惑時使用スキル:3555>\r\n<経験済職業 62-3,123-5,129-6,254-5>\r\n<人間時追加特徴 391>\r\n<初期装備0:845>\r\n<初期装備1:283>\r\n<初期装備2:179>\r\n<初期装備3:56>\r\n<スキルタイプ強化 46-15>\r\n<主人格>\r\n<イラスト:人外モドキ>",
				actor.getField("Notes"));
	}

	@Test
	public void testActorFitsMonsterWithWindowsLine() throws IOException {
		Actor actor = new Actor();
		actor.setContent("Actor 064\r\n Name \"Teeny\"\r\n Nickname \"Tiny Lamia\"\r\n Notes \"<カテゴリー：ラミア>\r\n<ナワバリ:228-21-26>\r\n<初期サブクラス 254>\r\n<TP基本値 5>\r\n<TPLv補正 30>\r\n<開始時TP 50%>\r\n<経験値曲線 383>\r\n<スキル変化 3147-3277>\r\n<誘惑時使用スキル:3555>\r\n<経験済職業 62-3,123-5,129-6,254-5>\r\n<人間時追加特徴 391>\r\n<初期装備0:845>\r\n<初期装備1:283>\r\n<初期装備2:179>\r\n<初期装備3:56>\r\n<スキルタイプ強化 46-15>\r\n<主人格>\r\n<イラスト:人外モドキ>\"\r\n");
		assertEquals(64, actor.getActorID());
		assertEquals("Teeny", actor.getField("Name"));
		assertEquals("Tiny Lamia", actor.getField("Nickname"));
		assertEquals(
				"<カテゴリー：ラミア>\r\n<ナワバリ:228-21-26>\r\n<初期サブクラス 254>\r\n<TP基本値 5>\r\n<TPLv補正 30>\r\n<開始時TP 50%>\r\n<経験値曲線 383>\r\n<スキル変化 3147-3277>\r\n<誘惑時使用スキル:3555>\r\n<経験済職業 62-3,123-5,129-6,254-5>\r\n<人間時追加特徴 391>\r\n<初期装備0:845>\r\n<初期装備1:283>\r\n<初期装備2:179>\r\n<初期装備3:56>\r\n<スキルタイプ強化 46-15>\r\n<主人格>\r\n<イラスト:人外モドキ>",
				actor.getField("Notes"));
	}

	@Test
	public void testActorsFitsDummyEntriesWithUnixLine() throws IOException {
		Actors actors = new Actors();
		actors.setContent("Actor 001\n Name \"**ダミー\"\nActor 002\n Name \"**ダミー\"\n");
		assertEquals(2, actors.size());
	}

	@Test
	public void testActorsFitsDummyEntriesWithWindowsLine() throws IOException {
		Actors actors = new Actors();
		actors.setContent("Actor 001\r\n Name \"**ダミー\"\r\nActor 002\r\n Name \"**ダミー\"\r\n");
		assertEquals(2, actors.size());
	}

	@Test
	public void testActorsFitsWithBOM() throws IOException {
		Actors actors = new Actors();
		actors.setContent("\uFEFFActor 001\r\n Name \"Luka\"\r\n");
	}

	@Test
	public void testActorsParseFullFile() throws IOException {
		File file = new File(testFolder, "Actors.txt");
		Actors actors = new Actors();
		actors.setContent(FileUtils.readFileToString(file, "UTF8"));
		assertEquals(2, actors.size());
	}

}

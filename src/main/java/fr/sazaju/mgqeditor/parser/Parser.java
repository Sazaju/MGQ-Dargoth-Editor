package fr.sazaju.mgqeditor.parser;

import java.io.InputStream;

public interface Parser<SentenceID> extends
		Iterable<SentenceID> {
	
	public void setContent(String content);

	public InputStream getInputStream();

	public Sentence getSentence(SentenceID id);

	public static interface Sentence {
		public void setContent(String content);

		public String getContent();
	}
}

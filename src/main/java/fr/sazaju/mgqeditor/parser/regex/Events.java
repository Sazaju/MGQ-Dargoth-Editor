package fr.sazaju.mgqeditor.parser.regex;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import fr.sazaju.mgqeditor.parser.Parser;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.standard.Choice;
import fr.vergne.parsing.layer.standard.Formula;
import fr.vergne.parsing.layer.standard.Loop;
import fr.vergne.parsing.layer.standard.Quantifier;
import fr.vergne.parsing.layer.standard.Suite;
import fr.vergne.parsing.layer.util.Newline;
import fr.vergne.parsing.layer.util.SeparatedLoop;

public class Events extends Suite implements Parser<Integer> {

	public Events() {
		super(new Formula("\uFEFF?+"),
				new Loop<>(Quantifier.POSSESSIVE,
						(Supplier<Formula>) () -> new Formula("(?:DisplayName " + Arg.STRING + "|//[^\n]*+)?+\n")),
				new SeparatedLoop<>(Quantifier.POSSESSIVE, (Supplier<Event>) Event::new, Newline::new));
	}

	@Override
	public Iterator<Integer> iterator() {
		Stream<Sentence> stream = getParserStream();
		final int[] index = { 0 };
		return stream.map((l) -> index[0]++).iterator();
	}

	@SuppressWarnings("unchecked")
	private Stream<Sentence> getParserStream() {
		return StreamSupport.stream(getEvents().spliterator(), false)
				.flatMap((event) -> StreamSupport.stream(event.spliterator(), false))
				.filter((line) -> line.getCurrent() instanceof Parser)
				.map((line) -> (Parser<Integer>) line.getCurrent())
				.flatMap((parser) -> StreamSupport.stream(convert(parser).spliterator(), false));
	}

	private Iterable<Sentence> convert(final Parser<Integer> parser) {
		// TODO Change Parser contract to iterate over sentences rather than
		// their IDs.
		Iterator<Integer> iterator = parser.iterator();
		return new Iterable<Sentence>() {

			@Override
			public Iterator<Sentence> iterator() {
				return new Iterator<Sentence>() {

					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public Sentence next() {
						return parser.getSentence(iterator.next());
					}
				};
			}
		};
	}

	@Override
	public Sentence getSentence(Integer id) {
		try {
			Iterator<Sentence> iterator = getParserStream().iterator();
			for (int i = 0; i < id; i++) {
				iterator.next();
			}
			return iterator.next();
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException("No sentence " + id);
		}
	}

	@SuppressWarnings("unchecked")
	public Iterable<Event> getEvents() {
		return (SeparatedLoop<Event, Newline>) get(2);
	}

	public void sort(Comparator<Event> comparator) {
		@SuppressWarnings("unchecked")
		SeparatedLoop<Event, Newline> loop = (SeparatedLoop<Event, Newline>) get(2);
		List<Event> list = StreamSupport.stream(loop.spliterator(), true).sorted(comparator)
				.collect(Collectors.toList());
		loop.clear();
		loop.addAll(0, list);
	}

	public int size() {
		return ((SeparatedLoop<?, ?>) get(2)).size();
	}

	public Event getEvent(int id) {
		for (Event event : getEvents()) {
			if (event.getEventID() == id) {
				return event;
			} else {
				// not found yet
			}
		}
		throw new NoSuchElementException("No event " + id);
	}

	public static class Event extends Suite implements Iterable<EventContentLine> {
		public Event() {
			super(new StartLine(), new Newline(), new SeparatedLoop<>(Quantifier.POSSESSIVE,
					(Supplier<EventContentLine>) EventContentLine::new, Newline::new));
		}

		public int getEventID() {
			return ((StartLine) get(0)).getID();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Iterator<EventContentLine> iterator() {
			return ((SeparatedLoop<EventContentLine, Newline>) get(2)).iterator();
		}

		@SuppressWarnings("unchecked")
		public int size() {
			return ((SeparatedLoop<EventContentLine, Newline>) get(2)).size();
		}

		@SuppressWarnings("unchecked")
		public EventContentLine getLine(int index) {
			return ((SeparatedLoop<EventContentLine, Newline>) get(2)).get(index);
		}

		@SuppressWarnings("unchecked")
		public void addLine(int index, EventContentLine line) {
			((SeparatedLoop<EventContentLine, Newline>) get(2)).add(index, line);
		}

		public void addLine(int index, String line) {
			addLine(index, new EventContentLine(line));
		}

		@SuppressWarnings("unchecked")
		public EventContentLine removeLine(int index) {
			return ((SeparatedLoop<EventContentLine, Newline>) get(2)).remove(index);
		}

		@Override
		public Event clone() {
			Event clone = new Event();
			String content = getContent();
			if (content != null) {
				clone.setContent(content);
			} else {
				// no content to set
			}
			return clone;
		}

		@Override
		public String toString() {
			if (getContent() == null) {
				return "Event[null]";
			} else {
				return "Event[" + getEventID() + "]";
			}
		}
	}

	public static class StartLine extends Suite {
		public StartLine() {
			super(new Formula(" *EVENT ++"), new Formula("[0-9]++"));
		}

		public StartLine(String content) {
			this();
			setContent(content);
		}

		public int getID() {
			return Integer.parseInt(get(1).getContent());
		}
	}

	public static class PageLine extends Formula {

		public PageLine() {
			super(" *PAGE ++[0-9]++");
		}
	}

	public static class EmptyLine extends Formula {

		public EmptyLine() {
			super(" *(?=\n|$)");
		}
	}

	public static class CommentLine extends Formula {

		public CommentLine() {
			super(" *//[^\n]*+");
		}
	}

	/**
	 * 
	 * 
	 * @author Sazaju HITOKAGE <sazaju@gmail.com>
	 *
	 */
	public static enum Arg {
		NULL("null"), BOOLEAN("true|false"), DIGIT("[-0-9]++"), HEXA("0x[0-9a-fA-F]++"), BYTES(
				"bytes\\(" + HEXA + "(?:," + HEXA + ")*+\\)"), INNER_STRING("(?:\\\\.|[^\"])*+"), STRING(
						"\"" + INNER_STRING + "\""), STRING_ARRAY("strings\\(" + STRING + "(?:," + STRING + ")*+\\)")
		// , BINARY("binary" + STRING)
		, SYMBOL("symbol" + STRING), ANY(
				NULL + "|" + BOOLEAN + "|" + DIGIT + "|" + BYTES + "|" + STRING + "|" + STRING_ARRAY + "|"
				// + BINARY + "|"
						+ SYMBOL);

		private final String regex;

		private Arg(String regex) {
			this.regex = "(?:" + regex + ")";
		}

		@Override
		public String toString() {
			return regex;
		}
	}

	public static class FunctionLine extends Formula {
		public FunctionLine() {
			super(" *[a-zA-Z0-9_]+\\((?:" + Arg.ANY + "(?:," + Arg.ANY + ")*+)?+\\)");
		}
	}

	public static class MessageLine extends Suite implements Parser<Integer> {
		public MessageLine() {
			super(new Formula(" *ShowMessage\\(\""), new Formula(Arg.INNER_STRING.toString()), new Formula("\"\\)"));
		}

		public MessageLine(String content) {
			this();
			setContent(content);
		}

		public String getMessage() {
			return get(1).getContent();
		}

		public void setMessage(String message) {
			get(1).setContent(message);
		}

		@Override
		public Iterator<Integer> iterator() {
			return Arrays.asList(0).iterator();
		}

		@Override
		public Sentence getSentence(Integer id) {
			if (id != 0) {
				throw new IndexOutOfBoundsException("Only ID 0 is available for a message, not " + id);
			} else {
				return new Sentence() {

					@Override
					public void setContent(String content) {
						setMessage(content);
					}

					@Override
					public String getContent() {
						return getMessage();
					}
				};
			}
		}

	}

	public static class ChoiceLine extends Suite implements Parser<Integer> {
		private final SeparatedLoop<Formula, Formula> choicesParser;

		@SuppressWarnings("unchecked")
		public ChoiceLine() {
			super(new Formula(" *ShowChoices\\(strings\\(\""),
					new SeparatedLoop<Formula, Formula>(Quantifier.POSSESSIVE,
							(Supplier<Formula>) () -> new Formula(Arg.INNER_STRING.toString()),
							() -> new Formula("\",\"")),
					new Formula("\"\\)," + Arg.DIGIT + "\\)"));
			choicesParser = (SeparatedLoop<Formula, Formula>) get(1);
		}

		public ChoiceLine(String content) {
			this();
			setContent(content);
		}

		public int size() {
			return choicesParser.size();
		}

		public List<String> getChoices() {
			List<String> list = new LinkedList<String>();
			for (Formula formula : choicesParser) {
				list.add(formula.getContent());
			}
			return list;
		}

		public void setChoices(List<String> choices) {
			if (choices == null) {
				throw new NullPointerException("No choice provided");
			} else if (choices.size() != choicesParser.size()) {
				throw new IllegalArgumentException(
						"You should provide " + choicesParser.size() + " choices, not " + choices.size());
			} else {
				Iterator<String> provider = choices.iterator();
				Iterator<Formula> receiver = choicesParser.iterator();
				while (provider.hasNext()) {
					receiver.next().setContent(provider.next());
				}
			}
		}

		public String getChoice(int index) {
			checkIndex(index);
			return choicesParser.get(index).getContent();
		}

		private void checkIndex(int index) {
			if (index < 0 || index >= size()) {
				throw new IndexOutOfBoundsException("The provided index (" + index
						+ ") does not correspond to a valid choice (0-" + (size() - 1) + ")");
			} else {
				// Index OK
			}
		}

		public void setChoice(int index, String choice) {
			checkIndex(index);
			choicesParser.get(index).setContent(choice);
		}

		@Override
		public Iterator<Integer> iterator() {
			return new Iterator<Integer>() {

				int next = 0;

				@Override
				public Integer next() {
					try {
						return next;
					} finally {
						next++;
					}
				}

				@Override
				public boolean hasNext() {
					return next < size();
				}
			};
		}

		@Override
		public Sentence getSentence(Integer id) {
			if (id < 0 || id >= size()) {
				throw new IndexOutOfBoundsException(id + " is not a valid ID for " + size() + " choices");
			} else {
				return new Sentence() {

					@Override
					public void setContent(String content) {
						setChoice(id, content);
					}

					@Override
					public String getContent() {
						return getChoice(id);
					}
				};
			}
		}
	}

	public static class Function355And655Line extends Suite implements Parser<Integer> {

		private static final String sep = "||";

		// TODO Manage partial cases (355 missing)
		public Function355And655Line() {
			super(new Formula(" *355\\(\""), new Formula(Arg.INNER_STRING.toString()), new Formula("\"\\)"),
					new Loop<Suite>((Supplier<Suite>) () -> new Suite(new Newline(), new Formula(" *655\\(\""),
							new Formula(Arg.INNER_STRING.toString()), new Formula("\"\\)"))));
		}

		public Function355And655Line(String content) {
			this();
			setContent(content);
		}

		private String getInnerString() {
			StringBuilder builder = new StringBuilder();
			builder.append(get(1).getContent());
			Loop<Suite> loop = get(3);
			for (Suite suite : loop) {
				builder.append(sep);
				builder.append(suite.get(2).getContent());
			}
			String rawString = builder.toString();
			String unescapedString = rawString.replaceAll("\\\\(.)", "$1");
			return unescapedString;
		}

		private void setInnerString(String string) {
			string = string.replaceAll("[\"\\\\]", "\\\\$0");
			LinkedList<String> split = new LinkedList<>(Arrays.asList(string.split("\\Q" + sep + "\\E")));
			get(1).setContent(split.removeFirst());
			Loop<Suite> loop = get(3);
			for (Suite suite : loop) {
				suite.get(2).setContent(split.removeFirst());
			}
		}

		public int size() {
			return getInnerString().replaceAll("a", "").replaceAll(Arg.STRING.toString(), "a").replaceAll("[^a]+", "")
					.length();
		}

		public String getFunctionString(int index) {
			if (index < 0 || index >= size()) {
				throw new IndexOutOfBoundsException("Invalid index " + index + ", size " + size());
			} else {
				Matcher matcher = Pattern.compile(Arg.STRING.toString()).matcher(getInnerString());
				while (matcher.find() && index > 0) {
					index--;
				}
				String string = matcher.group();
				return string.substring(1, string.length() - 1);
			}
		}

		public void setFunctionString(int index, String content) {
			if (content == null) {
				throw new NullPointerException("Null content provided");
			} else if (index < 0 || index >= size()) {
				throw new IndexOutOfBoundsException("Invalid index " + index + ", size " + size());
			} else {
				StringBuffer sb = new StringBuffer();
				String complete = getInnerString();
				Matcher matcher = Pattern.compile(Arg.STRING.toString()).matcher(complete);
				while (matcher.find() && index > 0) {
					index--;
				}
				sb.append(complete.substring(0, matcher.start()));
				sb.append("\"");
				sb.append(content);
				sb.append("\"");
				sb.append(complete.substring(matcher.end()));
				setInnerString(sb.toString());
			}
		}

		public void setFunctionStrings(List<String> contents) {
			if (contents == null) {
				throw new NullPointerException("Null contents provided");
			} else if (contents.size() != size()) {
				throw new IllegalArgumentException("The provided contents (" + contents.size()
						+ ") do not correspond to the actual size (" + size() + ")");
			} else {
				int index = 0;
				for (String content : contents) {
					setFunctionString(index, content);
					index++;
				}
			}
		}

		public List<String> getFunctionStrings() {
			List<String> list = new LinkedList<>();
			Matcher matcher = Pattern.compile(Arg.STRING.toString()).matcher(getInnerString());
			while (matcher.find()) {
				String string = matcher.group();
				string = string.substring(1, string.length() - 1);
				list.add(string);
			}
			return list;
		}

		public Object getLinesCount() {
			Loop<Suite> loop = get(3);
			return loop.size() + 1;
		}

		public String getLineContent(int line) {
			StringBuilder builder = new StringBuilder();
			Loop<Suite> loop = get(3);
			if (line == 0) {
				builder.append(get(0).getContent());
				builder.append(get(1).getContent());
				builder.append(get(2).getContent());
			} else if (line - 1 < loop.size()) {
				Suite suite = loop.get(line - 1);
				builder.append(suite.get(1).getContent());
				builder.append(suite.get(2).getContent());
				builder.append(suite.get(3).getContent());
			} else {
				throw new IndexOutOfBoundsException(
						"No line " + line + " for " + (loop.size() + 1) + " lines of functions");
			}
			return builder.toString();
		}

		@Override
		public Iterator<Integer> iterator() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Sentence getSentence(Integer id) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public static class EventContentLine extends Choice {
		public EventContentLine() {
			super(new PageLine(), new MessageLine(), new ChoiceLine(), new Function355And655Line(), new FunctionLine(),
					new CommentLine(), new EmptyLine());
			setReferenceAlternative(3);
		}

		public EventContentLine(String content) {
			this();
			setContent(content);
		}

		@Override
		public EventContentLine clone() {
			EventContentLine clone = new EventContentLine();
			String content = getContent();
			if (content != null) {
				clone.setContent(content);
			} else {
				// no content to set
			}
			return clone;
		}
	}

	public static class EventLine extends Choice {
		public EventLine() {
			super(flatten(new StartLine(), new EventContentLine()));
			this.setReferenceAlternative(4);
		}

		private static Collection<? extends Layer> flatten(StartLine title, EventContentLine contentLine) {
			Collection<Layer> collection = new LinkedList<Layer>();
			collection.add(title);
			for (int i = 0; i < contentLine.size(); i++) {
				collection.add(contentLine.getAlternative(i));
			}
			return collection;
		}

		public EventLine(String content) {
			this();
			setContent(content);
		}
	}
}

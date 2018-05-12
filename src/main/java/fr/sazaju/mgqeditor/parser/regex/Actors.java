package fr.sazaju.mgqeditor.parser.regex;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import fr.sazaju.mgqeditor.parser.Parser;
import fr.vergne.parsing.layer.standard.Formula;
import fr.vergne.parsing.layer.standard.Loop;
import fr.vergne.parsing.layer.standard.Quantifier;
import fr.vergne.parsing.layer.standard.Suite;
import fr.vergne.parsing.layer.util.Newline;

public class Actors extends Suite implements Parser<Integer> {

	public Actors() {
		super(new Formula("\uFEFF?+"), new Loop<>(Quantifier.POSSESSIVE,
				(Supplier<Actor>) Actor::new));
	}

	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {

			private final Iterator<Actor> actorIterator = getActors()
					.iterator();

			@Override
			public boolean hasNext() {
				return actorIterator.hasNext();
			}

			@Override
			public Integer next() {
				return actorIterator.next().getActorID();
			}

			@Override
			public void remove() {
				actorIterator.remove();
			}
		};
	}

	@Override
	public Sentence getSentence(Integer id) {
		try {
			final Actor actor = getActor(id);
			return new Sentence() {

				@Override
				public void setContent(String content) {
					actor.setField("Name", content);
				}

				@Override
				public String getContent() {
					return actor.getField("Name");
				}
			};
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public Iterable<Actor> getActors() {
		List<Actor> actors = new LinkedList<>();
		for (Actor actor : (Loop<Actor>) get(1)) {
			if (actor.getField("Name").contains("ダミー")) {
				// pass
			} else {
				actors.add(actor);
			}
		}
		return actors;
	}

	@SuppressWarnings("unchecked")
	public int size() {
		return ((Loop<Actor>) get(1)).size();
	}

	public Actor getActor(int id) {
		for (Actor actor : getActors()) {
			if (actor.getActorID() == id) {
				if (actor.getField("Name").equals("**ダミー")) {
					throw new RuntimeException("Dummy actor: " + actor);
				} else {
					return actor;
				}
			} else {
				// not found yet
			}
		}
		throw new NoSuchElementException("No actor " + id);
	}

	public static class Actor extends Suite implements Iterable<ActorField> {
		public Actor() {
			super(new Formula("Actor "), new Formula("[0-9]++"), new Newline(),
					new Loop<>(Quantifier.POSSESSIVE,
							(Supplier<ActorField>) ActorField::new));
		}

		public int getActorID() {
			return Integer.parseInt(get(1).getContent());
		}

		public String getField(String name) {
			for (ActorField field : this) {
				if (field.getName().equals(name)) {
					return field.getValue();
				} else {
					// not found yet
				}
			}
			return null;
		}

		public void setField(String name, String value) {
			for (ActorField field : this) {
				if (field.getName().equals(name)) {
					field.setValue(value);
					return;
				} else {
					// not found yet
				}
			}
			throw new IllegalArgumentException("Unknown field: " + name);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Iterator<ActorField> iterator() {
			return ((Loop<ActorField>) get(3)).iterator();
		}

		@Override
		public Actor clone() {
			Actor clone = new Actor();
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
			return "Actor[" + getActorID() + "]";
		}
	}

	public static class ActorField extends Suite {
		public ActorField() {
			super(new Formula(" "), new Formula("[^ ]++"), new Formula(" \""),
					new Formula("[^\"]++"), new Formula("\""), new Newline());
		}

		public String getName() {
			return get(1).getContent();
		}

		public String getValue() {
			return get(3).getContent();
		}

		public void setValue(String value) {
			get(3).setContent(value);
		}

		@Override
		public ActorField clone() {
			ActorField clone = new ActorField();
			String content = getContent();
			if (content != null) {
				clone.setContent(content);
			} else {
				// no content to set
			}
			return clone;
		}
	}
}

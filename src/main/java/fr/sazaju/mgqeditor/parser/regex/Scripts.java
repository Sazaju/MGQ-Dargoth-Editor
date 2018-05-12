package fr.sazaju.mgqeditor.parser.regex;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.logging.Logger;

import fr.sazaju.mgqeditor.parser.Parser;
import fr.sazaju.mgqeditor.parser.regex.Scripts.FullSentenceID;
import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Choice;
import fr.vergne.parsing.layer.standard.Formula;
import fr.vergne.parsing.layer.standard.Quantifier;
import fr.vergne.parsing.layer.standard.Suite;
import fr.vergne.parsing.layer.util.SeparatedLoop;

public class Scripts extends Suite implements Parser<FullSentenceID> {

	private static final Logger logger = Logger.getLogger(Scripts.class
			.getName());

	public Scripts() {
		super(new Formula("[\\s\\S]*?"), new Formula("[A-Z_]++ = \\{"),
				new Blank(), new SeparatedLoop<Monster, Blank>(
						Quantifier.POSSESSIVE,
						(Supplier<Monster>) Monster::new, Blank::new),
				new Blank(), new Formula("\\}[\\s\\S]*+"));
	}

	@Override
	public Iterator<FullSentenceID> iterator() {
		return new Iterator<FullSentenceID>() {

			private final Iterator<Monster> monsterIterator = getMonsters()
					.iterator();
			private Monster monster = null;
			private Iterator<Attack> attackIterator = null;
			private Attack attack = null;
			private Iterator<ScriptSentence> sentenceIterator = null;

			@Override
			public boolean hasNext() {
				if (sentenceIterator != null && sentenceIterator.hasNext()) {
					return true;
				} else if (attackIterator != null && attackIterator.hasNext()) {
					attack = attackIterator.next();
					logger.finer("Check attack " + attack + "...");
					sentenceIterator = attack.getSentences().iterator();
					return hasNext();
				} else if (monsterIterator.hasNext()) {
					monster = monsterIterator.next();
					logger.fine("Check monster " + monster + "...");
					attackIterator = monster.iterator();
					attack = null;
					return hasNext();
				} else {
					return false;
				}
			}

			@Override
			public FullSentenceID next() {
				if (hasNext()) {
					ScriptSentence sentence = sentenceIterator.next();
					logger.finest("Sentence found: " + sentence + "...");
					return new FullSentenceID(monster, attack, sentence);
				} else {
					throw new NoSuchElementException();
				}
			}

			@Override
			public void remove() {
				sentenceIterator.remove();
			}
		};
	}

	public Sentence getSentence(FullSentenceID sentenceID) {
		for (Monster monster : getMonsters()) {
			if (!sentenceID.isMonster(monster)) {
				// not the right monster
			} else {
				for (Attack attack : monster) {
					if (!sentenceID.isAttack(attack)) {
						// not the right attack
					} else {
						for (ScriptSentence sentence : attack.getSentences()) {
							if (!sentenceID.isSentence(sentence)) {
								// not the right sentence
							} else {
								return sentence;
							}
						}
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Iterable<Monster> getMonsters() {
		return ((SeparatedLoop<Monster, Blank>) get(3));
	}

	@SuppressWarnings("unchecked")
	public int size() {
		return ((SeparatedLoop<Monster, Blank>) get(3)).size();
	}

	public static class Monster extends Suite implements Iterable<Attack> {
		public Monster() {
			super(new Formula("[0-9]++ => \\{ # "), new Formula("[^\n]*+"),
					new Blank(), new SeparatedLoop<Attack, Blank>(
							Quantifier.POSSESSIVE,
							(Supplier<Attack>) Attack::new, Blank::new),
					new Blank(), new Formula("\\},"));
		}

		@SuppressWarnings("unchecked")
		@Override
		public Iterator<Attack> iterator() {
			return ((SeparatedLoop<Attack, Blank>) get(3)).iterator();
		}

		@SuppressWarnings("unchecked")
		public int size() {
			return ((SeparatedLoop<Attack, Blank>) get(3)).size();
		}

		public int getMonsterID() {
			return Integer.parseInt(get(0).getContent().replaceFirst(
					"[^0-9].*", ""));
		}

		public String getMonsterComment() {
			return get(1).getContent();
		}

		@Override
		public Object clone() {
			Monster clone = new Monster();
			if (getContent() != null) {
				clone.setContent(getContent());
			} else {
				// keep it without content
			}
			return clone;
		}

		@Override
		public String toString() {
			return getMonsterID() + " (" + getMonsterComment() + ")";
		}
	}

	public static class Attack extends Suite implements Iterable<ArrayEntry> {
		public Attack() {
			super(
					new AttackIDs(),
					new Formula(" => \\{ # ?+"),
					new Formula("[^\n]*+"),
					new Blank(),
					new SeparatedLoop<ArrayEntry, Blank>(Quantifier.POSSESSIVE,
							(Supplier<ArrayEntry>) ArrayEntry::new, Blank::new),
					new Blank(), new Formula("\\},"));
		}

		@SuppressWarnings("unchecked")
		@Override
		public Iterator<ArrayEntry> iterator() {
			return ((SeparatedLoop<ArrayEntry, Blank>) get(4)).iterator();
		}

		@SuppressWarnings("unchecked")
		public int size() {
			return ((SeparatedLoop<ArrayEntry, Blank>) get(4)).size();
		}

		public int getAttackID() {
			return getAttackIDs().getMainID();
		}

		public AttackIDs getAttackIDs() {
			return (AttackIDs) get(0);
		}

		public String getAttackComment() {
			return get(2).getContent();
		}

		@Override
		public Object clone() {
			Attack clone = new Attack();
			if (getContent() != null) {
				clone.setContent(getContent());
			} else {
				// keep it without content
			}
			return clone;
		}

		@Override
		public String toString() {
			return getAttackIDs() + " (" + getAttackComment() + ")";
		}

		public Collection<ScriptSentence> getSentences() {
			Collection<ScriptSentence> sentences = new LinkedList<>();
			for (ArrayEntry arrayEntry : this) {
				if (arrayEntry.getArrayEntryID().startsWith("word_")) {
					sentences.add(new ScriptSentence(arrayEntry));
				} else {
					// not a sentence
				}
			}
			return sentences;
		}
	}

	public static class AttackIDs extends Choice implements Iterable<Integer> {
		public AttackIDs() {
			super(new Suite(new Formula("\\["),
					new SeparatedLoop<Formula, Formula>(Quantifier.POSSESSIVE,
							(Supplier<Formula>) () -> new Formula("[0-9]++"),
							() -> new Formula(", ?+", ","), 1,
							Integer.MAX_VALUE), new Formula("\\]")), new Suite(
					new Formula("[0-9]++"), new Formula("\\.\\."), new Formula(
							"[0-9]++")), new Formula("[0-9]++"));
		}

		@Override
		public Iterator<Integer> iterator() {
			if (getCurrent() == getAlternative(0)) {
				final Suite alternative = (Suite) getCurrent();
				@SuppressWarnings("unchecked")
				final SeparatedLoop<Formula, Atom> loop = (SeparatedLoop<Formula, Atom>) alternative
						.get(1);
				return new Iterator<Integer>() {

					final Iterator<Formula> iterator = loop.iterator();

					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public Integer next() {
						return Integer.parseInt(iterator.next().getContent());
					}

					@Override
					public void remove() {
						iterator.remove();
					}
				};
			} else if (getCurrent() == getAlternative(1)) {
				final int first = Integer.parseInt(((Suite) getCurrent())
						.get(0).getContent());
				final int last = Integer.parseInt(((Suite) getCurrent()).get(2)
						.getContent());
				return new Iterator<Integer>() {

					private int next = first > last ? last : first;
					private final int max = first > last ? first : last;

					@Override
					public boolean hasNext() {
						return next <= max;
					}

					@Override
					public Integer next() {
						int current = next;
						next++;
						return current;
					}

					@Override
					public void remove() {
						throw new RuntimeException(
								"You cannot remove attack IDs.");
					}
				};
			} else if (getCurrent() == getAlternative(2)) {
				final int value = Integer.parseInt(((Formula) getCurrent())
						.getContent());
				return Arrays.asList(value).iterator();
			} else {
				throw new RuntimeException("Unmanaged alternative: "
						+ getCurrent());
			}
		}

		public int getMainID() {
			Integer min = null;
			for (Integer id : this) {
				if (min == null || min > id) {
					min = id;
				} else {
					// worse ID
				}
			}
			return min;
		}

		@Override
		public Object clone() {
			AttackIDs clone = new AttackIDs();
			if (getContent() != null) {
				clone.setContent(getContent());
			} else {
				// keep it without content
			}
			return clone;
		}

		@Override
		public String toString() {
			return getContent();
		}
	}

	public static class ArrayEntry extends Suite {
		public ArrayEntry() {
			super(new Formula(":[^ ]++"), new Formula(" => "), new Formula(
					"[^\n]*"), new Formula(","));
		}

		public String getArrayEntryID() {
			return get(0).getContent().substring(1);
		}

		public String getArrayEntryContent() {
			return get(2).getContent();
		}

		public void setArrayEntryContent(String content) {
			get(2).setContent(content);
		}

		@Override
		public Object clone() {
			ArrayEntry clone = new ArrayEntry();
			if (getContent() != null) {
				clone.setContent(getContent());
			} else {
				// keep it without content
			}
			return clone;
		}

		@Override
		public String toString() {
			return getArrayEntryID();
		}
	}

	public static class ScriptSentence implements Sentence {
		private final ArrayEntry entry;
		private final Suite parser = new Suite(new Formula("\\[\""),
				new Formula("[^\"]*+"), new Formula(
						"\", \"[^\"]*+\", ?[0-9]++\\]"));

		public ScriptSentence(ArrayEntry entry) {
			this.entry = entry;
			parser.setContent(entry.getArrayEntryContent());
		}

		public String getSentenceID() {
			return entry.getArrayEntryID();
		}

		@Override
		public String getContent() {
			return parser.get(1).getContent();
		}

		@Override
		public void setContent(String content) {
			parser.get(1).setContent(content);
			entry.setArrayEntryContent(parser.getContent());
		}

		@Override
		public String toString() {
			return getSentenceID();
		}
	}

	public static class Blank extends Formula {
		public Blank() {
			// set a default content
			super("\\s*+", "\n");
		}

		@Override
		public Object clone() {
			return new Blank();
		}
	}

	public static class FullSentenceID {
		private final Monster monster;
		private final Attack attack;
		private final ScriptSentence sentence;

		public FullSentenceID(Monster monster, Attack attack,
				ScriptSentence sentence) {
			this.monster = monster;
			this.attack = attack;
			this.sentence = sentence;
		}

		public boolean isMonster(Monster monster) {
			if (this.monster.getMonsterID() == monster.getMonsterID()) {
				return this.monster.getMonsterComment().equals(
						monster.getMonsterComment());
			} else {
				return false;
			}
		}

		public boolean isAttack(Attack attack) {
			if (this.attack.getAttackID() == attack.getAttackID()) {
				return this.attack.getAttackComment().equals(
						attack.getAttackComment());
			} else {
				return false;
			}
		}

		public boolean isSentence(ScriptSentence sentence) {
			return this.sentence.getSentenceID().equals(
					sentence.getSentenceID());
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			} else if (obj instanceof FullSentenceID) {
				FullSentenceID id = (FullSentenceID) obj;
				return isMonster(id.monster) && isAttack(id.attack)
						&& isSentence(id.sentence);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return monster.getMonsterComment().hashCode()
					+ attack.getAttackComment().hashCode()
					+ sentence.hashCode();
		}

		@Override
		public String toString() {
			return "[M=" + monster.getMonsterID() + "/"
					+ monster.getMonsterComment() + ",A="
					+ attack.getAttackID() + "/" + attack.getAttackComment()
					+ ",S=" + sentence.getSentenceID() + "]";
		}
	}
}

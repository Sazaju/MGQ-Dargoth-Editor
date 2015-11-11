package fr.sazaju.mgqeditor.regex;

import java.util.Iterator;

import fr.sazaju.mgqeditor.regex.Scripts.Monster;
import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Choice;
import fr.vergne.parsing.layer.standard.Formula;
import fr.vergne.parsing.layer.standard.Option;
import fr.vergne.parsing.layer.standard.Quantifier;
import fr.vergne.parsing.layer.standard.Suite;
import fr.vergne.parsing.layer.util.SeparatedLoop;

public class Scripts extends Suite implements Iterable<Monster> {

	public Scripts() {
		super(new Formula("[\\s\\S]*?"), new Formula("[A-Z_]++ = \\{"),
				new Blank(), new SeparatedLoop<Monster, Blank>(
						Quantifier.POSSESSIVE, new Monster(), new Blank()),
				new Blank(), new Formula("\\}[\\s\\S]*+"));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Monster> iterator() {
		return ((SeparatedLoop<Monster, Blank>) get(3)).iterator();
	}

	@SuppressWarnings("unchecked")
	public int size() {
		return ((SeparatedLoop<Monster, Blank>) get(3)).size();
	}

	public Sentence getSentence(FullSentenceID sentenceID) {
		for (Monster monster : this) {
			if (!sentenceID.isMonster(monster)) {
				// not the right monster
			} else {
				for (Attack attack : monster) {
					if (!sentenceID.isAttack(attack)) {
						// not the right attack
					} else {
						for (Sentence sentence : attack) {
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

	public static class Monster extends Suite implements Iterable<Attack> {
		public Monster() {
			super(new Formula("[0-9]++"), new Atom(" => { # "), new Formula(
					"[^\n]*+"), new Blank(), new SeparatedLoop<Attack, Blank>(
					Quantifier.POSSESSIVE, new Attack(), new Blank()),
					new Blank(), new Formula("\\},"));
		}

		@SuppressWarnings("unchecked")
		@Override
		public Iterator<Attack> iterator() {
			return ((SeparatedLoop<Attack, Blank>) get(4)).iterator();
		}

		@SuppressWarnings("unchecked")
		public int size() {
			return ((SeparatedLoop<Attack, Blank>) get(4)).size();
		}

		public int getMonsterID() {
			return Integer.parseInt(get(0).getContent());
		}

		public String getMonsterComment() {
			return get(2).getContent();
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

	public static class Attack extends Suite implements Iterable<Sentence> {
		public Attack() {
			super(new AttackIDs(), new Formula(" => \\{ # ?+"), new Formula(
					"[^\n]*+"), new Blank(), new Option<Suite>(new Suite(
					new Info(), new Blank())),
					new SeparatedLoop<Sentence, Blank>(Quantifier.POSSESSIVE,
							new Sentence(), new Blank()), new Blank(),
					new Formula("\\},"));
		}

		@SuppressWarnings("unchecked")
		@Override
		public Iterator<Sentence> iterator() {
			return ((SeparatedLoop<Sentence, Blank>) get(5)).iterator();
		}

		@SuppressWarnings("unchecked")
		public int size() {
			return ((SeparatedLoop<Sentence, Blank>) get(5)).size();
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

		public Info getInfo() {
			@SuppressWarnings("unchecked")
			Option<Suite> option = (Option<Suite>) get(4);
			if (option.isPresent()) {
				return option.getOption().get(0);
			} else {
				return null;
			}
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
	}

	public static class AttackIDs extends Choice implements Iterable<Integer> {
		public AttackIDs() {
			super(new Suite(new Atom("["), new SeparatedLoop<Formula, Atom>(
					Quantifier.POSSESSIVE, new Formula("[0-9]++"),
					new Atom(","), 1, Integer.MAX_VALUE), new Atom("]")),
					new Suite(new Formula("[0-9]++"), new Atom(".."),
							new Formula("[0-9]++")));
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

	public static class Info extends Suite {
		public Info() {
			super(new Formula(":ct_pic => \""), new Formula("[^\"]++"),
					new Formula("\","), new Blank(),
					new Formula(":ct_se => \""), new Formula("[^\"]++"),
					new Formula("\","), new Blank(),
					new Formula(":ct_type => "), new Choice(new Atom(":basic"),
							new Atom(":slide"), new Atom(":focus"), new Atom(
									":long")), new Formula(","));
		}

		public String getPic() {
			return get(1).getContent();
		}

		public String getSE() {
			return get(5).getContent();
		}

		public String getType() {
			return get(9).getContent();
		}

		@Override
		public Object clone() {
			Info clone = new Info();
			if (getContent() != null) {
				clone.setContent(getContent());
			} else {
				// keep it without content
			}
			return clone;
		}
	}

	public static class Sentence extends Suite {
		public Sentence() {
			super(new Formula(":word_[0-9]++ => \\[\""),
					new Formula("[^\"]*+"), new Formula(
							"\", \"[^\"]*+\", [0-9]++\\],"));
		}

		public String getSentenceID() {
			return get(0).getContent().substring(1).replaceAll(" .*", "");
		}

		public String getMessage() {
			return get(1).getContent();
		}

		public void setMessage(String content) {
			get(1).setContent(content);
		}

		@Override
		public Object clone() {
			Sentence clone = new Sentence();
			if (getContent() != null) {
				clone.setContent(getContent());
			} else {
				// keep it without content
			}
			return clone;
		}

		@Override
		public String toString() {
			return getSentenceID();
		}
	}

	public static class Blank extends Formula {
		public Blank() {
			super("\\s*+");
			// set a default content
			setContent("\n");
		}

		@Override
		public Object clone() {
			return new Blank();
		}
	}

	public static class FullSentenceID {
		private final Monster monster;
		private final Attack attack;
		private final Sentence sentence;

		public FullSentenceID(Monster monster, Attack attack, Sentence sentence) {
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

		public boolean isSentence(Sentence sentence) {
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

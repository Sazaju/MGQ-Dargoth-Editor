package fr.sazaju.mgqeditor.parser.regex;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import fr.sazaju.mgqeditor.parser.Parser;
import fr.sazaju.mgqeditor.parser.regex.Items.ItemID;
import fr.vergne.parsing.layer.standard.Formula;
import fr.vergne.parsing.layer.standard.Loop;
import fr.vergne.parsing.layer.standard.Option;
import fr.vergne.parsing.layer.standard.Quantifier;
import fr.vergne.parsing.layer.standard.Suite;
import fr.vergne.parsing.layer.util.Newline;

public class Items extends Suite implements Parser<ItemID> {

	public Items() {
		super(new Formula("\uFEFF?+"), new Loop<>(Quantifier.POSSESSIVE,
				(Supplier<Item>) Item::new));
	}

	@Override
	public Iterator<ItemID> iterator() {
		return new Iterator<ItemID>() {

			private final Iterator<Item> itemIterator = getItems().iterator();

			@Override
			public boolean hasNext() {
				return itemIterator.hasNext();
			}

			@Override
			public ItemID next() {
				return new ItemID(itemIterator.next());
			}

			@Override
			public void remove() {
				itemIterator.remove();
			}
		};
	}

	@Override
	public Sentence getSentence(final ItemID id) {
		try {
			final Item item = getItem(id.type, id.number);
			return new Sentence() {

				@Override
				public void setContent(String content) {
					item.setField("Name", content);
				}

				@Override
				public String getContent() {
					return item.getField("Name");
				}
			};
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public Iterable<Item> getItems() {
		List<Item> items = new LinkedList<>();
		for (Item item : (Loop<Item>) get(1)) {
			if (item.getField("Name").contains("ダミー")) {
				// pass
			} else {
				items.add(item);
			}
		}
		return items;
	}

	@SuppressWarnings("unchecked")
	public int size() {
		return ((Loop<Item>) get(1)).size();
	}

	public Item getItem(String type, int id) {
		for (Item item : getItems()) {
			if (item.getItemID() == id && item.getItemType().equals(type)) {
				if (item.getField("Name").equals("**ダミー")) {
					throw new RuntimeException("Dummy item: " + item);
				} else {
					return item;
				}
			} else {
				// not found yet
			}
		}
		throw new NoSuchElementException("No item " + type + " " + id);
	}

	public static class Item extends Suite implements Iterable<ItemField> {
		public Item() {
			super(new Formula("(?:Item|Weapon|Armor) "),
					new Formula("[0-9]++"), new Newline(), new Loop<>(
							Quantifier.POSSESSIVE,
							(Supplier<ItemField>) ItemField::new),
					new Option<>(new Comment()), new Formula("(?:\r?\n)*+"));
		}

		public int getItemID() {
			return Integer.parseInt(get(1).getContent());
		}

		public String getItemType() {
			return get(0).getContent().trim();
		}

		public String getField(String name) {
			for (ItemField field : this) {
				if (field.getName().equals(name)) {
					return field.getValue();
				} else {
					// not found yet
				}
			}
			return null;
		}

		public void setField(String name, String content) {
			for (ItemField field : this) {
				if (field.getName().equals(name)) {
					field.setValue(content);
					return;
				} else {
					// not found yet
				}
			}
			throw new NoSuchElementException("No field " + name);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Iterator<ItemField> iterator() {
			return ((Loop<ItemField>) get(3)).iterator();
		}

		@Override
		public Item clone() {
			Item clone = new Item();
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
				return "Item[?]";
			} else {
				return "Item[" + getItemID() + "]";
			}
		}
	}

	public static class ItemField extends Suite {
		public ItemField() {
			super(new Formula(" "), new Formula("[^ ]++"), new Formula(" \""),
					new Formula("[^\"]++"), new Formula("\" ?+"), new Newline());
		}

		public String getName() {
			return get(1).getContent();
		}

		public String getValue() {
			return get(3).getContent();
		}

		public void setValue(String content) {
			get(3).setContent(content);
		}

		@Override
		public ItemField clone() {
			ItemField clone = new ItemField();
			String content = getContent();
			if (content != null) {
				clone.setContent(content);
			} else {
				// no content to set
			}
			return clone;
		}
	}

	public static class Comment extends Suite {
		public Comment() {
			super(new Formula(" "), new Formula("// ?+"), new Formula(
					"[^\r\n]++"), new Newline());
		}

		public String getValue() {
			return get(2).getContent();
		}

		@Override
		public Comment clone() {
			Comment clone = new Comment();
			String content = getContent();
			if (content != null) {
				clone.setContent(content);
			} else {
				// no content to set
			}
			return clone;
		}
	}

	public static class ItemID {
		public final String type;
		public final int number;

		public ItemID(Item item) {
			this.type = item.getItemType();
			this.number = item.getItemID();
		}

		@Override
		public String toString() {
			return type + " " + number;
		}
	}
}

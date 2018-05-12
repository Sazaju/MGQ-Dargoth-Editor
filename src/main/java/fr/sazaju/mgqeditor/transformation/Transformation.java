package fr.sazaju.mgqeditor.transformation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import fr.sazaju.mgqeditor.fix.util.Version;
import fr.sazaju.mgqeditor.parser.regex.Events.EventLine;
import fr.sazaju.mgqeditor.transformation.OperationBuilder.Operation;
import fr.vergne.parsing.layer.util.Newline;
import fr.vergne.parsing.layer.util.SeparatedLoop;

public interface Transformation extends Iterable<Operation> {

	// TODO test
	default SeparatedLoop<EventLine, Newline> apply(SeparatedLoop<EventLine, Newline> source) {
		Version translation = new Version((i) -> source.get(i).getContent(), source.size());
		for (Operation operation : this) {
			translation = operation.transform(translation);
		}

		SeparatedLoop<EventLine, Newline> result = new SeparatedLoop<>((Supplier<EventLine>) EventLine::new,
				Newline::new);
		for (String line : translation) {
			result.add(line);
		}
		return result;
	}

	// TODO test
	public static Transformation createFromOperations(List<Operation> operations) {
		List<Operation> list = new LinkedList<Operation>(operations);
		return new Transformation() {

			@Override
			public Iterator<Operation> iterator() {
				return list.iterator();
			}

		};
	}

}

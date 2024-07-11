package de.medizininformatikinitiative.medgraph.common.mvc;

import java.util.function.Consumer;

/**
 * Extension of the {@link Dispatcher} which can be invoked by anyone to dispatch events. Use this if you want to
 * implement listener management via delegation rather than extension.
 *
 * @author Markus Budeus
 */
public class OpenDispatcher<T> extends Dispatcher<T> {

	@Override
	public void dispatchEvent(Consumer<T> event) {
		super.dispatchEvent(event);
	}

}


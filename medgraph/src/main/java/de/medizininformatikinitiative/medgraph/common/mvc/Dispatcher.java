package de.medizininformatikinitiative.medgraph.common.mvc;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Abstract superclass for other classes which want to dispatch events to listeners.
 * <p>
 * This class takes care of registering and unregistering the listeners. Every listener can only be registered once.
 *
 * @param <T> the type of the listener class.
 * @author Markus Budeus
 */
public abstract class Dispatcher<T> {

	// Yeah, I totally copied this class from a different project of mine :)

	private final Set<T> listeners = ConcurrentHashMap.newKeySet();

	protected Dispatcher() {
	}

	/**
	 * Registers a listener on this dispatcher, causing it to receive whatever events are dispatched by this class.
	 * <p>
	 * Registering a listener multiple times will not cause it to be notified multiple times about events.
	 *
	 * @param listener the listener to register
	 */
	public void registerListener(T listener) {
		listeners.add(listener);
	}

	/**
	 * Unregisters a listener from this dispatcher, causing it to no longer receive events from this class.
	 * <p>
	 * Unregistering a listener which is not registered has no effect.
	 *
	 * @param listener the listener to unregister
	 */
	public void unregisterListener(T listener) {
		listeners.remove(listener);
	}

	/**
	 * Dispatches an event to all currently registered listeners.
	 * @param event the action to invoke on every active listener.
	 */
	protected void dispatchEvent(Consumer<T> event) {
		listeners.forEach(event);
	}

}

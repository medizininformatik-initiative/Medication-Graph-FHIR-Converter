package de.medizininformatikinitiative.medgraph.common.mvc;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.medizininformatikinitiative.medgraph.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract test class for dispatchers. This class only tests basic listener invocation functionality, it's the
 * responsibility of subclasses to verify that the correct data is handed to the listeners.
 *
 * @param <T> the type of dispatcher to test
 * @param <U> the type of listener used by the dispatcher
 * @author Markus Budeus
 */
public abstract class DispatcherTest<T extends Dispatcher<U>, U> extends UnitTest {

	protected T sut;

	@BeforeEach
	public void setUp() throws Exception {
		sut = constructSut();
	}

	@Test
	public void simpleDispatch() {
		U listener = constructListenerMock();
		sut.registerListener(listener);
		dispatchEvent(sut);
		verifyEventReceived(listener);
	}

	@Test
	public void multiListenerDispatch() {
		U l1 = constructListenerMock();
		U l2 = constructListenerMock();
		U l3 = constructListenerMock();

		sut.registerListener(l1);
		sut.registerListener(l2);
		sut.registerListener(l3);

		dispatchEvent(sut);
		verifyEventReceived(l1);
		verifyEventReceived(l2);
		verifyEventReceived(l3);
	}

	@Test
	public void removeListeners() {
		U l1 = constructListenerMock();
		U l2 = constructListenerMock();
		U l3 = constructListenerMock();

		sut.registerListener(l1);
		sut.registerListener(l2);
		sut.registerListener(l3);

		sut.unregisterListener(l1);

		dispatchEvent(sut);
		verifyEventReceived(l1, 0);
		verifyEventReceived(l2);
		verifyEventReceived(l3);

		sut.unregisterListener(l2);

		dispatchEvent(sut);
		verifyEventReceived(l1, 0);
		verifyEventReceived(l2, 1);
		verifyEventReceived(l3, 2);
	}

	@Test
	public void concurrentAdd() throws InterruptedException {
		int amount = 100;

		ArrayList<U> listeners = new ArrayList<>(amount);
		Thread[] threads = new Thread[amount];
		for (int i = 0; i < amount; i++) {
			U listener = constructListenerMock();
			listeners.add(listener);
			threads[i] = new Thread(() -> sut.registerListener(listener));
		}

		for (Thread t : threads) {
			t.start();
		}
		for (Thread t : threads) {
			t.join();
		}

		dispatchEvent(sut);

		for (U listener : listeners) {
			verifyEventReceived(listener);
		}
	}

	@Test
	public void concurrentAddAndDispatch() throws InterruptedException {
		int amount = 100;
		int dispatches = 20;

		Thread[] threads = new Thread[amount + dispatches];
		AtomicBoolean success = new AtomicBoolean(true);

		for (int i = 0; i < amount; i++) {
			U listener = constructListenerMock();
			threads[i] = new Thread(() -> {
				try {
					sut.registerListener(listener);
				} catch (Exception e) {
					e.printStackTrace();
					success.set(false);
				}
			});
		}
		for (int i = 0; i < dispatches; i++) {
			threads[amount + i] = new Thread(() -> {
				try {
					dispatchEvent(sut);
				} catch (Exception e) {
					e.printStackTrace();
					success.set(false);
				}
			});
		}

		UnitTest.shuffleArray(threads);

		// Only verify this does not crash with ConcurrentModificationException
		for (Thread t : threads) {
			t.start();
		}
		for (Thread t : threads) {
			t.join();
		}

		assertTrue(success.get(), "At least one thread threw an exception!");
	}

	@Test
	public void listenersNotStaticallySaved() {
		T sut2 = constructSut();

		U l1 = constructListenerMock();
		U l2 = constructListenerMock();
		U l3 = constructListenerMock();

		sut.registerListener(l1);
		sut.registerListener(l2);
		sut2.registerListener(l1);
		sut2.registerListener(l3);

		dispatchEvent(sut);
		verifyEventReceived(l1, 1);
		verifyEventReceived(l2, 1);
		verifyEventReceived(l3, 0);

		sut2.unregisterListener(l1);

		dispatchEvent(sut);
		verifyEventReceived(l1, 2);
		verifyEventReceived(l2, 2);
		verifyEventReceived(l3, 0);
	}

	/**
	 * Constructs a SUT. Be aware this method may be called multiple times within a single test.
	 */
	protected abstract T constructSut();

	/**
	 * Constructs a listener. Be aware this method may be called multiple times within a single test.
	 */
	protected abstract U constructListenerMock();

	/**
	 * Dispatches a simple event.
	 *
	 * @param dispatcher the dispatcher which shall dispatch the event
	 */
	protected abstract void dispatchEvent(T dispatcher);

	/**
	 * Like {@link #verifyEventReceived(Object, int)} with the amount of times being 1.
	 */
	protected void verifyEventReceived(U listener) {
		verifyEventReceived(listener, 1);
	}

	/**
	 * Verifies the type of event dispatched by {@link #dispatchEvent(Dispatcher)} has been received by the listener the
	 * given amount of times in the current test.
	 *
	 * @param listener the listener which should have received the event
	 * @param times    the amount of times the listener should have been invoked on the current test
	 */
	protected abstract void verifyEventReceived(U listener, int times);

}
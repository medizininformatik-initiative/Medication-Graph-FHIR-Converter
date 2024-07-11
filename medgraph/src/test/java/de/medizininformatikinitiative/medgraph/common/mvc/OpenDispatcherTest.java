package de.medizininformatikinitiative.medgraph.common.mvc;

import static org.mockito.Mockito.*;

/**
 * @author Markus Budeus
 */
@SuppressWarnings("ClassEscapesDefinedScope")
public class OpenDispatcherTest extends DispatcherTest<OpenDispatcher<OpenDispatcherTest.Listener>, OpenDispatcherTest.Listener> {

	@Override
	protected OpenDispatcher<Listener> constructSut() {
		return new OpenDispatcher<>();
	}

	@Override
	protected Listener constructListenerMock() {
		return mock();
	}

	@Override
	protected void dispatchEvent(OpenDispatcher<Listener> dispatcher) {
		dispatcher.dispatchEvent(Listener::invoke);
	}

	@Override
	protected void verifyEventReceived(Listener listener, int times) {
		verify(listener, times(times)).invoke();
	}

	static class Listener {
		void invoke() {

		}
	}

}

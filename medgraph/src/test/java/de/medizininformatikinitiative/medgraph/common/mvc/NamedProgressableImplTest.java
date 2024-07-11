package de.medizininformatikinitiative.medgraph.common.mvc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Markus Budeus
 */
public class NamedProgressableImplTest extends DispatcherTest<NamedProgressableImpl, Progressable.Listener> {

	@Test
	public void initialState() {
		assertEquals(0, sut.getProgress());
		assertEquals(5, sut.getMaxProgress());
		assertArrayEquals(new String[0], sut.getCurrentTaskStack());
	}

	@Test
	public void setTaskStack() {
		NamedProgressable.Listener listener = mock();
		Progressable.Listener listener1 = mock();
		sut.registerListener(listener);
		sut.registerListener(listener1);

		sut.setTaskStack("A", "B");

		verify(listener).onTaskStackChanged(new String[] { "A", "B" });
		assertArrayEquals(new String[] {"A", "B"}, sut.getCurrentTaskStack());
		verify(listener1, never()).onProgressChanged(anyInt(), anyInt());
	}

	@Test
	public void setProgress() {
		NamedProgressable.Listener listener = mock();
		Progressable.Listener listener1 = mock();
		sut.registerListener(listener);
		sut.registerListener(listener1);

		sut.setProgress(2);

		verify(listener).onProgressChanged(2, 5);
		verify(listener1).onProgressChanged(2, 5);
		assertEquals(2, sut.getProgress());
	}


	@Test
	public void setMaxProgress() {
		sut.setProgress(1);

		NamedProgressable.Listener listener = mock();
		Progressable.Listener listener1 = mock();
		sut.registerListener(listener);
		sut.registerListener(listener1);

		sut.setMaxProgress(2);

		verify(listener).onProgressChanged(1, 2);
		verify(listener1).onProgressChanged(1, 2);
		assertEquals(2, sut.getMaxProgress());
	}

	@Override
	protected NamedProgressableImpl constructSut() {
		return new NamedProgressableImpl(5);
	}

	@Override
	protected Progressable.Listener constructListenerMock() {
		return mock();
	}

	@Override
	protected void dispatchEvent(NamedProgressableImpl dispatcher) {
		dispatcher.dispatchEvent(listener -> listener.onProgressChanged(3, 5));
	}

	@Override
	protected void verifyEventReceived(Progressable.Listener listener, int times) {
		verify(listener, times(times)).onProgressChanged(3, 5);
	}
}
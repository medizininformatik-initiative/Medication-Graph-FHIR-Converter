package de.medizininformatikinitiative.medgraph.common.mvc;

import org.jetbrains.annotations.NotNull;

/**
 * Incomplete implementation of {@link NamedProgressable} taking care of boilerplate stuff.
 *
 * @author Markus Budeus
 */
public class NamedProgressableImpl extends Dispatcher<Progressable.Listener> implements NamedProgressable {

	private int progress = 0;
	private int maxProgress = 1;
	@NotNull
	private String[] taskStack = new String[0];

	public NamedProgressableImpl() {
		this(1);
	}

	public NamedProgressableImpl(int maxProgress) {
		this.maxProgress = maxProgress;
	}

	protected void setTaskStack(String... taskStack) {
		this.taskStack = taskStack;
		dispatchEvent(listener -> {
			if (listener instanceof NamedProgressable.Listener l) l.onTaskStackChanged(taskStack);
		});
	}

	protected void setProgress(int progress) {
		this.progress = progress;
		dispatchEvent(listener -> listener.onProgressChanged(progress, maxProgress));
	}

	protected void setMaxProgress(int maxProgress) {
		this.maxProgress = maxProgress;
		dispatchEvent(listener -> listener.onProgressChanged(progress, maxProgress));
	}

	@Override
	public int getMaxProgress() {
		return maxProgress;
	}

	/**
	 * {@inheritDoc}
	 * Passing a {@link NamedProgressable.Listener} works and will cause the corresponding task stack updates to be
	 * dispatched.
	 */
	@Override
	public void registerListener(Progressable.Listener listener) {
		super.registerListener(listener);
	}

	@Override
	public int getProgress() {
		return progress;
	}

	@Override
	public @NotNull String[] getCurrentTaskStack() {
		return taskStack;
	}

}

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
		if (taskStack == null) throw new NullPointerException("The task stack cannot be null!");
		this.taskStack = taskStack;
		dispatchEvent(listener -> {
			if (listener instanceof NamedProgressable.Listener l) l.onTaskStackChanged(taskStack);
		});
	}

	protected void setProgress(int progress) {
		this.progress = progress;
		dispatchProgressChanged();
	}

	protected void setMaxProgress(int maxProgress) {
		this.maxProgress = maxProgress;
		dispatchProgressChanged();
	}

	protected void setProgress(int progress, int maxProgress) {
		this.progress = progress;
		this.maxProgress = maxProgress;
		dispatchProgressChanged();
	}

	protected void incrementProgress() {
		this.setProgress(progress + 1);
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

	private void dispatchProgressChanged() {
		dispatchEvent(listener -> listener.onProgressChanged(progress, maxProgress));
	}

}

package de.medizininformatikinitiative.medgraph.common.mvc;

/**
 * Represents a task capable of reporting progress.
 *
 * @author Markus Budeus
 */
public interface Progressable {

	/**
	 * Returns the current progress of the task.
	 */
	int getProgress();

	/**
	 * Returns the max progress of the task, i.e. the progress when the task is complete.
	 */
	int getMaxProgress();

	/**
	 * Registers a {@link Listener} on this instance.
	 *
	 * @param listener the listener to send callbacks to from now on
	 */
	void registerListener(Listener listener);

	/**
	 * Unregisters the listener from this instance.
	 *
	 * @param listener the listener to unregister
	 */
	void unregisterListener(Listener listener);

	/**
	 * A listener receiving callbacks from {@link Progressable} tasks.
	 */
	interface Listener {
		/**
		 * Invoked when the progress has changed.
		 *
		 * @param progress the current progress
		 * @param maxProgress the maximum progress
		 */
		void onProgressChanged(int progress, int maxProgress);
	}

}

package de.medizininformatikinitiative.medgraph.common.mvc;

import org.jetbrains.annotations.NotNull;

/**
 * {@link Progressable}-extension which provides a name for the task currently being executed (as part of a major, progressable task).
 *
 * @author Markus Budeus
 */
public interface NamedProgressable extends Progressable {

	/**
	 * Returns the names of the tasks currently underway. The first entry is the name of the primary task, the second
	 * entry is the current subtask of the primary task which is underway, the third entry the current subtask of the
	 * secondary task, ...
	 */
	@NotNull
	String[] getCurrentTaskStack();

	interface Listener extends Progressable.Listener {

		/**
		 * Called when a task in the task stack changed.
		 * @param taskStack the current task stack
		 */
		void onTaskStackChanged(String[] taskStack);

	}

}

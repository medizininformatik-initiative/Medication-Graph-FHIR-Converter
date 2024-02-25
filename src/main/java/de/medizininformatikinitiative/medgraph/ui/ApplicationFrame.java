package de.medizininformatikinitiative.medgraph.ui;

import javax.swing.*;

/**
 * A frame of the application between which the application JFrame can switch.
 *
 * @author Markus Budeus
 */
public class ApplicationFrame extends JPanel {

	protected final Runnable completionCallback;

	public ApplicationFrame(Runnable completionCallback) {
		this.completionCallback = completionCallback;
	}

	protected void onNavigateTo() {

	}

	protected void complete() {
		if (completionCallback != null)
			completionCallback.run();
	}
}

package de.tum.med.aiim.markusbudeus.ui;

import javax.swing.*;

public class ApplicationFrame extends JPanel {

	protected final Runnable completionCallback;

	public ApplicationFrame(Runnable completionCallback) {
		this.completionCallback = completionCallback;
	}

	protected void complete() {
		if (completionCallback != null)
			completionCallback.run();
	}
}

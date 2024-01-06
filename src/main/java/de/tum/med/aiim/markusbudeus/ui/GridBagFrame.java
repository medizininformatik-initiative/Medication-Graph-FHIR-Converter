package de.tum.med.aiim.markusbudeus.ui;

import java.awt.*;

public class GridBagFrame extends ApplicationFrame {

	protected GridBagConstraints gbc;

	public GridBagFrame(Runnable completionCallback) {
		super(completionCallback);

		GridBagLayout layout = new GridBagLayout();
		gbc = new GridBagConstraints();
		layout.setConstraints(this, gbc);
		setLayout(layout);

	}

}

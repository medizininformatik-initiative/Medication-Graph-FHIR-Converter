package de.tum.med.aiim.markusbudeus.ui;

import java.awt.*;

public class GridBagFrame extends ApplicationFrame {

	protected GridBagConstraints gbc;

	public GridBagFrame(Runnable completionCallback) {
		super(completionCallback);

		GridBagLayout layout = new GridBagLayout();
		gbc = new GridBagConstraints();

		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.insets = new Insets(2, 2, 2, 2);

		layout.setConstraints(this, gbc);
		setLayout(layout);

	}

}

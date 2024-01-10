package de.tum.med.aiim.markusbudeus.ui;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends ApplicationFrame {

	private Runnable onSelectConnectionOptions;
	private Runnable onSelectGraphPopulator;
	private Runnable onSelectSearchDialog;

	public MainMenu(Runnable completionCallback) {
		super(completionCallback);

		setLayout(new GridLayout(3, 1));

		JButton buttonConnectionOptions = new JButton("Configure Database Connection");
		JButton buttonGraphPopulator = new JButton("Populate Database");
		JButton buttonSearch = new JButton("Search Database");

		add(buttonGraphPopulator);
		add(buttonSearch);
		add(buttonConnectionOptions);

		buttonConnectionOptions.addActionListener((a) -> run(onSelectConnectionOptions));
		buttonGraphPopulator.addActionListener((a) -> run(onSelectGraphPopulator));
		buttonSearch.addActionListener((a) -> run(onSelectSearchDialog));
	}

	public void setOnSelectConnectionOptions(Runnable onSelectConnectionOptions) {
		this.onSelectConnectionOptions = onSelectConnectionOptions;
	}

	public void setOnSelectGraphPopulator(Runnable onSelectGraphPopulator) {
		this.onSelectGraphPopulator = onSelectGraphPopulator;
	}

	public void setOnSelectSearchDialog(Runnable onSelectSearchDialog) {
		this.onSelectSearchDialog = onSelectSearchDialog;
	}

	private void run(Runnable runnable) {
		if (runnable != null) runnable.run();
	}

}

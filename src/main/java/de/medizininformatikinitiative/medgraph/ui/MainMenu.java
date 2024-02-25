package de.medizininformatikinitiative.medgraph.ui;

import javax.swing.*;
import java.awt.*;

/**
 * The main menu frame.
 *
 * @author Markus Budeus
 */
public class MainMenu extends ApplicationFrame {

	private Runnable onSelectConnectionOptions;
	private Runnable onSelectGraphPopulator;
	private Runnable onSelectSearchDialog;

	private Runnable onSelectExportDialog;

	public MainMenu(Runnable completionCallback) {
		super(completionCallback);

		setLayout(new GridLayout(4, 1));

		JButton buttonConnectionOptions = new JButton("Configure Database Connection");
		JButton buttonGraphPopulator = new JButton("Populate Database");
		JButton buttonSearch = new JButton("Search Database");
		JButton buttonExport = new JButton("Export FHIR Instances");

		add(buttonGraphPopulator);
		add(buttonSearch);
		add(buttonExport);
		add(buttonConnectionOptions);

		buttonConnectionOptions.addActionListener((a) -> run(onSelectConnectionOptions));
		buttonGraphPopulator.addActionListener((a) -> run(onSelectGraphPopulator));
		buttonSearch.addActionListener((a) -> run(onSelectSearchDialog));
		buttonExport.addActionListener((a) -> run(onSelectExportDialog));
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

	public void setOnSelectExportDialog(Runnable onSelectExportDialog) {
		this.onSelectExportDialog = onSelectExportDialog;
	}

	private void run(Runnable runnable) {
		if (runnable != null) runnable.run();
	}

}

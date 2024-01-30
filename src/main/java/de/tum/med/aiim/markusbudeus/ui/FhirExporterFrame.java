package de.tum.med.aiim.markusbudeus.ui;

import de.tum.med.aiim.markusbudeus.fhirexporter.Main;
import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Set;

import static java.awt.GridBagConstraints.*;

public class FhirExporterFrame extends GridBagFrame {

	private static final String MEDICATION_OUT = "medication";
	private static final String SUBSTANCE_OUT = "substance";
	private static final String COMPANY_OUT = "organisation";

	public static void main(String[] args) {
		Tools.createFrame(FhirExporterFrame::new);
	}

	private final JTextField txtOutputDir;
	private final JButton buttonExport;
	private final JButton buttonReturn;
	private final JLabel errorText = new JLabel(" ");

	private boolean exportUnderway = false;

	public FhirExporterFrame(Runnable completionCallback) {
		super(completionCallback);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = HORIZONTAL;
		gbc.gridwidth = REMAINDER;
		gbc.weighty = 0;
		JLabel description1 = new JLabel("Use this tool to export FHIR instances from the Graph Database.");
		JLabel description2 = new JLabel("Specify a directory into which to export and then run the export.");
		add(description1, gbc);
		gbc.gridy = 1;
		add(description2, gbc);

		gbc.anchor = WEST;
		gbc.gridwidth = 1;
		gbc.gridy = 2;
		gbc.weightx = 0;
		JLabel outputDir = new JLabel("Export Path: ");
		add(outputDir, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.anchor = EAST;
		txtOutputDir = new JTextField(40);
		add(txtOutputDir, gbc);

		gbc.gridx = 2;
		gbc.weightx = 0;
		JButton buttonBrowse = new JButton("Browse");
		buttonBrowse.addActionListener(e -> {
			File dir = Tools.selectDirectory(this);
			if (dir != null) txtOutputDir.setText(dir.getAbsolutePath());
		});
		add(buttonBrowse, gbc);

		gbc.gridy += 1;
		gbc.gridx = 2;
		gbc.anchor = EAST;
		gbc.fill = NONE;
		buttonExport = new JButton("Run export");
		buttonExport.addActionListener(e -> runExport());
		add(buttonExport, gbc);

		gbc.gridx = 0;
		gbc.anchor = WEST;
		buttonReturn = new JButton("Exit");
		buttonReturn.addActionListener(e -> this.complete());
		add(buttonReturn, gbc);

		gbc.gridy += 1;
		gbc.gridwidth = REMAINDER;
		gbc.fill = HORIZONTAL;
		errorText.setForeground(Color.red);
		add(errorText, gbc);

		gbc.gridy += 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = BOTH;
		TextOutputArea.INSTANCE.clear();
		add(TextOutputArea.INSTANCE, gbc);
	}

	private void runExport() {
		if (exportUnderway) return;
		errorText.setText(" ");
		exportUnderway = true;
		buttonExport.setEnabled(false);
		buttonReturn.setEnabled(false);

		new Thread(() -> {
			long time = System.currentTimeMillis();
			System.out.println("Running export, this may take a while...");
			try {
				System.out.println("Preparing output directory...");
				Path outputDir = resolveAndVerifyExportDir();
				if (outputDir == null) return;

				InputStream noticeInput = Objects.requireNonNull(getClass().getResourceAsStream("/NOTICE.txt"));
				Files.copy(noticeInput, outputDir.resolve("NOTICE.TXT"), StandardCopyOption.REPLACE_EXISTING);

				doExports(outputDir);
				System.out.println("All done. ("+(System.currentTimeMillis()-time)+"ms)");

			} catch (AccessDeniedException e) {
				errorText.setText("Access to output directory denied.");
			} catch (IOException e) {
				errorText.setText("Something went wrong.");
				e.printStackTrace();
			} finally {
				exportUnderway = false;
				buttonExport.setEnabled(true);
				buttonReturn.setEnabled(true);
			}
		}).start();
	}

	private void doExports(Path directory) {
		DatabaseConnection.runSession(session -> {
			try {
				System.out.println("Exporting substances...");
				Main.exportSubstances(session, directory.resolve(SUBSTANCE_OUT), false);
				System.out.println("Exporting medications...");
				Main.exportMedications(session, directory.resolve(MEDICATION_OUT), false);
				System.out.println("Exporting organizations...");
				Main.exportOrganizations(session, directory.resolve(COMPANY_OUT));
			} catch (IOException e) {
				errorText.setText("Something went wrong.");
				e.printStackTrace();
			}
		});
	}

	/**
	 * Verifies the output dir exists as well as the required subdirectories, creating them.
	 */
	private Path resolveAndVerifyExportDir() throws IOException {
		String text = txtOutputDir.getText();

		if (text.isBlank()) {
			errorText.setText("You must specify an export directory!");
			return null;
		}

		Path path = Path.of(text);

		if (Files.exists(path)) {
			if (!Files.isDirectory(path)) {
				errorText.setText("The output path is not a directory!");
				return null;
			}
		} else {
			Files.createDirectories(path);
		}

		for (String outputDir : Set.of(MEDICATION_OUT, SUBSTANCE_OUT, COMPANY_OUT)) {
			Path p = path.resolve(outputDir);
			if (Files.exists(p)) {
				if (!Files.isDirectory(p)) {
					errorText.setText("A file named " + outputDir + " already exists in the output directory!");
					return null;
				}
			} else {
				Files.createDirectory(p);
			}
		}

		return path;
	}


}

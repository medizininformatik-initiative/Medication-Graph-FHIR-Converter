package de.tum.med.aiim.markusbudeus.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import static java.awt.GridBagConstraints.*;

public class PopulatorUi extends GridBagFrame {

	private static final String[] REQUIRED_FILES = new String[] {
			"CATALOGENTRY.CSV",
			"COMPANY.CSV",
			"COMPANYADDRESS.CSV",
			"COMPOSITIONELEMENT.CSV",
			"COMPOSITIONELEMENTEQUI.CSV",
			"ITEM.CSV",
			"ITEM_ATC.CSV",
			"ITEM_COMPOSITIONELEMENT.CSV",
			"MOLECULE.CSV",
			"PACKAGE.CSV",
			"PRODUCT.CSV",
			"PRODUCT_COMPANY.CSV",
			"PRODUCT_FLAG.CSV"
	};
	private static final String[] REQUIRED_RESOURCE_FILES = new String[] {
			"amice_stoffbezeichnungen_utf8.csv",
			"custom_synonymes.csv",
			"dose_form_mapping.csv",
			"gsrs_matches.csv",
	};

	private final PrintStream cachedSysOut = System.out;

	private final JTextField txtMmiPharmindexData;
	private final JTextField txtNeo4jImportDirectory;

	private final JLabel errorLabel = new JLabel();

	private final TextOutputArea consoleOutput;

	public PopulatorUi(Runnable completionCallback) {
		super(completionCallback);


		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = EAST;
		gbc.insets.right = 8;

		txtMmiPharmindexData = new JTextField();
		txtNeo4jImportDirectory = new JTextField();

		add(new JLabel("Path to MMI Pharmindex raw data:"), gbc);
		gbc.gridy = 1;
		add(new JLabel("Path to Neo4j import directory: "), gbc);
		gbc.insets.right = 0;

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = WEST;
		add(txtMmiPharmindexData);
		gbc.gridy = 1;
		add(txtNeo4jImportDirectory);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = REMAINDER;
		gbc.anchor = CENTER;
		add(new JLabel("In RPM-based distributions, the default Neo4j import directory is /var/lib/neo4j/import\n" +
				"Otherwise, it's in <neo4j-home>/import"), gbc);

		gbc.gridy = 3;
		JButton buttonRun = new JButton("Run import");
		buttonRun.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				runImport();
			}
		});
		add(buttonRun, gbc);


		gbc.gridy = 4;
		errorLabel.setForeground(Color.red);
		add(errorLabel, gbc);

		gbc.gridy = 5;

		consoleOutput = new TextOutputArea();
		add(consoleOutput, gbc);
	}

	private void runImport() {
		errorLabel.setText("");
		try {
			File mmiPharmindexDir = checkAndGetMmiPharmindexDir();
			if (mmiPharmindexDir == null) return;

			if (!copyRequiredFilesToImportDir(mmiPharmindexDir)) {
				return;
			}


		} catch (IOException e) {
			// TODO
			errorLabel.setText("Something went wrong.");
		} catch (NullPointerException | URISyntaxException e) {
			errorLabel.setText("Something went wrong. Please contact the developer.");
		}
	}

	private File checkAndGetMmiPharmindexDir() {
		File dir = new File(txtMmiPharmindexData.getText());
		if (!dir.exists()) {
			errorLabel.setText("The given directory with the MMI Pharmindex data does not exist!");
			return null;
		}
		if (!dir.isDirectory()) {
			errorLabel.setText("The path given as MMI Pharmindex data directory does not point to a directory!");
			return null;
		}

		for (String requiredFile: REQUIRED_FILES) {
			File target = new File(dir.getAbsolutePath() + File.separator + requiredFile);
			if (!target.exists()) {
				errorLabel.setText("Could not find the file "+requiredFile+" in the MMI Pharmindex data directory!");
				return null;
			}
		}
		return dir;
	}

	private boolean copyRequiredFilesToImportDir(File sourceDir) throws IOException, URISyntaxException {
		File targetDir = new File(txtNeo4jImportDirectory.getText());
		if (!targetDir.exists()) {
			errorLabel.setText("The given Neo4j import directory does not exist!");
			return false;
		}
		if (!targetDir.isDirectory()) {
			errorLabel.setText("The given Neo4j import directory is not a directory!");
			return false;
		}

		File mmiTargetDir = new File(targetDir + File.separator + "mmi_pharmindex");
		if (!mmiTargetDir.exists())
			if (!mmiTargetDir.mkdir())
				throw new IOException("Failed to create subdirectory in Neo4j import directory");

		Path target = mmiTargetDir.toPath();
		Path source = sourceDir.toPath();
		for (String file: REQUIRED_FILES) {
			Path original = source.resolve(file);
			Path targetFile = target.resolve(file);
			Files.copy(original, targetFile, StandardCopyOption.REPLACE_EXISTING);
		}

		target = targetDir.toPath();
		for (String resource : REQUIRED_RESOURCE_FILES) {
			Path resPath = Path.of(Objects.requireNonNull(getClass().getResource("/" + resource)).toURI());
			Path targetPath = target.resolve(resource);
			Files.copy(resPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
		}
		return true;
	}

	@Override
	protected void complete() {
		System.setOut(cachedSysOut);
	}
}

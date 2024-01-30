package de.tum.med.aiim.markusbudeus.ui;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import static java.awt.GridBagConstraints.*;

public class PopulatorUi extends GridBagFrame {

	public static void main(String[] args) {
		Tools.createFrame(PopulatorUi::new);
	}

	private static final String[] REQUIRED_FILES = new String[]{
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
	private static final String[] REQUIRED_RESOURCE_FILES = new String[]{
			"amice_stoffbezeichnungen_utf8.csv",
			"custom_synonymes.csv",
			"dose_form_mapping.csv",
			"gsrs_matches.csv",
			"NOTICE.txt",
	};

	private static final String MMI_PHARMINDEX_FILES_SUBPATH = "mmi_pharmindex";

	private final JTextField txtMmiPharmindexData;
	private final JTextField txtNeo4jImportDirectory;

	private final JLabel errorLabel = new JLabel(" ");
	private final JButton buttonRun;
	private final JButton buttonExit;

	public PopulatorUi(Runnable completionCallback) {
		super(completionCallback);

		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = EAST;
		gbc.insets.right = 8;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridwidth = REMAINDER;
		add(new JLabel("Use this tool to populate your Neo4j database with the medication data from (primarily) the MMI Pharmindex."), gbc);

		gbc.gridy = 1;
		gbc.gridwidth = 1;
		add(new JLabel("Path to MMI Pharmindex raw data:"), gbc);
		gbc.gridy = 2;
		add(new JLabel("Path to Neo4j import directory: "), gbc);
		gbc.insets.right = 0;

		Font font = new Font("Monospaced", Font.PLAIN, 14);
		txtMmiPharmindexData = new JTextField("", 40);
		txtNeo4jImportDirectory = new JTextField("", 40);
		txtMmiPharmindexData.setFont(font);
		txtNeo4jImportDirectory.setFont(font);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = WEST;
		gbc.weightx = 1.0;
		add(txtMmiPharmindexData, gbc);
		gbc.gridy = 2;
		add(txtNeo4jImportDirectory, gbc);

		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		JButton browseMmiPharmIndex = new JButton("Browse");
		browseMmiPharmIndex.addActionListener(e -> {
			File f = Tools.selectDirectory(this);
			if (f != null)
				txtMmiPharmindexData.setText(f.getAbsolutePath());
		});
		add(browseMmiPharmIndex, gbc);
		gbc.gridy = 2;
		JButton browseNeo4jImport = new JButton("Browse");
		browseNeo4jImport.addActionListener(e -> {
			File f = Tools.selectDirectory(this);
			if (f != null)
				txtNeo4jImportDirectory.setText(f.getAbsolutePath());
		});
		add(browseNeo4jImport, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 1.0;
		gbc.gridwidth = REMAINDER;
		gbc.anchor = CENTER;
		add(new JLabel("In RPM-based distributions, the default Neo4j import directory is /var/lib/neo4j/import"), gbc);

		gbc.gridy = 4;
		add(new JLabel("Otherwise, it's <neo4j-home>/import"), gbc);

		gbc.gridy = 5;
		gbc.fill = NONE;
		gbc.gridwidth = 1;
		gbc.anchor = WEST;
		buttonExit = new JButton("Back");
		buttonExit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				complete();
			}
		});
		add(buttonExit, gbc);

		gbc.gridx = 2;
		gbc.anchor = EAST;
		buttonRun = new JButton("Run import");
		buttonRun.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				runImport();
			}
		});
		add(buttonRun, gbc);

		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.anchor = CENTER;
		gbc.fill = HORIZONTAL;
		gbc.gridwidth = REMAINDER;
		errorLabel.setForeground(Color.red);
		add(errorLabel, gbc);

		gbc.gridy = 7;
		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.fill = BOTH;
		add(TextOutputArea.INSTANCE, gbc);
	}

	private void runImport() {
		errorLabel.setText(" ");
		buttonRun.setEnabled(false);
		buttonExit.setEnabled(false);

		Thread executor = new Thread(()  -> {
			try {
				System.out.println("Running import. Depending on your system, this may take a few minutes.");
				System.out.println("Preparing file import...");
				File mmiPharmindexDir = checkAndGetMmiPharmindexDir();
				if (mmiPharmindexDir == null) return;

				String neo4jImportPath = txtNeo4jImportDirectory.getText();
				if (!copyRequiredFilesToImportDir(mmiPharmindexDir, neo4jImportPath)) {
					return;
				}

				System.out.println("Populating database...");
				Main.runMigrators();

				System.out.println("Cleaning up...");
				removeFilesFromNeo4jImportDir(neo4jImportPath);

				System.out.println("All done. The database is ready.");

			} catch (AccessDeniedException e) {
				errorLabel.setText("Access denied: "+e.getMessage());
			} catch (NullPointerException | URISyntaxException e) {
				errorLabel.setText("Something went wrong. Please contact the developer.");
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
				errorLabel.setText("Something went wrong.");
			} finally {
				buttonRun.setEnabled(true);
				buttonExit.setEnabled(true);
			}
		});
		executor.start();
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

		for (String requiredFile : REQUIRED_FILES) {
			File target = new File(dir.getAbsolutePath() + File.separator + requiredFile);
			if (!target.exists()) {
				errorLabel.setText(
						"Could not find the file " + requiredFile + " in the MMI Pharmindex data directory!");
				return null;
			}
		}
		return dir;
	}

	private boolean copyRequiredFilesToImportDir(File sourceDir, String neo4jImportPath)
	throws IOException, URISyntaxException {
		File targetDir = new File(neo4jImportPath);
		if (!targetDir.exists()) {
			errorLabel.setText("The given Neo4j import directory does not exist!");
			return false;
		}
		if (!targetDir.isDirectory()) {
			errorLabel.setText("The given Neo4j import directory is not a directory!");
			return false;
		}

		File mmiTargetDir = new File(targetDir + File.separator + MMI_PHARMINDEX_FILES_SUBPATH);
		if (!mmiTargetDir.exists())
			if (!mmiTargetDir.mkdir())
				throw new IOException("Failed to create subdirectory in Neo4j import directory");

		// Copy MMI Pharmindex files
		Path target = mmiTargetDir.toPath();
		Path source = sourceDir.toPath();
		for (String file : REQUIRED_FILES) {
			Path original = source.resolve(file);
			Path targetFile = target.resolve(file);
			Files.copy(original, targetFile, StandardCopyOption.REPLACE_EXISTING);
		}

		// Copy other files
		target = targetDir.toPath();
		for (String resource : REQUIRED_RESOURCE_FILES) {
			InputStream stream = Objects.requireNonNull(getClass().getResourceAsStream("/" + resource));
			Path targetPath = target.resolve(resource);
			Files.copy(stream, targetPath, StandardCopyOption.REPLACE_EXISTING);
		}
		return true;
	}

	private void removeFilesFromNeo4jImportDir(String neo4jImportPath) throws IOException {
		Path neo4jImportDir = Path.of(neo4jImportPath);

		Path mmiTargetDir = neo4jImportDir.resolve(MMI_PHARMINDEX_FILES_SUBPATH);

		for (String filename : REQUIRED_FILES) {
			Files.delete(mmiTargetDir.resolve(filename));
		}

		for (String filename : REQUIRED_RESOURCE_FILES) {
			Files.delete(neo4jImportDir.resolve(filename));
		}

		Files.delete(mmiTargetDir);
	}

}

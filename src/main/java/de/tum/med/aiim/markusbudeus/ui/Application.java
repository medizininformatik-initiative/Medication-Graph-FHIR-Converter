package de.tum.med.aiim.markusbudeus.ui;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Function;

public class Application extends JFrame {

	public static void main(String[] args) {

		for (int i = 0; i < args.length - 1; i += 2) {
			applyArgument(args[i], args[i+1]);
		}

		EventQueue.invokeLater(() -> {
			Application application = new Application();

			try (DatabaseConnection connection = new DatabaseConnection()) {
				if (connection.testConnection() == DatabaseConnection.ConnectionResult.SUCCESS) {
					application.navigateToMainMenu();
				} else {
					application.navigateTo(ConnectionDialog::new);
				}
			} catch (Exception e) {
				application.navigateTo(ConnectionDialog::new);
			}

			application.setVisible(true);
		});
	}

	private static void applyArgument(String arg, String value) {
		switch (arg) {
			case "-dburi" -> DatabaseConnection.uri = value;
			case "-dbuser" -> DatabaseConnection.user = value;
			case "-dbpassword" -> DatabaseConnection.password = value.toCharArray();
		}
	}

	public Application() {
		setTitle("MII Medication Graph Database Tool");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
	}

	private void navigateTo(Function<Runnable, ApplicationFrame> frameConstructor) {
		navigateTo(frameConstructor.apply(this::navigateToMainMenu));
	}

	private void navigateTo(ApplicationFrame frame) {
		setContentPane(frame);
		pack();
		frame.onNavigateTo();
	}

	private void navigateToMainMenu() {
		MainMenu mainMenu = new MainMenu(this::exit);
		mainMenu.setOnSelectConnectionOptions(() -> navigateTo(ConnectionDialog::new));
		mainMenu.setOnSelectGraphPopulator(() -> navigateTo(PopulatorUi::new));
		mainMenu.setOnSelectSearchDialog(() -> navigateTo(SearchFrame::new));
		mainMenu.setOnSelectExportDialog(() -> navigateTo(FhirExporterFrame::new));
		navigateTo(mainMenu);
		setSize(600, 400);
	}

	private void exit() {
		this.dispose();
		System.exit(0);
	}

}

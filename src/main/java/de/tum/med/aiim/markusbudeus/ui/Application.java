package de.tum.med.aiim.markusbudeus.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Function;

public class Application extends JFrame {

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			Application application = new Application();
			application.navigateTo(ConnectionDialog::new);
			application.setVisible(true);
		});
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

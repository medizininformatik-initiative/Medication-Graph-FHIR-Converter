package de.tum.med.aiim.markusbudeus.ui;

import javax.swing.*;
import java.awt.*;
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
	}

	private void navigateTo(Function<Runnable, ApplicationFrame> frameConstructor) {
		navigateTo(frameConstructor.apply(this::navigateToMainMenu));
	}

	private void navigateTo(ApplicationFrame frame) {
		setContentPane(frame);
		pack();
	}

	private void navigateToMainMenu() {
		MainMenu mainMenu = new MainMenu(this::dispose);
		mainMenu.setOnSelectConnectionOptions(() -> navigateTo(ConnectionDialog::new));
		mainMenu.setOnSelectGraphPopulator(() -> navigateTo(PopulatorUi::new));
		mainMenu.setOnSelectSearchDialog(() -> navigateTo(SearchFrame::new));
		navigateTo(mainMenu);
		setSize(600, 400);
	}

}

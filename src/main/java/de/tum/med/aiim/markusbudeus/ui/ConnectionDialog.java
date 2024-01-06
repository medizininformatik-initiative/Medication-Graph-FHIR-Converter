package de.tum.med.aiim.markusbudeus.ui;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.AuthenticationException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.awt.GridBagConstraints.*;

public class ConnectionDialog extends JPanel {

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			JFrame frame = new JFrame();
			frame.add(new ConnectionDialog());
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setSize(400, 400);
			frame.setVisible(true);
		});
	}

	private final JTextField txtConnection;
	private final JTextField txtUser;
	private final JPasswordField txtPassword;
	private final JLabel errorLabel = new JLabel();

	public Runnable completionCallback;

	public ConnectionDialog() {
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		layout.setConstraints(this, gbc);
		setLayout(layout);

		gbc.gridx = 0;
		gbc.anchor = EAST;
		gbc.gridy = 0;
		gbc.insets.right = 7;
		add(createLabel("Connection URL:"), gbc);
		gbc.gridy = 1;
		add(createLabel("User:"), gbc);
		gbc.gridy = 2;
		add(createLabel("Password:"), gbc);


		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = WEST;
		gbc.insets.right = 0;
		int columns = 20;
		txtConnection = new JTextField(columns);
		txtConnection.setText(DatabaseConnection.uri);
		txtUser = new JTextField(columns);
		txtUser.setText(DatabaseConnection.user);
		txtPassword = new JPasswordField(columns);
		txtPassword.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					apply();
			}
		});
		add(txtConnection, gbc);
		gbc.gridy = 1;
		add(txtUser, gbc);
		gbc.gridy = 2;
		add(txtPassword, gbc);

		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.anchor = CENTER;
		add(Box.createVerticalStrut(10), gbc);

		gbc.gridy = 5;
		JButton buttonConfirm = new JButton("OK");
		buttonConfirm.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				apply();
			}
		});
		add(buttonConfirm, gbc);

		gbc.gridy = 6;
		errorLabel.setForeground(Color.red);
		add(errorLabel, gbc);
	}

	private void apply() {
		errorLabel.setText("");
		String uri = txtConnection.getText();
		String user = txtUser.getText();
		char[] password = txtPassword.getPassword();

		try {
			DatabaseConnection connection = new DatabaseConnection(uri, user, password);
			Session s = connection.createSession();
			s.beginTransaction();
			s.close();
			connection.close();
			DatabaseConnection.setConnection(uri, user, password);

			if (completionCallback != null)
				completionCallback.run();
		} catch (IllegalArgumentException e) {
			errorLabel.setText("Connection String invalid!");
			e.printStackTrace();
		} catch (AuthenticationException e) {
			errorLabel.setText("Authentication failed!");
		}
	}

	private JLabel createLabel(String text) {
		return new JLabel(text);
	}

}

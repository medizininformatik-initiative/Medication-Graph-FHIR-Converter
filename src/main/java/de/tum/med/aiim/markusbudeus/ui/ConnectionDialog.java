package de.tum.med.aiim.markusbudeus.ui;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.AuthenticationException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.awt.GridBagConstraints.*;

public class ConnectionDialog extends GridBagFrame {

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			JFrame frame = new JFrame();
			frame.add(new ConnectionDialog(null));
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setSize(400, 180);
			frame.setVisible(true);
		});
	}

	private final JTextField txtConnection;
	private final JTextField txtUser;
	private final JPasswordField txtPassword;
	private final JLabel errorLabel = new JLabel(" ");

	public ConnectionDialog(Runnable completionCallback) {
		super(completionCallback);

		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

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
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		int columns = 20;
		txtConnection = new JTextField(columns);
		txtConnection.setText(DatabaseConnection.uri);
		txtUser = new JTextField(columns);
		txtUser.setText(DatabaseConnection.user);
		txtPassword = new JPasswordField(columns);
		txtPassword.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
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
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		JButton buttonConfirm = new JButton("OK");
		buttonConfirm.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				apply();
			}
		});
		add(buttonConfirm, gbc);

		gbc.gridy = 6;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(Box.createVerticalStrut(10), gbc);

		gbc.gridy = 7;
		errorLabel.setForeground(Color.red);
		add(errorLabel, gbc);
	}

	private void apply() {
		errorLabel.setText(" ");
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

			complete();
		} catch (IllegalArgumentException e) {
			errorLabel.setText("Connection String invalid!");
			e.printStackTrace();
		} catch (AuthenticationException e) {
			errorLabel.setText("Authentication failed!");
		} catch (ServiceUnavailableException e) {
			errorLabel.setText("Failed to connect to Neo4j. Is the service running?");
		}
	}

	private JLabel createLabel(String text) {
		return new JLabel(text);
	}

}

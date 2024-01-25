package de.tum.med.aiim.markusbudeus.matcher.ui;

import de.tum.med.aiim.markusbudeus.matcher.model.ProductWithPzn;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class ResultDisplayComponent {

	public static JPanel construct(ProductWithPzn target, Color highlightColor) {
		JPanel outerPanel = new JPanel();
		outerPanel.setLayout(new BorderLayout());
		Border border = BorderFactory.createLineBorder(Color.black, 2);
		outerPanel.setBorder(border);
		JPanel contentPanel = new JPanel();
		Border padding = BorderFactory.createEmptyBorder(10,10, 10, 10);
		contentPanel.setBorder(padding);
		contentPanel.setLayout(new BorderLayout());

		JLabel nameLabel = new JLabel(target.getName());
		JLabel mmiIdLabel = new JLabel("MMI ID: " + target.getMmiId());
		if (highlightColor != null) {
			contentPanel.setBackground(highlightColor);
		}
		JLabel pznLabel = new JLabel("PZN: " + target.getPzn());
		nameLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
		nameLabel.setForeground(Color.black);
		mmiIdLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
		mmiIdLabel.setForeground(Color.darkGray);
		pznLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
		nameLabel.setForeground(Color.black);

		contentPanel.add(nameLabel, BorderLayout.PAGE_START);
		contentPanel.add(mmiIdLabel, BorderLayout.LINE_END);
		contentPanel.add(pznLabel, BorderLayout.LINE_START);

		outerPanel.add(contentPanel, BorderLayout.CENTER);
		outerPanel.setMaximumSize(new Dimension(2000, 60));

		return outerPanel;
	}

}

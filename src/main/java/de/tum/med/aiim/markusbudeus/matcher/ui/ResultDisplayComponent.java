package de.tum.med.aiim.markusbudeus.matcher.ui;

import de.tum.med.aiim.markusbudeus.matcher.model.FinalMatchingTarget;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class ResultDisplayComponent {

	private static final Color BEST_MATCH_COLOR = Color.yellow;
	private static final Color HIGHLIGHT_COLOR = new Color(0x91, 0xff, 0xff);

	public static JPanel construct(MatchingTarget target, boolean highlight) {
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
		String pzn = "";
		if (target instanceof FinalMatchingTarget p) {
			pzn = "PZN: " + p.getPzn();
			contentPanel.setBackground(BEST_MATCH_COLOR);
		} else if (highlight) {
			contentPanel.setBackground(HIGHLIGHT_COLOR);
		}
		JLabel pznLabel = new JLabel(pzn);
		nameLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
		nameLabel.setForeground(Color.black);
		mmiIdLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
		mmiIdLabel.setForeground(Color.darkGray);
		pznLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
		nameLabel.setForeground(Color.black);

		contentPanel.add(nameLabel, BorderLayout.PAGE_START);
		contentPanel.add(mmiIdLabel, BorderLayout.LINE_START);
		contentPanel.add(pznLabel, BorderLayout.LINE_END);

		outerPanel.add(contentPanel, BorderLayout.CENTER);
		outerPanel.setMaximumSize(new Dimension(2000, 60));

		return outerPanel;
	}

}

package de.tum.med.aiim.markusbudeus.matcher.ui;

import de.tum.med.aiim.markusbudeus.matcher.model.*;
import de.tum.med.aiim.markusbudeus.ui.Tools;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.math.BigDecimal;

import static java.awt.GridBagConstraints.*;

public class ResultDisplayComponent {

	public static void main(String[] args) {
		FinalMatchingTarget sample = new FinalMatchingTarget(1, "Amazing pharmaceutical product!", "00000101",
				java.util.List.of(
						new Drug("Tablette", "Tablet", new Amount(BigDecimal.TEN, "mg"),
								java.util.List.of(new CorrespondingActiveIngredient(
										"ASS-Pulver", new AmountRange(BigDecimal.valueOf(581), null, "mg"),
										"Acetylsalicylsäure", new AmountRange(BigDecimal.valueOf(500), null, "mg")
								))),
						new Drug("Flasche", "oral solution", new Amount(BigDecimal.valueOf(100), "ml"),
								java.util.List.of(new ActiveIngredient(
										"Water", new AmountRange(BigDecimal.valueOf(90), BigDecimal.valueOf(110), "ml")
										)))
				));
		Tools.createFrame(construct(sample, null));
	}

	public static JPanel construct(FinalMatchingTarget target, Color highlightColor) {
		JPanel outerPanel = new JPanel();
		outerPanel.setLayout(new BorderLayout());
		Border border = BorderFactory.createLineBorder(Color.black, 2);
		outerPanel.setBorder(border);
		JPanel contentPanel = new JPanel();
		Border padding = BorderFactory.createEmptyBorder(4, 8, 4, 8);
		contentPanel.setBorder(padding);
		GridBagLayout gridBagLayout = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		gridBagLayout.setConstraints(contentPanel, gbc);
		contentPanel.setLayout(gridBagLayout);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.fill = HORIZONTAL;
		gbc.gridwidth = REMAINDER;
		JLabel nameLabel = new JLabel(target.getName());
		JLabel mmiIdLabel = new JLabel("MMI ID: " + target.getMmiId());
		if (highlightColor != null) {
			contentPanel.setBackground(highlightColor);
		}
		JLabel pznLabel = new JLabel("PZN: " + target.getPzn());
		nameLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
		nameLabel.setForeground(Color.black);
		mmiIdLabel.setFont(new Font("Monospaced", Font.BOLD, 11));
		mmiIdLabel.setForeground(Color.darkGray);
		pznLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
		nameLabel.setForeground(Color.black);

		gbc.insets = new Insets(0, 0, 4, 0);
		contentPanel.add(nameLabel, gbc);
		gbc.gridy += 1;
		gbc.insets.bottom = 0;

		Font drugFont = new Font("Monospaced", Font.PLAIN, 12);
		for (Drug d : target.drugs) {
			String htmlString = "<html>" + d.toString().replace("\n", "<br>").replace(" ", "&nbsp;");
			JLabel drugLabel = new JLabel(htmlString);
			drugLabel.setFont(drugFont);
			contentPanel.add(drugLabel, gbc);
			gbc.gridy += 1;
		}

		gbc.gridwidth = 1;
		gbc.insets.top = 4;
		gbc.fill = NONE;
		gbc.anchor = WEST;
		contentPanel.add(mmiIdLabel, gbc);
		gbc.gridx = 1;
		gbc.anchor = EAST;
		contentPanel.add(pznLabel, gbc);

		outerPanel.add(contentPanel, BorderLayout.CENTER);
//		outerPanel.setMaximumSize(new Dimension(2000, 60));

		return outerPanel;
	}

}

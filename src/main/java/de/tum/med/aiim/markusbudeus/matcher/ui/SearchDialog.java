package de.tum.med.aiim.markusbudeus.matcher.ui;

import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class SearchDialog extends JSplitPane {

	private final JTextField searchField;
	private final JPanel bottom;
	private final JButton searchButton;
	private final BiConsumer<SearchDialog, String> onSearch;

	private boolean searching = false;

	private final Executor searchExecutor = Executors.newSingleThreadExecutor();

	public SearchDialog(BiConsumer<SearchDialog, String> onSearch) {
		super(JSplitPane.VERTICAL_SPLIT);

		this.onSearch = onSearch;

		JPanel top = new JPanel();

		searchField = new JTextField(null, 40);
		searchField.setFont(new Font("Monospaced", Font.PLAIN, 18));
		top.add(searchField);
		searchButton = new JButton("Search");
		top.add(searchButton);

		searchButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				considerSearch();
			}
		});
		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					considerSearch();
			}
		});

		bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane(bottom,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		setTopComponent(top);
		setBottomComponent(scrollPane);
	}

	private synchronized void considerSearch() {
		if (searching) return;
		if (onSearch != null) {
			String text = searchField.getText();
			if (!text.isBlank()) {
				search(text);
			}
		}
	}

	private void search(String text) {
		searching = true;
		searchButton.setEnabled(false);
		searchExecutor.execute(() -> {
			onSearch.accept(this, text);
			EventQueue.invokeLater(() -> {
				searchButton.setEnabled(true);
				searching = false;
			});
		});
	}

	public void applyResults(java.util.List<MatchingTarget> goodResults, java.util.List<MatchingTarget> otherResults) {
		EventQueue.invokeLater(() -> {
			bottom.removeAll();
			for (MatchingTarget t : goodResults) {
				bottom.add(ResultDisplayComponent.construct(t, true));
			}
			for (MatchingTarget t : otherResults) {
				bottom.add(ResultDisplayComponent.construct(t, false));
			}
			bottom.revalidate();
			bottom.repaint();
		});
	}

	public static void createFrame(BiConsumer<SearchDialog, String> onSearch) {
		EventQueue.invokeLater(() -> {
			JFrame frame = new JFrame("MII Medication Matcher");

			frame.add(new SearchDialog(onSearch));

			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setSize(600, 400);
			frame.setVisible(true);
		});
	}

}

package de.tum.med.aiim.markusbudeus.matcher.ui;

import de.tum.med.aiim.markusbudeus.matcher.model.FinalMatchingTarget;
import de.tum.med.aiim.markusbudeus.matcher.model.ResultSet;

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

	private static final Color BEST_MATCH_COLOR = Color.yellow;
	private static final Color GOOD_MATCH_COLOR = new Color(0x91, 0xff, 0xff);

	public static void createFrame(BiConsumer<SearchDialog, String> onSearch) {
		EventQueue.invokeLater(() -> {
			JFrame frame = new JFrame("MII Medication Matcher");

			frame.add(new SearchDialog(onSearch));

			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setSize(800, 400);
			frame.setVisible(true);
		});
	}

	private final JTextField searchField;
	private final JPanel bottom;
	private final JButton searchButton;
	private final JButton returnButton;
	private final BiConsumer<SearchDialog, String> onSearch;

	private Runnable onReturn;

	private boolean searching = false;

	private final Executor searchExecutor = Executors.newSingleThreadExecutor();

	public SearchDialog(BiConsumer<SearchDialog, String> onSearch) {
		super(JSplitPane.VERTICAL_SPLIT);

		this.onSearch = onSearch;

		JPanel top = new JPanel();

		returnButton = new JButton("Exit");
		returnButton.setVisible(false);
		top.add(returnButton);
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

		returnButton.addActionListener(e -> {
			if (onReturn != null)
				onReturn.run();
		});

		bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane(bottom,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		scrollPane.setMinimumSize(new Dimension(400, 400));
		scrollPane.setPreferredSize(new Dimension(750, 400));

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

	public void applyResults(ResultSet<FinalMatchingTarget> resultSet) {
		EventQueue.invokeLater(() -> {
			bottom.removeAll();
			if (resultSet.bestResult != null) {
				bottom.add(ResultDisplayComponent.construct(resultSet.bestResult, BEST_MATCH_COLOR));
			}
			for (FinalMatchingTarget t : resultSet.goodResults) {
				bottom.add(ResultDisplayComponent.construct(t, GOOD_MATCH_COLOR));
			}
			for (FinalMatchingTarget t : resultSet.otherResults) {
				bottom.add(ResultDisplayComponent.construct(t, null));
			}
			bottom.revalidate();
			bottom.repaint();
		});
	}

	public void setOnReturn(Runnable onReturn) {
		returnButton.setVisible(true);
		this.onReturn = onReturn;
	}

}

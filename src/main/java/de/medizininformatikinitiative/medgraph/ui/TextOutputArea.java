package de.medizininformatikinitiative.medgraph.ui;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * A text output area which can be used to send messages to the user.
 *
 * @author Markus Budeus
 */
public class TextOutputArea extends JScrollPane {

	private static final TextOutputAreaPrintStream sOutStream;
	private static final TextOutputAreaPrintStream sErrStream;

	/**
	 * This instance displays systemout and systemerr messages.
	 */
	public static final TextOutputArea CONSOLE_INSTANCE = new TextOutputArea();

	static {
		sOutStream = new TextOutputAreaPrintStream(System.out, CONSOLE_INSTANCE);
		sErrStream = new TextOutputAreaPrintStream(System.err, CONSOLE_INSTANCE);
		System.setOut(sOutStream);
		System.setErr(sErrStream);
	}

	private final JTextArea textArea;
	private boolean pendingLinefeed = false;

	private TextOutputArea() {
		super(null, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);

		Font font = new Font("Monospaced", Font.PLAIN, 12);
		this.textArea = new JTextArea(10, 20);
		textArea.setFont(font);
		textArea.setEditable(false);
		this.setViewportView(textArea);
	}

	public synchronized void clear() {
		pendingLinefeed = false;
		this.textArea.setText("");
	}

	public synchronized void appendNewline() {
		if (!pendingLinefeed) {
			pendingLinefeed = true;
		} else {
			append("");
			pendingLinefeed = true;
		}
	}

	public synchronized void append(String text) {
		if (pendingLinefeed) {
			text = "\n" + text;
			pendingLinefeed = false;
		}
		final String t = text;

		Integer caretPositionFromEnd = null;
		int lastLinefeed = t.lastIndexOf('\n');
		if (lastLinefeed != -1) {
			caretPositionFromEnd = t.length() - lastLinefeed - 1;
		}
		final Integer finalCaretPositionFromEnd = caretPositionFromEnd;

		EventQueue.invokeLater(() -> {
			textArea.append(t);
			if (finalCaretPositionFromEnd != null)
				textArea.setCaretPosition(textArea.getDocument().getLength() - finalCaretPositionFromEnd);
		});
	}

	private static class TextOutputAreaPrintStream extends PrintStream {

		private final TextOutputArea toa;

		public TextOutputAreaPrintStream(OutputStream out, TextOutputArea toa) {
			super(out);
			this.toa = toa;
		}

		@Override
		public void print(String s) {
			super.print(s);
			append(s);
		}

		@Override
		public void print(Object obj) {
			super.print(obj);
			append(String.valueOf(obj));
		}

		@Override
		public void println() {
			super.println();
			addNewline();
		}

		@Override
		public void println(String x) {
			super.println(x);
			addNewline();
		}

		@Override
		public void println(Object x) {
			super.println(x);
			addNewline();
		}

		private void addNewline() {
			toa.appendNewline();
		}

		private void append(String text) {
			toa.append(text);
		}
	}

}

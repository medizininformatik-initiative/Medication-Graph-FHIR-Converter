package de.tum.med.aiim.markusbudeus.ui;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class TextOutputArea extends JScrollPane {

	private static final TextOutputAreaPrintStream sOutStream;
	private static final TextOutputAreaPrintStream sErrStream;
	public static final TextOutputArea INSTANCE = new TextOutputArea();

	static {
		sOutStream = new TextOutputAreaPrintStream(System.out, INSTANCE);
		sErrStream = new TextOutputAreaPrintStream(System.err, INSTANCE);
		System.setOut(sOutStream);
		System.setErr(sErrStream);
	}

	private final JTextArea textArea;

	private TextOutputArea() {
		super(null, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);

		Font font = new Font("Monospaced", Font.PLAIN, 12);
		this.textArea = new JTextArea(10, 20);
		textArea.setFont(font);
		textArea.setEditable(false);
		this.setViewportView(textArea);
	}

	private static class TextOutputAreaPrintStream extends PrintStream {

		private final TextOutputArea toa;
		private boolean pendingNewline = false;

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
			if (!pendingNewline) {
				pendingNewline = true;
			} else {
				append("");
				pendingNewline = true;
			}
		}

		private void append(String text) {
			if (pendingNewline) {
				text = "\n" + text;
				pendingNewline = false;
			}
			final String t = text;
			EventQueue.invokeLater(() -> {
				toa.textArea.append(t);
				toa.textArea.setCaretPosition(toa.textArea.getDocument().getLength());
			});
		}
	}

}

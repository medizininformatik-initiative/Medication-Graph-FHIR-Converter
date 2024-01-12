package de.tum.med.aiim.markusbudeus.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Function;

public class Tools {

	public static void createFrame(Function<Runnable, ApplicationFrame> frameConstructor) {
		EventQueue.invokeLater(() ->		{
			JFrame jFrame = new JFrame();
			jFrame.add(frameConstructor.apply(jFrame::dispose));
			jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			jFrame.pack();
			jFrame.setVisible(true);
		});
	}

	public static File selectDirectory(JComponent parent) {
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showOpenDialog(parent);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		} else {
			return null;
		}
	}

}

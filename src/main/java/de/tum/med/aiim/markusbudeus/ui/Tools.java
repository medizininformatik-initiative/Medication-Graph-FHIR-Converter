package de.tum.med.aiim.markusbudeus.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class Tools {

	public static void createFrame(Function<Runnable, ApplicationFrame> frameConstructor) {
		AtomicReference<JFrame> frameReference = new AtomicReference<>();
		createFrame(frameConstructor.apply(() -> frameReference.get().dispose()), frameReference::set);
	}

	public static void createFrame(Component component) {
		createFrame(component, null);
	}

	public static void createFrame(Component component, Consumer<JFrame> onCreateFrame) {
		EventQueue.invokeLater(() ->		{
			JFrame jFrame = new JFrame();
			jFrame.add(component);
			jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			jFrame.pack();
			if (onCreateFrame != null)
				onCreateFrame.accept(jFrame);
			jFrame.setVisible(true);
		});
	}

	public static File selectDirectory(JComponent parent) {
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
		int returnVal = fc.showOpenDialog(parent);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		} else {
			return null;
		}
	}

}

package de.tum.med.aiim.markusbudeus.ui;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import de.tum.med.aiim.markusbudeus.matcher.FinalTransformer;
import de.tum.med.aiim.markusbudeus.matcher.Main;
import de.tum.med.aiim.markusbudeus.matcher.algorithm.MastersThesisAlgorithm;
import de.tum.med.aiim.markusbudeus.matcher.algorithm.MatchingAlgorithm;
import de.tum.med.aiim.markusbudeus.matcher.data.SubSortingTree;
import de.tum.med.aiim.markusbudeus.matcher.model.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;
import de.tum.med.aiim.markusbudeus.matcher.ui.SearchDialog;

import java.awt.*;

import static de.tum.med.aiim.markusbudeus.matcher.Main.toResultSet;

public class SearchFrame extends ApplicationFrame {

	public SearchFrame(Runnable completionCallback) {
		super(completionCallback);

		SearchDialog d = new SearchDialog((dialog, searchTerm) -> {
			DatabaseConnection.runSession(session -> {
				MatchingAlgorithm algorithm = new MastersThesisAlgorithm(session);
				FinalTransformer finalTransformer = new FinalTransformer(session);
				HouselistEntry entry = new HouselistEntry();
				entry.searchTerm = searchTerm;
				SubSortingTree<MatchingTarget> result = algorithm.match(entry);
				Main.ResultSet resultSet = toResultSet(result, finalTransformer);
				dialog.applyResults(resultSet);
			});
		});

		add(d);
		d.setOnReturn(this::complete);
	}

}

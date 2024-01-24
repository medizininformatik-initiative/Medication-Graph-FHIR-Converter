package de.tum.med.aiim.markusbudeus.ui;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import de.tum.med.aiim.markusbudeus.matcher.FinalTransformer;
import de.tum.med.aiim.markusbudeus.matcher.Main;
import de.tum.med.aiim.markusbudeus.matcher.algorithm.MastersThesisAlgorithm;
import de.tum.med.aiim.markusbudeus.matcher.data.SubSortingTree;
import de.tum.med.aiim.markusbudeus.matcher.model.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;
import de.tum.med.aiim.markusbudeus.matcher.ui.SearchDialog;
import org.neo4j.driver.Session;

import static de.tum.med.aiim.markusbudeus.matcher.Main.toResultSet;

public class SearchFrame extends ApplicationFrame {

	private final DatabaseConnection connection;
	private final MastersThesisAlgorithm algorithm;
	private final FinalTransformer finalTransformer;

	public SearchFrame(Runnable completionCallback) {
		super(completionCallback);

		this.connection = new DatabaseConnection();
		Session session = connection.createSession();
		algorithm = new MastersThesisAlgorithm(session);
		finalTransformer = new FinalTransformer(session);

		SearchDialog d = new SearchDialog((dialog, searchTerm) -> {
			long time = System.currentTimeMillis();
			HouselistEntry entry = new HouselistEntry();
			entry.searchTerm = searchTerm;
			SubSortingTree<MatchingTarget> result = algorithm.match(entry);
			Main.ResultSet resultSet = toResultSet(result, finalTransformer);
			long timeTaken = System.currentTimeMillis() - time;
			System.out.println("Search \""+searchTerm+"\" took "+timeTaken+"ms.");
			dialog.applyResults(resultSet);
		});

		add(d);
		d.setOnReturn(this::complete);
	}

	@Override
	protected void complete() {
		connection.close();
		super.complete();
	}
}

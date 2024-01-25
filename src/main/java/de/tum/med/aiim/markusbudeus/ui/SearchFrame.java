package de.tum.med.aiim.markusbudeus.ui;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import de.tum.med.aiim.markusbudeus.matcher.BestMatchTransformer;
import de.tum.med.aiim.markusbudeus.matcher.FinalResultTransformer;
import de.tum.med.aiim.markusbudeus.matcher.Main;
import de.tum.med.aiim.markusbudeus.matcher.algorithm.MastersThesisAlgorithm;
import de.tum.med.aiim.markusbudeus.matcher.data.SubSortingTree;
import de.tum.med.aiim.markusbudeus.matcher.model.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;
import de.tum.med.aiim.markusbudeus.matcher.ui.SearchDialog;
import org.neo4j.driver.Session;

import java.util.List;

import static de.tum.med.aiim.markusbudeus.matcher.Main.toResultSet;

public class SearchFrame extends ApplicationFrame {

	private final DatabaseConnection connection;
	private final MastersThesisAlgorithm algorithm;
	private final BestMatchTransformer bestMatchTransformer;

	private final FinalResultTransformer finalResultTransformer;

	public SearchFrame(Runnable completionCallback) {
		super(completionCallback);

		this.connection = new DatabaseConnection();
		Session session = connection.createSession();
		algorithm = new MastersThesisAlgorithm(session);
		bestMatchTransformer = new BestMatchTransformer(session);
		finalResultTransformer = new FinalResultTransformer(session);

		SearchDialog d = new SearchDialog((dialog, searchTerm) -> {
			long time = System.currentTimeMillis();
			HouselistEntry entry = new HouselistEntry();
			entry.searchTerm = searchTerm;
			SubSortingTree<MatchingTarget> result = algorithm.match(entry);
			Main.ResultSet resultSet = toResultSet(result, bestMatchTransformer);

			// TODO Remove this bs
			if (resultSet.bestResult != null) {
				System.out.println(finalResultTransformer.transform(List.of(resultSet.bestResult)).get(0).drugs.get(0));
			}

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

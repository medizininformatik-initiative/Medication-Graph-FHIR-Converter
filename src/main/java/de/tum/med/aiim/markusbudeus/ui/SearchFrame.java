package de.tum.med.aiim.markusbudeus.ui;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import de.tum.med.aiim.markusbudeus.matcher.BestMatchTransformer;
import de.tum.med.aiim.markusbudeus.matcher.FinalResultTransformer;
import de.tum.med.aiim.markusbudeus.matcher.algorithm.MastersThesisAlgorithm;
import de.tum.med.aiim.markusbudeus.matcher.data.SubSortingTree;
import de.tum.med.aiim.markusbudeus.matcher.model.*;
import de.tum.med.aiim.markusbudeus.matcher.ui.SearchDialog;
import org.neo4j.driver.Session;

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
			long t0 = System.currentTimeMillis();
			SubSortingTree<MatchingTarget> result = algorithm.match(entry);
			long t1 = System.currentTimeMillis();
			ResultSet<ProductWithPzn> resultSet = toResultSet(result, bestMatchTransformer);
			long t2 = System.currentTimeMillis();
			ResultSet<FinalMatchingTarget> transformedResultSet = finalResultTransformer.transform(resultSet);
			long t3 = System.currentTimeMillis();
			long timeTaken = System.currentTimeMillis() - time;
			System.out.println("Search \""+searchTerm+"\" took "+timeTaken+"ms. " +
					"("+(t1-t0)+"ms matching, "+(t2-t1)+"ms result set, "+(t3-t2)+"ms final transformation.)");
			dialog.applyResults(transformedResultSet);
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

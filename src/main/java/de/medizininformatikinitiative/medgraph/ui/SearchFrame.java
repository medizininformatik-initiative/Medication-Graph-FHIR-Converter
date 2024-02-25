package de.medizininformatikinitiative.medgraph.ui;

import de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.matcher.BestMatchTransformer;
import de.medizininformatikinitiative.medgraph.matcher.FinalResultTransformer;
import de.medizininformatikinitiative.medgraph.matcher.Matcher;
import de.medizininformatikinitiative.medgraph.matcher.algorithm.MastersThesisAlgorithm;
import de.medizininformatikinitiative.medgraph.matcher.data.SubSortingTree;
import de.medizininformatikinitiative.medgraph.matcher.model.*;
import de.medizininformatikinitiative.medgraph.matcher.ui.SearchDialog;
import org.neo4j.driver.Session;

import java.awt.*;

/**
 * The frame used for the search algorithm.
 *
 * @author Markus Budeus
 */
public class SearchFrame extends ApplicationFrame {

	private final DatabaseConnection connection;
	private final MastersThesisAlgorithm algorithm;
	private final BestMatchTransformer bestMatchTransformer;

	private final FinalResultTransformer finalResultTransformer;

	private final SearchDialog dialog;

	public SearchFrame(Runnable completionCallback) {
		super(completionCallback);

		setLayout(new BorderLayout());

		this.connection = new DatabaseConnection();
		Session session = connection.createSession();
		algorithm = new MastersThesisAlgorithm(session);
		bestMatchTransformer = new BestMatchTransformer(session);
		finalResultTransformer = new FinalResultTransformer(session);

		dialog = new SearchDialog((d, searchTerm) -> {
			long time = System.currentTimeMillis();
			HouselistEntry entry = new HouselistEntry();
			entry.searchTerm = searchTerm;
			long t0 = System.currentTimeMillis();
			SubSortingTree<MatchingTarget> result = algorithm.match(entry);
			long t1 = System.currentTimeMillis();
			ResultSet<ProductWithPzn> resultSet = Matcher.toResultSet(result, bestMatchTransformer);
			long t2 = System.currentTimeMillis();
			ResultSet<FinalMatchingTarget> transformedResultSet = finalResultTransformer.transform(resultSet);
			long t3 = System.currentTimeMillis();
			long timeTaken = System.currentTimeMillis() - time;
			System.out.println("Search \"" + searchTerm + "\" took " + timeTaken + "ms. " +
					"(" + (t1 - t0) + "ms matching, " + (t2 - t1) + "ms result set, " + (t3 - t2) + "ms final transformation.)");
			d.applyResults(transformedResultSet);
		});

		add(dialog);
		dialog.setOnReturn(this::complete);
		setPreferredSize(new Dimension(800, 500));
	}

	@Override
	protected void onNavigateTo() {
		dialog.focusSearch();
	}

	@Override
	protected void complete() {
		connection.close();
		super.complete();
	}
}

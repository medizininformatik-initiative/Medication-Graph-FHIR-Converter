package de.tum.med.aiim.markusbudeus.matcher.model;

import java.util.List;
public class ResultSet<T extends ProductWithPzn> {
	public final T bestResult;
	public final List<T> goodResults;
	public final List<T> otherResults;

	public ResultSet(T bestResult, List<T> goodResults,
	                 List<T> otherResults) {
		this.bestResult = bestResult;
		this.goodResults = goodResults;
		this.otherResults = otherResults;
	}
}
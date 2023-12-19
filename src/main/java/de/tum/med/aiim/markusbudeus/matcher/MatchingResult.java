package de.tum.med.aiim.markusbudeus.matcher;

public class MatchingResult<T, S> {

	public final T searchTerm;
	public final Match<S> result;

	public MatchingResult(T searchTerm, Match<S> result) {
		this.searchTerm = searchTerm;
		this.result = result;
	}

}
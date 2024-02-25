package de.medizininformatikinitiative.medgraph;


import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * {@link org.neo4j.driver.Session}-implementation which throws an exception when used.
 */
public class FakeSession implements Session {
	@Override
	public Transaction beginTransaction() {
		fail();
		return null;
	}

	@Override
	public Transaction beginTransaction(TransactionConfig transactionConfig) {
		fail();
		return null;
	}

	@Override
	public <T> T readTransaction(TransactionWork<T> transactionWork) {
		fail();
		return null;
	}

	@Override
	public <T> T executeRead(TransactionCallback<T> callback) {
		fail();
		return Session.super.executeRead(callback);
	}

	@Override
	public <T> T readTransaction(TransactionWork<T> transactionWork, TransactionConfig transactionConfig) {
		fail();
		return null;
	}

	@Override
	public <T> T executeRead(TransactionCallback<T> transactionCallback, TransactionConfig transactionConfig) {
		fail();
		return null;
	}

	@Override
	public <T> T writeTransaction(TransactionWork<T> transactionWork) {
		fail();
		return null;
	}

	@Override
	public <T> T executeWrite(TransactionCallback<T> callback) {
		fail();
		return Session.super.executeWrite(callback);
	}

	@Override
	public void executeWriteWithoutResult(Consumer<TransactionContext> contextConsumer) {
		fail();
		Session.super.executeWriteWithoutResult(contextConsumer);
	}

	@Override
	public <T> T writeTransaction(TransactionWork<T> transactionWork, TransactionConfig transactionConfig) {
		fail();
		return null;
	}

	@Override
	public <T> T executeWrite(TransactionCallback<T> transactionCallback, TransactionConfig transactionConfig) {
		fail();
		return null;
	}

	@Override
	public void executeWriteWithoutResult(Consumer<TransactionContext> contextConsumer, TransactionConfig config) {
		fail();
		Session.super.executeWriteWithoutResult(contextConsumer, config);
	}

	@Override
	public Result run(String s, TransactionConfig transactionConfig) {
		fail();
		return null;
	}

	@Override
	public Result run(String s, Map<String, Object> map, TransactionConfig transactionConfig) {
		fail();
		return null;
	}

	@Override
	public Result run(Query query, TransactionConfig transactionConfig) {
		fail();
		return null;
	}

	@Override
	public Bookmark lastBookmark() {
		fail();
		return null;
	}

	@Override
	public Set<Bookmark> lastBookmarks() {
		fail();
		return null;
	}

	@Override
	public boolean isOpen() {
		fail();
		return false;
	}

	@Override
	public void close() {
		fail();

	}

	@Override
	public Result run(String s, Value value) {
		fail();
		return null;
	}

	@Override
	public Result run(String s, Map<String, Object> map) {
		fail();
		return null;
	}

	@Override
	public Result run(String s, Record record) {
		fail();
		return null;
	}

	@Override
	public Result run(String s) {
		fail();
		return null;
	}

	@Override
	public Result run(Query query) {
		fail();
		return null;
	}

	private void fail() {
		throw new IllegalStateException("Fake Session object was accessed!");
	}
}

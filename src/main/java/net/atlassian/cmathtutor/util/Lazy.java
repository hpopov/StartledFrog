package net.atlassian.cmathtutor.util;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {

    private Supplier<T> newInstanceSupplier;
    private T cachedInstance;

    public static <T> Lazy<T> of(Supplier<T> newInstanceSupplier) {
	return new Lazy<T>(newInstanceSupplier);
    }

    private Lazy(Supplier<T> newInstanceSupplier) {
	this.newInstanceSupplier = newInstanceSupplier;
    }

    @Override
    public synchronized T get() {
	T instance = cachedInstance;
	return instance == null ? cachedInstance = newInstanceSupplier.get() : instance;
    }

    public void clear() {
	cachedInstance = null;
    }
}

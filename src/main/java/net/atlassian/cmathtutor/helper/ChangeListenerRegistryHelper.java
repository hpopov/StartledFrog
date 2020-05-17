package net.atlassian.cmathtutor.helper;

import java.util.LinkedList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
/**
 * Helper class to collect references to all {@link ChangeListener}, which are going to be registered
 * within single Presenter as {@link WeakChangeListener}s.
 * <br>
 * Typical usage is the following:
 * <ol>
 * <li>Create an instance of {@link ChangeListenerRegistryHelper} during Presenter creation</li>
 * <li>Use {@link ChangeListenerRegistryHelper#registerChangeListener(ChangeListener)} each time during
 *  {@link WeakChangeListener} usage for binding</li>
 * <li>When Presenter will be garbage collected, {@link ChangeListenerRegistryHelper} will be as well,
 *  so all {@link WeakChangeListener}s will be able to be garbage collected</li>
 * </ol>
 * @author Hryhorii Popov
 *
 */
public class ChangeListenerRegistryHelper {

    private List<ChangeListener<?>> changeListenersRegistry = new LinkedList<>();

    public <T> WeakChangeListener<T> registerChangeListener(ChangeListener<T> changeListener) {
	changeListenersRegistry.add(changeListener);
	return new WeakChangeListener<T>(changeListener);
    }
}

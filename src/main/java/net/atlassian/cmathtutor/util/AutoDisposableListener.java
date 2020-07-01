package net.atlassian.cmathtutor.util;

import java.util.function.Predicate;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AutoDisposableListener<T> implements ChangeListener<T> {

    private Predicate<T> trigger;
    private Runnable handler;

    @Override
    public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
        if (trigger.test(newValue)) {
            observable.removeListener(this);
            handler.run();
        }
    }
}

package net.atlassian.cmathtutor.domain.persistence.translate.java.instance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.atlassian.cmathtutor.domain.persistence.translate.java.Type;

@Getter(onMethod = @__(@Override))
@AllArgsConstructor
public abstract class AbstractInstance<T extends Type> implements Instance<T> {

    protected T type;
}

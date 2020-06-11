package net.atlassian.cmathtutor.domain.persistence.translate.java.velocity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.atlassian.cmathtutor.domain.persistence.translate.java.PackagedType;

@Getter(onMethod = @__(@Override))
@AllArgsConstructor
public class Application implements PackagedType {

    private String packageName;
    private String name;
}

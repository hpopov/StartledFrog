package net.atlassian.cmathtutor.domain.persistence.translate.java.velocity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.atlassian.cmathtutor.domain.persistence.translate.java.PackagedType;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@Getter(onMethod = @__(@Override))
public abstract class AbstractDeclarationData implements PackagedType {

    @EqualsAndHashCode.Include
    protected String name;
    @EqualsAndHashCode.Include
    protected String packageName;

    public abstract List<PackagedType> getTypesToImport();
}

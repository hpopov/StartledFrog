package net.atlassian.cmathtutor.domain.persistence.descriptor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.atlassian.cmathtutor.domain.persistence.Identifiable;

@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractDescriptor implements Identifiable {

    @Getter(onMethod = @__(@Override))
    @Setter
    private String id;

}

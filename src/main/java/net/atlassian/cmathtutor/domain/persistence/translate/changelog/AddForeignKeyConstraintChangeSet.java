package net.atlassian.cmathtutor.domain.persistence.translate.changelog;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter(onMethod = @__(@Override))
@Setter
@SuperBuilder
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.NONE)
public class AddForeignKeyConstraintChangeSet extends AbstractChangeSet {

    private AddForeignKeyConstraint addForeignKeyConstraint;
}

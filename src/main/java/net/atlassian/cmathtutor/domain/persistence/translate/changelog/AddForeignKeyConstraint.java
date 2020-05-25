package net.atlassian.cmathtutor.domain.persistence.translate.changelog;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@XmlAccessorType(XmlAccessType.NONE)
public class AddForeignKeyConstraint {

    @XmlAttribute(required = true)
    private String baseColumnNames;

    @XmlAttribute(required = true)
    private String baseTableName;

    @XmlAttribute(required = true)
    private String constraintName;

    @Builder.Default
    @XmlAttribute(required = true)
    private boolean deferrable = false;

    @Builder.Default
    @XmlAttribute(required = true)
    private boolean initiallyDeferred = false;

    @XmlAttribute(required = true)
    private FkCascadeActionType onDelete;

    @Builder.Default
    @XmlAttribute(required = true)
    private FkCascadeActionType onUpdate = FkCascadeActionType.RESTRICT;

    @XmlAttribute(required = true)
    private String referencedColumnNames;

    @XmlAttribute(required = true)
    private String referencedTableName;

    @Builder.Default
    @XmlAttribute(required = true)
    private boolean validate = true;
}

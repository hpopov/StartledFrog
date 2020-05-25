package net.atlassian.cmathtutor.domain.persistence.translate.changelog;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@XmlAccessorType(XmlAccessType.NONE)
public class Column extends ColumnReference {

    @XmlAttribute
    private boolean autoIncrement;

    @XmlAttribute
    @XmlJavaTypeAdapter(ColumnType.Adapter.class)
    private ColumnType type;

    @XmlElement
    private Constraints constraints;
}

package net.atlassian.cmathtutor.domain.persistence.translate.changelog;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractChangeSet {

    @XmlAttribute(required = true)
    private String id;
    @XmlAttribute(required = true)
    private String author;

    @XmlElement(required = false)
    public CreateTable getCreateTable() {
	return null;
    }

    @XmlElement(required = false)
    public AddForeignKeyConstraint getAddForeignKeyConstraint() {
	return null;
    }

    @XmlElement(required = false)
    public CreateIndex getCreateIndex() {
	return null;
    }
}

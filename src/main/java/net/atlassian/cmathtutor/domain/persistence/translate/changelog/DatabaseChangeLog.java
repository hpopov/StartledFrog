package net.atlassian.cmathtutor.domain.persistence.translate.changelog;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlRootElement(namespace = "http://www.liquibase.org/xml/ns/dbchangelog")
@XmlAccessorType(XmlAccessType.NONE)
@NoArgsConstructor
@Getter
@Setter
public class DatabaseChangeLog {

    @XmlElement(name = "changeSet")
    private List<AbstractChangeSet> abstractChangeSets = new LinkedList<>();
}

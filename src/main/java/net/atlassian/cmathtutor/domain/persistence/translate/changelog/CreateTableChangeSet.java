package net.atlassian.cmathtutor.domain.persistence.translate.changelog;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@XmlAccessorType(XmlAccessType.NONE)
public class CreateTableChangeSet extends AbstractChangeSet {

    @XmlElement(required = true)
    private CreateTable createTable;

}

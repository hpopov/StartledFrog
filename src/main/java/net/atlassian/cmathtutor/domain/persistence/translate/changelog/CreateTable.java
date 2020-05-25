package net.atlassian.cmathtutor.domain.persistence.translate.changelog;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
@XmlAccessorType(XmlAccessType.NONE)
public class CreateTable {

    @XmlAttribute(required = true)
    private String tableName;

    @XmlElement(name = "column")
    private List<Column> columns;

    public CreateTable() {
	columns = new LinkedList<>();
    }
}

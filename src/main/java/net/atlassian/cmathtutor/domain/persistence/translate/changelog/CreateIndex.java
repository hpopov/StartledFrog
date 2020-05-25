package net.atlassian.cmathtutor.domain.persistence.translate.changelog;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

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
public class CreateIndex {

    @XmlAttribute(required = true)
    private String indexName;

    @XmlAttribute(required = true)
    private String tableName;

    @XmlElement(required = true)
    private ColumnReference column;
}

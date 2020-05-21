package net.atlassian.cmathtutor.domain.persistence.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.atlassian.cmathtutor.domain.persistence.Identifiable;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public abstract class AbstractIdentifyableModel implements Identifiable {

    @Getter(onMethod = @__({ @Override, @XmlAttribute, @XmlID }))
    @Setter
    private String id;
}

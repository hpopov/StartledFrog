package net.atlassian.cmathtutor.domain.persistence.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.atlassian.cmathtutor.domain.persistence.Identifiable;

@XmlAccessorType(XmlAccessType.NONE)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public abstract class AbstractIdentifyableModel implements Identifiable {

    @Getter(onMethod = @__({ @Override, @XmlAttribute(required = true), @XmlID }))
    @Setter
    private String id;
}

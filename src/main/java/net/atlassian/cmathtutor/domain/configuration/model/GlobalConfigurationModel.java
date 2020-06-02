package net.atlassian.cmathtutor.domain.configuration.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.atlassian.cmathtutor.model.Project;

@XmlRootElement(name = "global-configuration")
@XmlAccessorType(XmlAccessType.PROPERTY)
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class GlobalConfigurationModel {
    private String rootPassword;
    private String user;
    private String password;
    private String database;

    @Getter(onMethod = @__(@XmlTransient))
    @XmlTransient
    private Project project;
    private String jdbcDriverPath;
    private String dockerMachineIp;
}

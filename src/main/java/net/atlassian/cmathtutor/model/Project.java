package net.atlassian.cmathtutor.model;

import java.io.File;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.atlassian.cmathtutor.ProjectBuildFramework;
import net.atlassian.cmathtutor.Version;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "startled-frog-project")
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class Project {

    @XmlTransient
    private File projectFolder;

    @XmlAttribute(name = "version", required = true)
    private Version version;

    @XmlElement(name = "root-package")
    private String rootPackage;

    @XmlElement(name = "application-name")
    private String applicationName;

    @XmlElement(name = "project-builder")
    private ProjectBuildFramework projectBuildFramework;

    @XmlElement(name = "sf-project-folder")
    private String startledFrogProjectFolder;
}

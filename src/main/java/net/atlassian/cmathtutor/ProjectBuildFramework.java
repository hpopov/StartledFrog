package net.atlassian.cmathtutor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProjectBuildFramework {

    MAVEN("pom.xml", "src/main/java", "src/main/resources");

    private String buildFileName;
    private String javaSourcesSubpath;
    private String resourcesSubpath;
}

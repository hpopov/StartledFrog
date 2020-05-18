package net.atlassian.cmathtutor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProjectBuildFramework {
    MAVEN("pom.xml");

    private String buildFileName;
}

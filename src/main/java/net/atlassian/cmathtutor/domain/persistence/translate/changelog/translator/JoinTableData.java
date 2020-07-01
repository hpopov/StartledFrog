package net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.Column;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.CreateTable;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class JoinTableData {

    private CreateTable createTable;
    private Column primaryAttributeJoinColumn;
    private Column secondaryAttributeJoinColumn;

    public String getName() {
        return createTable.getTableName();
    }
}

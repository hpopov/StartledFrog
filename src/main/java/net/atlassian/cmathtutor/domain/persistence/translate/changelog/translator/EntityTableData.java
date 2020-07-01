package net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.Column;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.CreateTable;

public class EntityTableData {

    @Getter
    private CreateTable createTable;
    private Map<ReferentialAttribute, Column> attributeToJoinColumns;

    public EntityTableData(String tableName) {
        this.createTable = new CreateTable(tableName, new LinkedList<>());
        attributeToJoinColumns = new HashMap<>();
    }

    public void addColumn(@NonNull Column column) {
        createTable.getColumns().add(column);
    }

    public boolean addJoinColumn(@NonNull ReferentialAttribute attribute, @NonNull Column column) {
        if (attributeToJoinColumns.containsKey(attribute)) {
            return false;
        }
        addColumn(column);
        attributeToJoinColumns.put(attribute, column);
        return true;
    }

    public Column getJoinColumnByReferentialAttribute(@NonNull ReferentialAttribute attribute) {
        return attributeToJoinColumns.get(attribute);
    }

    public String getName() {
        return createTable.getTableName();
    }
}

package net.atlassian.cmathtutor.domain.persistence.translate.java.velocity;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.atlassian.cmathtutor.domain.persistence.translate.java.PackagedType;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Getter
public class EntityData extends AbstractDeclarationData implements PackagedType {

    private List<VariableData> fields = new LinkedList<>();
    private String tableName;

    public EntityData(String name, String packageName, String tableName) {
	super(name, packageName);
	this.tableName = tableName;
    }

    @Override
    public List<PackagedType> getTypesToImport() {
	return fields.stream().flatMap(field -> field.getContainedTypes().stream()).distinct()
		.filter(type -> !type.getPackageName().equals(packageName)).collect(Collectors.toList());
    }

}

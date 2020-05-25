package net.atlassian.cmathtutor.domain.persistence.translate.java.instance;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.springframework.data.rest.core.annotation.RestResource;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.atlassian.cmathtutor.domain.persistence.translate.java.Annotation;

public final class AnnotationInstances {

    public static AnnotationInstance<Id> id() {
	return newAnnotationInstance(Id.class);
    }

    private static <T extends java.lang.annotation.Annotation> AnnotationInstance<T> newAnnotationInstance(
	    Class<T> clazz) {
	return new AnnotationInstance<>(Annotation.of(clazz), Collections.emptyMap());
    }

    public static AnnotationInstance<GeneratedValue> generatedValueIdentity() {
	return newAnnotationInstance(GeneratedValue.class,
		Collections.singletonMap("strategy", EnumInstance.ofConstant(GenerationType.IDENTITY)));
    }

    private static <T extends java.lang.annotation.Annotation> AnnotationInstance<T> newAnnotationInstance(
	    Class<T> clazz, Map<String, Instance<?>> annotationValues) {
	return new AnnotationInstance<>(Annotation.of(clazz), annotationValues);
    }

    public static AnnotationInstance<Column> column(String name, boolean nullable, boolean unique) {
	Map<String, Instance<?>> values = new LinkedHashMap<>(3);
	values.put("name", PrimitiveInstances.newString(name));
	if (!nullable) {
	    values.put("nullable", PrimitiveInstances.newBoolean(nullable));
	}
	if (unique) {
	    values.put("unique", PrimitiveInstances.newBoolean(unique));
	}

	return newAnnotationInstance(Column.class, values);
    }

    public static AnnotationInstance<Column> column(String name, boolean nullable) {
	return column(name, nullable, false);
    }

    public static AnnotationInstance<Column> column(String name) {
	return column(name, true, false);
    }

    public static AnnotationInstance<ToString.Exclude> toStringExclude() {
	return newAnnotationInstance(ToString.Exclude.class);
    }

    public static AnnotationInstance<EqualsAndHashCode.Exclude> equalsAndHashCodeExclude() {
	return newAnnotationInstance(EqualsAndHashCode.Exclude.class);
    }

    public static AnnotationInstance<RestResource> restResource(boolean exported) {
	return newAnnotationInstance(RestResource.class,
		Collections.singletonMap("exported", PrimitiveInstances.newBoolean(exported)));
    }

    public static AnnotationInstance<JsonIgnore> jsonIgnore() {
	return newAnnotationInstance(JsonIgnore.class);
    }

    public static ManyToOneBuilder manyToOneBuilder() {
	return new ManyToOneBuilder();
    }

    public static AnnotationInstance<Transient> javaxPersistenceTransient() {
	return newAnnotationInstance(Transient.class);
    }

    public static OneToOneBuilder oneToOneBuilder() {
	return new OneToOneBuilder();
    }

    public static JoinColumnBuilder joinColumnBuilder() {
	return new JoinColumnBuilder();
    }

    public static OneToManyBuilder oneToManyBuilder() {
	return new OneToManyBuilder();
    }

    public static JoinTableBuilder joinTableBuilder() {
	return new JoinTableBuilder();
    }

    public static class ManyToOneBuilder {
	private Map<String, Instance<?>> values = new LinkedHashMap<>(3);

	public ManyToOneBuilder fetch(FetchType fetch) {
	    values.put("fetch", EnumInstance.ofConstant(fetch));
	    return this;
	}

	public ManyToOneBuilder cascade(CascadeType cascade, CascadeType... cascades) {
	    values.put("cascade", EnumInstance.listOfConstants(cascade, cascades));
	    return this;
	}

	public ManyToOneBuilder optional(boolean optional) {
	    values.put("optional", PrimitiveInstances.newBoolean(optional));
	    return this;
	}

	public AnnotationInstance<ManyToOne> build() {
	    return newAnnotationInstance(ManyToOne.class, values);
	}
    }

    public static class OneToOneBuilder {
	private Map<String, Instance<?>> values = new LinkedHashMap<>(3);

	public OneToOneBuilder fetch(FetchType fetch) {
	    values.put("fetch", EnumInstance.ofConstant(fetch));
	    return this;
	}

	public OneToOneBuilder cascade(CascadeType cascade, CascadeType... cascades) {
	    values.put("cascade", EnumInstance.listOfConstants(cascade, cascades));
	    return this;
	}

	public OneToOneBuilder optional(boolean optional) {
	    values.put("optional", PrimitiveInstances.newBoolean(optional));
	    return this;
	}

	public OneToOneBuilder mappedBy(String mappedBy) {
	    values.put("mappedBy", PrimitiveInstances.newString(mappedBy));
	    return this;
	}

	public AnnotationInstance<OneToOne> build() {
	    return newAnnotationInstance(OneToOne.class, values);
	}
    }

    public static class JoinColumnBuilder {
	private Map<String, Instance<?>> values = new LinkedHashMap<>(3);

	public JoinColumnBuilder name(String name) {
	    values.put("name", PrimitiveInstances.newString(name));
	    return this;
	}

	public JoinColumnBuilder nullable(boolean nullable) {
	    if (nullable) {
		values.remove("nullable");
	    } else {
		values.put("nullable", PrimitiveInstances.newBoolean(false));
	    }
	    return this;
	}

	public JoinColumnBuilder unique(boolean unique) {
	    if (unique) {
		values.put("unique", PrimitiveInstances.newBoolean(unique));
	    } else {
		values.remove("unique");
	    }
	    return this;
	}

	public JoinColumnBuilder referencedColumnName(String referencedColumnName) {
	    values.put("referencedColumnName", PrimitiveInstances.newString(referencedColumnName));
	    return this;
	}

	public AnnotationInstance<JoinColumn> build() {
	    return newAnnotationInstance(JoinColumn.class, values);
	}
    }

    public static class OneToManyBuilder {
	private Map<String, Instance<?>> values = new LinkedHashMap<>(3);

	public OneToManyBuilder fetch(FetchType fetch) {
	    values.put("fetch", EnumInstance.ofConstant(fetch));
	    return this;
	}

	public OneToManyBuilder cascade(CascadeType cascade, CascadeType... cascades) {
	    values.put("cascade", EnumInstance.listOfConstants(cascade, cascades));
	    return this;
	}

	public OneToManyBuilder mappedBy(String mappedBy) {
	    values.put("mappedBy", PrimitiveInstances.newString(mappedBy));
	    return this;
	}

	public AnnotationInstance<OneToOne> build() {
	    return newAnnotationInstance(OneToOne.class, values);
	}
    }

    public static class JoinTableBuilder {
	private Map<String, Instance<?>> values = new LinkedHashMap<>(3);

	public JoinTableBuilder name(String name) {
	    values.put("name", PrimitiveInstances.newString(name));
	    return this;
	}

	public JoinTableBuilder.JoinColumnBuilder inverseJoinColumns() {
	    return new JoinTableBuilder.JoinColumnBuilder("inverseJoinColumns");
	}

	public JoinTableBuilder.JoinColumnBuilder joinColumns() {
	    return new JoinTableBuilder.JoinColumnBuilder("joinColumns");
	}

	public AnnotationInstance<JoinTable> build() {
	    return newAnnotationInstance(JoinTable.class, values);
	}

	public class JoinColumnBuilder {
	    private Map<String, Instance<?>> values = new LinkedHashMap<>(3);
	    private String joinTableValuesKey;

	    public JoinColumnBuilder(String joinTableValuesKey) {
		this.joinTableValuesKey = joinTableValuesKey;
	    }

	    public JoinColumnBuilder name(String name) {
		values.put("name", PrimitiveInstances.newString(name));
		return this;
	    }

	    public JoinColumnBuilder nullable(boolean nullable) {
		if (nullable) {
		    values.remove("nullable");
		} else {
		    values.put("nullable", PrimitiveInstances.newBoolean(false));
		}
		return this;
	    }

	    public JoinColumnBuilder unique(boolean unique) {
		if (unique) {
		    values.put("unique", PrimitiveInstances.newBoolean(unique));
		} else {
		    values.remove("unique");
		}
		return this;
	    }

	    public JoinColumnBuilder referencedColumnName(String referencedColumnName) {
		values.put("referencedColumnName", PrimitiveInstances.newString(referencedColumnName));
		return this;
	    }

	    public JoinTableBuilder build() {
		JoinTableBuilder.this.values.put(joinTableValuesKey, newAnnotationInstance(JoinColumn.class, values));
		return JoinTableBuilder.this;
	    }
	}
    }

}

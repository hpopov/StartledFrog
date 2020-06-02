package net.atlassian.cmathtutor.domain.persistence.translate.java.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.atlassian.cmathtutor.domain.persistence.translate.java.EnumType;

public class EnumInstance<T extends Enum<T>> extends AbstractInstance<EnumType<T>>
	implements ImportableInstance<EnumType<T>> {

    public static <T extends Enum<T>> EnumInstance<T> ofConstant(Enum<T> value) {
	return new EnumInstance<>(value);
    }

    @SafeVarargs
    public static <T extends Enum<T>> ImportableInstance<EnumType<T>> listOfConstants(Enum<T> value,
	    Enum<T>... values) {
	if (values.length == 0) {
	    return ofConstant(value);
	}
	return new ImportableInstance<EnumType<T>>() {

	    private List<EnumInstance<T>> innerInstances = new ArrayList<>(values.length + 1);
	    private EnumType<T> commonType;
	    {
		EnumInstance<T> firstInstance = ofConstant(value);
		commonType = firstInstance.getType();
		innerInstances.add(firstInstance);
		innerInstances.addAll(Stream.of(values).map(EnumInstance::ofConstant).collect(Collectors.toList()));
	    }

	    @Override
	    public EnumType<T> getType() {
		return commonType;
	    }

	    @Override
	    public String toString() {
		StringBuilder sb = innerInstances.stream().map(inst -> inst.toString() + ", ").collect(
			StringBuilder::new,
			StringBuilder::append, StringBuilder::append);
		return "{ " + sb.substring(0, sb.length() - 2) + " }";
	    }
	};
    }

    private Enum<T> value;

    private EnumInstance(Enum<T> value) {
	super(new EnumType<>(value.getDeclaringClass()));
	this.value = value;
    }

    @Override
    public String toString() {
	return type.getName() + "." + value.name();
    }
}

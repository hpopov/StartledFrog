package net.atlassian.cmathtutor.domain.persistence.translate.java.instance;

import net.atlassian.cmathtutor.domain.persistence.translate.java.PrimitiveType;

public final class PrimitiveInstances {

    public static Instance<PrimitiveType> newInteger(Integer value) {
	return new SimpleInstance(PrimitiveType.INTEGER, value);
    }

    public static Instance<PrimitiveType> newDouble(Double value) {
	return new SimpleInstance(PrimitiveType.DOUBLE, value);
    }

    public static Instance<PrimitiveType> newLong(Long value) {
	return new AbstractInstance<PrimitiveType>(PrimitiveType.LONG) {
	    @Override
	    public String toString() {
		return String.valueOf(value) + (value != null ? "L" : "");
	    }
	};
    }

    public static Instance<PrimitiveType> newString(String value) {
	return new AbstractInstance<PrimitiveType>(PrimitiveType.STRING) {
	    @Override
	    public String toString() {
		if (value == null) {
		    return "null";
		}
		return "\"" + value + "\"";
	    }
	};
    }

    public static Instance<PrimitiveType> newBoolean(Boolean value) {
	return new SimpleInstance(PrimitiveType.BOOLEAN, value);
    }

    private static class SimpleInstance extends AbstractInstance<PrimitiveType> {

	private Object value;

	public SimpleInstance(PrimitiveType type, Object value) {
	    super(type);
	    this.value = value;
	}

	@Override
	public String toString() {
	    return String.valueOf(value);
	}
    }

}

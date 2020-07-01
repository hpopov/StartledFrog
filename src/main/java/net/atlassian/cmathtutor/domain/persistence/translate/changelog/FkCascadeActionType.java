package net.atlassian.cmathtutor.domain.persistence.translate.changelog;

import java.util.stream.Stream;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FkCascadeActionType {

    CASCADE("CASCADE"),
    RESTRICT("RESTRICT"),
    SET_NULL("SET NULL");

    private String sqlName;

    public static class Adapter extends XmlAdapter<String, FkCascadeActionType> {

        @Override
        public String marshal(FkCascadeActionType data) throws Exception {
            return data.sqlName;
        }

        @Override
        public FkCascadeActionType unmarshal(String value) throws Exception {
            return Stream.of(FkCascadeActionType.values()).filter(data -> data.sqlName.contentEquals(value)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "enum FkCascadeActionType doesn't have a proper constant for name " + value));
        }
    }
}

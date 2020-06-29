package net.atlassian.cmathtutor.domain.persistence.translate.changelog;

import java.util.stream.Stream;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ColumnType {

    INT("INT"),
    BIGINT("BIGINT"),
    TINYINT("TINYINT(1)"),
    VARCHAR("VARCHAR(64)"),
    DOUBLE("DOUBLE"),
    BIG_VARCHAR("VARCHAR(1024)");

    private String sqlName;

    public static class Adapter extends XmlAdapter<String, ColumnType> {

        @Override
        public String marshal(ColumnType data) throws Exception {
            return data.sqlName;
        }

        @Override
        public ColumnType unmarshal(String value) throws Exception {
            return Stream.of(ColumnType.values()).filter(data -> data.sqlName.contentEquals(value)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "enum ColumnType doesn't have a proper constant for name " + value));
        }
    }
}

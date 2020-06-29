package net.atlassian.cmathtutor.util;

import java.util.stream.Stream;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CaseUtil {

    private static final String UNDERSCORE = "_";
    private static final String TOKEN_SPLIT_REGEX = "([ .:,\\t\\r\\n]+|(?<=[a-z0-9])(?=[A-Z]))";

    public static String toCapitalizedCamelCase(@NonNull String string) {
        return Stream.of(string.split(TOKEN_SPLIT_REGEX)).map(String::toLowerCase)
                .filter(token -> !token.isEmpty()).map(token -> {
                    String stringToReplace = "[" + token.charAt(0) + "]";
                    String replacement = String.valueOf(Character.toUpperCase(token.charAt(0)));
                    return token.replaceFirst(stringToReplace, replacement);
                })
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    }

    public static String toLowerCamelCase(@NonNull String string) {
        if (string.length() == 0) {
            return string;
        }
        return Character.toLowerCase(string.charAt(0)) + toCapitalizedCamelCase(string).substring(1);
    }

    public static String toSnakeCase(@NonNull String string) {
        StringBuilder sb = Stream.of(string.split(TOKEN_SPLIT_REGEX)).map(String::toLowerCase)
                .filter(token -> !token.isEmpty()).map(token -> token + UNDERSCORE)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append);
        if (sb.length() <= UNDERSCORE.length()) {
            log.error("snakeCased string is empty for input string {}", string);
            if (sb.length() < UNDERSCORE.length()) {
                return sb.toString();
            }
        }
        String snakeCasedString = sb.substring(0, sb.length() - UNDERSCORE.length());
        return snakeCasedString;
    }

    public static String capitalizeFirstCharacter(@NonNull String string) {
        if (string.isEmpty()) {
            return string;
        }
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

    public static String trimAndUppercaseFirstLetter(String name) {
        name = name.trim();
        name = name.replaceAll("[ ]+", " ");
        if (name.isEmpty()) {
            return name;
        }
        char firstLetter = name.charAt(0);
        if (false == Character.isUpperCase(firstLetter)) {
            name = Character.toUpperCase(firstLetter) + name.substring(1);
        }
        return name;
    }

    public static String trimAndLowercaseFirstLetter(String name) {
        name = name.trim();
        name = name.replaceAll("[ ]+", " ");
        if (name.isEmpty()) {
            return name;
        }
        char firstLetter = name.charAt(0);
        if (false == Character.isLowerCase(firstLetter)) {
            name = Character.toLowerCase(firstLetter) + name.substring(1);
        }
        return name;
    }
}

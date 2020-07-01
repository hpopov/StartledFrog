package net.atlassian.cmathtutor.util;

import java.util.UUID;

public class UidUtil {

    public static String getUId() {
        return UUID.randomUUID().toString();
    }
}

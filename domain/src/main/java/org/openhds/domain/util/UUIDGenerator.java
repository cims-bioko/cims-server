package org.openhds.domain.util;

import static java.util.UUID.randomUUID;

public class UUIDGenerator {
    public static String generate() {
        return randomUUID().toString().replace("-","");
    }
}

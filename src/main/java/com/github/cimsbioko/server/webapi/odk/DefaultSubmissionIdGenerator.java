package com.github.cimsbioko.server.webapi.odk;

import java.util.UUID;

public class DefaultSubmissionIdGenerator implements SubmissionIdGenerator {
    @Override
    public String generateId() {
        return String.format("uuid:%s", UUID.randomUUID());
    }
}

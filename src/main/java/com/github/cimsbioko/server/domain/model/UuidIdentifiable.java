package com.github.cimsbioko.server.domain.model;

/**
 * Identify entities that can be identified by a uuid.
 * <p>
 * BSH
 */
public interface UuidIdentifiable {

    String getUuid();

    void setUuid(String uuid);

}
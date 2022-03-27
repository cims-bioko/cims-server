package com.github.cimsbioko.server.service.impl.indexing;

public class EntitiesAddedEvent implements IndexingEvent {

    private final long added;
    private final long total;

    public EntitiesAddedEvent(long added, long total) {
        this.added = added;
        this.total = total;
    }

    public long getAdded() {
        return added;
    }

    public long getTotal() {
        return total;
    }
}

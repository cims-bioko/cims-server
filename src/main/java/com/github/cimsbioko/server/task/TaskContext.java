package com.github.cimsbioko.server.task;

import java.io.File;

public class TaskContext {

    private File destinationFile;

    public TaskContext(File destinationFile) {
        this.destinationFile = destinationFile;
    }

    public File getDestinationFile() {
        return destinationFile;
    }

}

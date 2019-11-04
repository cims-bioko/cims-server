package com.github.cimsbioko.server.domain;

import java.util.Date;

public interface Task {
    Date getStarted();
    Date getFinished();
    int getItemCount();
    String getDescriptor();
}

package com.github.cimsbioko.server.service;

import java.io.File;

public interface MobileDbGenerator {
    File getTarget();
    void generateMobileDb();
}

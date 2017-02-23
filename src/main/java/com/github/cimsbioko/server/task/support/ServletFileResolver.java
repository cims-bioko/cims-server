package com.github.cimsbioko.server.task.support;

import org.springframework.stereotype.Component;

import java.io.File;

import javax.annotation.Resource;

@Component
public class ServletFileResolver implements FileResolver {

    @Resource
    File dataDir;

    @Override
    public File resolveMobileDBFile() {
        return new File(dataDir, "cims-tablet.db");
    }
}

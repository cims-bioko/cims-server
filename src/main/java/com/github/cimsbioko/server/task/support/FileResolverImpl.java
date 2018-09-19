package com.github.cimsbioko.server.task.support;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

@Component
public class FileResolverImpl implements FileResolver {

    @Resource
    File dataDir;

    @Override
    public File resolveMobileDBFile() {
        return new File(dataDir, "cims-tablet.db");
    }
}

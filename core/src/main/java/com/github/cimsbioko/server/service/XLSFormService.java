package com.github.cimsbioko.server.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

public interface XLSFormService {
    ZipFile convertXLSForm(InputStream xlsxInput) throws IOException;
}

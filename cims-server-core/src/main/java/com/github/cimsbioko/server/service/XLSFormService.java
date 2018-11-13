package com.github.cimsbioko.server.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface XLSFormService {
    File generateXForm(InputStream xlsxInput) throws IOException;
}

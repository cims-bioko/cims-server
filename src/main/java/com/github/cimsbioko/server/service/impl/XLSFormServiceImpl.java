package com.github.cimsbioko.server.service.impl;


import com.github.batkinson.jxlsform.api.XLSFormFactory;
import com.github.batkinson.jxlsform.poi.WorkbookFactory;
import com.github.batkinson.jxlsform.xform.Generator;
import com.github.cimsbioko.server.service.XLSFormService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class XLSFormServiceImpl implements XLSFormService {

    @Autowired
    private Generator generator;

    @Autowired
    private XLSFormFactory xlsformFactory;

    @Autowired
    private WorkbookFactory workbookFactory;

    @Override
    public File generateXForm(InputStream xlsxInput) throws IOException {
        File tempFile = File.createTempFile("xform", "xml");
        try (FileWriter formWriter = new FileWriter(tempFile)) {
            generator.generateXForm(xlsformFactory.create(workbookFactory.create(xlsxInput)), formWriter);
            return tempFile;
        }
    }

}

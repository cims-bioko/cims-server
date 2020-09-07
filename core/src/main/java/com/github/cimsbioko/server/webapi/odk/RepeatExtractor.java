package com.github.cimsbioko.server.webapi.odk;

import org.jdom2.Document;

import java.util.List;

public interface RepeatExtractor {
    List<String[]> extractRepeats(Document formDoc);
}

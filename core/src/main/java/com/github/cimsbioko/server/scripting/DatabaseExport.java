package com.github.cimsbioko.server.scripting;

import java.util.Map;

public interface DatabaseExport {
    String[] initScripts();
    Map<String, String> exportQueries();
    String[] postScripts();
    String exportSchedule();
}
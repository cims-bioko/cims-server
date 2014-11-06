package org.openhds.domain.model;

import org.openhds.domain.annotations.Description;

public class AppSettings {

    @Description(description="Version number of the application.")
    String versionNumber;

    @Description(description="Version name/mnemonic of the application.")
    String versionName;

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
}
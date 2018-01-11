package com.github.cimsbioko.server.web.ui;

public class NavigationMenuBean {

    private boolean showUtilities;
    private boolean showConfiguration;


    public void clear() {
        showUtilities = false;
        showConfiguration = false;
    }

    public void showHome() {
        this.clear();
    }

    public boolean isShowUtilities() {
        return showUtilities;
    }

    public void setShowUtilities(boolean showUtilities) {
        this.clear();
        this.showUtilities = showUtilities;
    }

    public boolean isShowConfiguration() {
        return showConfiguration;
    }

    public void setShowConfiguration(boolean showConfiguration) {
        this.clear();
        this.showConfiguration = showConfiguration;
    }
}

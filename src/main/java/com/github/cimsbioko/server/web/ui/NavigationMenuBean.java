package com.github.cimsbioko.server.web.ui;

import java.util.ArrayList;
import java.util.Iterator;

public class NavigationMenuBean {

    ArrayList<String> trail = new ArrayList<>();

    boolean showHome;
    boolean showUtilities;
    boolean showDevelopers;
    boolean showConfiguration;

    String nextItem;
    String currentItem;

    /**
     * Returns the breadcrumb trail
     */
    public String getBreadcrumbTrail() {
        String result = "";
        Iterator<String> itr = trail.iterator();

        while (itr.hasNext()) {
            result += itr.next();
            if (itr.hasNext())
                result += " > ";
        }
        return result;
    }

    /**
     * If the item doesn't exist in the trail then add it.
     * If the item already exists, remove all items after it.
     */
    public void addCrumb(String item) {

        // if the item is not currently in the trail
        if (!trail.contains(item)) {
            if (showConfiguration) {
                // the page to be viewed is of a different group
                if (!nextItem.equals(currentItem)) {

                    // special cases, because they are handled by flows
                    if (!currentItem.equals("PregnancyOutcome") &&
                            !currentItem.equals("InMigration"))
                        trail.clear();

                    if (showConfiguration)
                        addItemToTrail("Configuration");
                    currentItem = nextItem;
                }
                // the page to be viewed is of the same group, so add it to the trail
                addItemToTrail(item);
            }
            // we are viewing the Utility forms
            else if (showUtilities) {

                // the page to be viewed is of a different group
                if (!nextItem.equals(currentItem)) {

                    trail.clear();
                    addItemToTrail("Utilities");
                    currentItem = nextItem;
                }
                // the page to be viewed is of the same group, so add it to the trail
                addItemToTrail(item);
            }
        }

        // the item is already in the trail
        else if (trail.contains(item)) {

            // get the index of the item
            int index = trail.indexOf(item);

            // keep all items before the index
            ArrayList<String> tempTrail = new ArrayList<>();
            for (int i = 0; i <= index; i++) {
                tempTrail.add(trail.get(i));
            }
            trail.clear();
            trail = tempTrail;
        }
    }

    /**
     * A safer way of adding items to the trail.
     * Ensures that it's not already there.
     */
    public void addItemToTrail(String item) {
        if (!trail.contains(item))
            trail.add(item);
    }

    /**
     * A special case method for handing the Pregnancy Outcome.
     * If the Pregnancy Outcome is a subflow, simply append to
     * the trail. If not, clear everything.
     */
    public void determineOriginForPregOutcome(Boolean flag) {

        if (Boolean.FALSE.equals(flag))
            clearTrailExceptFirst();

        addItemToTrail("PregnancyOutcome Create");
    }

    /**
     * A special case method, used for transition out of
     * a flow. This method is only used from within a flow.
     * It will remove all items following the item specified
     * in the trail except the last item.
     */
    public void removeItemFromTrailInFlow(String item) {
        Iterator<String> itr = trail.iterator();

        ArrayList<String> temp = new ArrayList<>();

        // get the index of the item in the trail.
        // this has to be obtained by using contains since
        // the trail will have something like 'Detail', 'Create', or 'Edit'
        // added to the end of the item.
        int index = 0;
        while (itr.hasNext()) {
            String crumb = itr.next();
            if (crumb.contains(item))
                break;
            index++;
        }

        // take only the items in the trail before the containing item
        for (int i = 0; i < index; i++)
            temp.add(trail.get(i));

        // add them to a temporary trail
        temp.add(trail.get(trail.size() - 1));
        trail = temp;
    }

    public void removeLastCrumb() {
        trail.remove(trail.size() - 1);
    }

    public void clearTrailExceptFirst() {
        ArrayList<String> tempTrail = new ArrayList<>();
        if (trail.size() > 0)
            tempTrail.add(trail.get(0));
        trail = tempTrail;
    }

    public void clear() {
        trail.clear();
        showHome = false;
        showUtilities = false;
        showDevelopers = false;
        showConfiguration = false;
    }

    public ArrayList<String> getTrail() {
        return trail;
    }

    public void setTrail(ArrayList<String> trail) {
        this.trail = trail;
    }

    public boolean isShowHome() {
        return showHome;
    }

    public void setShowHome(boolean showHome) {
        this.clear();
        this.showHome = showHome;
        trail.add("Home");
    }

    public boolean isShowUtilities() {
        return showUtilities;
    }

    public void setShowUtilities(boolean showUtilities) {
        this.clear();
        this.showUtilities = showUtilities;
        trail.add("Utilities");
    }

    public boolean isShowDevelopers() {
        return showDevelopers;
    }

    public void setShowDevelopers(boolean showDevelopers) {
        this.clear();
        this.showDevelopers = showDevelopers;
        trail.add("Developers");
    }

    public String getNextItem() {
        return nextItem;
    }

    public void setNextItem(String nextItem) {
        this.nextItem = nextItem;

        if (this.currentItem == null)
            this.currentItem = this.nextItem;
    }

    public String getCurrentItem() {
        return currentItem;
    }

    public void setCurrentItem(String currentItem) {
        this.currentItem = currentItem;
    }

    public boolean isShowConfiguration() {
        return showConfiguration;
    }

    public void setShowConfiguration(boolean showConfiguration) {
        this.clear();
        this.showConfiguration = showConfiguration;
        trail.add("Configuration");
    }
}

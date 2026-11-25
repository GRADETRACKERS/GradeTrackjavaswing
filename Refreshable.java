package gradetrack.ui;

/** Implemented by every tab so the main window can redraw it after data changes. */
interface Refreshable {
    void refresh();
}

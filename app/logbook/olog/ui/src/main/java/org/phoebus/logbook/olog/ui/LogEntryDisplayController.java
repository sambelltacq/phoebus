/*
 * Copyright (C) 2024 European Spallation Source ERIC.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.phoebus.logbook.olog.ui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.phoebus.logbook.LogEntry;
import org.phoebus.logbook.olog.ui.write.LogEntryEditorStage;
import org.phoebus.olog.es.api.model.LogGroupProperty;
import org.phoebus.olog.es.api.model.OlogLog;


public class LogEntryDisplayController {

    @FXML
    @SuppressWarnings("unused")
    private SingleLogEntryDisplayController singleLogEntryDisplayController;
    @FXML
    @SuppressWarnings("unused")
    private MergedLogEntryDisplayController mergedLogEntryDisplayController;

    private LogEntryTableViewController logEntryTableViewController;
    @FXML
    private ToggleButton showHideLogEntryGroupButton;
    @FXML
    private ToolBar toolBar;
    @FXML
    private Button replyButton;
    @FXML
    private Region spring;
    @FXML
    private Button goBackButton;
    @FXML
    private Button goForwardButton;
    @FXML
    private BorderPane emptyPane;
    @FXML
    private Node singleLogEntryDisplay;
    @FXML
    private Node mergedLogEntryDisplay;

    private final SimpleObjectProperty<LogEntry> logEntryProperty =
            new SimpleObjectProperty<>();


    private final SimpleBooleanProperty hasLinkedEntriesProperty = new SimpleBooleanProperty(false);

    private static final int EMPTY = 0;
    private static final int SINGLE = 1;
    private static final int MERGED = 2;
    private final SimpleIntegerProperty currentViewProperty = new SimpleIntegerProperty(EMPTY);

    @FXML
    public void initialize() {
        replyButton.disableProperty()
                .bind(Bindings.createBooleanBinding(() -> logEntryProperty.get() == null, logEntryProperty));
        showHideLogEntryGroupButton.disableProperty().bind(hasLinkedEntriesProperty.not());
        toolBar.setVisible(LogbookUIPreferences.log_entry_groups_support);
        toolBar.setManaged(LogbookUIPreferences.log_entry_groups_support);
        emptyPane.visibleProperty()
                .bind(Bindings.createBooleanBinding(() -> currentViewProperty.get() == EMPTY, currentViewProperty));
        singleLogEntryDisplay.visibleProperty()
                .bind(Bindings.createBooleanBinding(() -> currentViewProperty.get() == SINGLE, currentViewProperty));
        mergedLogEntryDisplay.visibleProperty()
                .bind(Bindings.createBooleanBinding(() -> currentViewProperty.get() == MERGED, currentViewProperty));
        HBox.setHgrow(spring, Priority.ALWAYS); // Spring to make subsequent elements right-aligned in the toolbar.
    }

    @FXML
    public void showHideLogEntryGroup() {
        if (showHideLogEntryGroupButton.selectedProperty().get()) {
            currentViewProperty.set(MERGED);
            mergedLogEntryDisplayController.setLogEntry(logEntryProperty.get());
            mergedLogEntryDisplayController.setLogSelectionHandler((logEntry) -> {
                Platform.runLater(() -> {
                    currentViewProperty.set(SINGLE);
                    if(logEntryTableViewController.selectLogEntry(logEntry)){
                        singleLogEntryDisplayController.setLogEntry(logEntry);
                    }
                });
                return null;
            });
        } else {
            currentViewProperty.set(SINGLE);
        }
    }

    @FXML
    public void reply() {
        // Show a new editor dialog. When user selects to save the reply entry, update the original log entry
        // to ensure that it contains the log group property.
        new LogEntryEditorStage(new OlogLog(),  logEntryProperty.get(), null).show();
    }

    @FXML
    public void newLogEntry(){
        // Show a new editor dialog.
        new LogEntryEditorStage(new OlogLog(),  null, null).show();
    }

    @FXML
    public void goBack() {
        logEntryTableViewController.goBackAndGoForwardActions.goBack();
    }

    @FXML
    public void goForward() {
        logEntryTableViewController.goBackAndGoForwardActions.goForward();
    }

    public void setLogEntry(LogEntry logEntry) {
        if(logEntry == null){
            currentViewProperty.set(EMPTY);
        }
        else{
            logEntryProperty.set(logEntry);
            singleLogEntryDisplayController.setLogEntry(logEntry);
            currentViewProperty.set(SINGLE);
            showHideLogEntryGroupButton.selectedProperty().set(false);
            hasLinkedEntriesProperty.set(logEntry.getProperties()
                    .stream().anyMatch(p -> p.getName().equals(LogGroupProperty.NAME)));
        }
    }

    public LogEntry getLogEntry() {
        return logEntryProperty.get();
    }

    /**
     * Updates the current {@link LogEntry} if it matches the passed argument.
     * @param logEntry A log entry that has been updated by user and saved by service.
     */
    public void updateLogEntry(LogEntry logEntry){
        // Log entry display may be "empty", i.e. logEntryProperty not set yet
        if(!logEntryProperty.isNull().get() && logEntryProperty.get().getId().equals(logEntry.getId())){
            setLogEntry(logEntry);
        }
    }

    public void setLogEntryTableViewController(LogEntryTableViewController logEntryTableViewController){
        this.logEntryTableViewController = logEntryTableViewController;
        goBackButton.disableProperty().bind(Bindings.isEmpty(logEntryTableViewController.goBackAndGoForwardActions.goBackActions));
        goForwardButton.disableProperty().bind(Bindings.isEmpty(logEntryTableViewController.goBackAndGoForwardActions.goForwardActions));
    }
}

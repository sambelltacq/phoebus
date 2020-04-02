package org.phoebus.logbook.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;
import org.phoebus.logbook.Property;

import java.util.List;
import java.util.stream.Collectors;

public class LogPropertiesController {

    @FXML
    TreeTableView<PropertyTreeNode> treeTableView;

    @FXML
    TreeTableColumn name;
    @FXML
    TreeTableColumn value;

    // Model for the properties view is a list of properties.
    List<Property> properties;

    @FXML
    public void initialize()
    {
        name.setMaxWidth(1f * Integer.MAX_VALUE * 50);
        name.setCellValueFactory(
                new Callback<TreeTableColumn.CellDataFeatures<PropertyTreeNode, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<PropertyTreeNode, String> p) {
                        return p.getValue().getValue().nameProperty();
                    }
                });

        value.setMaxWidth(1f * Integer.MAX_VALUE * 50);
        value.setCellValueFactory(
                new Callback<TreeTableColumn.CellDataFeatures<PropertyTreeNode, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<PropertyTreeNode, String> p) {
                        return p.getValue().getValue().valueProperty();
                    }
                });

        constructTree();

    }

    private void constructTree() {
        if (this.properties != null && !this.properties.isEmpty())
        {
            TreeItem root = new TreeItem(new PropertyTreeNode("properties", " "));
            root.getChildren().setAll(properties.stream().map(property -> {
                PropertyTreeNode node = new PropertyTreeNode(property.getName(), " ");
                TreeItem<PropertyTreeNode> treeItem = new TreeItem<>(node);
                property.getAttributes().entrySet().stream().forEach(entry -> {
                    treeItem.getChildren().add(new TreeItem<>(new PropertyTreeNode(entry.getKey(), entry.getValue())));
                });
                treeItem.setExpanded(true);
                return treeItem;
            }).collect(Collectors.toSet()));
            treeTableView.setRoot(root);
            treeTableView.setShowRoot(false);
        }
    }

    /**
     * Set the list of properties to be displayed.
     * @param properties
     */
    public void setProperties(List<Property> properties)
    {
        this.properties = properties;
        constructTree();
    }

    /**
     * @return The list of logentry properties
     */
    public List<Property> getProperties()
    {
        return properties;
    }

    private static class PropertyTreeNode
    {
        private SimpleStringProperty name;
        private SimpleStringProperty value;

        public SimpleStringProperty nameProperty() {
            if (name == null) {
                name = new SimpleStringProperty(this, "name");
            }
            return name;
        }

        public SimpleStringProperty valueProperty() {
            if (value == null) {
                value = new SimpleStringProperty(this, "value");
            }
            return value;
        }

        private PropertyTreeNode(String name, String value) {
            this.name = new SimpleStringProperty(name);
            this.value = new SimpleStringProperty(value);
        }
    }
}

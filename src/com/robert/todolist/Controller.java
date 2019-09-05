package com.robert.todolist;

import com.robert.todolist.datamodel.TodoData;
import com.robert.todolist.datamodel.TodoItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {

    private List<TodoItem> todoItems;

    @FXML
    private TextArea itemDetailsTextArea;

    @FXML
    private ListView<TodoItem> todoListView;

    @FXML
    private Label deadlineLabel;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private ContextMenu listContextMenu;

    @FXML
    private ToggleButton filterToggleButton;

    @FXML
    private FilteredList<TodoItem> filteredList;

    private Predicate<TodoItem> showAll;
    private Predicate<TodoItem> todaysItems;

    public void initialize() {

        listContextMenu = new ContextMenu(); //This is the menu that will show when user right clicks on an item.
        MenuItem deleteMenuItem = new MenuItem("Delete");
        //Now we do the code to delete the item when right clicked on
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });
        listContextMenu.getItems().addAll(deleteMenuItem);

        todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TodoItem>() {
            @Override
            public void changed(ObservableValue<? extends TodoItem> observableValue, TodoItem oldValue, TodoItem newValue) {
              if(newValue != null)
              {
                  TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                  itemDetailsTextArea.setText(item.getDetails());

                  DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                  deadlineLabel.setText(df.format(item.getDeadline()));
              }
            }
        });

        showAll = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem item) {
                return true;
            }
        };

        todaysItems = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem item) {
                return (item.getDeadline().equals(LocalDate.now()));
            }
        };

        filteredList = new FilteredList<>(TodoData.getInstance().getTodoItems(),showAll);

        //Creating a sorted list and overriding compare form comparator class in order to show list of to-do items sorted
        SortedList<TodoItem> sortedList = new SortedList<TodoItem>(filteredList,
                new Comparator<TodoItem>() {
                    @Override
                    public int compare(TodoItem o1, TodoItem o2) {
                        return o1.getDeadline().compareTo(o2.getDeadline()); //Java class for dates has a compareTo method already implemented
                    }
                });

        todoListView.setItems(sortedList); //Binds listView to the observableList in TodoData class
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); //Selects only 1 item at a time
        todoListView.getSelectionModel().selectFirst();

        //This method is meant to highlight items based on a criteria(close deadline), it takes a todoItem and highlights
        //all cells where this item appears
        todoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> todoItemListView) {
                ListCell<TodoItem> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(TodoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty) {
                            setText(null);
                        }
                        else {
                            setText(item.getShortDescription());
                            if(item.getDeadline().isBefore(LocalDate.now().plusDays(1))) //If today is the deadline, but also items that are inserted with past due dates will be colored red
                                setTextFill(Color.RED); //Show text in red

                            else if(item.getDeadline().equals(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.ORANGE);
                            }
                        }

                    }
                };

                //This is once we delete an item, the list will be updated and will not show an empty cell
                cell.emptyProperty().addListener( //This is a lambda expression
                        (obs,wasEmpty,isNowEmpty) -> {
                            if(isNowEmpty) { //If cell was deleted...
                                cell.setContextMenu(null); //...don't show and empty space, else...
                            } else {
                                cell.setContextMenu(listContextMenu); //...update the list
                            }
                        });

                return cell;
            }
        });
    }

    public void showNewItemDialog() {

        //Creating the drop-down menu
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add new Todo Item");

        //Loads TodoItemDialog.fxml
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));
        try
        {
            //When a new todoItem is created the load method that will be called is the one in the DialogController class
            //and not in the Controller one.
            dialog.getDialogPane().setContent(fxmlLoader.load());

        }catch(IOException e) {
            System.out.println("couldn't load the dialog pane...");
            System.out.println(e.getMessage());
            return;
        }

        //Buttons for accepting or canceling the addition fo the new todoItem.
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {

            DialogController controller = fxmlLoader.getController();
            TodoItem newItem = controller.processResult(); //Creates the new to-do item and returns it.
            todoListView.getSelectionModel().select(newItem); //When new item is created, it will also be selected
            System.out.println("OK pressed");

        } else
            System.out.println("Cancel pressed");
    }

    @FXML
    public void handleKeyPressed(KeyEvent keyEvent) {

        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem(); //Get the item selected

        if(selectedItem != null) { //Checks if an item was selected
            if(keyEvent.getCode().equals(KeyCode.DELETE)) //if the key pressed was delete, then ...
                deleteItem(selectedItem); //...deletes the item.
        }
    }

    @FXML
    public void handleClickListView() {

        //This class tracks which item is selected
        TodoItem item = todoListView.getSelectionModel().getSelectedItem();
        itemDetailsTextArea.setText(item.getDetails());
        deadlineLabel.setText(item.getDeadline().toString());

    }

    public void deleteItem(TodoItem item) {
        //Now before we delete the item, we will ask first the user if indeed he wants to delete an item. For this
        //we will use a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Todo Item");
        alert.setHeaderText("Delete item: " + item.getShortDescription());
        alert.setContentText("Are you sure? Press OK to confirm, or cancel to Back out.");
        //This restricts user from doing something else once the confirmation dialog shows up.
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            TodoData.getInstance().deleteTodoItem(item);
        }

    }

    /**
     * Filters the list if button "Today's Items" is selected, if not it will show all items
     */
    @FXML
    public void handleFilterButton() {

        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(filterToggleButton.isSelected()) {
            filteredList.setPredicate(todaysItems); //Predicate to show all items

            //This fixes a bug where after filtering the list, the user was still able to see the last item selected
            //whether it was filtered or not.
            if(filteredList.isEmpty()) {
                itemDetailsTextArea.clear();
                deadlineLabel.setText("");
            }else if(filteredList.contains(selectedItem)) {
                todoListView.getSelectionModel().select(selectedItem);
            } else {
                todoListView.getSelectionModel().selectFirst();
            }
        }else {
            filteredList.setPredicate(showAll); //Predicate to show only items due today
            todoListView.getSelectionModel().select(selectedItem);
        }
    }

    @FXML
    public void handleExit() {
        Platform.exit();
    }
}

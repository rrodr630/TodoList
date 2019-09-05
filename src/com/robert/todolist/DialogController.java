package com.robert.todolist;

import com.robert.todolist.datamodel.TodoData;
import com.robert.todolist.datamodel.TodoItem;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class DialogController {

    @FXML
    private TextField shortDescriptionField;

    @FXML
    private TextArea detailsArea;

    @FXML
    private DatePicker deadlinePicker;

    public TodoItem processResult() {
        String shortDescription = shortDescriptionField.getText().trim();
        String details = detailsArea.getText().trim();
        LocalDate deadlineValue = deadlinePicker.getValue();

        TodoItem newItem = new TodoItem(shortDescription,details,deadlineValue);

        //When user presses ok, this will create the enw todoItem.
        TodoData.getInstance().addTodoItem(newItem);

        return newItem;
    }
}

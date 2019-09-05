package com.robert.todolist.datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

/**
 * An important note is that this class is a Singleton (can only have one instance) and also
 * its whole purpose is to save and load data from a file.
 */
public class TodoData {

    private static TodoData instance = new TodoData();//This is the only instance of this class, which calls the private constructor
    private static String filename = "TodoListITems.txt";

    //ObservableList makes possible dataBinding... also observableList is more specialize than List from java.util
    //meaning a much better performance and complexity
    private ObservableList<TodoItem> todoItems;
    private DateTimeFormatter formatter;

    public static TodoData getInstance() {
        return instance;
    }

    private TodoData() {
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    }

    public ObservableList<TodoItem> getTodoItems() {
        return todoItems;
    }

    public void addTodoItem(TodoItem item) {
        todoItems.add(item);
    }

    /**
     * Try to use a dataBase for this method and the following one as well to store and read
     * info from a database.
     * @throws IOException
     */
    public void loadTodoItems() throws IOException {
        todoItems = FXCollections.observableArrayList();
        Path path = Paths.get(filename);
        BufferedReader br = Files.newBufferedReader(path);

        String input;

        while ((input = br.readLine()) != null) {
            String[] itemPieces = input.split("\t");

            String shortDescription = itemPieces[0];
            String details = itemPieces[1];
            String dateString = itemPieces[2];

            LocalDate date = LocalDate.parse(dateString, formatter);

            TodoItem todoItem = new TodoItem(shortDescription, details, date);
            todoItems.add(todoItem);
        }

        try {

        } finally {
            if(br != null) {
                br.close();
            }
        }
    }

    public void storeTodoItems() throws IOException {

        Path path = Paths.get(filename);
        BufferedWriter bw = Files.newBufferedWriter(path);

        //Homework is to change this try block for "try with resources" (re-watch this section of the course)
        try {

            Iterator<TodoItem> iter = todoItems.iterator();

            while(iter.hasNext())
            {
                TodoItem item = iter.next();
                bw.write(String.format("%s\t%s\t%s",
                        item.getShortDescription(),
                        item.getDetails(),
                        item.getDeadline().format(formatter)
                        ));
                bw.newLine();
            }

        } finally {
            if(bw != null) {
                bw.close();
            }
        }
    }

    public void deleteTodoItem(TodoItem item) {
        todoItems.remove(item);
    }

}

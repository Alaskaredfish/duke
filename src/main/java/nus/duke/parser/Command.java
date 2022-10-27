package nus.duke.parser;

import nus.duke.data.DukeException;
import nus.duke.storage.Storage;
import nus.duke.ui.Messages;
import nus.duke.ui.Ui;
import nus.duke.tasklist.*;
import nus.duke.tasklist.TaskList;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Represents an executable command.
 */
public class Command {
    static private String command;
    static private String description;
    static private LocalDateTime dateAndTime1;
    static private LocalDateTime dateAndTime2;
    static private String preposition;
    static private int index;
    private boolean isEXit = false;

    /**
     * Command constructor for the "list" command.
     */
    public Command(String command){
        this.command = command;
    }
    /**
     * Command constructor for the "bye" command.
     */
    public Command(String command, boolean isExit){
        this.command = command;
        this.isEXit = isExit;
    }
    /**
     * Command constructor for the commands with index, E.g. "mark", "unmark" and "delete".
     */
    public Command(String command, int index) {
        this.command = command;
        this.index = index;
    }
    /**
     * Command constructor for the "todo" command.
     */
    public Command(String command, String description) {
        this.command = command;
        this.description = description;
    }
    /**
     * Command constructor for the "deadline" command.
     */
    public Command(String command, String description, String preposition, String date1, String time1) {
        this.command = command;
        this.description = description;
        this.preposition = preposition;
        this.dateAndTime1 = LocalDateTime.parse(date1 + " " + time1, TaskList.STORAGE_FORMATTER);
    }
    /**
     * Command constructor for the "event" command.
     */
    public Command(String command, String description, String preposition, String date1, String time1, String date2, String time2) {
        this.command = command;
        this.description = description;
        this.preposition = preposition;
        this.dateAndTime1 = LocalDateTime.parse(date1 + " " + time1, TaskList.STORAGE_FORMATTER);
        this.dateAndTime2 = LocalDateTime.parse(date2 + " " + time2, TaskList.STORAGE_FORMATTER);
    }

    /**
     * Extract the isExit of the current command.
     */
    public boolean isExit() {
        return this.isEXit;
    }
    /**
     * Executes the command.
     */
    public void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
        ArrayList<Task> list = tasks.getList();
        switch (this.command) {
            case "bye":
                byeCommand(ui);
                break;
            case "mark":
                markCommand(list);
                storage.save(tasks);
                break;
            case "unmark":
                unMarkCommand(list);
                storage.save(tasks);
                break;
            case "delete":
                deleteCommand(tasks, ui);
                storage.save(tasks);
                break;
            case "list":
                tasks.printList();
                break;
            case "todo":
            case "deadline":
            case "event":
                try {
                    addCommand(tasks, ui, storage);
                } catch (DukeException e){
                    throw new DukeException(e.getMessage());
                }
                break;
            default:
                ui.print(Messages.MESSAGE_NOT_A_TASK);

        }
    }
    /**
     * Mark the task as done with reference to the index provided in the command.
     *
     * @throws DukeException is the index in this command is out of the task list range.
     */
    public void markCommand(ArrayList<Task> list) throws DukeException {
        try {
            list.get(this.index).markAsDone(0);
        } catch (IndexOutOfBoundsException e){
            throw new DukeException(Messages.MESSAGE_TASK_NUMBER_OUT_OF_RANGE);
        }
    }
    /**
     * Mark the task as undone with reference to the index provided in the command.
     *
     * @throws DukeException is the index in this command is out of the task list range.
     */
    public void unMarkCommand(ArrayList<Task> list) throws DukeException {
        try {
            list.get(this.index).markAsUndone(0);
        } catch (IndexOutOfBoundsException e){
            throw new DukeException(Messages.MESSAGE_TASK_NUMBER_OUT_OF_RANGE);
        }
    }
    /**
     * Delete the task with reference to the index provided in the command.
     *
     * @throws DukeException is the index in this command is out of the task list range.
     */
    public void deleteCommand(TaskList tasks, Ui ui) throws DukeException {
        ArrayList<Task> list = tasks.getList();
        String removedTask;
        try {
            removedTask = list.get(this.index).toString(1);
            list.remove(this.index);
        }catch (IndexOutOfBoundsException e){
            throw new DukeException(Messages.MESSAGE_TASK_NUMBER_OUT_OF_RANGE);
        }
        tasks.changeListCount("-");
        ui.echo(removedTask, "removed");
    }
    /**
     * Add three types of tasks "todo", "deadline" & "event" to the task list, save to a .txt file and echo off.
     */
    public void addCommand (TaskList tasks, Ui ui, Storage storage) throws DukeException {
        ArrayList<Task> list = tasks.getList();
        //check if the task to be added is existed
        if (tasks.getListCount() > 0) {
            try {
                isDuplicate(tasks);
            } catch (DukeException e) {
                throw new DukeException(e.getMessage());
            }
        }
        switch (command) {
            case "todo":
                list.add(new Todo(description));
                break;
            case "deadline":
                list.add(new Deadline(description, preposition, dateAndTime1));
                break;
            case "event":
                list.add(new Event(description, preposition, dateAndTime1, dateAndTime2));
                break;
            default:
        }
        tasks.changeListCount("+");
        storage.save(tasks);
        ui.echo(list.get(tasks.getListCount() - 1).toString(1), "added");
    }
    /**
     * Execute the "bye" command by displaying the bye message.
     */
    public void byeCommand (Ui ui){
        ui.showByeMessage();
    }

    /**
     * Checker before adding command, scan through the task list for any existing duplicated task.
     *
     * @throws DukeException if any of the existing task is identical to the task to be added.
     */
    public static void isDuplicate (TaskList tasks) throws DukeException{
        ArrayList<Task> list = tasks.getList();
        for (int i = 0; i < tasks.getListCount(); i++) {
            switch (command) {
                case "todo":
                    if(list.get(i).getDescription().equals(description)){
                        throw new DukeException(Messages.MESSAGE_DUPLICATE_TASK);
                    }
                    break;
                case "deadline":
                    if(list.get(i).getDescription().equals(description)){
                        if (list.get(i).getDateAndTime(1).equals(dateAndTime1.format(TaskList.DISPLAY_FORMATTER))) {
                            throw new DukeException(Messages.MESSAGE_DUPLICATE_TASK);
                        }
                    }
                    break;
                case "event":
                    if(list.get(i).getDescription().equals(description)){
                        if (list.get(i).getDateAndTime(1).equals(dateAndTime1.format(TaskList.DISPLAY_FORMATTER) + "-" + dateAndTime2.format(TaskList.DISPLAY_FORMATTER))) {
                            throw new DukeException(Messages.MESSAGE_DUPLICATE_TASK);
                        }
                    }
                    break;
                default:
                    return;
            }
        }
    }
}
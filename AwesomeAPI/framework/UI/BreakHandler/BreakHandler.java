package com.AwesomeAPI.framework.UI.BreakHandler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runemate.game.api.hybrid.util.StopWatch;
import com.runemate.game.api.hybrid.util.calculations.Random;
import com.runemate.game.api.script.framework.AbstractBot;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BreakHandler {

    TableView<BreakTracker> TV_BreakHandler;
    TableColumn<BreakTracker, String> TC_Start;
    TableColumn<BreakTracker, String> TC_Duration;
    TableColumn<BreakTracker, String> TC_End;

    private ObservableList<BreakTracker> breakTracker = FXCollections.observableArrayList();
    private List<String> startTimes = new ArrayList<>();
    private List<String> durationsL = new ArrayList<>();

    /**
     * Used to determine if a bot is breaking or not.
     *
     * @param stopWatch   A StopWatch from the main bot to use to check times.
     * @return Boolean that is true when a break contains the current StopWatch time, false if none do.
     */
    public boolean isBreaking(StopWatch stopWatch) {
        checkBreaks();
        boolean breaking = false;
        int breakTimes[] = new int[startTimes.size()];
        int durations[] = new int[durationsL.size()];

        for (int i = 0; i < breakTimes.length; i++) {
            breakTimes[i] = convertToMilli(startTimes.get(i));
            durations[i] = convertToMilli(durationsL.get(i));
        }
        for (int i = 0; i < breakTimes.length; i++) {
            if (breakTimes[i] <= 0 || durations[i] <= 0) {
                break;
            } else {
                if (breakTimes[i] <= (stopWatch.getRuntime()) && (breakTimes[i] + durations[i]) >= stopWatch.getRuntime()) {
                    breaking = true;
                    break;
                }
            }
        }
        return breaking;
    }

    /**
     * Used to check user input times for a break handler.
     *
     * @param time A use input String for a runtime in format 00:00:00.
     * @return True if the format contains the ":"s and appropriate numbers with no other characters.
     */
    public static boolean checkValid(String time) {
        if (!time.matches("^\\d{2}:\\d{2}:\\d{2}$")) {
            System.out.println("Sent");
            return false;
        }
        return true;
    }

    /**
     * Used to calculate the end time as a time String from a start and duration String.
     *
     * @param start    A String time format for the start time.
     * @param duration A String time format for the duration of the break.
     * @return A String that is the end time for the break.
     */
    public static String getEnd(String start, String duration) {
        return revertToString(convertToMilli(start) + convertToMilli(duration));
    }

    /**
     * Used to calculate the start time as a time String from a duration and end String.
     *
     * @param duration A String time format for the duration of the break.
     * @param end      A String time format for the end time of the break.
     * @return A String that is the Start time of the break.
     */
    public static String getStart(String duration, String end) {
        return revertToString(convertToMilli(end) - convertToMilli(duration));
    }

    /**
     * Used to get the duration of a break from the start and end time Strings.
     *
     * @param start A String time format for the start time of the break.
     * @param end   A String time format for the end of the break.
     * @return A String that is the duration of the break.
     */
    public static String getDuration(String start, String end) {
        return revertToString(convertToMilli(end) - convertToMilli(start));
    }

    /**
     * Used to convert a String time to an int in milliseconds.
     * Calls the checkValid() method to check if the input time String is a valid time String.
     *
     * @param runtime A String time to be converted to milliseconds.
     * @return An int that is the number of whole milliseconds the runtime represents.
     */
    public static int convertToMilli(String runtime) {
        int numOfColons = 0;
        int converted = 0;
        for (int l = 0; l < runtime.length(); l++) {
            if (runtime.charAt(l) == ':') {
                numOfColons++;
            }
        }
        if (numOfColons != 2) {
        } else if (!checkValid(runtime)) {
        } else {
            converted = Integer.parseInt(runtime.substring(0, runtime.indexOf(':'))) * 3600000
                    + Integer.parseInt(runtime.substring(runtime.indexOf(':') + 1, runtime.lastIndexOf(':'))) * 60000
                    + Integer.parseInt(runtime.substring(runtime.lastIndexOf(':') + 1, runtime.length())) * 1000;
        }
        return converted;
    }

    /**
     * Takes a time in milliseconds as an int and converts it back to a time String.
     * The max time unit is Hours. This does not go to days, be warned.
     *
     * @param breakTime An int of the milliseconds for a time.
     * @return The time String from the milliseconds.
     */
    public static String revertToString(int breakTime) {
        String breakTime1 = "";
        int hours;
        int minutes;
        int seconds;


        if ((hours = (breakTime) / 3600000) < 10)
            breakTime1 = breakTime1 + "0" + hours + ":";
        else {
            breakTime1 = breakTime1 + hours + ":";
        }


        if ((minutes = (((breakTime) % 3600000) / 60000)) < 10)
            breakTime1 = breakTime1 + "0" + minutes + ":";
        else {
            breakTime1 = breakTime1 + minutes + ":";
        }


        if ((seconds = ((breakTime) % 3600000 % 60000) / 1000) < 10) {
            breakTime1 = breakTime1 + "0" + seconds;
        } else {
            breakTime1 = breakTime1 + seconds;
        }

        return breakTime1;
    }

    /**
     * An EventHandler that will fill in the different ListViews you have for your break handler.
     * I wish i knew how to use TableView :( .
     *
     * @param LV_Start    A ListView for the start times.
     * @param LV_Duration A ListView for the Durations.
     * @param LV_End      A ListView for the ends.
     * @return An EventHandler that will execute the event whenever someone edits the ListView this is added to.
     */
    public static EventHandler<ListView.EditEvent<String>> listViewFillInTimes(ListView<String> LV_Start, ListView<String> LV_Duration, ListView<String> LV_End) {
        return event -> {
            event.getSource().getItems().set(event.getIndex(), event.getNewValue());
            System.out.println("Edited :D");
            String time;
            int row;
            String time1;
            if (event.getSource().getId().equals("LV_Start")) {
                System.out.println("TC_Start :D");
                row = event.getIndex();
                if (!(time = LV_Duration.getItems().get(row)).isEmpty()) {
                    time1 = event.getSource().getItems().get(row);
                    System.out.println("Time: " + time + " row: " + row + " Time1: " + time1);
                    LV_End.getItems().set(row, BreakHandler.getEnd(time, time1));
                } else if (!(time = LV_End.getItems().get(row)).isEmpty()) {
                    time1 = event.getSource().getItems().get(row);
                    LV_Duration.getItems().set(row, BreakHandler.getDuration(time, time1));
                }
            } else if (event.getSource().getId().equals("LV_Duration")) {
                row = event.getIndex();
                if (!(time = LV_Start.getItems().get(row)).isEmpty()) {
                    time1 = event.getSource().getItems().get(row);
                    LV_End.getItems().set(row, BreakHandler.getEnd(time, time1));
                } else if (!(time = LV_End.getItems().get(row)).isEmpty()) {
                    time1 = event.getSource().getItems().get(row);
                    LV_Start.getItems().set(row, BreakHandler.getStart(time1, time));
                }
            } else if (event.getSource().getId().equals("LV_End")) {
                row = event.getIndex();
                if (!(time = LV_Duration.getItems().get(row)).isEmpty()) {
                    time1 = event.getSource().getItems().get(row);
                    LV_Start.getItems().set(row, BreakHandler.getStart(time, time1));
                } else if (!(time = LV_Start.getItems().get(row)).isEmpty()) {
                    time1 = event.getSource().getItems().get(row);
                    LV_Duration.getItems().set(row, BreakHandler.getDuration(time, time1));
                }
            }
        };
    }



    public static class BreakTracker {

        private final StringProperty startTime;
        private final StringProperty duration;
        private final StringProperty endTime;

        public BreakTracker(String startTime, String duration, String endTime) {
            this.startTime = new SimpleStringProperty(startTime);
            this.endTime = new SimpleStringProperty(endTime);
            this.duration = new SimpleStringProperty(duration);
        }

        public String getStartTime() {
            return startTime.get();
        }

        public String getDuration() {
            return duration.get();
        }

        public String getEndTime() {
            return endTime.get();
        }

        public void setStartTime(String time) {
            startTime.set(time);
        }

        public void setDuration(String time) {
            duration.set(time);
        }

        public void setEndTime(String time) {
            endTime.set(time);
        }

        public StringProperty startTimeProperty() {
            return startTime;
        }

        public StringProperty durationProperty() {
            return duration;
        }

        public StringProperty endProperty() {
            return endTime;
        }

    }

    public void createBreakHandler(TitledPane pane, AbstractBot bot){

        TV_BreakHandler = new TableView<>();
        TC_Start = new TableColumn<>("Start");
        TC_Duration = new TableColumn<>("Duration");
        TC_End = new TableColumn<>("End");

        TC_Start.setOnEditCommit(tableFillInTimes(breakTracker));
        TC_Duration.setOnEditCommit(tableFillInTimes(breakTracker));
        TC_End.setOnEditCommit(tableFillInTimes(breakTracker));

        TC_Start.setCellFactory(TextFieldTableCell.forTableColumn());
        TC_Duration.setCellFactory(TextFieldTableCell.forTableColumn());
        TC_End.setCellFactory(TextFieldTableCell.forTableColumn());

        TC_Start.setEditable(true);
        TC_Duration.setEditable(true);
        TC_End.setEditable(true);

        TC_Start.setCellValueFactory(param -> param.getValue().startTimeProperty());
        TC_Duration.setCellValueFactory(param -> param.getValue().durationProperty());
        TC_End.setCellValueFactory(param -> param.getValue().endProperty());

        TV_BreakHandler.getColumns().addAll(TC_Start, TC_Duration, TC_End);

        TV_BreakHandler.setPrefHeight(200);

        TV_BreakHandler.setEditable(true);

        TV_BreakHandler.setItems(breakTracker);

        TV_BreakHandler.getItems().addAll(new BreakTracker("", "", ""), new BreakTracker("", "", ""));

        TV_BreakHandler.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Separator separator = new Separator(Orientation.HORIZONTAL);

        Button BN_Save = new Button("Save");

        BN_Save.setTooltip(new Tooltip("You must enter a name \n to save in case you didn't."));

        TextField TF_ProfName = new TextField("Profile Name");

        Separator separator1 = new Separator(Orientation.VERTICAL);
        separator.setVisible(false);
        separator1.setVisible(false);
        separator1.setPrefWidth(200);

        Button BN_Generate = new Button("Generate");

        Button BN_Clear = new Button("Clear");

        Separator separator2 = new Separator(Orientation.VERTICAL);
        separator2.setVisible(false);
        Separator separator3 = new Separator(Orientation.VERTICAL);
        separator3.setVisible(false);

        HBox hBox1 = new HBox(BN_Save, separator2, TF_ProfName, separator1, BN_Generate, separator3, BN_Clear);
        hBox1.setAlignment(Pos.CENTER);

        Button BN_Load = new Button("Load");

        BN_Load.setTooltip(new Tooltip("You must select a previous break name \n before you can click load just so you know."));

        Separator separator4 = new Separator(Orientation.VERTICAL);
        separator4.setVisible(false);

        ComboBox<String> CB_Prof = new ComboBox<>();

        Separator separator5 = new Separator(Orientation.VERTICAL);
        separator5.setVisible(false);

        Button BN_Delete = new Button("Delete");

        HBox hBox2 = new HBox(BN_Load, separator4, CB_Prof, separator5, BN_Delete);
        hBox2.setAlignment(Pos.CENTER);

        VBox vBox = new VBox(TV_BreakHandler, separator, hBox1, hBox2);


        pane.setContent(vBox);

        CB_Prof.getItems().addAll(loadProfs(bot));
        BN_Generate.setOnAction(generateBreaks(breakTracker));
        BN_Clear.setOnAction(clearBreaks(breakTracker));
        BN_Load.setOnAction(loadBreaks(breakTracker, CB_Prof, bot));
        BN_Save.setOnAction(saveBreaks(breakTracker, TF_ProfName, CB_Prof, bot));
        BN_Delete.setOnAction(deleteProf(CB_Prof, bot));

        Tooltip tooltip = new Tooltip("Press enter after typing your break \n to have it save.");
        TV_BreakHandler.setTooltip(tooltip);


    }


    public void checkBreaks() {
        BreakHandler.BreakTracker tracker;
        startTimes.clear();
        durationsL.clear();

        for (int i = 0; i < breakTracker.size(); i++) {
            tracker = breakTracker.get(i);
            startTimes.add(i, tracker.getStartTime());
            durationsL.add(i, tracker.getDuration());
        }
    }

    public static EventHandler<TableColumn.CellEditEvent<BreakTracker, String>> tableFillInTimes(List<BreakTracker> breakTracker) {
        return event -> {
            String time;
            int row;
            int col;
            String time1;
            time = event.getNewValue();
            if (BreakHandler.checkValid(time)) {
                row = event.getTablePosition().getRow();
                System.out.println(row);
                col = event.getTablePosition().getColumn();
                if (breakTracker.size() - 1 <= row) {
                    breakTracker.add(new BreakTracker("", "", ""));
                }
                if (col == 0) { //Start time
                    breakTracker.get(row).setStartTime(event.getNewValue());
                    if (!(time1 = breakTracker.get(row).endProperty().get()).isEmpty()) {
                        breakTracker.get(row).setDuration(BreakHandler.getDuration(time, time1));
                    } else if (!(time1 = breakTracker.get(row).durationProperty().get()).isEmpty()) {
                        breakTracker.get(row).setEndTime(BreakHandler.getEnd(time, time1));
                    }
                } else if (col == 1) { //Duration
                    breakTracker.get(row).setDuration(event.getNewValue());
                    if (!(time1 = breakTracker.get(row).endProperty().get()).isEmpty()) {
                        breakTracker.get(row).setStartTime(BreakHandler.getStart(time, time1));
                    } else if (!(time1 = breakTracker.get(row).startTimeProperty().get()).isEmpty()) {
                        breakTracker.get(row).setEndTime(BreakHandler.getEnd(time1, time));
                    }
                } else {
                    breakTracker.get(row).setEndTime(event.getNewValue());
                    if (!(time1 = breakTracker.get(row).startTimeProperty().get()).isEmpty()) {
                        breakTracker.get(row).setDuration(BreakHandler.getDuration(time1, time));
                    } else if (!(time1 = breakTracker.get(row).durationProperty().get()).isEmpty()) {
                        breakTracker.get(row).setStartTime(BreakHandler.getStart(time1, time));
                    }
                }
            }
        };
    }

    public static EventHandler<ActionEvent> generateBreaks(List<BreakTracker> breakTracker) {
        return event -> {
            breakTracker.clear();
            for (int i = 0; i < 15; i++) {
                int duration = 0;
                int startTime = 0;
                if (i > 0)
                    breakTracker.add(new BreakTracker(revertToString((startTime = convertToMilli(breakTracker.get(i - 1).getEndTime()) + 6 * Random.nextInt(900000, 3600001))),
                            revertToString((duration = 6 * Random.nextInt(450000, 3600001))), revertToString(startTime + duration)));
                else
                    breakTracker.add(new BreakTracker(revertToString((startTime = 6 * Random.nextInt(900000, 3600001))),
                            revertToString((duration = 6 * Random.nextInt(450000, 3600001))), revertToString(startTime + duration)));
            }
        };
    }

    public static EventHandler<ActionEvent> clearBreaks(List<BreakTracker> breakTracker) {
        return event -> {
            breakTracker.clear();
            breakTracker.add(new BreakTracker("", "", ""));
        };
    }

    public static EventHandler<ActionEvent> saveBreaks(List<BreakTracker> breakTracker, TextField profTF, ComboBox<String> profsBox, AbstractBot bot) {
        return event -> {
            String prof = profTF.getText();
            if(prof != null) {
                profsBox.getItems().add(prof);
                prof = prof.replaceAll(" ", "~");
                JsonArray allBreaks = new JsonArray();
                JsonObject jBreaks = new JsonObject();
                JsonArray listOfStarts = new JsonArray();
                JsonArray listOfDurations = new JsonArray();
                JsonArray listOfEnds = new JsonArray();

                JsonParser parser = new JsonParser();
                JsonElement name = parser.parse(prof);
                System.out.println(name);




                for (int i = 0; i < breakTracker.size(); i++) {
                    listOfStarts.add(breakTracker.get(i).getStartTime());
                }
                for (int i = 0; i < breakTracker.size(); i++) {
                    listOfDurations.add(breakTracker.get(i).getDuration());
                }
                for (int i = 0; i < breakTracker.size(); i++) {
                    listOfEnds.add(breakTracker.get(i).getEndTime());
                }

                jBreaks.add("Start", listOfStarts);
                jBreaks.add("Duration", listOfDurations);
                jBreaks.add("End", listOfEnds);
                jBreaks.add("Name", name);


                String value = bot.getSettings().getProperty("AwesomeBreaks");

                System.out.println("Old Setting: " + value);

                if(value != null) {
                    JsonArray response = parser.parse(value).getAsJsonArray();
                    for (int index = 0; index < response.size(); index++) {
                        JsonObject jsonObject = (JsonObject) response.get(index);
                        allBreaks.add(jsonObject);
                    }
                }

                allBreaks.add(jBreaks);
                System.out.println("New: " + allBreaks);
                bot.getSettings().setProperty("AwesomeBreaks", allBreaks.toString());
                System.out.println("Saved");
            }
        };
    }

    public static EventHandler<ActionEvent> loadBreaks(List<BreakTracker> breakTracker, ComboBox<String> profName, AbstractBot bot) {
        return event -> {
            breakTracker.clear();
            String prof = profName.getSelectionModel().getSelectedItem();

            if (prof != null) {

                prof = prof.replaceAll(" ", "~");

                String value = bot.getSettings().getProperty("AwesomeBreaks");

                if (value != null) {

                    JsonParser parser = new JsonParser();

                    JsonArray array = parser.parse(value).getAsJsonArray();

                    JsonArray listOfStarts = new JsonArray();

                    JsonObject obj = new JsonObject();

                    JsonArray listOfDurations = new JsonArray();

                    JsonArray listOfEnds = new JsonArray();

                    for (int i = 0; i < array.size(); i++) {
                        if ((obj = array.get(i).getAsJsonObject()).get("Name").getAsString().equals(prof)) {
                            System.out.println("Found Breaks");
                            listOfStarts = obj.getAsJsonArray("Start");
                            listOfDurations = obj.getAsJsonArray("Duration");
                            listOfEnds = obj.getAsJsonArray("End");
                            break;
                        }
                    }

                    for (int i = 0; i < listOfStarts.size(); i++) {
                        breakTracker.add(new BreakTracker(listOfStarts.get(i).getAsString(), listOfDurations.get(i).getAsString(), listOfEnds.get(i).getAsString()));
                    }

                }
            }
        };
    }

    public static List<String> loadProfs(AbstractBot bot) {
        List<String> profs = new ArrayList<>();
        String settings = bot.getSettings().getProperty("AwesomeBreaks");
        System.out.println(settings);
        if(settings != null) {
            JsonParser parser = new JsonParser();
            JsonArray obj = parser.parse(settings).getAsJsonArray();
            for(int index = 0; index < obj.size(); index++) {
                String names = obj.get(index).getAsJsonObject().get("Name").getAsString().replaceAll("~", " ");
                profs.add(names);
            }
            System.out.println(profs);
            return profs;
        }
        System.out.println("Properties is empty");
        return Collections.emptyList();
    }

    public static EventHandler<ActionEvent> deleteProf(ComboBox<String> cb_profile, AbstractBot bot) {
        return event -> {
            String name = cb_profile.getSelectionModel().getSelectedItem();

            if(name != null) {

                cb_profile.getItems().remove(name);

                name = name.replaceAll(" ", "~");

                String settings = bot.getSettings().getProperty("AwesomeBreaks");

                if (settings != null) {

                    JsonParser parser = new JsonParser();

                    JsonArray array = parser.parse(settings).getAsJsonArray();

                    for (int i = 0; i < array.size(); i++) {
                        if (array.get(i).getAsJsonObject().get("Name").getAsString().equals(name)) {
                            System.out.println("Removed");
                            array.remove(i);
                            break;
                        }
                    }

                    bot.getSettings().setProperty("AwesomeBreaks", array.toString());
                }
            }
        };
    }
}

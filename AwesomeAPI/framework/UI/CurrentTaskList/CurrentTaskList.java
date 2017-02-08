package com.AwesomeAPI.framework.UI.CurrentTaskList;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CurrentTaskList {
    private ObservableList<String> currentTaskList = FXCollections.observableArrayList();
    private String prevTask;
    private int count;

    public void addTask(String task){
        int i;
        if(prevTask != null && prevTask.equals(task)){
            for(i = 0; i < currentTaskList.size(); i++){
                String temp = currentTaskList.get(i).replace(" (" + count + ")", "");
                System.out.println("Temp: " + temp);
                if(temp.equals(task)){
                    break;
                }
            }
            count++;
            int finalI = i;
            if(finalI != currentTaskList.size())
                Platform.runLater(() -> currentTaskList.set(finalI, task + " (" + count + ")"));

        }
        else{
            count = 0;
            Platform.runLater(() -> currentTaskList.add(0, task));

        }

        prevTask = task;
    }

    public int getCount(){
        return count;
    }

    public String getPrevTask(){
        return prevTask;
    }

    public ObservableList<String> getList(){
        return currentTaskList;
    }


}

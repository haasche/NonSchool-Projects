package com.AwesomeAPI.framework.UI.ItemTrackerPane;

import com.runemate.game.api.hybrid.entities.definitions.ItemDefinition;
import com.runemate.game.api.hybrid.net.GrandExchange;
import com.runemate.game.api.hybrid.util.StopWatch;
import com.runemate.game.api.script.framework.core.LoopingThread;
import com.runemate.game.api.script.framework.listeners.events.ItemEvent;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class ItemTrackerPane {

    private ObservableList<itemTracker> items = FXCollections.observableArrayList();
    private Label totalProfit = new Label();
    private List<item> itemList = new ArrayList<>();

    public class item{
        private String name;
        private int price;

        public item(String name, int price){
            this.name = name;
            this.price = price;
        }

        public String getName(){
            return name;
        }

        public int getPrice(){return price;}
    }

    public void createTableView(TitledPane pane){

        TableColumn<itemTracker, String> TC_ItemName, TC_ItemAmount, TC_ItemHour, TC_Profit, TC_ProfitHour;

        TC_ItemName = new TableColumn<>("Item");
        TC_ItemAmount = new TableColumn<>("Amount");
        TC_ItemHour = new TableColumn<>("Amount P/H");
        TC_Profit = new TableColumn<>("Profit");
        TC_ProfitHour = new TableColumn<>("Profit P/H");

        TC_ItemName.setCellValueFactory(param -> param.getValue().itemNameProperty());
        TC_ItemAmount.setCellValueFactory(param -> param.getValue().itemAmountProperty());
        TC_ItemHour.setCellValueFactory(param -> param.getValue().itemHourProperty());
        TC_Profit.setCellValueFactory(param -> param.getValue().profitProperty());
        TC_ProfitHour.setCellValueFactory(param -> param.getValue().profitHourProperty());

        TC_ItemAmount.styleProperty().set("-fx-alignment: CENTER");
        TC_ItemName.styleProperty().set("-fx-alignment: CENTER");
        TC_ItemHour.styleProperty().set("-fx-alignment: CENTER");
        TC_Profit.styleProperty().set("-fx-alignment: CENTER");
        TC_ProfitHour.styleProperty().set("-fx-alignment: CENTER");

        TableView<itemTracker> tableView = new TableView<>();
        tableView.styleProperty().set("-fx-alignment: CENTER");
        tableView.setItems(items);
        tableView.getColumns().addAll(TC_ItemName, TC_ItemAmount, TC_ItemHour, TC_Profit, TC_ProfitHour);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(200);

        Separator separator = new Separator(Orientation.HORIZONTAL);

        VBox vBox = new VBox(tableView, separator, totalProfit);
        vBox.setAlignment(Pos.CENTER_RIGHT);

        pane.setContent(vBox);

        createItemProfitUpdater();
    }

    public void refreshItems(ItemEvent event) {

        //If it's adding an item
        if(event.getType().equals(ItemEvent.Type.ADDITION)) {
            ItemDefinition def;
            if ((def = event.getItem().getDefinition()) != null) {
                String name;
                if ((name = def.getName()) != null) {
                    for (int i = 0; i < items.size(); i++) {
                        itemTracker item;
                        if ((item = items.get(i)).getItemName().equals(name)) {
                            item.setItemAmount(Integer.parseInt(item.getItemAmount()) + event.getQuantityChange() + "");
                            int price = 0;
                            for (int j = 0; j < itemList.size(); j++) {
                                item myItem;
                                if ((myItem = itemList.get(j)).getName().equals(name)) {
                                    price = myItem.getPrice();
                                }
                            }
                            item.setProfit(Integer.parseInt(item.getProfit()) + price * event.getQuantityChange() + "");
                            break;
                        }
                        if (i == items.size() - 1) {
                            GrandExchange.Item exch;
                            int price = 0;
                            if (def.isTradeable() && (exch = GrandExchange.lookup(def.getId())) != null) {
                                price = exch.getPrice();
                                itemList.add(new item(name, price));
                            }
                            items.add(new itemTracker(name, event.getQuantityChange() + "", 0 + "", price + "", 0 + ""));
                        }
                    }
                    if (items.size() == 0) {
                        GrandExchange.Item exch;
                        int price = 0;
                        if (def.isTradeable() && (exch = GrandExchange.lookup(def.getId())) != null) {
                            price = exch.getPrice();
                            itemList.add(new item(name, price));
                        }
                        items.add(new itemTracker(name, event.getQuantityChange() + "", 0 + "", price + "", 0 + ""));
                    }
                }
            }
        }

        //If its removing an item
        else if(event.getType().equals(ItemEvent.Type.REMOVAL)){
            ItemDefinition def;
            if ((def = event.getItem().getDefinition()) != null) {
                String name;
                if ((name = def.getName()) != null) {
                    for (int i = 0; i < items.size(); i++) {
                        itemTracker item;
                        if ((item = items.get(i)).getItemName().equals(name)) {
                            item.setItemAmount(Integer.parseInt(item.getItemAmount()) + event.getQuantityChange() * -1 + "");
                            int price = 0;
                            for (int j = 0; j < itemList.size(); j++) {
                                item myItem;
                                if ((myItem = itemList.get(j)).getName().equals(name)) {
                                    price = myItem.getPrice();
                                }
                            }
                            item.setProfit(Integer.parseInt(item.getProfit()) + price * event.getQuantityChange() * -1 + "");

                            break;
                        }
                        if (i == items.size() - 1) {
                            GrandExchange.Item exch;
                            int price = 0;
                            if (def.isTradeable() && (exch = GrandExchange.lookup(def.getId())) != null) {
                                price = exch.getPrice();
                                itemList.add(new item(name, price));
                            }
                            items.add(new itemTracker(name, event.getQuantityChange() * -1 + "", 0 + "", price * -1 + "", 0 + ""));
                        }
                    }
                    if (items.size() == 0) {
                        GrandExchange.Item exch;
                        int price = 0;
                        if (def.isTradeable() && (exch = GrandExchange.lookup(def.getId())) != null) {
                            price = exch.getPrice();
                            itemList.add(new item(name, price));
                        }
                        items.add(new itemTracker(name, event.getQuantityChange() * -1 + "", 0 + "", price * -1 + "", 0 + ""));
                    }
                }
            }
        }
    }

    public int getNumOfItem(String item){
        itemTracker temp;
        for (itemTracker item1 : items) {
            if ((temp = item1).getItemName().equals(item)) {
                return Integer.parseInt(temp.getItemAmount());
            }
        }
        return 0;
    }

    public void createItemProfitUpdater(){
        StopWatch watch = new StopWatch();
        watch.start();
        LoopingThread thread = new LoopingThread (() -> {
            int profitT = 0;
            int profitPHT = 0;
            for (int i = 0; i < items.size(); i++) {
                itemTracker item;
                if ((item = items.get(i)) != null) {
                    int amount;
                    if ((amount = Integer.parseInt(item.getItemAmount())) != 0) {
                        item.setItemHour(Math.round(amount / ((double)watch.getRuntime() / 3600000)) + "");
                    }
                    else{
                        item.setItemHour(0 + "");
                    }
                    int profit;
                    long profitPH;
                    if ((profit = Integer.parseInt(item.getProfit())) != 0) {
                        profitPH = Math.round(profit / ((double)watch.getRuntime() / 3600000));
                        item.setProfitHour(profitPH + "");
                        profitT += profit;
                        profitPHT += profitPH;
                    }
                    else{
                        item.setProfitHour(0 + "");
                    }
                }
            }
            int finalProfitPHT = profitPHT;
            int finalProfitT = profitT;
            Platform.runLater(() -> totalProfit.setText("Total Profit: " + finalProfitT + " | Total Profit P/H: " + finalProfitPHT));
        }, 500);
        thread.start();
    }

    public static class itemTracker {

        private final StringProperty itemName;
        private final StringProperty itemAmount;
        private final StringProperty itemHour;
        private final StringProperty profit;
        private final StringProperty profitHour;

        public itemTracker(String itemName, String itemAmount, String itemHour, String profit, String profitHour) {
            this.itemName = new SimpleStringProperty(itemName);
            this.itemAmount = new SimpleStringProperty(itemAmount);
            this.itemHour = new SimpleStringProperty(itemHour);
            this.profit = new SimpleStringProperty(profit);
            this.profitHour = new SimpleStringProperty(profitHour);
        }

        public String getItemName() {
            return itemName.get();
        }

        public String getItemAmount() {
            return itemAmount.get();
        }

        public String getItemHour() {
            return itemHour.get();
        }

        public String getProfit() {
            return profit.get();
        }

        public String getProfitHour(){
            return profitHour.get();
        }

        public void setItemName(String name) {
            itemName.set(name);
        }

        public void setItemAmount(String amount) {
            itemAmount.set(amount);
        }

        public void setItemHour(String hour) {
            itemHour.set(hour);
        }

        public void setProfit(String itemProfit){
            profit.set(itemProfit);
        }

        public void setProfitHour(String profit){
            profitHour.set(profit);
        }

        public StringProperty itemNameProperty() {
            return itemName;
        }

        public StringProperty itemAmountProperty() {
            return itemAmount;
        }

        public StringProperty itemHourProperty() {
            return itemHour;
        }

        public StringProperty profitProperty() {
            return profit;
        }

        public StringProperty profitHourProperty() {
            return profitHour;
        }

    }
}

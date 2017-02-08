package com.AgTRS.bots.AgilityTRS;

import com.AwesomeAPI.framework.UI.BreakHandler.BreakHandler;
import com.AwesomeAPI.framework.UI.ItemTrackerPane.ItemTrackerPane;
import com.AwesomeAPI.framework.UI.SkillTrackerPane.SkillTrackerPane;
import com.runemate.game.api.hybrid.Environment;
import com.runemate.game.api.hybrid.GameEvents;
import com.runemate.game.api.hybrid.RuneScape;
import com.runemate.game.api.hybrid.entities.GameObject;
import com.runemate.game.api.hybrid.location.Area;
import com.runemate.game.api.hybrid.location.Coordinate;
import com.runemate.game.api.hybrid.net.GrandExchange;
import com.runemate.game.api.hybrid.util.Resources;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.core.LoopingThread;
import com.runemate.game.api.script.framework.listeners.InventoryListener;
import com.runemate.game.api.script.framework.listeners.SkillListener;
import com.runemate.game.api.script.framework.listeners.events.ItemEvent;
import com.runemate.game.api.script.framework.listeners.events.SkillEvent;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AgilityInfoUI extends VBox implements Initializable, SkillListener, InventoryListener{
    public static String location;
    private AgilityTRS bot;

    private List<String> obstacles = new ArrayList<>();
    private List<Area.Rectangular> areas = new ArrayList<>();
    private List<Coordinate> spots = new ArrayList<>();
    private GameObject obstacle;
    private List<String> interactions = new ArrayList<>();
    private List<SkillTrackerPane.ProgressIndicatorBar> skillBars = new ArrayList<>();
    private SkillTrackerPane skillTrackerClass;
    private BreakHandler breakHandlerClass = new BreakHandler();

    @FXML
    private Label LL_Version;

    @FXML
    private TitledPane TP_SkillTracker;

    @FXML
    private Label LL_BotName;

    @FXML
    private ComboBox<String> CB_Course;

    @FXML
    private Label LL_Runtime;

    @FXML
    private TextField TF_StopTime;

    //New Additions 7:16PM 11.18.2016

    @FXML private TitledPane TP_ItemTracker;

    @FXML private TitledPane TP_BreakHandler;

    @FXML private ListView<String> LV_CurrentTask;

    @FXML private TitledPane TP_Currently;

    @FXML private CheckBox CB_Alch;

    //End

    private ItemTrackerPane itemTrackerPane = new ItemTrackerPane();

    private List<String> startTimes = new ArrayList<>();
    private List<String> durations = new ArrayList<>();
    private int level;
    private String time;
    private double progress;
    private TitledPane pane;
    private LoopingThread uiUpdate = new LoopingThread(this::updateUI, 500);
    private int inc;
    private List<Label> skillLabels = new ArrayList<>();
    private List<Label> expHourLabels = new ArrayList<>();
    private List<Label> timeToLevels = new ArrayList<>();
    private int row;
    private String time1;
    private StringPropertyBase timeString = new SimpleStringProperty();
    private String userTime;

    private ObservableList<BreakHandler.BreakTracker> breakTracker = FXCollections.observableArrayList();
    private int col;
    private int itemCount;
    private GrandExchange.Item lookup;
    private int price;
    private int profit;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LL_BotName.setText(bot.getMetaData().getName());
        LL_Version.setText("V " + bot.getMetaData().getVersion());
        uiUpdate.start();
        try {
            if(bot.getPlatform().invokeAndWait(() -> Environment.isOSRS())) {
                CB_Course.getItems().addAll("Gnome Stronghold", "Draynor Village", "Al Kharid",
                        "Varrock", "Canifis", "Falador", "Seers' Village", "Pollnivneach", "Rellekka", "Ardougne");
            }
            else{
                CB_Course.getItems().addAll("Burthorpe", "Gnome Stronghold");
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        CB_Course.setOnAction(onCourseChanged());
        bot.watch.start();
        LL_Runtime.setText("Runtime: ");

        skillTrackerClass.createSkillTracker(TP_SkillTracker);

        breakHandlerClass.createBreakHandler(TP_BreakHandler, bot);

        itemTrackerPane.createTableView(TP_ItemTracker);

        EventHandler<ActionEvent> setAlch = event -> bot.alch = CB_Alch.isSelected();
        CB_Alch.setOnAction(setAlch);
    }

    @Override
    public void onItemAdded(ItemEvent event){
      itemTrackerPane.refreshItems(event);
    }

    @Override
    public void onItemRemoved(ItemEvent event){
        itemTrackerPane.refreshItems(event);
    }

    private EventHandler<ActionEvent> onCourseChanged() {
        return event -> {
            System.out.println("Course Changed");
            bot.obstacles.clear();
            bot.areas.clear();
            bot.spots.clear();
            bot.interactions.clear();
            bot.location = CB_Course.getSelectionModel().getSelectedItem();
            setAllSettings();
        };
    }

    private void setAllSettings() {
        System.out.println("Saved settings");
        switch (bot.location) {
            case "Gnome Stronghold":
                bot.areas.add(new Area.Rectangular(new Coordinate(2469, 3440,  0), new Coordinate(2489, 3433, 0)));
                bot.spots.add(new Coordinate(2474, 3436, 0));
                bot.obstacles.add("Log balance");
                bot.areas.add(new Area.Rectangular(new Coordinate(2469, 3434, 0), new Coordinate(2479, 3424, 0)));
                bot.spots.add(new Coordinate(2474, 3426, 0));
                bot.obstacles.add("Obstacle net");
                bot.areas.add(new Area.Rectangular(new Coordinate(2476, 3424, 1), new Coordinate(2471, 3421, 1)));
                bot.spots.add(new Coordinate(2473, 3423, 1));
                bot.obstacles.add("Tree branch");
                bot.areas.add(new Area.Rectangular(new Coordinate(2477, 3421, 2), new Coordinate(2472, 3418, 2)));
                bot.spots.add(new Coordinate(2477, 3420, 2));
                bot.obstacles.add("Balancing rope");
                bot.areas.add(new Area.Rectangular(new Coordinate(2488, 3421, 2), new Coordinate(2483, 3418, 2)));
                bot.spots.add(new Coordinate(2485, 3420, 2));
                bot.obstacles.add("Tree branch");
                bot.areas.add(new Area.Rectangular(new Coordinate(2490, 3426, 0), new Coordinate(2481, 3417, 0)));
                bot.spots.add(new Coordinate(2486, 3425, 0));
                bot.obstacles.add("Obstacle net");
                bot.areas.add(new Area.Rectangular(new Coordinate(2490, 3432, 0), new Coordinate(2481, 3427, 0)));
                bot.spots.add(new Coordinate(2484, 3430, 0));
                bot.obstacles.add("Obstacle pipe");


                break;

            case "Draynor Village":
                bot.areas.add(new Area.Rectangular(new Coordinate(3103, 3277, 0), new Coordinate(3105, 3280, 0)));
                bot.spots.add(new Coordinate(3103, 3279, 0));
                bot.obstacles.add("Rough wall");
                bot.areas.add(new Area.Rectangular(new Coordinate(3097, 3277, 3), new Coordinate(3102, 3281, 3)));
                bot.spots.add(new Coordinate(3099, 3277, 3));
                bot.obstacles.add("Tightrope");
                bot.areas.add(new Area.Rectangular(new Coordinate(3088, 3273, 3), new Coordinate(3091, 3276, 3)));
                bot.spots.add(new Coordinate(3091, 3276, 3));
                bot.obstacles.add("Tightrope");
                bot.areas.add(new Area.Rectangular(new Coordinate(3089, 3265, 3), new Coordinate(3094, 3267, 3)));
                bot.spots.add(new Coordinate(3089, 3265, 3));
                bot.obstacles.add("Narrow wall");
                bot.areas.add(new Area.Rectangular(new Coordinate(3088, 3257, 3), new Coordinate(3088, 3261, 3)));
                bot.spots.add(new Coordinate(3088, 3257, 3));
                bot.obstacles.add("Wall");
                bot.areas.add(new Area.Rectangular(new Coordinate(3088, 3255, 3), new Coordinate(3094, 3255, 3)));
                bot.spots.add(new Coordinate(3094, 3255, 3));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(3096, 3261, 3), new Coordinate(3101, 3256, 3)));
                bot.spots.add(new Coordinate(3101, 3261, 3));
                bot.obstacles.add("Crate");

                break;
            case "Al Kharid":
                bot.areas.add(new Area.Rectangular(new Coordinate(3272, 3195, 0), new Coordinate(3274, 3196, 0)));
                bot.spots.add(new Coordinate(3278, 3194, 0));
                bot.obstacles.add("Rough wall");
                bot.areas.add(new Area.Rectangular(new Coordinate(3278, 3192, 3), new Coordinate(3272, 3180, 3)));
                bot.spots.add(new Coordinate(3272, 3182, 3));
                bot.obstacles.add("Tightrope");
                bot.areas.add(new Area.Rectangular(new Coordinate(3272, 3173, 3), new Coordinate(3265, 3161, 3)));
                bot.spots.add(new Coordinate(3091, 3276, 3));
                bot.obstacles.add("Cable");
                bot.areas.add(new Area.Rectangular(new Coordinate(3302, 3176, 3), new Coordinate(3283, 3160, 3)));
                bot.spots.add(new Coordinate(3301, 3163, 3));
                bot.obstacles.add("Zip line");
                bot.areas.add(new Area.Rectangular(new Coordinate(3318, 3165, 1), new Coordinate(3313, 3160, 1)));
                bot.spots.add(new Coordinate(3318, 3165, 1));
                bot.obstacles.add("Tropical tree");
                bot.areas.add(new Area.Rectangular(new Coordinate(3318, 3179, 2), new Coordinate(3312, 3174, 2)));
                bot.spots.add(new Coordinate(3315, 3179, 2));
                bot.obstacles.add("Roof top beams");
                bot.areas.add(new Area.Rectangular(new Coordinate(3318, 3186, 3), new Coordinate(3312, 3180, 3)));
                bot.spots.add(new Coordinate(3314, 3186, 3));
                bot.obstacles.add("Tightrope");
                bot.areas.add(new Area.Rectangular(new Coordinate(3305, 3186, 3), new Coordinate(3298, 3193, 3)));
                bot.spots.add(new Coordinate(3301, 3193, 3));
                bot.obstacles.add("Gap");

                break;
            case "Varrock":
                bot.areas.add(new Area.Rectangular(new Coordinate(3221, 3416, 0), new Coordinate(3213, 3412, 0)));
                bot.spots.add(new Coordinate(3222, 3414, 0));
                bot.obstacles.add("Rough wall");
                bot.areas.add(new Area.Rectangular(new Coordinate(3219, 3410, 3), new Coordinate(3214, 3419, 3)));
                bot.spots.add(new Coordinate(3214, 3414, 3));
                bot.obstacles.add("Clothes line");
                bot.areas.add(new Area.Rectangular(new Coordinate(3208, 3413, 3), new Coordinate(3201, 3419, 3)));
                bot.spots.add(new Coordinate(3201, 3416, 3));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(3197, 3416, 1), new Coordinate(3194, 3416, 1)));
                bot.spots.add(new Coordinate(3194, 3416, 3));
                bot.obstacles.add("Wall");
                bot.areas.add(new Area.Rectangular(new Coordinate(3198, 3406, 3), new Coordinate(3192, 3402, 3)));
                bot.spots.add(new Coordinate(3192, 3402, 3));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(3183, 3401, 3), new Coordinate(3208, 3382, 3)));
                bot.spots.add(new Coordinate(3208, 3398, 3));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(3218, 3403, 3), new Coordinate(3232, 3393, 3)));
                bot.spots.add(new Coordinate(3232, 3402, 3));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(3236, 3408, 3), new Coordinate(3240, 3403, 3)));
                bot.spots.add(new Coordinate(3236, 3408, 3));
                bot.obstacles.add("Ledge");
                bot.areas.add(new Area.Rectangular(new Coordinate(3236, 3410, 3), new Coordinate(3240, 3415, 3)));
                bot.spots.add(new Coordinate(3238, 3415, 3));
                bot.obstacles.add("Edge");
                break;

            case "Canifis":
                bot.areas.add(new Area.Rectangular(new Coordinate(3507, 3488, 0), new Coordinate(3505, 3487 ,0)));
                bot.spots.add(new Coordinate(3507, 3488, 0));
                bot.obstacles.add("Tall tree");
                bot.areas.add(new Area.Rectangular(new Coordinate(3505, 3489, 2), new Coordinate(3509, 3498, 2)));
                bot.spots.add(new Coordinate(3506, 3496, 2));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(3497, 3504, 2), new Coordinate(3503, 3506, 2)));
                bot.spots.add(new Coordinate(3498, 3504, 2));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(3492, 3499, 2), new Coordinate(3487, 3504, 2)));
                bot.spots.add(new Coordinate(3487, 3499, 2));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(3479, 3492, 3), new Coordinate(3475, 3499, 3)));
                bot.spots.add(new Coordinate(3479, 3493, 3));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(3484, 3487, 2), new Coordinate(3477, 3481, 2)));
                bot.spots.add(new Coordinate(3480, 3484, 2));
                bot.obstacles.add("Pole-vault");
                bot.areas.add(new Area.Rectangular(new Coordinate(3487, 3478, 3), new Coordinate(3503, 3469, 3)));
                bot.spots.add(new Coordinate(3503, 3475, 3));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(3515, 3482, 2), new Coordinate(3509, 3475, 2)));
                bot.spots.add(new Coordinate(3510, 3482, 2));
                bot.obstacles.add("Gap");

                break;
            case "Falador":
                bot.areas.add(new Area.Rectangular(new Coordinate(3037, 3341 , 0), new Coordinate(3035, 3339, 0)));
                bot.spots.add(new Coordinate(3031, 3341, 0));
                bot.obstacles.add("Rough wall");
                bot.areas.add(new Area.Rectangular(new Coordinate(3036, 3342, 3), new Coordinate(3040, 3343, 3)));
                bot.spots.add(new Coordinate(3039, 3343, 3));
                bot.obstacles.add("Tightrope");
                bot.areas.add(new Area.Rectangular(new Coordinate(3045, 3341, 3), new Coordinate(3051, 3349, 3)));
                bot.spots.add(new Coordinate(3050, 3349, 3));
                bot.obstacles.add("Hand holds");
                bot.areas.add(new Area.Rectangular(new Coordinate(3050, 3357, 3), new Coordinate(3048, 3358, 3)));
                bot.spots.add(new Coordinate(3048, 3358, 3));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(3048, 3361, 3), new Coordinate(3045, 3367, 3)));
                bot.spots.add(new Coordinate(3045, 3361, 3));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(3041, 3361, 3), new Coordinate(3034, 3364, 3)));
                bot.spots.add(new Coordinate(3035, 3361, 3));
                bot.obstacles.add("Tightrope");
                bot.areas.add(new Area.Rectangular(new Coordinate(3029, 3352, 3), new Coordinate(3026, 3354, 3)));
                bot.spots.add(new Coordinate(3027, 3353, 3));
                bot.obstacles.add("Tightrope");
                bot.areas.add(new Area.Rectangular(new Coordinate(3020, 3353, 3), new Coordinate(3009, 3358, 3)));
                bot.spots.add(new Coordinate(3018, 3353, 3));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(3022, 3349, 3), new Coordinate(3016, 3343, 3)));
                bot.spots.add(new Coordinate(3016, 3347, 3));
                bot.obstacles.add("Ledge");
                bot.spots.add(new Coordinate(3012, 3344, 3));
                bot.areas.add(new Area.Rectangular(new Coordinate(3014, 3346, 3), new Coordinate(3011, 3344, 3)));
                bot.obstacles.add("Ledge");
                bot.areas.add(new Area.Rectangular(new Coordinate(3013, 3342, 3), new Coordinate(3009, 3335, 3)));
                bot.spots.add(new Coordinate(3012, 3336, 3));
                bot.obstacles.add("Ledge");
                bot.areas.add(new Area.Rectangular(new Coordinate(3012, 3334, 3), new Coordinate(3017, 3331, 3)));
                bot.spots.add(new Coordinate(3017, 3334, 3));
                bot.obstacles.add("Ledge");
                bot.areas.add(new Area.Rectangular(new Coordinate(3019, 3335, 3), new Coordinate(3024, 3332, 3)));
                bot.spots.add(new Coordinate(3023, 3333, 3));
                bot.obstacles.add("Edge");

                break;
            case "Seers' Village":
                bot.areas.add(new Area.Rectangular(new Coordinate(2730, 3489, 0), new Coordinate(2728, 3487, 0)));
                bot.spots.add(new Coordinate(2729, 3487, 0));
                bot.obstacles.add("Wall");
                bot.areas.add(new Area.Rectangular(new Coordinate(2729, 3491, 3), new Coordinate(2721, 3497, 3)));
                bot.spots.add(new Coordinate(2721, 3494, 3));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(2713, 3488, 2), new Coordinate(2705, 3494, 2)));
                bot.spots.add(new Coordinate(2710, 3490, 2));
                bot.obstacles.add("Tightrope");
                bot.areas.add(new Area.Rectangular(new Coordinate(2715, 3481, 2), new Coordinate(2710, 3477, 2)));
                bot.spots.add(new Coordinate(2710, 3977, 2));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(2715, 3475, 3), new Coordinate(2700, 3470, 3)));
                bot.spots.add(new Coordinate(2703, 3470, 3));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(2702, 3465, 2), new Coordinate(2698, 3460, 2)));
                bot.spots.add(new Coordinate(2702, 3464, 2));
                bot.obstacles.add("Edge");

                break;
            case "Pollnivneach":
                bot.spots.add(new Coordinate(3352, 2962, 0));
                bot.spots.add(new Coordinate(3350, 2968, 1));
                bot.spots.add(new Coordinate(3354, 2975, 1));
                bot.spots.add(new Coordinate(3362, 2977, 1));
                bot.spots.add(new Coordinate(3369, 2976, 1));
                bot.spots.add(new Coordinate(3365, 2982, 1));
                bot.spots.add(new Coordinate(3358, 2984, 2));
                bot.spots.add(new Coordinate(3360, 2995, 2));
                bot.spots.add(new Coordinate(3362, 3002, 2));

                bot.areas.add(new Area.Rectangular(new Coordinate(3352, 2962, 0), new Coordinate(3350, 2961, 0)));
                bot.areas.add(new Area.Rectangular(new Coordinate(3346, 2964, 1), new Coordinate(3351, 2968, 1)));
                bot.areas.add(new Area.Rectangular(new Coordinate(3352, 2973, 1), new Coordinate(3355, 2976, 1)));
                bot.areas.add(new Area.Rectangular(new Coordinate(3360, 2977, 1), new Coordinate(3362, 2979, 1)));
                bot.areas.add(new Area.Rectangular(new Coordinate(3366, 2975, 1), new Coordinate(3369, 2976, 1)));
                bot.areas.add(new Area.Rectangular(new Coordinate(3365, 2982, 1), new Coordinate(3369, 2988, 1)));
                bot.areas.add(new Area.Rectangular(new Coordinate(3355, 2980, 2), new Coordinate(3365, 2985, 2)));
                bot.areas.add(new Area.Rectangular(new Coordinate(3357, 2991, 2), new Coordinate(3366, 2995, 2)));
                bot.areas.add(new Area.Rectangular(new Coordinate(3356, 3000, 2), new Coordinate(3362, 3003, 2)));

                bot.obstacles.add("Basket");
                bot.obstacles.add("Market stall");
                bot.obstacles.add("Banner");
                bot.obstacles.add("Gap");
                bot.obstacles.add("Tree");
                bot.obstacles.add("Rough wall");
                bot.obstacles.add("Monkeybars");
                bot.obstacles.add("Tree");
                bot.obstacles.add("Drying line");


                break;
            case "Rellekka":
                bot.areas.add(new Area.Rectangular(new Coordinate(2624, 3677, 0), new Coordinate(2626, 3679, 0)));
                bot.spots.add(new Coordinate(2625, 3677, 0));
                bot.obstacles.add("Rough wall");
                bot.areas.add(new Area.Rectangular(new Coordinate(2626, 3676, 3), new Coordinate(2622, 3672, 3)));
                bot.spots.add(new Coordinate(2622, 3672, 3));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(2622, 3668, 3), new Coordinate(2615, 3658, 3)));
                bot.spots.add(new Coordinate(2622, 3658, 3));
                bot.obstacles.add("Tightrope");
                bot.areas.add(new Area.Rectangular(new Coordinate(2626, 3655, 3), new Coordinate(2630, 3651, 3)));
                bot.spots.add(new Coordinate(2630, 3655, 3));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(2639, 3653, 3), new Coordinate(2644, 3649, 3)));
                bot.spots.add(new Coordinate(2644, 3653, 3));
                bot.obstacles.add("Gap");
                bot.areas.add(new Area.Rectangular(new Coordinate(2643, 3662, 3), new Coordinate(2650, 3657, 3)));
                bot.spots.add(new Coordinate(2647, 3662, 3));
                bot.obstacles.add("Tightrope");
                bot.areas.add(new Area.Rectangular(new Coordinate(2666, 3685, 3), new Coordinate(2655, 3665, 3)));
                bot.spots.add(new Coordinate(2655, 3674, 3));
                bot.obstacles.add("Pile of fish");
                break;

            case "Ardougne":
                bot.obstacles.add("Wooden Beams");
                bot.spots.add(new Coordinate(2673, 3298, 0));
                bot.areas.add(new Area.Rectangular(new Coordinate(2672, 3297, 0), new Coordinate(2674, 3298, 0)));
                bot.obstacles.add("Gap");
                bot.spots.add(new Coordinate(2671, 3309, 3));
                bot.areas.add(new Area.Rectangular(new Coordinate(2671, 3299, 3), new Coordinate(2671, 3309, 3)));
                bot.obstacles.add("Plank");
                bot.spots.add(new Coordinate(2662, 3318, 3));
                bot.areas.add(new Area.Rectangular(new Coordinate(2665, 3318, 3), new Coordinate(2662, 3318, 3)));
                bot.obstacles.add("Gap");
                bot.spots.add(new Coordinate(2654, 3318, 3));
                bot.areas.add(new Area.Rectangular(new Coordinate(2656, 3318, 3), new Coordinate(2654, 3318, 3)));
                bot.obstacles.add("Gap");
                bot.spots.add(new Coordinate(2653, 3310, 3));
                bot.areas.add(new Area.Rectangular(new Coordinate(2653, 3314, 3), new Coordinate(2653, 3310, 3)));
                bot.obstacles.add("Steep roof");
                bot.spots.add(new Coordinate(2653, 3300, 3));
                bot.areas.add(new Area.Rectangular(new Coordinate(2651, 3309, 3), new Coordinate(2653, 3300, 3)));
                bot.obstacles.add("Gap");
                bot.spots.add(new Coordinate(2656, 3297, 3));
                bot.areas.add(new Area.Rectangular(new Coordinate(2656, 3297, 3), new Coordinate(2657, 3298, 3)));
                break;

            case "Gnome Stronghold(Lower)":
                bot.areas.add(new Area.Rectangular(new Coordinate(2473, 3436,  0), new Coordinate(2475, 3438, 0)));
                bot.spots.add(new Coordinate(2474, 3436, 0));
                bot.obstacles.add("Log balance");
                bot.areas.add(new Area.Rectangular(new Coordinate(2477, 3429, 0), new Coordinate(2469, 3419, 0)));
                bot.spots.add(new Coordinate(2474, 3426, 0));
                bot.obstacles.add("Obstacle net");
                bot.areas.add(new Area.Rectangular(new Coordinate(2476, 3424, 1), new Coordinate(2471, 3422, 1)));
                bot.spots.add(new Coordinate(2473, 3423, 1));
                bot.obstacles.add("Tree branch");
                bot.areas.add(new Area.Rectangular(new Coordinate(2477, 3421, 2), new Coordinate(2472, 3418, 2)));
                bot.spots.add(new Coordinate(2477, 3420, 2));
                bot.obstacles.add("Balancing rope");
                bot.areas.add(new Area.Rectangular(new Coordinate(2488, 3421, 2), new Coordinate(2483, 3418, 2)));
                bot.spots.add(new Coordinate(2485, 3420, 2));
                bot.obstacles.add("Tree branch");
                bot.areas.add(new Area.Rectangular(new Coordinate(2490, 3426, 0), new Coordinate(2481, 3417, 0)));
                bot.spots.add(new Coordinate(2486, 3425, 0));
                bot.obstacles.add("Obstacle net");
                bot.areas.add(new Area.Rectangular(new Coordinate(2490, 3432, 0), new Coordinate(2481, 3427, 0)));
                bot.spots.add(new Coordinate(2484, 3430, 0));
                bot.obstacles.add("Obstacle pipe");
                break;

            case ("Burthorpe"):
                bot.areas.add(new Area.Rectangular(new Coordinate(2921, 3551, 0), new Coordinate(2913, 3553, 0)));
                bot.spots.add(new Coordinate(2919, 3552, 0));
                bot.obstacles.add("Log beam");
                bot.areas.add(new Area.Rectangular(new Coordinate(2921, 3554, 0), new Coordinate(2918, 3560, 0)));
                bot.spots.add(new Coordinate(2919, 3561, 0));
                bot.obstacles.add("Wall");
                bot.areas.add(new Area.Polygonal(new Coordinate(2920, 3562, 1), new Coordinate(2918, 3562, 1), new Coordinate(2918, 3564, 1), new Coordinate(2916, 3564, 1), new Coordinate(2916, 3565, 1), new Coordinate(2920, 3565, 1)));
                bot.spots.add(new Coordinate(2916, 3564, 1));
                bot.obstacles.add("Balancing ledge");
                bot.areas.add(new Area.Polygonal(new Coordinate(2913, 3565, 1), new Coordinate(2913, 3563, 1), new Coordinate(2911, 3563, 1), new Coordinate(2911, 3560, 1), new Coordinate(2909, 3560, 1), new Coordinate(2909, 3565, 1)));
                bot.spots.add(new Coordinate(2910, 3562, 1));
                bot.obstacles.add("Obstacle low wall");
                bot.areas.add(new Area.Rectangular(new Coordinate(2912, 3563, 1), new Coordinate(2912, 3561, 1)));
                bot.spots.add(new Coordinate(2912, 3562, 1));
                bot.obstacles.add("Rope swing");
                bot.areas.add(new Area.Rectangular(new Coordinate(2917, 3563, 1), new Coordinate(2916, 3560, 1)));
                bot.spots.add(new Coordinate(2917, 3561, 1));
                bot.obstacles.add("Monkey bars");
                bot.areas.add(new Area.Rectangular(new Coordinate(2915, 3554, 1), new Coordinate(2917, 3553, 1)));
                bot.spots.add(new Coordinate(2917, 3553, 1));
                bot.obstacles.add("Ledge");
                break;

        }
    }

    public AgilityInfoUI(AgilityTRS bot) {
        this.bot = bot;
        skillTrackerClass = new SkillTrackerPane(bot);
        bot.getEventDispatcher().addListener(this);

        // Load the fxml file using RuneMate's Resources class.
        FXMLLoader loader = new FXMLLoader();

        // Input your InfoUI FXML file location here.
        // NOTE: DO NOT FORGET TO ADD IT TO MANIFEST AS A RESOURCE
        Future<InputStream> stream = bot.getPlatform().invokeLater(() -> Resources.getAsStream("com/AgTRS/bots/AgilityTRS/AgilityInfoUI.fxml"));

        // Set this class as root AND Controller for the Java FX GUI
        loader.setController(this);
        loader.setRoot(this);

        try {
            loader.load(stream.get());
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void updateUI() {
        LV_CurrentTask.setItems(bot.currentTaskList.getList());

        if(!bot.currentTaskList.getList().isEmpty()) {
            Platform.runLater(() -> TP_Currently.setText("Currently: " + bot.currentTaskList.getList().get(0)));
        }

        bot.isBreaking = breakHandlerClass.isBreaking(bot.watch);

        Platform.runLater(() -> LL_Runtime.setText("Runtime: " + bot.watch.getRuntimeAsString()));

        // bot.isBreaking = BreakHandler.isBreaking(TV_BreakHandler.getItems().toArray(new String[TC_Start.getItems().size()]), TC_Duration.getItems().toArray(new String[TC_Duration.getItems().size()]), bot.watch);
        if(!(userTime = TF_StopTime.getText()).equals("00:00:00") && BreakHandler.checkValid(userTime) && BreakHandler.convertToMilli(userTime) > 0){
            if(bot.watch.getRuntime() >= BreakHandler.convertToMilli(userTime)){
                GameEvents.Universal.LOGIN_HANDLER.disable();
                GameEvents.Universal.LOBBY_HANDLER.disable();
                while(Environment.getBot().isRunning() && RuneScape.isLoggedIn()){
                    if(RuneScape.logout()) {
                        Execution.delayUntil(() -> !RuneScape.isLoggedIn(), 10000);
                    }
                }
                Environment.getBot().stop();
            }
        }
    }

    @Override
    public void onExperienceGained(SkillEvent event) {
        skillTrackerClass.updateLevels(event);
    }
}

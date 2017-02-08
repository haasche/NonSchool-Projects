package com.AwesomeAPI.framework.UI.SkillTrackerPane;

import com.AwesomeAPI.framework.UI.BreakHandler.BreakHandler;
import com.runemate.game.api.hybrid.Environment;
import com.runemate.game.api.hybrid.GameEvents;
import com.runemate.game.api.hybrid.RuneScape;
import com.runemate.game.api.hybrid.local.Skill;
import com.runemate.game.api.hybrid.util.StopWatch;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.AbstractBot;
import com.runemate.game.api.script.framework.core.BotPlatform;
import com.runemate.game.api.script.framework.core.LoopingThread;
import com.runemate.game.api.script.framework.listeners.events.SkillEvent;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

public class SkillTrackerPane {

    private int level;
    private double progress;
    private TitledPane pane;
    private int expLeft;
    private VBox vBox = new VBox();
    public AbstractBot bot;
    private BotPlatform platform;
    private StopWatch watch = new StopWatch();
    private String skillName = "";
    private boolean updating = false;
    public LoopingThread loopingThread;
    private List<ProgressIndicatorPane> progressIndicatorPanes = new ArrayList<>();
    private ProgressIndicatorPane progressIndicatorPane;
    private ProgressIndicatorBar progressIndicatorBar;
    private Label LL_ExpAmount;
    private ObservableList<Node> children;
    private Node temp;
    private String text;
    private int tempGoal;
    private StopWatch runTime = new StopWatch();
    private Skill currentSkill;

    /**
     * The constructor for the pane that creates a SkillTrackerPane object for a bot
     * @param bot -> The bot being run
     */
    public SkillTrackerPane(AbstractBot bot){
        this.bot = bot;
        runTime.start();
    }

    /**
     * Pretty self explanatory, a label updater Thread that loops every 500ms.
     */
    public void createLabelUpdater() {
        loopingThread = new LoopingThread(() -> {

            checkGoals();

            updating = true;

            for (int i = 0; i < progressIndicatorPanes.size(); i++) {

                int finalI = i;

                Platform.runLater(() -> {

                    progressIndicatorPane = progressIndicatorPanes.get(finalI);

                    progressIndicatorPane.getLL_ExpHour().setText(
                            Math.round(Integer.parseInt(progressIndicatorPane.getLL_ExpAmount()
                                    .getText()) / ((double) runTime.getRuntime() / 3600000)) + "");

                    try {
                        if (bot != null && bot.isRunning() && (platform = bot.getPlatform()) != null && platform.invokeAndWait(
                                RuneScape::isLoggedIn)) {

                            expLeft = bot.getPlatform().invokeAndWait(() ->
                                    Skill.valueOf(progressIndicatorPane.getPB_Bar().getSkill().toUpperCase())
                                            .getExperienceToNextLevel());

                            if (expLeft > 0) {
                                progressIndicatorPane.getLL_TimeToNext().setText(
                                        BreakHandler.revertToString((int) (3600000 * (((double) (expLeft) /
                                                Integer.parseInt(progressIndicatorPane.getLL_ExpHour().getText()))))));
                            }
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
            updating = false;
        }, 500);
        loopingThread.start();
    }

    /**
     * Calls a method that either adds or updates a SkillTracker TitledPane.
     * @param event -> The skill which changed
     */
    public void updateLevels(SkillEvent event) {
        if (progressIndicatorPanes.isEmpty()) {
            addSkillBar(event);
        }

        for (int i = 0; i < progressIndicatorPanes.size(); i++) {

            if ((progressIndicatorBar = (progressIndicatorPane = progressIndicatorPanes.get(i)).getPB_Bar()).getSkill().equals(event.getSkill().toString())) {
                try {
                    level = bot.getPlatform().invokeAndWait(() -> event.getSkill().getBaseLevel());

                    progress = bot.getPlatform().invokeAndWait(() ->
                            (double) (100 - event.getSkill().getExperienceToNextLevelAsPercent()) / 100.0);

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

                int finalI = i;

                Platform.runLater(() -> progressIndicatorBar.syncProgress(
                        progress, level, (level - progressIndicatorBar.getLevel())));

                Platform.runLater(() -> (LL_ExpAmount = progressIndicatorPane.getLL_ExpAmount()).setText(
                        Integer.parseInt(LL_ExpAmount.getText()) + event.getChange() + ""));

                break;

            } else if (i == progressIndicatorPanes.size() - 1) {
                addSkillBar(event);
            }
        }
    }

    /**
     * Adds a SkillBar (ProgressBar and Label) to a list of ProgressIndicator bars
     * @param event -> The SkillEvent
     */
    public void addSkillBar(SkillEvent event){
        try {
            level = bot.getPlatform().invokeAndWait(() -> (currentSkill = event.getSkill()).getBaseLevel());
            progress = bot.getPlatform().invokeAndWait(() -> (double)(100 - currentSkill.getExperienceToNextLevelAsPercent())/100.0);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        ProgressIndicatorBar progressIndicatorBar = new ProgressIndicatorBar(progress, currentSkill.toString(), level, 0);

        Platform.runLater(() -> {

            Label LL_ExpHour = new Label("0");
            Label LL_TimeToNext = new Label("0");
            Label LL_ExpGained = new Label(event.getChange() + "");

            TextField TF_Goal = new TextField("Lvl #");
            TF_Goal.setPrefWidth(40);
            TF_Goal.setAlignment(Pos.CENTER);
            TF_Goal.textProperty().addListener((observable, oldValue, newValue) -> {
                watch.reset();
                System.out.println("textfield changed from " + oldValue + " to " + newValue);
                watch.start();
            });

            Button BN_Remove = new Button("X");
            BN_Remove.setFont(Font.font("comic sans", FontWeight.EXTRA_BOLD, 10));
            BN_Remove.styleProperty().set("-fx-text-fill: rgb(62, 206, 110)");
            BN_Remove.setOnAction(deleteSkillTracker());

            HBox hbox = new HBox(new Label("Exp Gained: "), LL_ExpGained,
                    new Separator(Orientation.VERTICAL),
                    new Label("Exp/Hour: "), LL_ExpHour, new Separator(Orientation.VERTICAL),
                    new Label("Time to Next: "), LL_TimeToNext, new Separator(Orientation.VERTICAL),
                    new Label("Goal: "),
                    TF_Goal, new Separator(Orientation.VERTICAL), BN_Remove);
            hbox.setAlignment(Pos.CENTER);
            hbox.setSpacing(10);

            pane = new TitledPane();
            pane.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
            pane.setGraphic(progressIndicatorBar);
            pane.setContent(hbox);

            progressIndicatorBar.prefWidthProperty().bind(pane.widthProperty().subtract(55));

            vBox.getChildren().addAll(pane, new Separator(Orientation.HORIZONTAL));

            progressIndicatorPanes.add(new ProgressIndicatorPane(progressIndicatorBar, LL_ExpGained,
                    LL_ExpHour, LL_TimeToNext, TF_Goal, BN_Remove));

        });
    }

    /**
     * Fills the SkillTracker TitledPane with a VBox that includes a Spinner and Add button
     * @param AN_SkillTracker -> The skilltracker TitledPane sorry the name should have been TP_SkillTracker
     */
    public void createSkillTracker(TitledPane AN_SkillTracker) {

        AN_SkillTracker.setContent(vBox);

        ObservableList<String> allSkills = FXCollections.observableArrayList("AGILITY", "ATTACK", "CONSTITUTION",
                "CONSTRUCTION", "COOKING", "CRAFTING", "DEFENSE", "DIVINATION", "DUNGEONEERING", "FARMING", "FIREMAKING",
                "FISHING", "FLETCHING", "HERBLORE", "HUNTER", "INVENTION", "MAGIC", "MINING", "PRAYER", "RANGED",
                "RUNECRAFTING", "SLAYER", "SMITHING", "STRENGTH", "SUMMONING", "THIEVING", "WOODCUTTING");

        Spinner<String> spinner = new Spinner<>(allSkills);

        Button createSkillTracker = new Button("Add");

        EventHandler<ActionEvent> checkSkillSelected = event -> {
            if((skillName = spinner.getValue()) != null){
                SkillEvent skillEvent = new SkillEvent(Skill.valueOf(skillName), SkillEvent.Type.EXPERIENCE_GAINED, 0, 0);
                addSkillBar(skillEvent);
            }
        };

        createSkillTracker.setOnAction(checkSkillSelected);

        HBox hbox = new HBox(spinner, new Separator(Orientation.VERTICAL), createSkillTracker);
        hbox.setAlignment(Pos.CENTER);

        vBox.getChildren().addAll(hbox, new Separator(Orientation.HORIZONTAL));

        createLabelUpdater();
    }

    /**
     * Deletes the SkillTracker which had it's delete button pressed
     * @return Does the removing.
     */
        private EventHandler<ActionEvent> deleteSkillTracker() {
        return event -> {
            for(int i = 0; i < progressIndicatorPanes.size(); i++){
                if(progressIndicatorPanes.get(i).getBN_Remove().equals(event.getSource())){

                    while(updating){ //To Keep from an error from deleting a pane while updating it.

                    }

                    temp = (children = vBox.getChildren()).get(i * 2 + 2);

                    children.remove(temp);
                    children.remove(children.get(i * 2 + 1));

                    progressIndicatorPanes.remove(i);
                }
            }
        };
    }

    /**
     * Checks the list of goals to see if any have been reached and logs out/stops bot if so.
     */
    public void checkGoals(){
        if(watch.getRuntime(TimeUnit.SECONDS) > 20) {

            for (int i = 0; i < progressIndicatorPanes.size(); i++) {

                if ((text = (progressIndicatorPane = progressIndicatorPanes.get(i)).getTF_LvlGoal().getText()).matches("^[1-9]\\d*$") && (tempGoal = Integer.parseInt(text)) > 0) {

                    try {
                        if(!loopingThread.isInterrupted()) {
                            if (bot.isRunning()) {
                                bot.getPlatform().invokeAndWait(() -> {
                                    if (Skill.valueOf(progressIndicatorPane.getPB_Bar().getSkill().toUpperCase()).getCurrentLevel() >= tempGoal) {
                                        GameEvents.Universal.LOGIN_HANDLER.disable();
                                        GameEvents.Universal.LOBBY_HANDLER.disable();
                                        while (Environment.getBot().isRunning() && RuneScape.isLoggedIn()) {
                                            if (RuneScape.logout()) {
                                                Execution.delayUntil(() -> !RuneScape.isLoggedIn(), 10000);
                                            }
                                        }
                                        loopingThread.interrupt();
                                        Environment.getBot().stop();
                                    }
                                });
                            }
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class ProgressIndicatorPane{

        private Label LL_ExpAmount;
        private Label LL_ExpHour;
        private Label LL_TimeToNext;
        private TextField TF_LvlGoal;
        private Button BN_Remove;
        private ProgressIndicatorBar PB_Bar;

        public ProgressIndicatorPane(ProgressIndicatorBar PB_Bar, Label LL_ExpAmount, Label LL_ExpHour, Label LL_TimeToNext, TextField TF_LvlGoal, Button BN_Remove){
            this.PB_Bar = PB_Bar;
            this.LL_ExpAmount = LL_ExpAmount;
            this.LL_ExpHour = LL_ExpHour;
            this.LL_TimeToNext = LL_TimeToNext;
            this.TF_LvlGoal = TF_LvlGoal;
            this.BN_Remove = BN_Remove;
        }

        public ProgressIndicatorBar getPB_Bar(){
            return PB_Bar;
        }

        public Label getLL_ExpAmount() {
            return LL_ExpAmount;
        }

        public Label getLL_ExpHour() {
            return LL_ExpHour;
        }

        public Label getLL_TimeToNext() {
            return LL_TimeToNext;
        }

        public TextField getTF_LvlGoal() {
            return TF_LvlGoal;
        }

        public Button getBN_Remove() {
            return BN_Remove;
        }

        public void setPB_Bar(ProgressIndicatorBar PB_Bar) {
            this.PB_Bar = PB_Bar;
        }

        public void setLL_ExpAmount(Label LL_ExpAmount) {
            this.LL_ExpAmount = LL_ExpAmount;
        }

        public void setLL_ExpHour(Label LL_ExpHour) {
            this.LL_ExpHour = LL_ExpHour;
        }

        public void setLL_TimeToNext(Label LL_TimeToNext) {
            this.LL_TimeToNext = LL_TimeToNext;
        }

        public void setTF_LvlGoal(TextField TF_LvlGoal) {
            this.TF_LvlGoal = TF_LvlGoal;
        }

        public void setBN_Remove(Button BN_Remove) {
            this.BN_Remove = BN_Remove;
        }
    }

    /**
     * A ProgressIndicator Object that creates a ProgressBar and Label TitledPane
     */
    public class ProgressIndicatorBar extends StackPane {
        final private int level;
        final private String skillName;

        final private ProgressBar bar = new ProgressBar();
        final private Text text = new Text();

        public ProgressIndicatorBar(final double progress, final String skillName, final int level, final int levelsGained) {
            this.level = level;
            this.skillName = skillName;

            text.setFont(Font.font("comic sans", FontWeight.EXTRA_BOLD, 16));

            String css = "-fx-accent rgb(" + ((double) 1 - progress) * 255 + ", " + progress * 255 + ", 0)";

            bar.styleProperty().set(css);

            bar.setMaxWidth(Double.MAX_VALUE); // allows the progress bar to expand to fill available horizontal space.

            bar.setMinHeight(27);

            getChildren().setAll(bar, text);

            syncProgress(progress, level, levelsGained);
        }

        public String getSkill() {
            return skillName;
        }

        public int getLevel() {
            return level;
        }

        // synchronizes the progress indicated with the progress and levels passed in.
        public void syncProgress(double progress, int level, int levelsGained) {
            if (progress == 0) {
                text.setText(skillName);

                bar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            } else {
                text.setText(skillName + " | Level: " + level + " | Levels Gained: " + levelsGained);

                String css = "-fx-accent: rgb(" + ((double) 1 - progress) * 255 + ", " + progress * 255 + ", 0)";

                bar.setProgress(progress);
                bar.styleProperty().set(css);
            }
        }
    }
}

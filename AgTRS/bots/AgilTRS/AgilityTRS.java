package com.AgTRS.bots.AgilityTRS;

import com.AwesomeAPI.framework.UI.CurrentTaskList.CurrentTaskList;
import com.AwesomeAPI.game.Actions.Actions;
import com.runemate.game.api.client.embeddable.EmbeddableUI;
import com.runemate.game.api.hybrid.entities.GameObject;
import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.location.Area;
import com.runemate.game.api.hybrid.location.Coordinate;
import com.runemate.game.api.hybrid.location.navigation.Path;
import com.runemate.game.api.hybrid.location.navigation.cognizant.RegionPath;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.hybrid.util.StopWatch;
import com.runemate.game.api.script.framework.tree.TreeBot;
import com.runemate.game.api.script.framework.tree.TreeTask;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;


public class AgilityTRS extends TreeBot implements EmbeddableUI{

    public static List<String> obstacles = new ArrayList<>();
    public static List<Area> areas = new ArrayList<>();
    public static List<Coordinate> spots = new ArrayList<>();
    public static GameObject obstacle;
    public static List<String> interactions = new ArrayList<>();
    public static Player me;
    private static Path path;
    public StopWatch watch = new StopWatch();

    private AgilityInfoUI infoUI;
    private SimpleObjectProperty botInterfaceProperty;
    public boolean isBreaking;

    public Animating animating;
    public CheckAnimation checkAnimation;
    public RunCourse runCourse;
    public NotAnimating notAnimating;
    public String location;
    public Actions actions = new Actions();
    public StopWatch stuckTimer = new StopWatch();
    private String prevTask;
    private int count;
    public CurrentTaskList currentTaskList = new CurrentTaskList();
    public boolean alch;


    public AgilityTRS(){
        setEmbeddableUI(this);
    }

    @Override
    public void onStart(String...args){
        setLoopDelay(123, 324);
    }

    @Override
    public ObjectProperty<? extends Node> botInterfaceProperty() {
        if (botInterfaceProperty == null) {
            // Initializing configUI in this manor is known as Lazy Instantiation
            botInterfaceProperty = new SimpleObjectProperty<>(infoUI = new AgilityInfoUI(this));
        }
        return botInterfaceProperty;
    }

    @Override
    public TreeTask createRootTask() {
        animating = new Animating(this);
        checkAnimation = new CheckAnimation(this);
        runCourse = new RunCourse(this);
        notAnimating = new NotAnimating(this);
        me = Players.getLocal();
        return new CheckAnimation(this);
    }

    public static boolean RegionPath(Coordinate place){
        if(place != null) {
            path = RegionPath.buildTo(place);
        }
        if(path != null){
            return path.step();
        }
        return false;
    }

    public static Coordinate getReachable(List<Coordinate> coordinateList){
        for(int i = 0; i < coordinateList.size(); i++) {
            if (coordinateList.get(i).isReachable()) {
                return coordinateList.get(i);
            }
        }
        return null;
    }

    public static boolean isAnimating(){
        return me != null && me.getAnimationId() != -1;
    }
}

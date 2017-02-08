package com.AgTRS.bots.AgilityTRS;

import com.AwesomeAPI.game.Actions.Actions;
import com.runemate.game.api.hybrid.entities.GameObject;
import com.runemate.game.api.hybrid.entities.GroundItem;
import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.entities.definitions.ItemDefinition;
import com.runemate.game.api.hybrid.local.Camera;
import com.runemate.game.api.hybrid.local.hud.Menu;
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory;
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem;
import com.runemate.game.api.hybrid.location.Coordinate;
import com.runemate.game.api.hybrid.queries.results.LocatableEntityQueryResults;
import com.runemate.game.api.hybrid.region.GameObjects;
import com.runemate.game.api.hybrid.region.GroundItems;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.osrs.local.hud.interfaces.Magic;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.tree.LeafTask;

import java.util.List;
import java.util.function.Predicate;

public class RunCourse extends LeafTask {

    private final AgilityTRS bot;
    private String interact;
    private Player me;
    private GameObject obstacle;
    private GroundItem grace;
    private LocatableEntityQueryResults<GameObject> obstacles;
    private Actions actions = new Actions();
    private List<String> interactions;
    private Coordinate myPos;

    public RunCourse(AgilityTRS bot) {
        this.bot = bot;
    }


    @Override
    public void execute() {
        if (!Magic.HIGH_LEVEL_ALCHEMY.isSelected() || Magic.HIGH_LEVEL_ALCHEMY.deactivate()) {
            me = Players.getLocal();
            myPos = me.getPosition();
            if ((grace = GroundItems.newQuery().names("Mark of grace").surroundingsReachable().results().nearest()) != null) {
                bot.currentTaskList.addTask("Grabbing Mark of grace");
                if (grace.isVisible() && (interact = grace.getDefinition().getGroundActions().get(0)) != null) {
                    if (grace.interact(interact)) {
                        Execution.delayUntil(() -> !grace.isValid(), 5000);
                    } else {
                        Camera.concurrentlyTurnTo(grace);
                        if (actions.walkToSpotB(grace.getPosition())) {
                            doAlch();
                        }
                    }
                } else {
                    if (bot.actions.walkToSpotB(grace.getPosition())) {
                        doAlch();
                    }
                }
            } else if (!bot.areas.isEmpty()) {
                for (int i = 0; i < bot.areas.size(); i++) {
                    if (bot.areas.get(i).contains(myPos)) {
                        if ((obstacle = GameObjects.newQuery().names(bot.obstacles.get(i)).filter(noAction).results().nearestTo(bot.spots.get(i))) != null) {
                            if (obstacle.distanceTo(myPos) > 10) {
                                bot.currentTaskList.addTask("Distance is > 10, Walking");
                                if (bot.actions.walkToSpot(bot.spots.get(i))) {
                                    doAlch();
                                } else {
                                    if (bot.actions.walkToSpotB(obstacle.getPosition())) {
                                        doAlch();
                                    }
                                }
                            } else {
                                if ((interact = obstacle.getDefinition().getActions().get(0)) != null) {
                                    bot.currentTaskList.addTask(interact + " " + bot.obstacles.get(i));
                                    if (obstacle.isVisible()) {
                                        if (obstacle.interact(interact)) {
                                            Execution.delayUntil(() -> bot.actions.isAnimating(me), 5000);
                                        } else {
                                            Camera.concurrentlyTurnTo(obstacle);
                                            if (!bot.actions.walkToSpot(bot.spots.get(i))) {
                                                if (bot.actions.walkToSpotB(obstacle.getPosition())) {
                                                    doAlch();
                                                }
                                            } else {
                                                doAlch();
                                            }
                                        }
                                    } else if (bot.currentTaskList.getCount() > 50) {
                                        bot.currentTaskList.addTask("Bot failed to interact 50+ times");
                                        if (bot.actions.walkToSpotB(obstacle.getPosition())) {
                                            doAlch();
                                        }
                                    } else {
                                        Camera.concurrentlyTurnTo(obstacle);
                                        if (!bot.actions.walkToSpot(bot.spots.get(i))) {
                                            if (bot.actions.walkToSpotB(obstacle.getPosition())) {
                                                doAlch();
                                            }
                                        } else {
                                            doAlch();
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    } else if (i == bot.areas.size() - 1) {
                        if ((obstacle = GameObjects.newQuery().names(bot.obstacles.get(0)).filter(noAction).results().nearestTo(bot.spots.get(0))) != null) {
                            if (obstacle.distanceTo(myPos) > 10) {
                                bot.currentTaskList.addTask("Distance is > 10, Walking");
                                System.out.println("Distance > 10, walking");
                                if (bot.actions.walkToSpotB(obstacle.getPosition())) {
                                    doAlch();
                                }
                            } else {
                                if ((interact = obstacle.getDefinition().getActions().get(0)) != null) {
                                    bot.currentTaskList.addTask(interact + " " + bot.obstacles.get(0));
                                    if (obstacle.isVisible()) {
                                        if (obstacle.interact(interact)) {
                                            Execution.delayUntil(() -> bot.actions.isAnimating(me), 5000);
                                        } else {
                                            Camera.concurrentlyTurnTo(obstacle);
                                            if (bot.actions.walkToSpotB(bot.spots.get(0))) {
                                                doAlch();
                                            }
                                        }
                                    } else {
                                        Camera.concurrentlyTurnTo(obstacle);
                                        if (bot.actions.walkToSpotB(bot.spots.get(0))) {
                                            doAlch();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void doAlch() {
        if (bot.alch) {
            boolean Alched = false;
            SpriteItem alchItem = Inventory.getItemIn(0);
            String interaction = "";
            ItemDefinition def;
            if((def = alchItem.getDefinition()) != null) {
                interaction = def.getName();
            }
            if (alchItem != null) {
                while (bot.isRunning() && bot.alch && (!Alched || Magic.HIGH_LEVEL_ALCHEMY.isSelected())) {
                    if (!Magic.HIGH_LEVEL_ALCHEMY.isSelected()){
                        if(Magic.HIGH_LEVEL_ALCHEMY.activate()){
                        }
                    }
                    else{
                        if(alchItem.interact("Cast", "High Level Alchemy -> " + interaction)){
                            Execution.delayUntil(() -> !Magic.HIGH_LEVEL_ALCHEMY.isSelected(), 2000);
                        }
                        System.out.println(Menu.getItems());
                    }
                }
            }
        }
    }

    private Predicate<GameObject> noAction = obs -> !obs.getDefinition().getActions().isEmpty();
}

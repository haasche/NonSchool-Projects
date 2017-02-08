package com.AgTRS.bots.AgilityTRS;

import com.runemate.game.api.hybrid.Environment;
import com.runemate.game.api.hybrid.GameEvents;
import com.runemate.game.api.hybrid.RuneScape;
import com.runemate.game.api.hybrid.entities.GameObject;
import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.local.hud.interfaces.Health;
import com.runemate.game.api.hybrid.region.GameObjects;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.tree.LeafTask;


public class Animating extends LeafTask {

    private final AgilityTRS bot;
    private GameObject food;
    private Player me;

    public Animating(AgilityTRS bot){
        this.bot = bot;
    }

    @Override
    public void execute() {
        System.out.println("Animating");
        me = Players.getLocal();
        if(bot.isBreaking){
            GameEvents.Universal.LOGIN_HANDLER.disable();
            GameEvents.Universal.LOBBY_HANDLER.disable();
            while(Environment.getBot().isRunning() && RuneScape.isLoggedIn() && RuneScape.logout()){
                Execution.delayUntil(() -> !RuneScape.isLoggedIn(), 10000);
            }
        }

        else{
            GameEvents.Universal.LOGIN_HANDLER.enable();
            GameEvents.Universal.LOBBY_HANDLER.enable();
        }

        if(Health.getCurrentPercent() < 50){
            if((food = GameObjects.newQuery().actions("Eat").results().first()) != null){
                if(food.interact("Eat")){
                    Execution.delayUntil(() -> !food.isValid(), 5000);
                }
            }
        }
        else
        Execution.delayUntil(() -> me != null && !bot.isAnimating(), () -> bot.isAnimating(), 5000);
    }
}

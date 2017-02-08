package com.AgTRS.bots.AgilityTRS;

import com.runemate.game.api.hybrid.GameEvents;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.script.framework.tree.BranchTask;
import com.runemate.game.api.script.framework.tree.TreeTask;


public class NotAnimating extends BranchTask {

    private final AgilityTRS bot;

    public NotAnimating(AgilityTRS bot){
        this.bot = bot;
        bot.me = Players.getLocal();
    }
    @Override
    public boolean validate() {
        System.out.println("Not animating");
        if(!bot.isBreaking) {
            GameEvents.Universal.LOGIN_HANDLER.enable();
            GameEvents.Universal.LOBBY_HANDLER.enable();
        }
        return !bot.isBreaking && bot.location != null;
    }

    @Override
    public TreeTask successTask() {
        return bot.runCourse;
    }

    @Override
    public TreeTask failureTask() {
        return bot.checkAnimation;
    }
}

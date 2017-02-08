package com.AgTRS.bots.AgilityTRS;

import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.local.hud.interfaces.Health;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.script.framework.tree.BranchTask;
import com.runemate.game.api.script.framework.tree.TreeTask;


public class CheckAnimation extends BranchTask {

    private final AgilityTRS bot;
    private Player me;

    public CheckAnimation(AgilityTRS bot){
        this.bot = bot;
    }


    @Override
    public boolean validate() {
        System.out.println("Check animation");
        me = Players.getLocal();
        return me != null && Health.getCurrentPercent() > 50 && !bot.isBreaking && !bot.isAnimating() && !me.isMoving();
    }

    @Override
    public TreeTask successTask() {
        return bot.notAnimating;
    }

    @Override
    public TreeTask failureTask() {
        return bot.animating;
    }


}

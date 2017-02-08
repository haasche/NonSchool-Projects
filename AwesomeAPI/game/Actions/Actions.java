package com.AwesomeAPI.game.Actions;

import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.location.Coordinate;
import com.runemate.game.api.hybrid.location.navigation.Path;
import com.runemate.game.api.hybrid.location.navigation.Traversal;
import com.runemate.game.api.hybrid.location.navigation.basic.BresenhamPath;
import com.runemate.game.api.hybrid.location.navigation.cognizant.RegionPath;

import java.util.List;
import java.util.function.Predicate;


public class Actions {

    private Coordinate prev;

    private Predicate<Coordinate> notReachable = coordinate -> !coordinate.isReachable();

    public boolean isAnimating(Player me){
        return me != null && me.getAnimationId() != -1;
    }

    public boolean walkToSpot(Coordinate place){
        RegionPath path = null;
        if(prev == null || prev != place) {
            prev = place;
            if (place != null) {
                path = RegionPath.buildTo(place);
            }
        }
        if(path != null && path.getNext() == null) {
            path = RegionPath.buildTo(place);
        }
        if(path != null) {
            return path.step();
        }
        else
            return false;
    }

    public boolean walkToSpotD(Coordinate place){
        Path path = null;
        if(prev == null || prev != place) {
            prev = place;
            if (place != null) {
                path = Traversal.getDefaultWeb().getPathBuilder().buildTo(place);
            }
        }
        if(path != null && path.getNext() == null) {
            path = Traversal.getDefaultWeb().getPathBuilder().buildTo(place);
        }
        if(path != null) {
            return path.step();
        }
        else
            return false;
    }

    public boolean walkToSpotB(Coordinate place){
        BresenhamPath path = null;
        if(prev == null || prev != place) {
            prev = place;
            if (place != null) {
                path = BresenhamPath.buildTo(place);
            }
        }
        if(path != null && path.getNext() == null) {
            path = BresenhamPath.buildTo(place);
        }
        if(path != null) {
            return path.step();
        }
        else
            return false;
    }


    public Coordinate getReachable(List<Coordinate> surroundingCoordinates) {
        surroundingCoordinates.removeIf(notReachable);
        if(!surroundingCoordinates.isEmpty())
        return surroundingCoordinates.get(0);
        else{
            return null;
        }
    }
}

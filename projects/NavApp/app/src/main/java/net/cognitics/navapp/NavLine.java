package net.cognitics.navapp;

/**
 * Created by Chris on 4/12/2018.
 */

public class NavLine {
    NavPoint navOne,navTwo;
    /**Usage:
     * (navPoint,navPoint) - Draw line between 2 points
     * (navPoint, null) - Draw line from point to bottom center screen
    */
    public NavLine(NavPoint navOne, NavPoint navTwo){
        this.navOne=navOne;
        this.navTwo=navTwo;
    }
    public NavPoint[] getPoints(){
        return new NavPoint[]{navOne,navTwo};
    }
}

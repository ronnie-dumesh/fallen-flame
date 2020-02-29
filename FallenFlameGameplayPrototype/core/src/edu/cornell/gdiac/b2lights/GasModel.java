// =============================================================================
// Xi Compiler
// GasModel.java
// =============================================================================


package edu.cornell.gdiac.b2lights;

import edu.cornell.gdiac.physics.obstacle.WheelObstacle;

public class GasModel extends WheelObstacle {

    /** Default gas radius. */
    private static final float GAS_RADIUS = 1f; // Might want to change this!

    /** How long a fire can last, in milliseconds. */
    private static final int FIRE_DURATION = 6000;

    /** Is this fire lit? */
    private boolean isLit;

    /** Time when it started burning. **/
    private long startBurn;

    public GasModel(float x, float y) {
        super(x, y, GAS_RADIUS);
        setSensor(true);
        isLit = false;
        // Set texture to something so it shows:
        // setTexture(toSomething);
    }

    /** Is this gas lit? */
    public boolean getLit() {
        return isLit;
    }

    /** Set this gas lit or not. */
    public void setLit(boolean value) {
        if ((isLit && value) || (!isLit && !value)) return;
        if (isLit) {
            throw new Error("Cannot unlight a fire!");
        }
        isLit = true;
        startBurn = System.currentTimeMillis();
    }

    /** How long till burnout in milliseconds. */
    public int timeToBurnout() {
        if (!isLit) return Integer.MAX_VALUE;
        int timeLeft = FIRE_DURATION - (int) (System.currentTimeMillis() - startBurn);
        return Math.max(timeLeft, 0);
    }


}

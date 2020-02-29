// =============================================================================
// Xi Compiler
// GasModel.java
// =============================================================================


package edu.cornell.gdiac.b2lights;


import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;

public class GasModel extends WheelObstacle {

    public GasModel(float x, float y) {
        super(x, y, 1.0f); // Might want to change this!
        //#region Implement me!
        //#endregion
    }

    /** Is this gas lit? */
    public boolean getLit() {
        return false;
        //#region Implement me!
        //#endregion
    }

    /** Set this gas lit or not. */
    public void setLit(boolean value) {
        //#region Implement me!
        //#endregion
    }

    /** How long till burnout in milliseconds. */
    public int timeToBurnout() {
        return 0;
        //#region Implement me!
        //#endregion
    }


}

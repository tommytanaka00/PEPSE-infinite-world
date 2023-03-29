package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * Represents the sun - moves across the sky in an elliptical path.
 */
public class Sun {
    /* Constants */
    private static final float SUN_RADIUS = 100;
    private static final float SUN_ROTATION_RADIUS = 200;
    private static final float DEGREES_TO_RADIANS = 0.017453292519943295f;
    private static final float INITIAL_ANGLE = 0f;
    private static final float FINAL_ANGLE = 360f;
    private static final double ELLIPSE_SHIFT = 0.1; // prevents zero division
    private static final double ELLIPSE_FACTOR = 1.5;
    private static final String SUN_TAG = "sun";

    /**
     * This function creates a yellow circle that moves in the sky in an elliptical path (in camera
     * coordinates).
     *
     * @param gameObjects      The collection of all participating game objects.
     * @param layer            The number of the layer to which the created sun should be added.
     * @param windowDimensions The dimensions of the windows.
     * @param cycleLength      The amount of seconds it should take the created game object to complete a full
     *                         cycle.
     * @return A new game object representing the sun.
     */
    public static GameObject create(GameObjectCollection gameObjects,
                                    int layer,
                                    Vector2 windowDimensions,
                                    float cycleLength) {

        Renderable oval = new OvalRenderable(Color.YELLOW);

        GameObject sun = new GameObject(Vector2.ZERO, new Vector2(SUN_RADIUS, SUN_RADIUS), oval);
        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        gameObjects.addGameObject(sun, layer);
        sun.setTag(SUN_TAG);

        new Transition<>(
                sun, //the game object being changed
                vectorAngle -> sun.setCenter(calcSunPosition(windowDimensions, vectorAngle)),  //the method to call
                INITIAL_ANGLE,    //initial angle value
                FINAL_ANGLE,   //final angle value
                Transition.LINEAR_INTERPOLATOR_FLOAT,  //use a linear interpolator
                cycleLength,   //transition fully over a day
                Transition.TransitionType.TRANSITION_LOOP,
                null);
        return sun;
    }

    /**
     * Calculates the sun position, so that the sun moves in an ellipse
     */
    private static Vector2 calcSunPosition(Vector2 windowDimensions, float angleInSky) {
        angleInSky = (float) (angleInSky * DEGREES_TO_RADIANS + Math.PI);
        Vector2 windowCenter = windowDimensions.mult(0.5f);
        float ellipticFactor = (float) (ELLIPSE_SHIFT + (ELLIPSE_FACTOR * Math.abs(Math.cos(angleInSky - Math.PI / 2))));
        return new Vector2(windowCenter.x() + ellipticFactor * SUN_ROTATION_RADIUS * ((float) Math.sin(angleInSky)),
                windowCenter.y() + SUN_ROTATION_RADIUS * ((float) Math.cos(angleInSky)));
    }
}

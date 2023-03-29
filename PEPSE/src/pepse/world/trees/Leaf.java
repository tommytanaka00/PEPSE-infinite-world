package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.components.Transition;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * class for an object of a TreeTop leaf
 */
public class Leaf extends GameObject {
    /* Constants */
    /**
     * Size of a Leaf
     */
    public static final float SIZE = 30;

    /**
     * tag for regular leaf
     */
    public static final String LEAF_ON_TREE_TAG = "leaf";

    /**
     * tag for falling leaf
     */
    public static final String LEAF_FALLING_TAG = "falling leaf";

    /* Transition */
    private Transition<Float> angleTransition;
    private Transition<Float> widthTransition;
    private Transition<Float> horizontalTransitionInFall;

    /**
     * Constructor
     *
     * @param topLeftCorner top left of the object
     * @param renderable    renderable object
     */
    public Leaf(Vector2 topLeftCorner, Renderable renderable) {
        super(topLeftCorner, Vector2.ONES.mult(SIZE), renderable);
    }

    /**
     * Sets the different transitions
     *
     * @param angleTransition the transition on the angle of the leaf
     * @param widthTransition the transition on the width of the leaf
     */
    public void setTransitions(Transition<Float> angleTransition, Transition<Float> widthTransition) {
        this.angleTransition = angleTransition;
        this.widthTransition = widthTransition;
    }

    /**
     * setter for the horizontal movement of the leaf during its fall
     *
     * @param horizontalTransitionInFall the transition on the horizontal velocity of the leaf
     */
    public void setFallTransition(Transition<Float> horizontalTransitionInFall) {
        this.horizontalTransitionInFall = horizontalTransitionInFall;
    }


    /**
     * removes the previous transitions
     */
    public void removeTransitions() {
        this.removeComponent(horizontalTransitionInFall);
        this.removeComponent(widthTransition);
        this.removeComponent(angleTransition);
    }

    /**
     * On collision, stops transitions and movements
     *
     * @param other     the object we collided with
     * @param collision the collision object
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        if (other instanceof Leaf || this.getTag().equals(LEAF_ON_TREE_TAG)) {
            return;
        }
        super.onCollisionEnter(other, collision);
        this.setVelocity(Vector2.ZERO);
        this.transform().setVelocityX(0);
        this.removeComponent(horizontalTransitionInFall);
        this.removeComponent(widthTransition);
        this.removeComponent(angleTransition);
    }

    /**
     * on collision stay action
     *
     * @param other     the object we collided with
     * @param collision the collision object
     */
    @Override
    public void onCollisionStay(GameObject other, Collision collision) {
        super.onCollisionStay(other, collision);
        onCollisionEnter(other, collision);
    }

}

package pepse.world.trees;

import danogl.collisions.GameObjectCollection;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.world.Block;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;

public class TreeTop {
    /* Constants */
    private static final int AVERAGE_TREE_LEAVES = 40;
    private static final int VARIATION_TREE_LEAVES = AVERAGE_TREE_LEAVES / 8;
    private static final float TREE_LEAVES_LOCATION = 7;
    private static final float FADEOUT_TIME = 7;
    private static final float FADEIN_TIME = 2;
    private static final float LEAF_FALL_VELOCITY = 100;
    private static final float HORIZONTAL_VELOCITY = 100f;
    private static final float LEAF_FALL_CYCLE = 5;
    private static final float LEAF_ANGLE_CYCLE = 6;
    private static final float LEAF_WIDTH_CYCLE = LEAF_ANGLE_CYCLE / 2;
    private static final float INITIAL_ANGLE = 0;
    private static final float FINAL_ANGLE = 40;
    private static final float INITIAL_DIMENSION = 0;
    private static final float FINAL_DIMENSION = Leaf.SIZE / 3;
    private static final float TIME_CONSTANT = 600f;

    /* Fields */
    private final int seed;
    private final int numOfLeaves;
    private final Vector2 topLeftCorner;
    private final Renderable renderable;
    private final HashSet<Leaf> leaves = new HashSet<>();
    private GameObjectCollection gameObjects;
    private int leavesLayer;
    private int fallingLeavesLayer;
    private int groundLayer;


    /**
     * constructor
     *
     * @param topLeftCorner top of the tree location
     * @param renderable    renderable for the tree top
     * @param seed          randomness seed
     */
    public TreeTop(Vector2 topLeftCorner, Renderable renderable, int seed) {
        this.topLeftCorner = topLeftCorner;
        this.renderable = renderable;
        this.seed = seed;
        numOfLeaves = AVERAGE_TREE_LEAVES + (int) (VARIATION_TREE_LEAVES * new Random(seed).nextFloat());
    }

    /**
     * Creates a treeTop in the given layer
     *
     * @param gameObjects        game Object which the Leaves will be added to
     * @param leavesLayer        the layer for leaves on tree
     * @param fallingLeavesLayer the layer for falling leaves
     * @param groundLayer        the layer of the ground
     */
    public void create(GameObjectCollection gameObjects, int leavesLayer, int fallingLeavesLayer,
                       int groundLayer) {
        this.gameObjects = gameObjects;
        this.leavesLayer = leavesLayer;
        this.fallingLeavesLayer = fallingLeavesLayer;
        this.groundLayer = groundLayer;
        for (int i = 0; i < numOfLeaves; i++) {
            float x = topLeftCorner.x() + TREE_LEAVES_LOCATION * Block.SIZE *
                    (new Random(Objects.hash(topLeftCorner.x() + i, seed)).nextFloat() - 0.5f);

            float y = topLeftCorner.y() + TREE_LEAVES_LOCATION * Block.SIZE *
                    (new Random(Objects.hash(topLeftCorner.y() + i, seed)).nextFloat() - 0.5f);
            Leaf leaf = new Leaf(new Vector2(x, y), renderable);
            gameObjects.addGameObject(leaf, leavesLayer);
            setLeafMovements(new Vector2(x, y), leaf);
            leaves.add(leaf);
            leaf.physics().preventIntersectionsFromDirection(Vector2.ZERO);
            gameObjects.layers().shouldLayersCollide(leavesLayer, leavesLayer, false);
            leaf.setTag(Leaf.LEAF_ON_TREE_TAG);
            leaf.physics().preventIntersectionsFromDirection(Vector2.ZERO);

        }

    }

    /**
     * Creates the movements of the leaf
     *
     * @param location the location of the leaf, for recreation
     * @param leaf     the leaf object
     */
    private void setLeafMovements(Vector2 location, Leaf leaf) {
        Runnable leafMovement = getLeavesTransition(leaf);
        new ScheduledTask(
                leaf,
                new Random(Objects.hash(location.x() + location.y(), seed)).nextFloat(),
                true,
                leafMovement);

        new ScheduledTask(
                leaf,
                TIME_CONSTANT * new Random(Objects.hash(location.x() * location.y(), seed)).nextFloat(),
                false,
                getFallTransition(location, leaf)
        );
    }

    /*
     * creates runnable of leaf fall
     */
    private Runnable getFallTransition(Vector2 location, Leaf leaf) {
        return () -> {
            updateLayers(leaf);
            leaf.renderer().fadeOut(FADEOUT_TIME, () -> onFadeOutEnd(location, leaf));
            leaf.transform().setVelocityY(LEAF_FALL_VELOCITY);
            leaf.setTag(Leaf.LEAF_FALLING_TAG);
            float randomVelocityFactor = new Random((long) (seed * location.x())).nextFloat() - 0.5f;
            Transition<Float> horizontalTransitionInFall = new Transition<>(
                    leaf, //the game object being changed
                    velocity -> leaf.transform().setVelocityX(velocity),  //the method to call
                    randomVelocityFactor * HORIZONTAL_VELOCITY,    //initial transition value
                    -randomVelocityFactor * HORIZONTAL_VELOCITY,   //final transition value
                    Transition.LINEAR_INTERPOLATOR_FLOAT,
                    LEAF_FALL_CYCLE,
                    Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                    null);  //nothing further to execute upon reaching final value
            leaf.setFallTransition(horizontalTransitionInFall);
        };
    }

    /*
     * creates runnable of leaf movement
     */
    private Runnable getLeavesTransition(Leaf leaf) {
        return () -> {
            Transition<Float> angleTransition = new Transition<>(
                    leaf, //the game object being changed
                    leaf.renderer()::setRenderableAngle,  //the method to call
                    INITIAL_ANGLE,    //initial transition value
                    FINAL_ANGLE,   //final transition value
                    Transition.LINEAR_INTERPOLATOR_FLOAT,
                    LEAF_ANGLE_CYCLE,
                    Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                    null);  //nothing further to execute upon reaching final value

            Transition<Float> widthTransition = new Transition<>(
                    leaf, //the game object being changed
                    dim -> leaf.setDimensions(new Vector2(Leaf.SIZE - dim, Leaf.SIZE)),  //the method to call
                    INITIAL_DIMENSION,    //initial transition value
                    FINAL_DIMENSION,   //final transition value
                    Transition.LINEAR_INTERPOLATOR_FLOAT,
                    LEAF_WIDTH_CYCLE,
                    Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                    null);  //nothing further to execute upon reaching final value
            leaf.removeTransitions();
            leaf.setTransitions(angleTransition, widthTransition);
        };
    }

    /*
     * updates the layers for falling leaf
     */
    private void updateLayers(Leaf leaf) {
        gameObjects.removeGameObject(leaf, leavesLayer);
        gameObjects.addGameObject(leaf, fallingLeavesLayer);
        gameObjects.layers().shouldLayersCollide(fallingLeavesLayer, groundLayer, true);
        gameObjects.layers().shouldLayersCollide(fallingLeavesLayer, leavesLayer, false);
    }

    /*
     * after fall ends procedure
     */
    private void onFadeOutEnd(Vector2 location, Leaf leaf) {
        leaf.setTag(Leaf.LEAF_ON_TREE_TAG);
        gameObjects.removeGameObject(leaf, fallingLeavesLayer);
        gameObjects.addGameObject(leaf, leavesLayer);
        leaf.transform().setVelocityX(0);
        leaf.setCenter(location);
        leaf.renderer().fadeIn(FADEIN_TIME);
        setLeafMovements(location, leaf);
    }


    /**
     * removes the treeTop parts
     *
     * @param gameObjects the game object to remove from
     */
    public void remove(GameObjectCollection gameObjects) {
        for (Leaf objectToRemove : leaves) {
            if (objectToRemove.getTag().equals(Leaf.LEAF_ON_TREE_TAG)) {
                gameObjects.removeGameObject(objectToRemove, leavesLayer);
            } else {
                gameObjects.removeGameObject(objectToRemove, fallingLeavesLayer);
            }
        }
    }
}

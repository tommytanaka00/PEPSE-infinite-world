package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.world.Block;

import java.awt.*;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/**
 * Responsible for the creation and management of trees.
 */
public class Tree {
    /* Constants */
    private static final int BASIC_TREE_HEIGHT = 10;
    private static final int TREE_HEIGHT_RANGE = 7;
    private static final String TREE_TAG = "tree";
    private static final int PLANT_BOUND = 100;
    private static final int PLANT_CHANCES = 5;

    /* World */
    private final int layer;
    private final int groundLayer;
    private int leavesLayer;
    private int fallingLeavesLayer;
    private final int seed;


    /* Rendering */
    private static final Color TRUNK_COLOR = new Color(100, 50, 20);
    private static final Color LEAVES_COLOR = new Color(50, 200, 30);
    private final RectangleRenderable trunkRectangle = new RectangleRenderable(ColorSupplier.approximateColor(TRUNK_COLOR));
    private final RectangleRenderable leafRectangle = new RectangleRenderable(ColorSupplier.approximateColor(LEAVES_COLOR));

    /* Fields */
    private final Vector2 windowDimensions;
    private final GameObjectCollection gameObjects;
    private final Function<Float, Float> height;
    private final Map<Integer, HashSet<Object>> treeInX = new Hashtable<>();

    /**
     * Constructor for tree
     *
     * @param gameObjects      gameObject manager
     * @param layer            the layer to put the tree in
     * @param groundLayer      the layer of the ground
     * @param windowDimensions the dimensions of the window
     * @param seed             the randomness seed
     * @param height           function which calculates the height of the terrain in a given x
     */
    public Tree(GameObjectCollection gameObjects,
                int layer,
                int groundLayer,
                Vector2 windowDimensions,
                int seed,
                Function<Float, Float> height) {
        this.gameObjects = gameObjects;
        this.layer = layer;
        this.groundLayer = groundLayer;
        this.windowDimensions = windowDimensions;
        this.seed = seed;
        this.height = height;
//        this.noiseGenerator = new NoiseGenerator(seed);
    }

    /**
     * a setter for the leaves layers
     *
     * @param leavesLayer        the layer for leaves on tree
     * @param fallingLeavesLayer the layer for falling leaves
     */
    public void setLeavesLayers(int leavesLayer, int fallingLeavesLayer) {
        this.leavesLayer = leavesLayer;
        this.fallingLeavesLayer = fallingLeavesLayer;
    }


    /**
     * This method creates trees in a given range of x-values.
     *
     * @param minX The lower bound of the given range (will be rounded to a multiple of Block.SIZE).
     * @param maxX The upper bound of the given range (will be rounded to a multiple of Block.SIZE).
     */
    public void createInRange(int minX, int maxX) {
        // Covers the closest multiple of Block.SIZE
        // however, this line is irrelevant, since we implemented
        // in a way that minX and maxX is a multiple of Block.SIZE
        minX = minX % Block.SIZE == 0 ? minX : (int) (minX - Block.SIZE - (minX % Block.SIZE));

        for (int x = minX; x < maxX; x += Block.SIZE) {
            if (toPlant(x)) {
                HashSet<Object> set = new HashSet<>();

                int treeHeight = (int) ((windowDimensions.y() - height.apply((float) x)) / Block.SIZE) + 2;
                int treeTopHeight = BASIC_TREE_HEIGHT + (int) (TREE_HEIGHT_RANGE * new Random(x).nextFloat());

                for (int i = 1; i <= treeTopHeight; i++) {
                    GameObject treeBark = new Block(new Vector2(x, windowDimensions.y() -
                            (treeHeight + i) * Block.SIZE), trunkRectangle);

                    gameObjects.addGameObject(treeBark, layer);
                    treeBark.setTag(TREE_TAG);
                    set.add(treeBark);
                }

                Vector2 treeTopVector = new Vector2(x, windowDimensions.y() - (treeHeight + treeTopHeight) * Block.SIZE);
                TreeTop treeTop = new TreeTop(treeTopVector, leafRectangle, seed);
                treeTop.create(gameObjects, leavesLayer, fallingLeavesLayer, groundLayer);
                set.add(treeTop);
                treeInX.put(x, set);
            }
        }
    }

    /**
     * This method removes trees in a given range of x-values.
     *
     * @param minX The lower bound of the given range (will be rounded to a multiple of Block.SIZE).
     * @param maxX The upper bound of the given range (will be rounded to a multiple of Block.SIZE).
     */
    public void removeInRange(int minX, int maxX) {
        // Covers the closest multiple of Block.SIZE
        // however, this line is irrelevant, since we implemented
        // in a way that minX and maxX is a mutiple of Block.SIZE
        minX = minX % Block.SIZE == 0 ? minX : (int) (minX - Block.SIZE - (minX % Block.SIZE));

        for (int x = minX; x < maxX; x += Block.SIZE) {
            if (toPlant(x)) {
                HashSet<Object> toDelete = treeInX.remove(x);

                for (Object objectToRemove : toDelete) {
                    if (objectToRemove instanceof TreeTop) {
                        ((TreeTop) objectToRemove).remove(gameObjects);
                    } else {
                        gameObjects.removeGameObject((GameObject) objectToRemove, layer);
                    }
                }
            }
        }
    }

    /*
     * decides whether a tree should be planted in a given x
     */
    private boolean toPlant(int x) {
        return (new Random((long) x * seed).nextInt(PLANT_BOUND)) < PLANT_CHANCES;
    }
}

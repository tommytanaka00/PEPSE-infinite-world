package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;

import java.awt.*;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

/**
 * Responsible for the creation and management of terrain.
 */
public class Terrain {

    private static final String LOWER_GROUND_TAG = "abstract ground";
    private static final String UPPER_GROUND_TAG = "concrete ground";
    private static final float CONCRETE_LAYER_DEPTH = 3;
    private final GameObjectCollection gameObjects;
    private final int groundLayer;
    private int backgroundLayer;
    private final double groundHeightAtX0;
    private static final int BASIC_GROUND_SIZE_FACTOR = 3;
    private static final int SEED_GROUND_FACTOR = 15;
    private static final float CARRIER_RUGGEDNESS_COEFFICIENT = 2.3f;
    private static final float MODULATING_RUGGEDNESS_COEFFICIENT = 10f;
    private static final float SPATIAL_COEFFICIENT = 2f;

    private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);
    private final Map<Integer, HashSet<GameObject>> terrainInX = new Hashtable<>();

    private final Vector2 windowDimensions;
    private final RectangleRenderable groundRectangle =
            new RectangleRenderable(ColorSupplier.approximateColor(BASE_GROUND_COLOR));
    //    private final RectangleRenderable virtualGroundRectangle =
//            new RectangleRenderable(ColorSupplier.approximateColor(new Color(150, 130, 100))); // for testing
    private final float firstSineCoefficient;
    private final float secondSineCoefficient;
    private final float secondSineFactor;
    private final float firstSineFactor;


    /**
     * Constructor
     *
     * @param gameObjects      The collection of all participating game objects.
     * @param groundLayer      The number of the layer to which the created ground objects should be added.
     * @param windowDimensions The dimensions of the windows.
     * @param seed             A seed for a random number generator.
     */
    public Terrain(GameObjectCollection gameObjects,
                   int groundLayer,
                   Vector2 windowDimensions,
                   int seed) {

        this.gameObjects = gameObjects;
        this.groundLayer = groundLayer;
        this.windowDimensions = windowDimensions;
        double groundSeed = new Random(seed).nextFloat();

        int basicGroundReference = (int) (windowDimensions.y() / BASIC_GROUND_SIZE_FACTOR);
        this.groundHeightAtX0 = (windowDimensions.y() / SEED_GROUND_FACTOR * groundSeed) +
                basicGroundReference;
        Random coefficientsCreator = new Random((int) groundHeightAtX0);
        this.firstSineFactor = coefficientsCreator.nextFloat() / (SPATIAL_COEFFICIENT * Block.SIZE);
        this.secondSineFactor = coefficientsCreator.nextFloat() / (SPATIAL_COEFFICIENT * Block.SIZE);
        this.firstSineCoefficient = CARRIER_RUGGEDNESS_COEFFICIENT * coefficientsCreator.nextFloat();
        this.secondSineCoefficient = MODULATING_RUGGEDNESS_COEFFICIENT * coefficientsCreator.nextFloat();
    }

    /**
     * This method return the ground height at a given location.
     *
     * @param x A number.
     * @return The ground height at the given location.
     */
    public float groundHeightAt(float x) {
        return (float) (groundHeightAtX0 + Block.SIZE * (Math.sin(x) +
                firstSineCoefficient * Math.sin(firstSineFactor * x) +
                secondSineCoefficient * Math.sin(secondSineFactor * x)));
    }

    /**
     * setter for the background layer
     * @param layer the layer of the lower terrain blocks
     */
    public void setBackgroundLayer(int layer) {
        this.backgroundLayer = layer;
    }


    /**
     * This method creates terrain in a given range of x-values.
     *
     * @param minX The lower bound of the given range (will be rounded to a multiple of Block.SIZE).
     * @param maxX The upper bound of the given range (will be rounded to a multiple of Block.SIZE).
     */
    public void createInRange(int minX, int maxX) {
        //Covers the closest multiple of Block.SIZE
        //however, this line is irrelevant in our implementation, since we implemented
        // in a way that minX and maxX is a multiple of Block.SIZE
        minX = minX % Block.SIZE == 0 ? minX : (int) (minX - Block.SIZE - (minX % Block.SIZE));

        for (int x = minX; x < maxX; x += Block.SIZE) {

            HashSet<GameObject> set = new HashSet<>();

            final float groundHeightAtX = groundHeightAt(x);
            for (float y = 0;
                 y < windowDimensions.y() - groundHeightAtX + (CONCRETE_LAYER_DEPTH - 1) * Block.SIZE;
                 y += Block.SIZE) {
                float height = windowDimensions.y() - Block.SIZE - y;
                GameObject ground = new Block(new Vector2(x, height), groundRectangle);

                //for efficiency, so that the lower layers cannot collide with other objects
                //which makes it so that avatar does not check collisions each time
                if (height > groundHeightAtX) {
                    gameObjects.addGameObject(ground, backgroundLayer);
                    ground.setTag(LOWER_GROUND_TAG);
                } else { //The upper concrete ground where avatar does collide with
                    gameObjects.addGameObject(ground, groundLayer);
                    ground.setTag(UPPER_GROUND_TAG);
                }
                set.add(ground);
            }
            terrainInX.put(x, set);
        }
    }

    /**
     * This method removes terrain in a given range of x-values.
     *
     * @param minX The lower bound of the given range (will be rounded to a multiple of Block.SIZE).
     * @param maxX The upper bound of the given range (will be rounded to a multiple of Block.SIZE).
     */
    public void removeInRange(int minX, int maxX) {
        //Covers the closest multiple of Block.SIZE
        //however, this line is irrelevant in our implementation, since we implemented
        // in a way that minX and maxX is a multiple of Block.SIZE
        minX = minX % Block.SIZE == 0 ? minX : (int) (minX - Block.SIZE - (minX % Block.SIZE));

        for (int x = minX; x < maxX; x += Block.SIZE) {
            HashSet<GameObject> toDelete = terrainInX.remove(x);

            for (GameObject objectToRemove : toDelete) {
                if (objectToRemove.getTag().equals(LOWER_GROUND_TAG)) {
                    gameObjects.removeGameObject(objectToRemove, backgroundLayer);
                } else { // (objectToRemove.getTag().equals(UPPER_GROUND))
                    gameObjects.removeGameObject(objectToRemove, groundLayer);
                }

            }
        }
    }


}

package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.components.CoordinateSpace;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.util.Vector2;
import pepse.world.*;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Tree;

import java.awt.*;

/**
 * The main class of the simulator.
 */
public class PepseGameManager extends GameManager {
    /* Constants */
    private static final float GRAVITY_LEVEL = 500;
    private static final Color SUN_HALO_COLOR = new Color(255, 255, 0, 20);
    private static final Vector2 ENERGY_LEVEL_TEXT_VECTOR = new Vector2(100, 20);
    private static final float CYCLE_LENGTH = 50;

    private static final int SEED = 3343;
    private static final String GAME_NAME = "PEPSE";


    /* Fields */
    private Avatar avatar;
    private Tree trees;
    private Terrain terrain;

    /* Frames */
    private static int windowFrameSize;
    private final Vector2 windowDimensions;
    private int curLeftWorldBoundary;
    private int curRightWorldBoundary;
    private final int framesInWindow = 3;

    /* Layers */
    private static final int NIGHT_LAYER = Layer.FOREGROUND;
    private static final int SKY_LAYER = Layer.BACKGROUND;
    private static final int SUN_LAYER = Layer.BACKGROUND + 1;
    private static final int SUN_HALO_LAYER = Layer.BACKGROUND + 2;
    private static final int TREE_LAYER = Layer.DEFAULT + 1;
    private static final int TERRAIN_LAYER = Layer.DEFAULT + 2;
    private static final int BACKGROUND_LAYER = Layer.DEFAULT + 3;
    private static final int LEAVES_LAYER = Layer.DEFAULT + 4;
    private static final int FALLING_LEAVES_LAYER = Layer.DEFAULT + 5;
    private static final int AVATAR_LAYER = Layer.DEFAULT + 6;
    private static final int AVATAR_ENERGY_DISPLAY_LAYER = Layer.UI;


    public PepseGameManager(String windowTitle) {
        super(windowTitle);
        this.windowDimensions = new Vector2((float) Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
                (float) Toolkit.getDefaultToolkit().getScreenSize().getHeight());
        windowFrameSize = (int) (((int) (((1f / framesInWindow) * this.windowDimensions.x()) / Block.SIZE)) * Block.SIZE);
    }

    /**
     * The method will be called once when a GameGUIComponent is created, and again after every invocation of
     * windowController.resetGame().
     *
     * @param imageReader      Contains a single method: readImage, which reads an image from disk.
     *                         See its documentation for help.
     * @param soundReader      Contains a single method: readSound, which reads a wav file from disk.
     *                         See its documentation for help.
     * @param inputListener    Contains a single method: isKeyPressed, which returns whether a given key is
     *                         currently pressed by the user or not. See its documentation.
     * @param windowController Contains an array of helpful, self-explanatory methods concerning the window.
     */
    @Override
    public void initializeGame(ImageReader imageReader,
                               SoundReader soundReader,
                               UserInputListener inputListener,
                               WindowController windowController) {
        super.initializeGame(imageReader, soundReader,
                inputListener, windowController);

        /* Create new objects for the field */
        this.terrain = new Terrain(gameObjects(), TERRAIN_LAYER, windowDimensions, SEED);
        terrain.setBackgroundLayer(BACKGROUND_LAYER);
        this.trees = new Tree(gameObjects(), TREE_LAYER,
                TERRAIN_LAYER, windowDimensions, SEED, terrain::groundHeightAt);
        trees.setLeavesLayers(LEAVES_LAYER, FALLING_LEAVES_LAYER);

        /* Create the sky */
        Sky.create(gameObjects(), windowDimensions, SKY_LAYER);

        /* Create the night, sun, and sun halo, and sync it all together */
        Night.create(gameObjects(),
                NIGHT_LAYER,
                windowDimensions,
                CYCLE_LENGTH
        );
        GameObject sun = Sun.create(gameObjects(),
                SUN_LAYER,
                windowDimensions,
                CYCLE_LENGTH
        );
        GameObject sunHalo = SunHalo.create(gameObjects(),
                SUN_HALO_LAYER,
                sun,
                SUN_HALO_COLOR
        );
        sunHalo.addComponent(deltaTime -> sunHalo.setCenter(sun.getCenter()));


        /* Create the world in frames */
        //one frame outside the screen on left
        curLeftWorldBoundary = -windowFrameSize;

        // framesInWindow frames in screen and one more outside on the right
        curRightWorldBoundary = windowFrameSize * (framesInWindow + 1);

        createInRange(curLeftWorldBoundary, curRightWorldBoundary);


        /* Create the avatar */
        Vector2 initialAvatarLocation = new Vector2(windowFrameSize * framesInWindow / 2f, -400);
        Avatar.setAvatarDimensions(Block.SIZE + Block.SIZE / 3f, Block.SIZE * 2);
        this.avatar = Avatar.create(gameObjects(),
                AVATAR_LAYER,
                initialAvatarLocation,
                inputListener,
                imageReader
        );
        gameObjects().layers().shouldLayersCollide(AVATAR_LAYER, TERRAIN_LAYER, true);
        gameObjects().layers().shouldLayersCollide(AVATAR_LAYER, TREE_LAYER, true);
        setCamera(new Camera(avatar,
                Vector2.ZERO, //initialAvatarLocation.subtract(Vector2.LEFT.mult(10)), todo: check this
                windowController.getWindowDimensions(),
                windowController.getWindowDimensions())
        );

        /* Create the energy level display of the avatar */
        GameObject avatarEnergyLevelText = AvatarEnergyLevelDisplay.create(gameObjects(),
                AVATAR_ENERGY_DISPLAY_LAYER,
                Vector2.ZERO,
                ENERGY_LEVEL_TEXT_VECTOR,
                avatar
        );
        avatarEnergyLevelText.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);

        /* Prevent intersections between objects and create gravity */
        avatar.physics().preventIntersectionsFromDirection(Vector2.ZERO);
        avatar.transform().setAccelerationY(GRAVITY_LEVEL);

    }


    public void createInRange(int minX, int maxX) {
        terrain.createInRange(minX, maxX);
        trees.createInRange(minX, maxX);
    }

    public void deleteInRange(int minX, int maxX) {
        terrain.removeInRange(minX, maxX);
        trees.removeInRange(minX, maxX);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        //If moving to right, create frame to the right, and delete leftmost frame
        if (avatar.getCenter().x() > (curLeftWorldBoundary + (framesInWindow * windowFrameSize))) {
            curLeftWorldBoundary = (curLeftWorldBoundary + windowFrameSize);
            curRightWorldBoundary = (curRightWorldBoundary + windowFrameSize);
            deleteInRange((curLeftWorldBoundary - windowFrameSize), curLeftWorldBoundary);
            createInRange((curRightWorldBoundary - windowFrameSize), curRightWorldBoundary);
        }

        //If moving to left, create frame to the left, and delete rightmost frame
        if (avatar.getCenter().x() < (curRightWorldBoundary - (framesInWindow * windowFrameSize))) {
            curLeftWorldBoundary = (curLeftWorldBoundary - windowFrameSize);
            curRightWorldBoundary = (curRightWorldBoundary - windowFrameSize);
            deleteInRange(curRightWorldBoundary, (curRightWorldBoundary + windowFrameSize));
            createInRange(curLeftWorldBoundary, (curLeftWorldBoundary + windowFrameSize));
        }
    }

    /**
     * Runs the entire simulation.
     *
     * @param args This argument should not be used.
     */
    public static void main(String[] args) {
        new PepseGameManager(GAME_NAME).run();
    }

}

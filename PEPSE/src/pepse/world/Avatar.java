package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.event.KeyEvent;

/**
 * An avatar can move around the world.
 */
public class Avatar extends GameObject {
    /* Constants */
    private static final float MOVEMENT_SPEED = 300;
    private static final float FLYING_SPEED = 50;
    private static final float JUMPING_SPEED = 280;

    //  On computers which are stronger than the aquariums stations, it can be increased to 400
    private static final float MAX_VELOCITY_Y_DOWN = 200;

    private static final float MAX_VELOCITY_Y_UP = -300;
    private static final int MAX_ENERGY_LEVEL = 100;
    private static final double ANIMATION_FRAME_RATE = 0.2;
    private static final double ENERGY_LEVEL_STEP = 0.5;
    private static final String BASIC_AVATAR_PATH = "pepse/assets/StandingMage.png";
    private static final String AVATAR_LEFT_MOVE_PATH = "pepse/assets/LeftFootMage.png";
    private static final String AVATAR_RIGHT_MOVE_PATH = "pepse/assets/RightFootMage.png";
    private static final float FELL_THROUGH_GROUND = 1800;

    /* Fields */
    private static float avatarLength = 40;
    private static float avatarHeight = 60;
    private static UserInputListener inputListener;
    private static Renderable staticAvatarRenderable;
    private static Renderable walkingAvatarRenderable;
    public float energyLevel;


    /**
     * Construct a new GameObject instance.
     *
     * @param topLeftCorner Position of the object, in window coordinates (pixels).
     *                      Note that (0,0) is the top-left corner of the window.
     * @param dimensions    Width and height in window coordinates.
     * @param renderable    The renderable representing the object. Can be null, in which case
     */
    public Avatar(Vector2 topLeftCorner, Vector2 dimensions, Renderable renderable) {
        super(topLeftCorner, dimensions, renderable);
        this.energyLevel = MAX_ENERGY_LEVEL;
    }


    /**
     * This function creates an avatar that can travel the world and is followed by the camera. The can stand,
     * walk, jump and fly, and never reaches the end of the world.
     *
     * @param gameObjects   The collection of all participating game objects.
     * @param layer         The number of the layer to which the created avatar should be added.
     * @param topLeftCorner The location of the top-left corner of the created avatar.
     * @param inputListener Used for reading input from the user.
     * @param imageReader   Used for reading images from disk or from within a jar.
     * @return A newly created representing the avatar.
     */
    public static Avatar create(GameObjectCollection gameObjects,
                                int layer, Vector2 topLeftCorner,
                                UserInputListener inputListener,
                                ImageReader imageReader) {

        Avatar.inputListener = inputListener;

        Avatar.staticAvatarRenderable = imageReader.readImage(BASIC_AVATAR_PATH,
                true);
        Avatar.walkingAvatarRenderable = new AnimationRenderable(new String[]{BASIC_AVATAR_PATH,
                AVATAR_LEFT_MOVE_PATH,
                AVATAR_RIGHT_MOVE_PATH},
                imageReader, true, ANIMATION_FRAME_RATE);

        Avatar avatar = new Avatar(topLeftCorner, new Vector2(avatarLength, avatarHeight), Avatar.staticAvatarRenderable);
        gameObjects.addGameObject(avatar, layer);

        return avatar;

    }

    /**
     * setter for the avatar dimensions
     *
     * @param length length of the avatar
     * @param height height of the avatar
     */
    public static void setAvatarDimensions(float length, float height){
        avatarLength = length;
        avatarHeight = height;
    }


    /**
     * Checks constantly for user input, and moves the avatar
     * as needed. He can jump, move left and right, and fly
     * In addition, restricts the maximum movement of the y-axis
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        renderer().setRenderable(Avatar.staticAvatarRenderable);

        /* If avatar is standing on something, energy regenerates */
        if (energyLevel < MAX_ENERGY_LEVEL && getVelocity().y() == 0) {
            energyLevel += ENERGY_LEVEL_STEP;
        }

        Vector2 movementDir = new Vector2(0, getVelocity().y());

        /* Move to left */
        if (inputListener.isKeyPressed(KeyEvent.VK_LEFT)) {
            movementDir = movementDir.add(Vector2.LEFT.mult(MOVEMENT_SPEED));
            renderer().setIsFlippedHorizontally(true);
            renderer().setRenderable(Avatar.walkingAvatarRenderable);
        }

        /* Move to right */
        if (inputListener.isKeyPressed(KeyEvent.VK_RIGHT)) {
            movementDir = movementDir.add(Vector2.RIGHT.mult(MOVEMENT_SPEED));
            renderer().setIsFlippedHorizontally(false);
            renderer().setRenderable(Avatar.walkingAvatarRenderable);
        }

        /* Jumping */
        if (inputListener.isKeyPressed(KeyEvent.VK_SPACE)) {
            if (getVelocity().y() == 0) {
                movementDir = movementDir.add(Vector2.UP.mult(JUMPING_SPEED));
            }
        }

        /* Flying */
        if (inputListener.isKeyPressed(KeyEvent.VK_SHIFT)
                && inputListener.isKeyPressed(KeyEvent.VK_SPACE)
                && energyLevel > 0) {
            movementDir = movementDir.add(Vector2.UP.mult(FLYING_SPEED));
            energyLevel -= ENERGY_LEVEL_STEP;
        }

        /* Restrict the maximum movement of the Y axis */
        if (movementDir.y() > MAX_VELOCITY_Y_DOWN) {
            movementDir = new Vector2(movementDir.x(), MAX_VELOCITY_Y_DOWN);
        } else if (movementDir.y() < MAX_VELOCITY_Y_UP) {
            movementDir = new Vector2(movementDir.x(), MAX_VELOCITY_Y_UP);
        }

        setVelocity(movementDir);

        /* THIS SHOULD NOT HAPPEN
           This is a fail-safe if the avatar falls through the ground
           (It looks natural, as if there is another world down below!) */
        if (getCenter().y() > FELL_THROUGH_GROUND)
        {
            setCenter(new Vector2(getCenter().x(), -1000));
            setVelocity(movementDir.multY(0));
        }
    }
}

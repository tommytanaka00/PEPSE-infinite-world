package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;

import java.awt.*;

public class AvatarEnergyLevelDisplay extends GameObject {
    private static final String ENERGY_DISPLAY_TEXT = "Energy level: ";
    /* Constants */
    private static float prevEnergyLevel;

    /* Fields */
    private static TextRenderable textRenderable;
    private static Avatar avatar;

    /**
     * Construct a new GameObject instance.
     *
     * @param topLeftCorner Position of the object, in window coordinates (pixels).
     *                      Note that (0,0) is the top-left corner of the window.
     * @param dimensions    Width and height in window coordinates.
     */
    public AvatarEnergyLevelDisplay(Vector2 topLeftCorner, Vector2 dimensions) {
        super(topLeftCorner, dimensions, textRenderable);
    }

    /**
     * Creates the energy display
     *
     * @param gameObjects game object to add it to
     * @param layer the layer to put it
     * @param topLeftCorner location of the object
     * @param dimensions dimensions of the object
     * @param avatar the avatar which it is connected to
     * @return AvatarEnergyLevelDisplay object
     */
    public static AvatarEnergyLevelDisplay create(GameObjectCollection gameObjects, int layer,
                                                  Vector2 topLeftCorner, Vector2 dimensions,
                                                  Avatar avatar) {
        AvatarEnergyLevelDisplay.avatar = avatar;
        AvatarEnergyLevelDisplay.prevEnergyLevel = avatar.energyLevel;
        AvatarEnergyLevelDisplay.textRenderable = new TextRenderable(ENERGY_DISPLAY_TEXT + prevEnergyLevel);
        AvatarEnergyLevelDisplay.textRenderable.setColor(Color.WHITE);

        AvatarEnergyLevelDisplay energyLevelText = new AvatarEnergyLevelDisplay(topLeftCorner, dimensions);
        gameObjects.addGameObject(energyLevelText, layer);
        return energyLevelText;
    }

    /**
     * Updates the energy level counter at the top left screen
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        if (avatar.energyLevel != prevEnergyLevel) {
            AvatarEnergyLevelDisplay.textRenderable.setString(ENERGY_DISPLAY_TEXT + avatar.energyLevel);
        }
        prevEnergyLevel = avatar.energyLevel;
    }

}

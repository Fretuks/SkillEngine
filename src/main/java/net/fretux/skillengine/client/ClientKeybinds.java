package net.fretux.skillengine.client;

import net.fretux.skillengine.SkillEngine;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class ClientKeybinds {
    public static KeyMapping OPEN_SKILLTREE;
    public static KeyMapping OPEN_ABILITIES;

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        OPEN_SKILLTREE = new KeyMapping(
                "key.skillengine.openskilltree",
                GLFW.GLFW_KEY_N,
                "key.categories.ui"
        );
        event.register(OPEN_SKILLTREE);

        OPEN_ABILITIES = new KeyMapping(
                "key.skillengine.openabilities",
                GLFW.GLFW_KEY_M,
                "key.categories.ui"
        );
        event.register(OPEN_ABILITIES);
    }
}

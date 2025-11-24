package net.fretux.skillengine.client;

import net.fretux.skillengine.SkillEngine;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(
    modid = SkillEngine.MODID,
    bus = Mod.EventBusSubscriber.Bus.MOD,
    value = Dist.CLIENT
)
public class ClientKeybinds {
    public static KeyMapping OPEN_SKILLTREE;

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        OPEN_SKILLTREE = new KeyMapping(
                "key.skillengine.openskilltree",
                GLFW.GLFW_KEY_N,
                "key.categories.ui"
        );
        event.register(OPEN_SKILLTREE);
    }
}

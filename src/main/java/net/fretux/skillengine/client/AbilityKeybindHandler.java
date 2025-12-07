package net.fretux.skillengine.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fretux.skillengine.network.PacketHandler;
import net.fretux.skillengine.network.ServerboundActivateAbilityPacket;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber
public class AbilityKeybindHandler {

    public static final KeyMapping ABILITY_1 = new KeyMapping(
            "key.skillengine.ability1", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.categories.gameplay"
    );

    public static final KeyMapping ABILITY_2 = new KeyMapping(
            "key.skillengine.ability2", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.categories.gameplay"
    );

    public static final KeyMapping ABILITY_3 = new KeyMapping(
            "key.skillengine.ability3", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, "key.categories.gameplay"
    );

    @SubscribeEvent
    public static void onInput(InputEvent.Key event) {
        if (ABILITY_1.consumeClick()) {
            if (SkilltreeClientState.getClientCooldown(1) <= 0) {
                PacketHandler.CHANNEL.sendToServer(new ServerboundActivateAbilityPacket(1));
            }
        }
        if (ABILITY_2.consumeClick()) {
            if (SkilltreeClientState.getClientCooldown(2) <= 0) {
                PacketHandler.CHANNEL.sendToServer(new ServerboundActivateAbilityPacket(2));
            }
        }
        if (ABILITY_3.consumeClick()) {
            if (SkilltreeClientState.getClientCooldown(3) <= 0) {
                PacketHandler.CHANNEL.sendToServer(new ServerboundActivateAbilityPacket(3));
            }
        }
    }
}

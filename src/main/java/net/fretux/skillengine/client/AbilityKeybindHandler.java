package net.fretux.skillengine.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fretux.skillengine.network.PacketHandler;
import net.fretux.skillengine.network.ServerboundActivateAbilityPacket;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class AbilityKeybindHandler {

    public static final int MAX_REGISTERED_ABILITY_SLOTS = 9;
    public static final KeyMapping[] ABILITY_KEYS = new KeyMapping[MAX_REGISTERED_ABILITY_SLOTS];

    static {
        for (int slot = 1; slot <= MAX_REGISTERED_ABILITY_SLOTS; slot++) {
            ABILITY_KEYS[slot - 1] = new KeyMapping(
                    "key.skillengine.ability" + slot,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    defaultKeyForSlot(slot),
                    "key.categories.gameplay"
            );
        }
    }

    public static void onInput(InputEvent.Key event) {
        int activeSlots = SkilltreeClientState.getAbilitySlots().length;
        int handledSlots = Math.min(activeSlots, ABILITY_KEYS.length);
        for (int slot = 1; slot <= handledSlots; slot++) {
            if (ABILITY_KEYS[slot - 1].consumeClick()) {
                if (SkilltreeClientState.getClientCooldown(slot) <= 0) {
                    PacketHandler.CHANNEL.sendToServer(new ServerboundActivateAbilityPacket(slot));
                }
            }
        }
    }

    private static int defaultKeyForSlot(int slot) {
        return switch (slot) {
            case 1 -> GLFW.GLFW_KEY_Z;
            case 2 -> GLFW.GLFW_KEY_X;
            case 3 -> GLFW.GLFW_KEY_C;
            default -> InputConstants.UNKNOWN.getValue();
        };
    }
}

package net.fretux.skillengine.client;

import net.fretux.skillengine.SkillEngine;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;

public class ClientEvents {
    public static void onKeyInput(InputEvent.Key event) {
        if (ClientKeybinds.OPEN_SKILLTREE != null &&
                ClientKeybinds.OPEN_SKILLTREE.consumeClick()) {

            Minecraft.getInstance().setScreen(new SkilltreeScreen());
        }
        if (ClientKeybinds.OPEN_ABILITIES != null &&
                ClientKeybinds.OPEN_ABILITIES.consumeClick()) {
            Minecraft.getInstance().setScreen(new AbilitySlotsScreen());
        }
    }
}

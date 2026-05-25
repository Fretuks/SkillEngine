package net.fretux.skillengine.client;

import net.minecraftforge.event.TickEvent;

public class ClientCooldownTicker {
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        int slots = SkilltreeClientState.getAbilitySlots().length;
        for (int slot = 1; slot <= slots; slot++) {
            int cd = SkilltreeClientState.getClientCooldown(slot);
            if (cd > 0) {
                SkilltreeClientState.updateCooldown(slot, cd - 1);
            }
        }
    }
}

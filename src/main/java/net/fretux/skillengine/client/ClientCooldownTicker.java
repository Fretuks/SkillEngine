package net.fretux.skillengine.client;

import net.minecraftforge.event.TickEvent;

public class ClientCooldownTicker {
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (int i = 0; i < 3; i++) {
            int cd = SkilltreeClientState.getClientCooldown(i + 1);
            if (cd > 0) {
                SkilltreeClientState.updateCooldown(i + 1, cd - 1);
            }
        }
    }
}

package net.fretux.skillengine.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.fretux.skillengine.SkillEngine;

@Mod.EventBusSubscriber(modid = SkillEngine.MODID, value = Dist.CLIENT)
public class ClientCooldownTicker {

    @SubscribeEvent
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

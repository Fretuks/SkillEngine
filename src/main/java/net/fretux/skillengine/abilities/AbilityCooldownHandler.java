package net.fretux.skillengine.abilities;

import net.fretux.skillengine.SkillEngine;
import net.fretux.skillengine.capability.PlayerSkillData;
import net.fretux.skillengine.capability.SkillEngineCapabilities;
import net.fretux.skillengine.network.ClientboundSyncCooldownPacket;
import net.fretux.skillengine.network.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = SkillEngine.MODID)
public class AbilityCooldownHandler {
    private static int syncCounter = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;
        event.player.getCapability(SkillEngineCapabilities.PLAYER_SKILLS).ifPresent(data -> {
            data.tickCooldowns();
            syncCounter++;
            if (syncCounter >= 10) {
                syncCounter = 0;
                for (int i = 1; i <= 3; i++) {
                    PacketHandler.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.player),
                            new ClientboundSyncCooldownPacket(i, data.getCooldown(i))
                    );
                }
            }
        });
    }
}

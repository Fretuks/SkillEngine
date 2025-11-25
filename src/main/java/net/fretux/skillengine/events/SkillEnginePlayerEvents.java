package net.fretux.skillengine.events;

import net.fretux.skillengine.SkillEngine;
import net.fretux.skillengine.capability.SkillEngineCapabilities;
import net.fretux.skillengine.network.ClientboundSyncSkillsPacket;
import net.fretux.skillengine.network.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = SkillEngine.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SkillEnginePlayerEvents {

    private static void sync(ServerPlayer player) {
        player.getCapability(SkillEngineCapabilities.PLAYER_SKILLS).ifPresent(data -> {
            PacketHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new ClientboundSyncSkillsPacket(data.getUnlockedNodes(), data.getSkillPoints())
            );
        });
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }
}

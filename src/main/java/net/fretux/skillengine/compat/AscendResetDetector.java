package net.fretux.skillengine.compat;

import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.skillengine.SkillEngine;
import net.fretux.skillengine.capability.PlayerSkillData;
import net.fretux.skillengine.capability.SkillEngineCapabilities;
import net.fretux.skillengine.network.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = SkillEngine.MODID)
public class AscendResetDetector {
    private static final String[] ASCEND_ATTRIBUTES = new String[]{
            "strength", "agility", "fortitude", "intelligence",
            "willpower", "charisma",
            "light_scaling", "medium_scaling", "heavy_scaling", "magic_scaling"
    };
    private static final Map<UUID, Integer> LAST_ATTRIBUTE_SUM = new HashMap<>();
    private static final Map<UUID, Integer> LAST_UNSPENT = new HashMap<>();

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(ascend -> {
            UUID id = player.getUUID();
            int currentSum = 0;
            for (String a : ASCEND_ATTRIBUTES) {
                currentSum += ascend.getAttributeLevel(a);
            }
            int lastSum = LAST_ATTRIBUTE_SUM.getOrDefault(id, currentSum);
            LAST_ATTRIBUTE_SUM.put(id, currentSum);
            int currentUnspent = ascend.getUnspentPoints();
            int lastUnspent = LAST_UNSPENT.getOrDefault(id, currentUnspent);
            LAST_UNSPENT.put(id, currentUnspent);
            boolean attributesDropped = lastSum > currentSum;
            boolean largeDrop = (lastSum - currentSum) >= 5;
            boolean unspentJump = currentUnspent > lastUnspent + 3;
            if (attributesDropped && largeDrop && unspentJump) {
                SkillEngine.LOGGER.info("[SkillEngine] Detected Ascend attribute reset. Resetting skilltree.");
                player.getCapability(SkillEngineCapabilities.PLAYER_SKILLS).ifPresent(skillData -> {
                    resetSkillEngine(skillData, player);
                });
            }
        });
    }

    private static void resetSkillEngine(PlayerSkillData data, Player player) {
        int refund = data.getTotalSkillCost();
        data.addSkillPoints(refund);
        data.clearAllNodes();
        SkillEngine.LOGGER.info("[SkillEngine] Skilltree fully reset. Refunded {} skill points.", refund);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.syncSkillsTo(serverPlayer);
        }
    }
}
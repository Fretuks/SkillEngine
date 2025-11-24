package net.fretux.skillengine.compat;

import net.fretux.ascend.player.PlayerStats;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.skillengine.SkillEngine;
import net.fretux.skillengine.capability.PlayerSkillData;
import net.fretux.skillengine.capability.SkillEngineCapabilities;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = SkillEngine.MODID) 
public class AscendCompatHandler {

    private static final int SKILL_POINTS_PER_ASCEND_LEVEL = 5;
    
    private static final Map<UUID, Integer> LAST_ASCEND_LEVEL = new ConcurrentHashMap<>();
    
    private static void initAscendLevel(Player player) {
        if (player.level().isClientSide) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(ascendStats -> {
            int currentLevel = ascendStats.getAscendLevel();
            LAST_ASCEND_LEVEL.put(player.getUUID(), currentLevel);
        });
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        initAscendLevel(event.getEntity());
    }
    
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity().level().isClientSide) return;
        Player newPlayer = event.getEntity();
        Player original = event.getOriginal();
        int last = LAST_ASCEND_LEVEL.getOrDefault(original.getUUID(), 1);
        LAST_ASCEND_LEVEL.put(newPlayer.getUUID(), last);
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        initAscendLevel(event.getEntity());
    }
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;

        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(ascendStats -> {
            int currentLevel = ascendStats.getAscendLevel();
            int lastLevel = LAST_ASCEND_LEVEL.getOrDefault(player.getUUID(), currentLevel);
            if (currentLevel > lastLevel) {
                int levelsGained = currentLevel - lastLevel;
                int pointsToGive = levelsGained * SKILL_POINTS_PER_ASCEND_LEVEL;
                player.getCapability(SkillEngineCapabilities.PLAYER_SKILLS).ifPresent(skillData -> {
                    skillData.addSkillPoints(pointsToGive);
                });
            }
            LAST_ASCEND_LEVEL.put(player.getUUID(), currentLevel);
        });
    }
}
package net.fretux.skillengine.network;

import net.fretux.ascend.player.PlayerStats;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.skillengine.SkillEngine;
import net.fretux.skillengine.capability.PlayerSkillData;
import net.fretux.skillengine.capability.SkillEngineCapabilities;
import net.fretux.skillengine.skilltree.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class ServerboundUnlockNodePacket {
    private final ResourceLocation nodeId;

    public ServerboundUnlockNodePacket(ResourceLocation nodeId) {
        this.nodeId = nodeId;
    }

    public static void encode(ServerboundUnlockNodePacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.nodeId);
    }

    public static ServerboundUnlockNodePacket decode(FriendlyByteBuf buf) {
        return new ServerboundUnlockNodePacket(buf.readResourceLocation());
    }

    public static void handle(ServerboundUnlockNodePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            SkillNode node = SkillNodeRegistry.get(msg.nodeId);
            AbilityNode ability = AbilityNodeRegistry.get(msg.nodeId);
            player.getCapability(SkillEngineCapabilities.PLAYER_SKILLS).ifPresent(data -> {
                if (node != null) {
                    if (canUnlockSkillNode(player, data, node)) {
                        data.unlockNode(node);
                        PacketHandler.syncSkillsTo(player);
                        PacketHandler.CHANNEL.send(
                                PacketDistributor.PLAYER.with(() -> player),
                                new ClientboundNodeUnlockedPacket(node.getId(), data.getSkillPoints())
                        );
                    } else {
                        SkillEngine.LOGGER.info("[SKILLENGINE] Rejecting unlock for {}", node.getId());
                    }
                    return;
                }
                if (ability != null) {
                    if (canUnlockAbility(player, data, ability)) {
                        data.unlockAbility(ability);
                        PacketHandler.syncSkillsTo(player);
                        PacketHandler.CHANNEL.send(
                                PacketDistributor.PLAYER.with(() -> player),
                                new ClientboundNodeUnlockedPacket(ability.getId(), data.getSkillPoints())
                        );
                    } else {
                        SkillEngine.LOGGER.info("[SKILLENGINE] Rejecting ability unlock for {}", ability.getId());
                    }
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

    private static boolean canUnlockSkillNode(ServerPlayer player, PlayerSkillData data, SkillNode node) {
        if (data.isUnlocked(node.getId())) {
            SkillEngine.LOGGER.debug("cannot unlock {}: already unlocked", node.getId());
            return false;
        }
        if (data.getSkillPoints() < node.getCost()) {
            SkillEngine.LOGGER.debug("cannot unlock {}: not enough points (have {}, need {})",
                    node.getId(), data.getSkillPoints(), node.getCost());
            return false;
        }
        for (ResourceLocation ex : node.getExclusiveWith()) {
            if (data.isUnlocked(ex)) {
                SkillEngine.LOGGER.debug("cannot unlock {}: mutually exclusive with {} which is already unlocked",
                        node.getId(), ex);
                return false;
            }
        }
        for (ResourceLocation parent : node.getLinks()) {
            if (!data.isUnlocked(parent)) {
                SkillEngine.LOGGER.debug("cannot unlock {}: parent {} is not unlocked",
                        node.getId(), parent);
                return false;
            }
        }
        PlayerStats stats = player.getCapability(PlayerStatsProvider.PLAYER_STATS).orElse(null);
        if (stats == null) {
            SkillEngine.LOGGER.debug("cannot unlock {}: no PlayerStats capability", node.getId());
            return false;
        }
        for (var e : node.getPrereqAttributes().entrySet()) {
            int current = stats.getAttributeLevel(e.getKey());
            if (current < e.getValue()) {
                SkillEngine.LOGGER.debug("cannot unlock {}: attribute {} is {}/{}",
                        node.getId(), e.getKey(), current, e.getValue());
                return false;
            }
        }
        return true;
    }


    private static boolean canUnlockAbility(ServerPlayer player, PlayerSkillData data, AbilityNode ability) {
        if (data.isAbilityUnlocked(ability.getId())) return false;
        for (ResourceLocation parent : ability.getLinks()) {
            boolean ok = data.isUnlocked(parent) || data.isAbilityUnlocked(parent);
            if (!ok) return false;
        }
        return true;
    }
}
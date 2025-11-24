package net.fretux.skillengine.capability;

import net.fretux.skillengine.SkillEngine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SkillEngine.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SkillEngineCapabilities {

    public static final Capability<PlayerSkillData> PLAYER_SKILLS =
            CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            SkillEngine.LOGGER.info("Attaching PlayerSkillData capability");
            event.addCapability(
                    new ResourceLocation(SkillEngine.MODID, "player_skill_data"),
                    new PlayerSkillProvider()
            );
        }
    }
}
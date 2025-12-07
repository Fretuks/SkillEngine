package net.fretux.skillengine.client;

import net.fretux.skillengine.skilltree.AbilityNode;
import net.fretux.skillengine.skilltree.AbilityNodeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AbilityHudOverlay {

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        int xStart = event.getGuiGraphics().guiWidth() - 80;
        int yStart = 10;
        for (int slot = 1; slot <= 3; slot++) {
            var abilityId = SkilltreeClientState.getAbilityInSlot(slot);
            if (abilityId == null) continue;
            AbilityNode ability = AbilityNodeRegistry.get(abilityId);
            if (ability == null) continue;
            int cd = SkilltreeClientState.getClientCooldown(slot);
            ResourceLocation icon = ability.getIcon();
            GuiGraphics gfx = event.getGuiGraphics();
            gfx.blit(icon, xStart, yStart, 0, 0, 16, 16, 16, 16);
            if (cd > 0) {
                String text = String.valueOf(cd / 20);
                gfx.drawString(mc.font, text, xStart + 20, yStart + 4, 0xFF5555);
            }
            gfx.drawString(mc.font, "Ability " + slot, xStart + 40, yStart + 4, 0xFFFFFF);
            yStart += 20;
        }
    }
}

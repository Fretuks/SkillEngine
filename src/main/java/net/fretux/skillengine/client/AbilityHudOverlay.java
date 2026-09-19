package net.fretux.skillengine.client;

import net.fretux.skillengine.skilltree.AbilityNode;
import net.fretux.skillengine.skilltree.AbilityNodeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderGuiEvent;

public class AbilityHudOverlay {
    public static void render(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        int xStart = event.getGuiGraphics().guiWidth() - 124;
        int yStart = 10;
        int slots = SkilltreeClientState.getAbilitySlots().length;
        for (int slot = 1; slot <= slots; slot++) {
            var abilityId = SkilltreeClientState.getAbilityInSlot(slot);
            if (abilityId == null) continue;
            AbilityNode ability = AbilityNodeRegistry.get(abilityId);
            if (ability == null) continue;
            int cd = SkilltreeClientState.getClientCooldown(slot);
            ResourceLocation icon = ability.getIcon();
            GuiGraphics gfx = event.getGuiGraphics();
            gfx.fill(xStart, yStart, xStart + 112, yStart + 26, SkillUi.BACKGROUND);
            gfx.fill(xStart, yStart, xStart + 2, yStart + 26, cd > 0 ? SkillUi.BORDER : SkillUi.ACCENT);
            if (icon != null) {
                gfx.blit(icon, xStart + 7, yStart + 5, 0, 0, 16, 16, 16, 16);
            }
            if (cd > 0) {
                String text = String.valueOf((cd + 19) / 20) + "s";
                gfx.drawString(mc.font, text, xStart + 84, yStart + 9, SkillUi.ACCENT);
            }
            gfx.drawString(mc.font, "Ability " + slot, xStart + 30, yStart + 9, SkillUi.TEXT);
            yStart += 30;
        }
    }
}

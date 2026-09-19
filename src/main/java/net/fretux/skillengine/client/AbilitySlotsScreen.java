package net.fretux.skillengine.client;

import net.fretux.skillengine.skilltree.AbilityNode;
import net.fretux.skillengine.skilltree.AbilityNodeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class AbilitySlotsScreen extends Screen {
    private static final int PANEL_WIDTH = 280;
    private static final int MIN_PANEL_HEIGHT = 204;
    private static final int SLOT_TEXT_RIGHT_PADDING = 8;
    private static final int REBIND_BUTTON_WIDTH = 62;
    public AbilitySlotsScreen() {
        super(Component.literal("Abilities"));
    }

    @Override
    protected void init() {
        int x = (width - PANEL_WIDTH) / 2;
        int panelHeight = panelHeight();
        int y = (height - panelHeight) / 2;
        int rowY = y + 52;
        int buttonH = 28;
        int buttonX = x + PANEL_WIDTH - REBIND_BUTTON_WIDTH - 16;
        int slots = SkilltreeClientState.getAbilitySlots().length;
        for (int slot = 1; slot <= slots; slot++) {
            int currentSlot = slot;
            addRenderableWidget(SkillUi.button(Component.literal("Rebind"), b -> openSelect(currentSlot))
                    .pos(buttonX, rowY + (slot - 1) * 40).size(REBIND_BUTTON_WIDTH, buttonH).build());
        }
        addRenderableWidget(SkillUi.button(Component.literal("Close"), b -> onClose())
                .pos(x + PANEL_WIDTH - 70, y + panelHeight - 28).size(60, 20).build());
    }

    private void openSelect(int slot) {
        Minecraft.getInstance().setScreen(new AbilitySelectScreen(this, slot));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void render(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        int x = (width - PANEL_WIDTH) / 2;
        int panelHeight = panelHeight();
        int y = (height - panelHeight) / 2;
        gfx.fill(0, 0, width, height, 0xAA000000);
        SkillUi.panel(gfx, x, y, PANEL_WIDTH, panelHeight);
        SkillUi.heading(gfx, font, "ABILITY LOADOUT", "Prepare your active skills", x, y);
        int rowY = y + 52;
        int slots = SkilltreeClientState.getAbilitySlots().length;
        for (int slot = 1; slot <= slots; slot++) {
            drawSlotRow(gfx, x + 16, rowY + (slot - 1) * 40, slot, "Slot " + slot);
        }
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    private void drawSlotRow(GuiGraphics gfx, int x, int y, int slot, String label) {
        gfx.fill(x, y - 2, x + PANEL_WIDTH - 32, y + 32, SkillUi.SURFACE);
        gfx.drawString(font, label.toUpperCase(java.util.Locale.ROOT), x + 8, y + 2, SkillUi.MUTED, false);
        ResourceLocation abilityId = SkilltreeClientState.getAbilityInSlot(slot);
        String title = "Empty";
        ResourceLocation icon = null;
        if (abilityId != null) {
            AbilityNode node = AbilityNodeRegistry.get(abilityId);
            if (node != null) {
                title = node.getTitle().getString();
                icon = node.getIcon();
            } else {
                title = abilityId.toString();
            }
        }
        if (icon != null) {
            int iconX = x + 8;
            gfx.blit(icon, iconX, y + 13, 0, 0, 16, 16, 16, 16);
            int textX = iconX + 22;
            gfx.drawString(font, fitTitle(title, textX), textX, y + 17, SkillUi.TEXT);
        } else {
            int textX = x + 8;
            gfx.drawString(font, fitTitle(title, textX), textX, y + 17, SkillUi.MUTED);
        }
    }

    private String fitTitle(String title, int textX) {
        int panelX = (width - PANEL_WIDTH) / 2;
        int buttonX = panelX + PANEL_WIDTH - REBIND_BUTTON_WIDTH - 16;
        int availableWidth = buttonX - SLOT_TEXT_RIGHT_PADDING - textX;
        if (availableWidth <= 0) return "";
        if (font.width(title) <= availableWidth) {
            return title;
        }
        String suffix = "...";
        int suffixWidth = font.width(suffix);
        if (availableWidth <= suffixWidth) {
            return "";
        }
        return font.plainSubstrByWidth(title, availableWidth - suffixWidth) + suffix;
    }

    private int panelHeight() {
        int slots = SkilltreeClientState.getAbilitySlots().length;
        return Math.max(MIN_PANEL_HEIGHT, 92 + slots * 40);
    }
}

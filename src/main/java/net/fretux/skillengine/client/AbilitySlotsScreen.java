package net.fretux.skillengine.client;

import net.fretux.skillengine.skilltree.AbilityNode;
import net.fretux.skillengine.skilltree.AbilityNodeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class AbilitySlotsScreen extends Screen {
    private static final int PANEL_WIDTH = 260;
    private static final int MIN_PANEL_HEIGHT = 180;
    private static final int SLOT_TEXT_RIGHT_PADDING = 8;
    private static final int REBIND_BUTTON_WIDTH = 80;
    public AbilitySlotsScreen() {
        super(Component.literal("Abilities"));
    }

    @Override
    protected void init() {
        int x = (width - PANEL_WIDTH) / 2;
        int panelHeight = panelHeight();
        int y = (height - panelHeight) / 2;
        int rowY = y + 40;
        int buttonH = 20;
        int buttonX = x + PANEL_WIDTH - REBIND_BUTTON_WIDTH - 15;
        int slots = SkilltreeClientState.getAbilitySlots().length;
        for (int slot = 1; slot <= slots; slot++) {
            int currentSlot = slot;
            addRenderableWidget(Button.builder(Component.literal("Rebind"), b -> openSelect(currentSlot))
                    .pos(buttonX, rowY + (slot - 1) * 35).size(REBIND_BUTTON_WIDTH, buttonH).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
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
        gfx.fill(x, y, x + PANEL_WIDTH, y + panelHeight, 0xFF222222);
        gfx.drawCenteredString(font, Component.literal("Ability Slots"), x + PANEL_WIDTH / 2, y + 12, 0xFFFFFF);
        int rowY = y + 40;
        int slots = SkilltreeClientState.getAbilitySlots().length;
        for (int slot = 1; slot <= slots; slot++) {
            drawSlotRow(gfx, x + 15, rowY + (slot - 1) * 35, slot, "Slot " + slot);
        }
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    private void drawSlotRow(GuiGraphics gfx, int x, int y, int slot, String label) {
        gfx.drawString(font, label + ":", x, y + 6, 0xFFFFFF);
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
            gfx.blit(icon, x + 20, y - 2, 0, 0, 20, 20, 20, 20);
            int textX = x + 46;
            gfx.drawString(font, fitTitle(title, textX), textX, y + 6, 0xDDDDDD);
        } else {
            int textX = x + 20;
            gfx.drawString(font, fitTitle(title, textX), textX, y + 6, 0x888888);
        }
    }

    private String fitTitle(String title, int textX) {
        int panelX = (width - PANEL_WIDTH) / 2;
        int buttonX = panelX + PANEL_WIDTH - REBIND_BUTTON_WIDTH - 15;
        int availableWidth = buttonX - SLOT_TEXT_RIGHT_PADDING - textX;
        if (availableWidth <= 0 || font.width(title) <= availableWidth) {
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
        return Math.max(MIN_PANEL_HEIGHT, 80 + slots * 35);
    }
}

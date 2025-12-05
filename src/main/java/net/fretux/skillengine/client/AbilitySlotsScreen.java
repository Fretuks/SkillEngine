package net.fretux.skillengine.client;

import net.fretux.skillengine.skilltree.AbilityNode;
import net.fretux.skillengine.skilltree.AbilityNodeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AbilitySlotsScreen extends Screen {
    private static final int PANEL_WIDTH = 260;
    private static final int PANEL_HEIGHT = 180;
    public AbilitySlotsScreen() {
        super(Component.literal("Abilities"));
    }

    @Override
    protected void init() {
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - PANEL_HEIGHT) / 2;
        int rowY = y + 40;
        int buttonW = 80;
        int buttonH = 20;
        int buttonX = x + PANEL_WIDTH - buttonW - 15;

        // Y slot = 1
        addRenderableWidget(Button.builder(Component.literal("Rebind"), b -> openSelect(1))
                .pos(buttonX, rowY).size(buttonW, buttonH).build());
        // X slot = 2
        addRenderableWidget(Button.builder(Component.literal("Rebind"), b -> openSelect(2))
                .pos(buttonX, rowY + 35).size(buttonW, buttonH).build());
        // C slot = 3
        addRenderableWidget(Button.builder(Component.literal("Rebind"), b -> openSelect(3))
                .pos(buttonX, rowY + 70).size(buttonW, buttonH).build());

        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .pos(x + PANEL_WIDTH - 70, y + PANEL_HEIGHT - 28).size(60, 20).build());
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
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - PANEL_HEIGHT) / 2;

        gfx.fill(0, 0, width, height, 0xAA000000);
        gfx.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xFF222222);
        gfx.drawCenteredString(font, Component.literal("Ability Slots"), x + PANEL_WIDTH / 2, y + 12, 0xFFFFFF);

        int rowY = y + 40;
        drawSlotRow(gfx, x + 15, rowY, 1, "Y");
        drawSlotRow(gfx, x + 15, rowY + 35, 2, "X");
        drawSlotRow(gfx, x + 15, rowY + 70, 3, "C");

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
            gfx.drawString(font, title, x + 46, y + 6, 0xDDDDDD);
        } else {
            gfx.drawString(font, title, x + 20, y + 6, 0x888888);
        }
    }
}

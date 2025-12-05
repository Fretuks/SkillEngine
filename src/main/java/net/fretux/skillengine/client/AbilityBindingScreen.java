package net.fretux.skillengine.client;

import net.fretux.skillengine.SkillEngine;
import net.fretux.skillengine.network.PacketHandler;
import net.fretux.skillengine.network.ServerboundBindAbilityPacket;
import net.fretux.skillengine.skilltree.AbilityNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AbilityBindingScreen extends Screen {

    private static final int PANEL_WIDTH = 220;
    private static final int PANEL_HEIGHT = 160;

    private final AbilityNode ability;

    public AbilityBindingScreen(AbilityNode ability) {
        super(Component.literal("Bind Ability"));
        this.ability = ability;
    }

    @Override
    protected void init() {
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - PANEL_HEIGHT) / 2;
        int buttonX = x + 20;
        int buttonY = y + 80;
        int boundSlot = SkilltreeClientState.getSlotOfAbility(ability.getId());
        Button yBtn = addRenderableWidget(Button.builder(Component.literal("Bind to Y"),
                b -> bind(1)).pos(buttonX, buttonY).size(80, 20).build());
        if (boundSlot == 1) {
            yBtn.active = false;
            yBtn.setMessage(Component.literal("Bound to Y"));
        }
        Button xBtn = addRenderableWidget(Button.builder(Component.literal("Bind to X"),
                b -> bind(2)).pos(buttonX + 90, buttonY).size(80, 20).build());
        if (boundSlot == 2) {
            xBtn.active = false;
            xBtn.setMessage(Component.literal("Bound to X"));
        }
        Button cBtn = addRenderableWidget(Button.builder(Component.literal("Bind to C"),
                b -> bind(3)).pos(buttonX, buttonY + 30).size(80, 20).build());
        if (boundSlot == 3) {
            cBtn.active = false;
            cBtn.setMessage(Component.literal("Bound to C"));
        }
        addRenderableWidget(Button.builder(Component.literal("Cancel"),
                b -> onClose()).pos(buttonX + 90, buttonY + 30).size(80, 20).build());
    }

    private void bind(int slot) {
        SkilltreeClientState.bindAbilityLocal(slot, ability.getId());
        SkillEngine.LOGGER.debug("[CLIENT] Binding ability {} to slot {} (optimistic)", ability.getId(), slot);
        PacketHandler.CHANNEL.sendToServer(new ServerboundBindAbilityPacket(ability.getId(), slot));
        onClose();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (key == 256) {
            onClose();
            return true;
        }
        return super.keyPressed(key, scancode, mods);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - PANEL_HEIGHT) / 2;
        gfx.fill(0, 0, width, height, 0xAA000000);
        gfx.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xFF222222);
        ResourceLocation icon = ability.getIcon();
        if (icon != null) {
            gfx.blit(icon, x + PANEL_WIDTH / 2 - 10, y + 10, 0, 0, 20, 20, 20, 20);
        }
        gfx.drawCenteredString(font, ability.getTitle(), x + PANEL_WIDTH / 2, y + 35, 0xFFFFFF);
        if (ability.getDescription() != null) {
            gfx.drawWordWrap(font, ability.getDescription(), x + 10, y + 50, PANEL_WIDTH - 20, 0xDDDDDD);
        }

        super.render(gfx, mouseX, mouseY, partialTick);
    }
}
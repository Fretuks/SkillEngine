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
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AbilityBindingScreen extends Screen {

    private static final int PANEL_WIDTH = 280;
    private static final int PANEL_HEIGHT = 180;
    private static final int PANEL_PADDING = 10;
    private static final int SLOT_BUTTON_WIDTH = 76;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 8;

    private final AbilityNode ability;

    public AbilityBindingScreen(AbilityNode ability) {
        super(Component.literal("Bind Ability"));
        this.ability = ability;
    }

    @Override
    protected void init() {
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - PANEL_HEIGHT) / 2;
        int buttonRowWidth = SLOT_BUTTON_WIDTH * 3 + BUTTON_GAP * 2;
        int buttonX = x + (PANEL_WIDTH - buttonRowWidth) / 2;
        int buttonY = y + PANEL_HEIGHT - 54;
        int boundSlot = SkilltreeClientState.getSlotOfAbility(ability.getId());
        Button yBtn = addRenderableWidget(Button.builder(Component.literal("Slot 1"),
                b -> bind(1)).pos(buttonX, buttonY).size(SLOT_BUTTON_WIDTH, BUTTON_HEIGHT).build());
        if (boundSlot == 1) {
            yBtn.active = false;
            yBtn.setMessage(Component.literal("Bound 1"));
        }
        Button xBtn = addRenderableWidget(Button.builder(Component.literal("Slot 2"),
                b -> bind(2)).pos(buttonX + SLOT_BUTTON_WIDTH + BUTTON_GAP, buttonY).size(SLOT_BUTTON_WIDTH, BUTTON_HEIGHT).build());
        if (boundSlot == 2) {
            xBtn.active = false;
            xBtn.setMessage(Component.literal("Bound 2"));
        }
        Button cBtn = addRenderableWidget(Button.builder(Component.literal("Slot 3"),
                b -> bind(3)).pos(buttonX + (SLOT_BUTTON_WIDTH + BUTTON_GAP) * 2, buttonY).size(SLOT_BUTTON_WIDTH, BUTTON_HEIGHT).build());
        if (boundSlot == 3) {
            cBtn.active = false;
            cBtn.setMessage(Component.literal("Bound 3"));
        }
        addRenderableWidget(Button.builder(Component.literal("Cancel"),
                b -> onClose()).pos(x + PANEL_WIDTH / 2 - 40, y + PANEL_HEIGHT - 28).size(80, BUTTON_HEIGHT).build());
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
    public void render(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
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
            drawDescription(gfx, x + PANEL_PADDING, y + 50, y + PANEL_HEIGHT - 60);
        }

        super.render(gfx, mouseX, mouseY, partialTick);
    }

    private void drawDescription(GuiGraphics gfx, int x, int y, int bottomY) {
        int width = PANEL_WIDTH - PANEL_PADDING * 2;
        List<FormattedText> lines = font.getSplitter().splitLines(
                ability.getDescription(),
                width,
                ability.getDescription().getStyle()
        );
        int lineY = y;
        for (FormattedText line : lines) {
            if (lineY + font.lineHeight > bottomY) {
                break;
            }
            gfx.drawString(font, line.getString(), x, lineY, 0xDDDDDD);
            lineY += font.lineHeight;
        }
    }
}

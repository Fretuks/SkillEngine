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
    private static final int PANEL_HEIGHT = 204;
    private static final int PANEL_PADDING = 16;
    private static final int SLOT_BUTTON_WIDTH = 76;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 6;

    private final AbilityNode ability;

    public AbilityBindingScreen(AbilityNode ability) {
        super(Component.literal("Bind Ability"));
        this.ability = ability;
    }

    @Override
    protected void init() {
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - PANEL_HEIGHT) / 2;
        int slotCount = SkilltreeClientState.getAbilitySlots().length;
        int visibleSlots = Math.min(slotCount, 3);
        int buttonRowWidth = SLOT_BUTTON_WIDTH * visibleSlots + BUTTON_GAP * Math.max(0, visibleSlots - 1);
        int buttonX = x + (PANEL_WIDTH - buttonRowWidth) / 2;
        int buttonY = y + PANEL_HEIGHT - 54;
        int boundSlot = SkilltreeClientState.getSlotOfAbility(ability.getId());
        for (int slot = 1; slot <= visibleSlots; slot++) {
            int currentSlot = slot;
            Button button = addRenderableWidget(SkillUi.button(Component.literal("Slot " + slot),
                    b -> bind(currentSlot))
                    .pos(buttonX + (SLOT_BUTTON_WIDTH + BUTTON_GAP) * (slot - 1), buttonY)
                    .size(SLOT_BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
            if (boundSlot == slot) {
                button.active = false;
                button.setMessage(Component.literal("Bound " + slot));
            }
        }
        addRenderableWidget(SkillUi.button(Component.literal("Cancel"),
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
        SkillUi.panel(gfx, x, y, PANEL_WIDTH, PANEL_HEIGHT);
        gfx.drawString(font, "ASSIGN ABILITY", x + 16, y + 14, SkillUi.ACCENT, false);
        ResourceLocation icon = ability.getIcon();
        if (icon != null) {
            gfx.blit(icon, x + 16, y + 34, 0, 0, 20, 20, 20, 20);
        }
        gfx.drawString(font, font.plainSubstrByWidth(ability.getTitle().getString(), PANEL_WIDTH - 64), x + 44, y + 40, SkillUi.TEXT, false);
        if (ability.getDescription() != null) {
            drawDescription(gfx, x + PANEL_PADDING, y + 66, y + PANEL_HEIGHT - 60);
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
            gfx.drawString(font, line.getString(), x, lineY, SkillUi.MUTED);
            lineY += font.lineHeight + 3;
        }
    }
}

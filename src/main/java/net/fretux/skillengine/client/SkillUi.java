package net.fretux.skillengine.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Shared charcoal and iron surfaces for the skill interface. */
final class SkillUi {
    static final int BACKGROUND = 0xFF181A1D;
    static final int SURFACE = 0xFF22252A;
    static final int HEADER = 0xFF292D32;
    static final int BORDER = 0xFF555B63;
    static final int ACCENT = 0xFFD0A447;
    static final int TEXT = 0xFFE7E7E7;
    static final int MUTED = 0xFFA9ADB2;

    private SkillUi() {}

    static void panel(GuiGraphics gfx, int x, int y, int width, int height) {
        gfx.fill(x + 3, y + 4, x + width + 3, y + height + 4, 0x66000000);
        gfx.fill(x - 1, y - 1, x + width + 1, y + height + 1, BORDER);
        gfx.fill(x, y, x + width, y + height, BACKGROUND);
    }

    static void heading(GuiGraphics gfx, Font font, String title, String subtitle, int x, int y) {
        gfx.drawString(font, title, x + 16, y + 14, TEXT, false);
        gfx.drawString(font, subtitle, x + 16, y + 29, MUTED, false);
    }

    static Builder button(Component label, Button.OnPress action) {
        return new Builder(label, action);
    }

    static final class Builder {
        private final Component label;
        private final Button.OnPress action;
        private int x, y, width = 80, height = 20;

        Builder(Component label, Button.OnPress action) {
            this.label = label;
            this.action = action;
        }

        Builder pos(int x, int y) { this.x = x; this.y = y; return this; }
        Builder size(int width, int height) { this.width = width; this.height = height; return this; }
        Button build() { return new IronButton(x, y, width, height, label, action); }
    }

    private static final class IronButton extends Button {
        IronButton(int x, int y, int width, int height, Component label, OnPress action) {
            super(x, y, width, height, label, action, DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            boolean focus = active && isHoveredOrFocused();
            int x = getX(), y = getY();
            gfx.fill(x, y, x + width, y + height, focus ? MUTED : BORDER);
            gfx.fill(x + 1, y + 1, x + width - 1, y + height - 1,
                    active ? (focus ? HEADER : SURFACE) : BACKGROUND);
            renderScrollingString(gfx, Minecraft.getInstance().font, 6, active ? TEXT : MUTED);
        }
    }
}

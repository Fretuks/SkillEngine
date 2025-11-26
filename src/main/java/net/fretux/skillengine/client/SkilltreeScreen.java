package net.fretux.skillengine.client;

import com.mojang.math.Axis;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.skillengine.network.PacketHandler;
import net.fretux.skillengine.network.ServerboundUnlockNodePacket;
import net.fretux.skillengine.skilltree.SkillNode;
import net.fretux.skillengine.skilltree.SkillNodeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class SkilltreeScreen extends Screen {

    private float zoom = 1.0f;
    private float minZoom = 0.5f;
    private float maxZoom = 2.0f;

    private double panX = 0;
    private double panY = 0;

    private boolean dragging = false;
    private double lastMouseX, lastMouseY;

    private SkillNode hoveredNode = null;
    private SkillNode selectedNode = null;

    public SkilltreeScreen() {
        super(Component.literal("Ascend Skilltree"));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        zoom += delta * 0.1f;
        zoom = Mth.clamp(zoom, minZoom, maxZoom);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (selectedNode != null) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (button == 0) {
            SkillNode clicked = findNodeAt(mouseX, mouseY);
            if (clicked != null) {
                if (SkilltreeClientState.isUnlocked(clicked.getId())) {
                    return true;
                }
                selectedNode = clicked;
                rebuildOverlayButtons();
                return true;
            }
            dragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (selectedNode != null) {
            return super.mouseDragged(mouseX, mouseY, button, dx, dy);
        }
        if (dragging && button == 0) {
            panX += dx;
            panY += dy;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx);
        int remaining = SkilltreeClientState.getCurrentSkillPoints();
        Component pointsText = Component.literal(remaining + " skillpoints remaining")
                .withStyle(ChatFormatting.YELLOW);
        gfx.drawString(font, pointsText, 10, 10, 0xFFFFFF);

        hoveredNode = findNodeAt(mouseX, mouseY);
        drawGraph(gfx);
        if (hoveredNode != null && selectedNode == null) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(hoveredNode.getTitle());
            String desc = hoveredNode.getDescription().getString();
            List<FormattedText> wrapped = font.getSplitter().splitLines(
                    desc,
                    220,
                    hoveredNode.getDescription().getStyle()
            );
            for (FormattedText ft : wrapped) {
                tooltip.add(Component.literal(ft.getString()).withStyle(ChatFormatting.GRAY));
            }
            tooltip.add(Component.literal("Cost: " + hoveredNode.getCost())
                    .withStyle(ChatFormatting.DARK_AQUA));
            if (!hoveredNode.getPrereqAttributes().isEmpty()) {
                tooltip.add(Component.literal("Requirements:")
                        .withStyle(ChatFormatting.GOLD));
                Minecraft.getInstance().player.getCapability(PlayerStatsProvider.PLAYER_STATS)
                        .ifPresent(stats -> {
                            hoveredNode.getPrereqAttributes().forEach((attr, required) -> {
                                int current = stats.getAttributeLevel(attr);
                                boolean ok = current >= required;
                                tooltip.add(
                                        Component.literal(" - " + attr + ": " + current + "/" + required)
                                                .withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.RED)
                                );
                            });
                        });
            }
            gfx.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
        if (selectedNode != null) {
            renderOverlay(gfx, selectedNode);
        }
        super.render(gfx, mouseX, mouseY, partialTicks);
    }

    private void drawGraph(GuiGraphics gfx) {
        for (SkillNode node : SkillNodeRegistry.all()) {
            int[] p1 = worldToScreen(node.getX(), node.getY());
            for (ResourceLocation neighborId : node.getLinks()) {
                SkillNode neighbor = SkillNodeRegistry.get(neighborId);
                if (neighbor == null) continue;
                int[] p2 = worldToScreen(neighbor.getX(), neighbor.getY());
                boolean bothUnlocked =
                        SkilltreeClientState.isUnlocked(node.getId()) &&
                                SkilltreeClientState.isUnlocked(neighbor.getId());
                int linkColor = bothUnlocked ? 0xFF88FF88 : 0xFF666666;
                drawLine(gfx, p1[0], p1[1], p2[0], p2[1], linkColor);

            }
        }
        for (SkillNode node : SkillNodeRegistry.all()) {
            int[] pos = worldToScreen(node.getX(), node.getY());
            int r = 12;
            boolean unlocked = SkilltreeClientState.isUnlocked(node.getId());
            int color = unlocked ? 0xFF44CC44 : 0xFF444444;
            if (node == hoveredNode) color = 0xFF777777;
            if (node == selectedNode) color = 0xFF999933;
            gfx.fill(pos[0] - r, pos[1] - r, pos[0] + r, pos[1] + r, color);
            ResourceLocation icon = node.getIcons();
            if (icon != null) {
                int size = 16;
                gfx.blit(icon,
                        pos[0] - size / 2,
                        pos[1] - size / 2,
                        0, 0,
                        size, size,
                        size, size);
            }
        }
    }

    private int[] worldToScreen(float wx, float wy) {
        double sx = width / 2.0 + panX + wx * zoom;
        double sy = height / 2.0 + panY - wy * zoom;
        return new int[]{(int) sx, (int) sy};
    }

    private SkillNode findNodeAt(double mouseX, double mouseY) {
        for (SkillNode node : SkillNodeRegistry.all()) {
            int[] pos = worldToScreen(node.getX(), node.getY());
            int r = 12;
            if (mouseX >= pos[0] - r && mouseX <= pos[0] + r &&
                    mouseY >= pos[1] - r && mouseY <= pos[1] + r) {
                return node;
            }
        }
        return null;
    }

    private void renderOverlay(GuiGraphics gfx, SkillNode node) {
        int w = 240;
        int h = 180;
        int x = (width - w) / 2;
        int y = (height - h) / 2;
        gfx.fill(0, 0, width, height, 0x88000000);
        gfx.fill(x, y, x + w, y + h, 0xFF222222);
        gfx.drawCenteredString(font, node.getTitle(), x + w / 2, y + 10, 0xFFFFFF);
        gfx.drawWordWrap(font, node.getDescription(), x + 10, y + 30, w - 20, 0xDDDDDD);
        final int[] textY = {y + 95};
        gfx.drawString(font,
                Component.literal("Cost: " + node.getCost() + " skill points")
                        .withStyle(ChatFormatting.AQUA),
                x + 10, textY[0],
                0xAAAAAA);
        textY[0] += 15;
        if (!node.getPrereqAttributes().isEmpty()) {
            gfx.drawString(font,
                    Component.literal("Requirements:")
                            .withStyle(ChatFormatting.GOLD),
                    x + 10, textY[0],
                    0xFFFFFF);
            textY[0] += 12;
            Minecraft.getInstance().player.getCapability(PlayerStatsProvider.PLAYER_STATS)
                    .ifPresent(stats -> {
                        for (var entry : node.getPrereqAttributes().entrySet()) {
                            String attr = entry.getKey();
                            int required = entry.getValue();
                            int current = stats.getAttributeLevel(attr);
                            boolean ok = current >= required;
                            Component line = Component.literal(
                                    " - " + attr + ": " + current + "/" + required
                            ).withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.RED);
                            gfx.drawString(font, line, x + 10, textY[0], 0xFFFFFF);
                            textY[0] += 12;
                        }
                    });
        }
    }

    private void rebuildOverlayButtons() {
        clearWidgets();
        if (selectedNode == null) return;
        int w = 240;
        int h = 160;
        int x = (width - w) / 2;
        int y = (height - h) / 2;
        boolean alreadyUnlocked = SkilltreeClientState.isUnlocked(selectedNode.getId());
        if (!alreadyUnlocked) {
            addRenderableWidget(Button.builder(Component.literal("Unlock"), btn -> {
                PacketHandler.CHANNEL.sendToServer(
                        new ServerboundUnlockNodePacket(selectedNode.getId())
                );
                selectedNode = null;
                clearWidgets();
            }).pos(x + w / 2 - 40, y + h - 30).size(80, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Close"), btn -> {
            selectedNode = null;
            clearWidgets();
        }).pos(x + w - 60, y + 10).size(50, 20).build());
    }

    private void drawLine(GuiGraphics gfx, int x1, int y1, int x2, int y2, int color) {
        float thickness = 2.0f;
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        float angle = (float) Math.atan2(dy, dx);
        gfx.pose().pushPose();
        gfx.pose().translate(x1, y1, 0);
        gfx.pose().mulPose(Axis.ZP.rotation(angle));
        gfx.fill(0,
                (int) (-thickness / 2f),
                (int) length,
                (int) (thickness / 2f),
                color);
        gfx.pose().popPose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
package net.fretux.skillengine.client;

import com.mojang.math.Axis;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.skillengine.network.PacketHandler;
import net.fretux.skillengine.network.ServerboundUnlockNodePacket;
import net.fretux.skillengine.skilltree.AbilityNode;
import net.fretux.skillengine.skilltree.AbilityNodeRegistry;
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
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SkilltreeScreen extends Screen {
    private static final int NODE_RADIUS = 12;
    private static final int ABILITY_RADIUS = 12;
    private static final int OVERLAY_WIDTH = 240;
    private static final int OVERLAY_HEIGHT = 180;
    private static final int OVERLAY_CONTENT_HEIGHT = 160;
    private static final float ZOOM_STEP = 0.1f;
    private static final float LINE_THICKNESS = 2.0f;
    private float zoom = 1.0f;
    private double panX = 0;
    private double panY = 0;
    private boolean dragging = false;
    private SkillNode hoveredNode = null;
    private SkillNode selectedNode = null;
    private AbilityNode hoveredAbility = null;
    private AbilityNode selectedAbility = null;

    public SkilltreeScreen() {
        super(Component.literal("Ascend Skilltree"));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        zoom += (float) (delta * ZOOM_STEP);
        float minZoom = 0.5f;
        float maxZoom = 2.0f;
        zoom = Mth.clamp(zoom, minZoom, maxZoom);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (selectedNode != null || selectedAbility != null) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (button == 0) {
            SkillNode clickedNode = findNodeAt(mouseX, mouseY);
            if (clickedNode != null) {
                if (isLockedByExclusivity(clickedNode)) {
                    assert Minecraft.getInstance().player != null;
                    Minecraft.getInstance().player.playSound(
                            net.minecraft.sounds.SoundEvents.VILLAGER_NO, 1f, 1f
                    );
                    return true;
                }
                if (SkilltreeClientState.isUnlocked(clickedNode.getId())) {
                    return true;
                }
                selectedNode = clickedNode;
                rebuildOverlayButtons();
                return true;
            }
            AbilityNode clickedAbility = findAbilityAt(mouseX, mouseY);
            if (clickedAbility != null) {
                if (SkilltreeClientState.isAbilityUnlocked(clickedAbility.getId())) {
                    Minecraft.getInstance().setScreen(new AbilityBindingScreen(clickedAbility));
                } else {
                    selectedAbility = clickedAbility;
                    rebuildAbilityOverlayButtons();
                }
                return true;
            }
            dragging = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (selectedNode != null || selectedAbility != null) {
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
    public void render(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx);
        renderSkillPointHeader(gfx);
        hoveredNode = findNodeAt(mouseX, mouseY);
        hoveredAbility = findAbilityAt(mouseX, mouseY);
        drawGraph(gfx);
        if (hoveredNode != null && selectedNode == null && selectedAbility == null) {
            renderNodeTooltip(gfx, hoveredNode, mouseX, mouseY);
        }
        if (selectedNode != null) {
            renderNodeOverlay(gfx, selectedNode);
        } else if (selectedAbility != null) {
            renderAbilityOverlay(gfx, selectedAbility);
        }
        super.render(gfx, mouseX, mouseY, partialTicks);
    }

    private void renderSkillPointHeader(GuiGraphics gfx) {
        int remaining = SkilltreeClientState.getCurrentSkillPoints();
        Component pointsText = Component.literal("Skillpoints Remaining: " + remaining)
                .withStyle(ChatFormatting.YELLOW);
        gfx.drawString(font, pointsText, 10, 10, 0xFFFFFF);
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
        for (AbilityNode ability : AbilityNodeRegistry.all()) {
            int[] p1 = worldToScreen(ability.getX(), ability.getY());
            for (ResourceLocation link : ability.getLinks()) {
                SkillNode node = SkillNodeRegistry.get(link);
                AbilityNode ability2 = AbilityNodeRegistry.get(link);
                if (node != null) {
                    int[] p2 = worldToScreen(node.getX(), node.getY());
                    drawLine(gfx, p1[0], p1[1], p2[0], p2[1], 0xFF6666AA);
                } else if (ability2 != null) {
                    int[] p2 = worldToScreen(ability2.getX(), ability2.getY());
                    drawLine(gfx, p1[0], p1[1], p2[0], p2[1], 0xFF6666AA);
                }
            }
        }
        for (AbilityNode ability : AbilityNodeRegistry.all()) {
            int[] pos = worldToScreen(ability.getX(), ability.getY());
            boolean unlocked = SkilltreeClientState.isAbilityUnlocked(ability.getId());
            int color;
            if (ability == selectedAbility) {
                color = 0xFFAA8833;
            } else if (ability == hoveredAbility) {
                color = 0xFF8888AA;
            } else if (unlocked) {
                color = 0xFF3399FF;
            } else {
                color = 0xFF223355;
            }
            gfx.fill(
                    pos[0] - ABILITY_RADIUS,
                    pos[1] - ABILITY_RADIUS,
                    pos[0] + ABILITY_RADIUS,
                    pos[1] + ABILITY_RADIUS,
                    color
            );
            ResourceLocation icon = ability.getIcon();
            if (icon != null) {
                gfx.blit(icon, pos[0] - 8, pos[1] - 8, 0, 0, 16, 16, 16, 16);
            }
        }
        for (SkillNode node : SkillNodeRegistry.all()) {
            int[] pos = worldToScreen(node.getX(), node.getY());
            int color = getNodeColor(node);
            gfx.fill(
                    pos[0] - NODE_RADIUS,
                    pos[1] - NODE_RADIUS,
                    pos[0] + NODE_RADIUS,
                    pos[1] + NODE_RADIUS,
                    color
            );
            ResourceLocation icon = node.getIcons();
            if (icon != null) {
                int size = 16;
                gfx.blit(
                        icon,
                        pos[0] - size / 2,
                        pos[1] - size / 2,
                        0, 0,
                        size, size,
                        size, size
                );
            }
        }
    }

    private void renderNodeTooltip(GuiGraphics gfx, SkillNode node, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(node.getTitle());
        String desc = node.getDescription().getString();
        List<FormattedText> wrapped = font.getSplitter().splitLines(
                desc,
                220,
                node.getDescription().getStyle()
        );
        for (FormattedText ft : wrapped) {
            tooltip.add(Component.literal(ft.getString()).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.literal("Cost: " + node.getCost())
                .withStyle(ChatFormatting.DARK_AQUA));
        if (!node.getPrereqAttributes().isEmpty()) {
            tooltip.add(Component.literal("Requirements:")
                    .withStyle(ChatFormatting.GOLD));
            assert Minecraft.getInstance().player != null;
            Minecraft.getInstance().player.getCapability(PlayerStatsProvider.PLAYER_STATS)
                    .ifPresent(stats -> node.getPrereqAttributes().forEach((attr, required) -> {
                        int current = stats.getAttributeLevel(attr);
                        boolean ok = current >= required;
                        tooltip.add(
                                Component.literal(" - " + attr + ": " + current + "/" + required)
                                        .withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.RED)
                        );
                    }));
        }
        if (!hoveredNode.getExclusiveWith().isEmpty()) {
            tooltip.add(Component.literal("Mutually Exclusive with:")
                    .withStyle(ChatFormatting.RED));
            for (ResourceLocation ex : hoveredNode.getExclusiveWith()) {
                SkillNode other = SkillNodeRegistry.get(ex);
                Component name = other != null ? other.getTitle() : Component.literal(ex.toString());
                tooltip.add(Component.literal(" - ").append(name.copy())
                        .withStyle(ChatFormatting.RED));
            }
        }
        gfx.renderComponentTooltip(font, tooltip, mouseX, mouseY);
    }

    private void renderNodeOverlay(GuiGraphics gfx, SkillNode node) {
        if (isLockedByExclusivity(node)) {
            return;
        }
        int x = (width - OVERLAY_WIDTH) / 2;
        int y = (height - OVERLAY_HEIGHT) / 2;
        gfx.fill(0, 0, width, height, 0x88000000);
        gfx.fill(x, y, x + OVERLAY_WIDTH, y + OVERLAY_HEIGHT, 0xFF222222);
        gfx.drawCenteredString(font, node.getTitle(), x + OVERLAY_WIDTH / 2, y + 10, 0xFFFFFF);
        gfx.drawWordWrap(font, node.getDescription(), x + 10, y + 30, OVERLAY_WIDTH - 20, 0xDDDDDD);
        final int[] textY = {y + 95};
        gfx.drawString(
                font,
                Component.literal("Cost: " + node.getCost() + " skill points")
                        .withStyle(ChatFormatting.AQUA),
                x + 10,
                textY[0],
                0xAAAAAA
        );
        textY[0] += 15;
        if (!node.getPrereqAttributes().isEmpty()) {
            gfx.drawString(
                    font,
                    Component.literal("Requirements:")
                            .withStyle(ChatFormatting.GOLD),
                    x + 10,
                    textY[0],
                    0xFFFFFF
            );
            textY[0] += 12;
            assert Minecraft.getInstance().player != null;
            Minecraft.getInstance().player.getCapability(PlayerStatsProvider.PLAYER_STATS)
                    .ifPresent(stats -> node.getPrereqAttributes().forEach((attr, required) -> {
                        int current = stats.getAttributeLevel(attr);
                        boolean ok = current >= required;

                        Component line = Component.literal(
                                " - " + attr + ": " + current + "/" + required
                        ).withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.RED);

                        gfx.drawString(font, line, x + 10, textY[0], 0xFFFFFF);
                        textY[0] += 12;
                    }));
        }
    }

    private void renderAbilityOverlay(GuiGraphics gfx, AbilityNode ability) {
        int x = (width - OVERLAY_WIDTH) / 2;
        int y = (height - OVERLAY_HEIGHT) / 2;
        gfx.fill(0, 0, width, height, 0x88000000);
        gfx.fill(x, y, x + OVERLAY_WIDTH, y + OVERLAY_HEIGHT, 0xFF222222);
        gfx.drawCenteredString(font, ability.getTitle(), x + OVERLAY_WIDTH / 2, y + 10, 0xFFFFFF);
        int textY = y + 30;
        if (ability.getDescription() != null) {
            gfx.drawWordWrap(font, ability.getDescription(), x + 10, textY, OVERLAY_WIDTH - 20, 0xDDDDDD);
            textY += 60;
        }
        gfx.drawString(font,
                Component.literal("Cooldown: " + ability.getCooldown() / 20 + " seconds")
                        .withStyle(ChatFormatting.AQUA),
                x + 10,
                textY,
                0xFFFFFF);
        textY += 15;
        if (!ability.getLinks().isEmpty()) {
            gfx.drawString(font,
                    Component.literal("Requires:")
                            .withStyle(ChatFormatting.GOLD),
                    x + 10,
                    textY,
                    0xFFFFFF);
            textY += 12;
            for (ResourceLocation parent : ability.getLinks()) {
                SkillNode parentNode = SkillNodeRegistry.get(parent);
                AbilityNode parentAbility = AbilityNodeRegistry.get(parent);
                Component name = parentNode != null
                        ? parentNode.getTitle()
                        : parentAbility != null
                        ? parentAbility.getTitle()
                        : Component.literal(parent.toString());
                gfx.drawString(font,
                        Component.literal(" - ").append(name),
                        x + 10,
                        textY,
                        0xFFFFFF);
                textY += 12;
            }
        }
    }

    private void rebuildOverlayButtons() {
        clearWidgets();
        if (selectedNode == null) {
            return;
        }
        int x = (width - OVERLAY_WIDTH) / 2;
        int y = (height - OVERLAY_CONTENT_HEIGHT) / 2;
        boolean alreadyUnlocked = SkilltreeClientState.isUnlocked(selectedNode.getId());
        if (!alreadyUnlocked) {
            addRenderableWidget(Button.builder(Component.literal("Unlock"), btn -> {
                        PacketHandler.CHANNEL.sendToServer(
                                new ServerboundUnlockNodePacket(selectedNode.getId())
                        );
                        selectedNode = null;
                        clearWidgets();
                    }).pos(x + OVERLAY_WIDTH / 2 - 40, y + OVERLAY_CONTENT_HEIGHT - 20)
                    .size(80, 20)
                    .build());
        }
        addRenderableWidget(Button.builder(Component.literal("Close"), btn -> {
                    selectedNode = null;
                    clearWidgets();
                }).pos(x + OVERLAY_WIDTH - 60, y - 5)
                .size(50, 20)
                .build());
    }

    private void rebuildAbilityOverlayButtons() {
        clearWidgets();
        if (selectedAbility == null) {
            return;
        }
        int x = (width - OVERLAY_WIDTH) / 2;
        int y = (height - OVERLAY_CONTENT_HEIGHT) / 2;
        boolean unlocked = SkilltreeClientState.isAbilityUnlocked(selectedAbility.getId());
        if (!unlocked) {
            addRenderableWidget(Button.builder(Component.literal("Unlock"), btn -> {
                        PacketHandler.CHANNEL.sendToServer(
                                new ServerboundUnlockNodePacket(selectedAbility.getId())
                        );
                        selectedAbility = null;
                        clearWidgets();
                    }).pos(x + OVERLAY_WIDTH / 2 - 40, y + OVERLAY_CONTENT_HEIGHT - 20)
                    .size(80, 20)
                    .build());
        }
        addRenderableWidget(Button.builder(Component.literal("Close"), btn -> {
                    selectedAbility = null;
                    clearWidgets();
                }).pos(x + OVERLAY_WIDTH - 60, y - 5)
                .size(50, 20)
                .build());
    }

    private int[] worldToScreen(float wx, float wy) {
        double sx = width / 2.0 + panX + wx * zoom;
        double sy = height / 2.0 + panY - wy * zoom;
        return new int[]{(int) sx, (int) sy};
    }

    private SkillNode findNodeAt(double mouseX, double mouseY) {
        for (SkillNode node : SkillNodeRegistry.all()) {
            int[] pos = worldToScreen(node.getX(), node.getY());
            if (isPointInCircle(mouseX, mouseY, pos[0], pos[1], NODE_RADIUS)) {
                return node;
            }
        }
        return null;
    }

    private AbilityNode findAbilityAt(double mouseX, double mouseY) {
        for (AbilityNode ability : AbilityNodeRegistry.all()) {
            int[] pos = worldToScreen(ability.getX(), ability.getY());
            if (isPointInCircle(mouseX, mouseY, pos[0], pos[1], ABILITY_RADIUS)) {
                return ability;
            }
        }
        return null;
    }

    private boolean isPointInCircle(double mx, double my, int cx, int cy, int radius) {
        return mx >= cx - radius && mx <= cx + radius &&
                my >= cy - radius && my <= cy + radius;
    }

    private boolean isLockedByExclusivity(SkillNode node) {
        return node.getExclusiveWith().stream().anyMatch(SkilltreeClientState::isUnlocked);
    }

    private int getNodeColor(SkillNode node) {
        boolean unlocked = SkilltreeClientState.isUnlocked(node.getId());
        boolean lockedByExclusivity = isLockedByExclusivity(node);
        int color;
        if (lockedByExclusivity) {
            color = 0xFFCC3333;
        } else if (node == selectedNode) {
            color = 0xFF999933;
        } else if (node == hoveredNode) {
            color = 0xFF777777;
        } else if (unlocked) {
            color = 0xFF44CC44;
        } else {
            color = 0xFF444444;
        }
        return color;
    }

    private void drawLine(GuiGraphics gfx, int x1, int y1, int x2, int y2, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        float angle = (float) Math.atan2(dy, dx);
        gfx.pose().pushPose();
        gfx.pose().translate(x1, y1, 0);
        gfx.pose().mulPose(Axis.ZP.rotation(angle));
        gfx.fill(
                0,
                (int) (-LINE_THICKNESS / 2f),
                (int) length,
                (int) (LINE_THICKNESS / 2f),
                color
        );
        gfx.pose().popPose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
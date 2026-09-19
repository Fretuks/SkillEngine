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
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class SkilltreeScreen extends Screen {
    private static final int NODE_RADIUS = 12;
    private static final int ABILITY_RADIUS = 12;
    private static final int NODE_ICON_SIZE = 16;
    private static final int OVERLAY_WIDTH = 280;
    private static final int UNAVAILABLE = 0xFF777D85;
    private static final int AVAILABLE = 0xFFD0A447;
    private static final int UNLOCKED = 0xFF72B77A;
    private static final int BLOCKED = 0xFFD16A65;
    private static final int LOCKED_PATH = 0xFF383D43;

    private record PathNode(ResourceLocation id, boolean ability) {}
    private record Connection(int x1, int y1, int x2, int y2, int color, boolean traced) {}
    private Button unlockButton;
    private Button inspectorCloseButton;
    private long inspectorOpenedAt;
    private int inspectorSlideOffset;
    private int inspectorAnimationStartOffset;
    private boolean inspectorClosing;
    private static final float INSPECTOR_SLIDE_MS = 180.0f;
    private boolean legendOpen;
    private static final float ZOOM_STEP = 0.1f;
    private static final float LINE_THICKNESS = 2.0f;
    private static final int GRID_SIZE = 32;
    private float zoom = 1.0f;
    private int detailScroll;
    private int maxDetailScroll;
    private double panX = 0;
    private double panY = 0;
    private boolean dragging = false;
    private SkillNode hoveredNode = null;
    private SkillNode selectedNode = null;
    private AbilityNode hoveredAbility = null;
    private AbilityNode selectedAbility = null;
    private ResourceLocation activeTree = null;
    private ResourceLocation highlightedNode = null;
    private ResourceLocation highlightedAbility = null;
    private boolean highlightSkillPoints = false;
    private Component unlockFailureMessage = null;

    public SkilltreeScreen() {
        super(Component.literal("Ascend Skilltree"));
    }

    @Override
    protected void init() {
        super.init();
        if (activeTree == null || !SkillNodeRegistry.trees().contains(activeTree)) {
            List<ResourceLocation> trees = SkillNodeRegistry.trees();
            activeTree = trees.isEmpty() ? null : trees.get(0);
        }
        if (selectedNode != null) rebuildOverlayButtons();
        else if (selectedAbility != null) rebuildAbilityOverlayButtons();
        else rebuildTreeTabs();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (legendOpen && isOverLegend(mouseX, mouseY)) return true;
        if (hasSelection() && isOverInspector(mouseX, mouseY)) {
            detailScroll = Mth.clamp(detailScroll - (int) (delta * 12), 0, maxDetailScroll);
            return true;
        }
        zoom += (float) (delta * ZOOM_STEP);
        float minZoom = 0.5f;
        float maxZoom = 2.0f;
        zoom = Mth.clamp(zoom, minZoom, maxZoom);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (legendOpen && isOverLegend(mouseX, mouseY)) return true;
        if (hasSelection() && isOverInspector(mouseX, mouseY)) return true;
        if (children().stream().anyMatch(child -> child.isMouseOver(mouseX, mouseY))) return true;
        if (mouseY < 36 || mouseY >= height - 23) return false;
        if (button == 0) {
            SkillNode clickedNode = findNodeAt(mouseX, mouseY);
            if (clickedNode != null) {
                if (clickedNode == selectedNode) {
                    closeInspector();
                    return true;
                }
                selectedAbility = null;
                selectedNode = clickedNode;
                rebuildOverlayButtons();
                revealSelection(clickedNode.getX(), clickedNode.getY());
                return true;
            }
            AbilityNode clickedAbility = findAbilityAt(mouseX, mouseY);
            if (clickedAbility != null) {
                if (clickedAbility == selectedAbility) {
                    closeInspector();
                    return true;
                }
                selectedNode = null;
                if (SkilltreeClientState.isAbilityUnlocked(clickedAbility.getId())) {
                    Minecraft.getInstance().setScreen(new AbilityBindingScreen(clickedAbility));
                } else {
                    selectedAbility = clickedAbility;
                    rebuildAbilityOverlayButtons();
                    revealSelection(clickedAbility.getX(), clickedAbility.getY());
                }
                return true;
            }
            if (hasSelection()) closeInspector();
            dragging = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
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
        updateInspectorAnimation();
        this.renderBackground(gfx);
        renderTreeBackdrop(gfx);
        boolean overCanvas = mouseY >= 36 && mouseY < height - 23
                && !(legendOpen && isOverLegend(mouseX, mouseY))
                && !(hasSelection() && isOverInspector(mouseX, mouseY))
                && children().stream().noneMatch(child -> child.isMouseOver(mouseX, mouseY));
        hoveredNode = overCanvas ? findNodeAt(mouseX, mouseY) : null;
        hoveredAbility = overCanvas ? findAbilityAt(mouseX, mouseY) : null;
        gfx.enableScissor(0, 36, width, height - 23);
        drawGraph(gfx);
        gfx.disableScissor();
        renderSkillPointHeader(gfx);
        renderUnlockFailure(gfx);
        if (hoveredNode != null && selectedNode == null && selectedAbility == null) {
            renderNodeTooltip(gfx, hoveredNode, mouseX, mouseY);
        }
        if (selectedNode != null) {
            renderNodeOverlay(gfx, selectedNode);
        } else if (selectedAbility != null) {
            renderAbilityOverlay(gfx, selectedAbility);
        }
        super.render(gfx, mouseX, mouseY, partialTicks);
        if (legendOpen) renderLegend(gfx);
    }

    private void renderSkillPointHeader(GuiGraphics gfx) {
        gfx.fill(0, 0, width, 36, SkillUi.BACKGROUND);
        gfx.fill(0, 35, width, 36, SkillUi.BORDER);
        String points = "Skill Points: " + SkilltreeClientState.getCurrentSkillPoints();
        int pointsX = width - font.width(points) - 32;
        gfx.fill(pointsX - 1, 7, width - 11, 29, highlightSkillPoints ? BLOCKED : SkillUi.BORDER);
        gfx.fill(pointsX, 8, width - 12, 28, SkillUi.SURFACE);
        gfx.drawString(font, points, pointsX + 10, 14, SkillUi.TEXT, false);
        gfx.fill(0, height - 23, width, height, SkillUi.BACKGROUND);
        gfx.drawString(font, "Legend", 38, height - 15, SkillUi.MUTED, false);
        String zoomLabel = Math.round(zoom * 100) + "%";
        gfx.drawString(font, zoomLabel, width - font.width(zoomLabel) - 14, height - 15, SkillUi.ACCENT, false);
    }

    private static String displayName(String value) {
        StringBuilder name = new StringBuilder();
        for (String word : value.replaceAll("[_/:.\\-]+", " ").split(" +")) {
            if (word.isEmpty()) continue;
            if (!name.isEmpty()) name.append(' ');
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
    }

    private void renderInspectorHeading(GuiGraphics gfx, Component title, ResourceLocation icon) {
        int x = inspectorX() + 12;
        int y = inspectorY();
        if (icon != null) {
            gfx.blit(icon, x, y + 5, NODE_ICON_SIZE, NODE_ICON_SIZE, 0, 0,
                    NODE_ICON_SIZE, NODE_ICON_SIZE, NODE_ICON_SIZE, NODE_ICON_SIZE);
            x += NODE_ICON_SIZE + 6;
        }
        int availableWidth = inspectorX() + inspectorWidth() - 30 - x;
        gfx.drawString(font, font.plainSubstrByWidth(title.getString(), availableWidth),
                x, y + 10, SkillUi.TEXT, false);
    }

    private Set<PathNode> prerequisiteChain(PathNode focus) {
        Set<PathNode> visited = new HashSet<>();
        if (focus == null) return visited;
        ArrayDeque<PathNode> pending = new ArrayDeque<>();
        pending.add(focus);
        while (!pending.isEmpty()) {
            PathNode current = pending.removeFirst();
            if (!visited.add(current)) continue;
            SkillNode node = current.ability() ? null : SkillNodeRegistry.get(current.id());
            AbilityNode ability = current.ability() ? AbilityNodeRegistry.get(current.id()) : null;
            List<ResourceLocation> parents = node != null ? node.getLinks()
                    : ability != null ? ability.getLinks() : List.of();
            for (ResourceLocation id : parents) {
                if (SkillNodeRegistry.get(id) != null) pending.add(new PathNode(id, false));
                else if (current.ability() && AbilityNodeRegistry.get(id) != null) {
                    pending.add(new PathNode(id, true));
                }
            }
        }
        return visited;
    }

    private int pathEmphasis(int color, boolean focused, boolean traced) {
        if (traced) {
            if (color == LOCKED_PATH) return SkillUi.MUTED;
            return mixColor(color, SkillUi.TEXT, 0.2f);
        }
        return focused ? mixColor(color, SkillUi.BACKGROUND, 0.45f) : color;
    }

    private static int mixColor(int from, int to, float amount) {
        int red = (int) (((from >> 16) & 255) * (1 - amount) + ((to >> 16) & 255) * amount);
        int green = (int) (((from >> 8) & 255) * (1 - amount) + ((to >> 8) & 255) * amount);
        int blue = (int) ((from & 255) * (1 - amount) + (to & 255) * amount);
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    private void renderTreeBackdrop(GuiGraphics gfx) {
        gfx.fill(0, 0, width, height, SkillUi.BACKGROUND);
        int spacing = Math.max(16, (int) (GRID_SIZE * zoom));
        int offsetX = Math.floorMod((int) panX + width / 2, spacing);
        int offsetY = Math.floorMod((int) panY + height / 2, spacing);
        for (int x = offsetX; x < width; x += spacing) {
            for (int y = offsetY; y < height; y += spacing) {
                gfx.fill(x, y, x + 1, y + 1, 0x30555B63);
            }
        }
    }

    private void drawGraph(GuiGraphics gfx) {
        PathNode focus = hoveredNode != null ? new PathNode(hoveredNode.getId(), false)
                : hoveredAbility != null ? new PathNode(hoveredAbility.getId(), true)
                : selectedNode != null ? new PathNode(selectedNode.getId(), false)
                : selectedAbility != null ? new PathNode(selectedAbility.getId(), true) : null;
        Set<PathNode> chain = prerequisiteChain(focus);
        List<Connection> connections = new ArrayList<>();
        for (SkillNode node : activeSkillNodes()) {
            int[] p1 = worldToScreen(node.getX(), node.getY());
            for (ResourceLocation neighborId : node.getLinks()) {
                SkillNode neighbor = SkillNodeRegistry.get(neighborId);
                if (neighbor == null) continue;
                int[] p2 = worldToScreen(neighbor.getX(), neighbor.getY());
                boolean bothUnlocked =
                        SkilltreeClientState.isUnlocked(node.getId()) &&
                                SkilltreeClientState.isUnlocked(neighbor.getId());
                int linkColor = bothUnlocked ? UNLOCKED
                        : isLockedByExclusivity(node) ? BLOCKED
                        : getNodeColor(node) == AVAILABLE && SkilltreeClientState.isUnlocked(neighborId)
                        ? AVAILABLE : LOCKED_PATH;
                boolean traced = chain.contains(new PathNode(node.getId(), false));
                connections.add(new Connection(p1[0], p1[1], p2[0], p2[1],
                        pathEmphasis(linkColor, focus != null, traced), traced));
            }
        }
        for (AbilityNode ability : AbilityNodeRegistry.all()) {
            int[] p1 = worldToScreen(ability.getX(), ability.getY());
            for (ResourceLocation link : ability.getLinks()) {
                SkillNode node = SkillNodeRegistry.get(link);
                AbilityNode ability2 = AbilityNodeRegistry.get(link);
                if (node != null) {
                    int[] p2 = worldToScreen(node.getX(), node.getY());
                    boolean traced = chain.contains(new PathNode(ability.getId(), true));
                    connections.add(new Connection(p1[0], p1[1], p2[0], p2[1],
                            pathEmphasis(abilityPathColor(ability, link), focus != null, traced), traced));
                } else if (ability2 != null) {
                    int[] p2 = worldToScreen(ability2.getX(), ability2.getY());
                    boolean traced = chain.contains(new PathNode(ability.getId(), true));
                    connections.add(new Connection(p1[0], p1[1], p2[0], p2[1],
                            pathEmphasis(abilityPathColor(ability, link), focus != null, traced), traced));
                }
            }
        }
        // Draw the traced chain last so unrelated crossing lines cannot obscure it.
        for (boolean traced : new boolean[]{false, true}) {
            for (Connection connection : connections) {
                if (connection.traced() != traced) continue;
                drawConnection(gfx, connection.x1(), connection.y1(), connection.x2(), connection.y2(), connection.color());
            }
        }
        for (AbilityNode ability : AbilityNodeRegistry.all()) {
            int[] pos = worldToScreen(ability.getX(), ability.getY());
            int radius = getRenderedNodeRadius(ABILITY_RADIUS);
            int color = SkilltreeClientState.isAbilityUnlocked(ability.getId()) ? UNLOCKED
                    : abilityRequirementsMet(ability) ? AVAILABLE : UNAVAILABLE;
            renderNodeFrame(gfx, pos[0], pos[1], radius, color,
                    ability == hoveredAbility || ability.getId().equals(highlightedAbility),
                    ability == selectedAbility);
            ResourceLocation icon = ability.getIcon();
            if (icon != null) {
                int size = getRenderedNodeIconSize();
                gfx.blit(icon, pos[0] - size / 2, pos[1] - size / 2,
                        size, size, 0, 0,
                        NODE_ICON_SIZE, NODE_ICON_SIZE, NODE_ICON_SIZE, NODE_ICON_SIZE);
            }
        }
        for (SkillNode node : activeSkillNodes()) {
            int[] pos = worldToScreen(node.getX(), node.getY());
            int radius = getRenderedNodeRadius(NODE_RADIUS);
            int color = getNodeColor(node);
            renderNodeFrame(gfx, pos[0], pos[1], radius, color,
                    node == hoveredNode || node.getId().equals(highlightedNode), node == selectedNode);
            ResourceLocation icon = node.getIcons();
            if (icon != null) {
                int size = getRenderedNodeIconSize();
                gfx.blit(
                        icon,
                        pos[0] - size / 2,
                        pos[1] - size / 2,
                        size, size,
                        0, 0,
                        NODE_ICON_SIZE, NODE_ICON_SIZE,
                        NODE_ICON_SIZE, NODE_ICON_SIZE
                );
            }
            renderCostBadge(gfx, pos[0], pos[1], radius, node.getCost());
        }
    }

    private void renderCostBadge(GuiGraphics gfx, int cx, int cy, int radius, int cost) {
        if (cost <= 1) return;
        String label = Integer.toString(cost);
        float scale = 0.75f;
        gfx.pose().pushPose();
        gfx.pose().translate(cx + radius - 3, cy + radius - 3, 0);
        gfx.pose().scale(scale, scale, 1);
        int badgeWidth = font.width(label);
        gfx.fill(-2, -1, badgeWidth + 2, font.lineHeight, SkillUi.BACKGROUND);
        gfx.drawString(font, label, 0, 0, SkillUi.TEXT, false);
        gfx.pose().popPose();
    }

    private void renderNodeFrame(GuiGraphics gfx, int cx, int cy, int radius, int color,
                                 boolean hovered, boolean selected) {
        if (hovered || selected) {
            int spread = selected ? 4 : 3;
            gfx.fill(cx - radius - spread, cy - radius - spread,
                    cx + radius + spread, cy + radius + spread, SkillUi.TEXT);
            gfx.fill(cx - radius - spread + 1, cy - radius - spread + 1,
                    cx + radius + spread - 1, cy + radius + spread - 1, SkillUi.BACKGROUND);
        }
        gfx.fill(cx - radius - 1, cy - radius - 1, cx + radius + 1, cy + radius + 1, color);
        gfx.fill(cx - radius + 1, cy - radius + 1, cx + radius - 1, cy + radius - 1, SkillUi.SURFACE);
    }

    private void renderNodeTooltip(GuiGraphics gfx, SkillNode node, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(node.getTitle());
        String status = skillStatus(node);
        if (!status.equals("Not enough skill points")) {
            tooltip.add(Component.literal(status).withStyle(ChatFormatting.GRAY));
        }
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
                .withStyle(ChatFormatting.GRAY));
        if (!node.getPrereqAttributes().isEmpty()) {
            tooltip.add(Component.literal("Stat Requirements:")
                    .withStyle(ChatFormatting.GOLD));
            assert Minecraft.getInstance().player != null;
            Minecraft.getInstance().player.getCapability(PlayerStatsProvider.PLAYER_STATS)
                    .ifPresent(stats -> node.getPrereqAttributes().forEach((attr, required) -> {
                        int current = stats.getAttributeLevel(attr);
                        boolean ok = current >= required;
                        tooltip.add(
                                Component.literal(" - " + displayName(attr) + ": " + current + "/" + required)
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
        int x = inspectorX();
        int y = inspectorY();
        renderOverlayPanel(gfx, x, y);
        renderInspectorHeading(gfx, node.getTitle(), node.getIcons());
        beginDetailBody(gfx, x, y);
        gfx.drawWordWrap(font, node.getDescription(), x + 16, y + 32, inspectorWidth() - 32, SkillUi.MUTED);
        int descriptionHeight = font.split(node.getDescription(), inspectorWidth() - 32).size() * font.lineHeight;
        final int[] textY = {y + 32 + descriptionHeight + 16};
        gfx.drawString(
                font,
                Component.literal("Cost: " + node.getCost() + " skill points")
                        .withStyle(ChatFormatting.GRAY),
                x + 16,
                textY[0],
                0xAAAAAA
        );
        textY[0] += 15;
        if (!node.getPrereqAttributes().isEmpty()) {
            gfx.drawString(
                    font,
                    Component.literal("Stat Requirements:")
                            .withStyle(ChatFormatting.GOLD),
                    x + 16,
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
                                " - " + displayName(attr) + ": " + current + "/" + required
                        ).withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.RED);

                        gfx.drawWordWrap(font, line, x + 16, textY[0], inspectorWidth() - 32, SkillUi.TEXT);
                        textY[0] += font.split(line, inspectorWidth() - 32).size() * font.lineHeight + 3;
                    }));
        }
        textY[0] = renderPrerequisites(gfx, node.getLinks(), x, textY[0], false);
        if (!node.getExclusiveWith().isEmpty()) {
            gfx.drawString(font, "Mutually exclusive with:", x + 16, textY[0], SkillUi.MUTED, false);
            textY[0] += 12;
            for (ResourceLocation id : node.getExclusiveWith()) {
                SkillNode other = SkillNodeRegistry.get(id);
                Component name = other == null ? Component.literal(id.toString()) : other.getTitle();
                gfx.drawWordWrap(font, name, x + 16, textY[0], inspectorWidth() - 32,
                        SkilltreeClientState.isUnlocked(id) ? BLOCKED : SkillUi.MUTED);
                textY[0] += font.split(name, inspectorWidth() - 32).size() * font.lineHeight + 3;
            }
        }
        endDetailBody(gfx, y, textY[0]);
        updateUnlockButton(skillStatus(node), getNodeColor(node) == AVAILABLE);
    }

    private void renderAbilityOverlay(GuiGraphics gfx, AbilityNode ability) {
        int x = inspectorX();
        int y = inspectorY();
        renderOverlayPanel(gfx, x, y);
        renderInspectorHeading(gfx, ability.getTitle(), ability.getIcon());
        beginDetailBody(gfx, x, y);
        int textY = y + 32;
        if (ability.getDescription() != null) {
            gfx.drawWordWrap(font, ability.getDescription(), x + 16, textY, inspectorWidth() - 32, 0xDDDDDD);
            textY += font.split(ability.getDescription(), inspectorWidth() - 32).size() * font.lineHeight + 16;
        }
        gfx.drawString(font,
                Component.literal("Cooldown: " + ability.getCooldown() / 20 + " seconds")
                        .withStyle(ChatFormatting.GRAY),
                x + 16,
                textY,
                0xFFFFFF);
        textY += 15;
        textY = renderPrerequisites(gfx, ability.getLinks(), x, textY, true);
        endDetailBody(gfx, y, textY);
        boolean unlocked = SkilltreeClientState.isAbilityUnlocked(ability.getId());
        updateUnlockButton(unlocked ? "Already unlocked"
                : abilityRequirementsMet(ability) ? "Ready to unlock" : "Unlock prerequisites first",
                !unlocked && abilityRequirementsMet(ability));
    }

    private void beginDetailBody(GuiGraphics gfx, int x, int y) {
        gfx.enableScissor(x + 16, y + 30, x + inspectorWidth() - 16, y + inspectorHeight() - 40);
        gfx.pose().pushPose();
        gfx.pose().translate(0, -detailScroll, 0);
    }

    private void endDetailBody(GuiGraphics gfx, int y, int bottom) {
        gfx.pose().popPose();
        gfx.disableScissor();
        maxDetailScroll = Math.max(0, bottom - (y + inspectorHeight() - 44));
        detailScroll = Math.min(detailScroll, maxDetailScroll);
        if (maxDetailScroll > 0) {
            int trackX = inspectorX() + inspectorWidth() - 9;
            int trackHeight = inspectorHeight() - 70;
            int thumbY = y + 30 + (trackHeight - 16) * detailScroll / maxDetailScroll;
            gfx.fill(trackX, y + 30, trackX + 2, y + 30 + trackHeight, SkillUi.BORDER);
            gfx.fill(trackX, thumbY, trackX + 2, thumbY + 16, SkillUi.ACCENT);
        }
    }

    private void renderOverlayPanel(GuiGraphics gfx, int x, int y) {
        SkillUi.panel(gfx, x, y, inspectorWidth(), inspectorHeight());
        gfx.fill(x, y, x + inspectorWidth(), y + 25, SkillUi.HEADER);
        gfx.fill(x + 12, y + 29, x + inspectorWidth() - 12, y + inspectorHeight() - 40, SkillUi.SURFACE);
    }

    private boolean hasSelection() {
        return selectedNode != null || selectedAbility != null;
    }

    private boolean usesSidebar() {
        return width >= 560;
    }

    private int inspectorWidth() {
        return Math.min(OVERLAY_WIDTH, width - 24);
    }

    private int inspectorHeight() {
        return Math.min(300, height - 72);
    }

    private int inspectorX() {
        return inspectorRestX() + inspectorSlideOffset;
    }

    private int inspectorRestX() {
        return usesSidebar() ? width - inspectorWidth() - 12 : (width - inspectorWidth()) / 2;
    }

    private void updateInspectorAnimation() {
        if (!hasSelection()) return;
        float progress = Mth.clamp((Util.getMillis() - inspectorOpenedAt) / INSPECTOR_SLIDE_MS, 0.0f, 1.0f);
        float remaining = 1.0f - progress;
        if (inspectorClosing && (progress >= 1.0f || !usesSidebar())) {
            finishClosingInspector();
            return;
        }
        float eased = 1.0f - remaining * remaining * remaining;
        int targetOffset = inspectorClosing ? inspectorWidth() + 16 : 0;
        inspectorSlideOffset = usesSidebar() ? Math.round(inspectorAnimationStartOffset
                + (targetOffset - inspectorAnimationStartOffset) * eased) : 0;
        if (unlockButton != null) unlockButton.setX(inspectorX() + inspectorWidth() - 96);
        if (inspectorCloseButton != null) inspectorCloseButton.setX(inspectorX() + inspectorWidth() - 23);
    }

    private int inspectorY() {
        return usesSidebar() ? 40 : (height - inspectorHeight()) / 2;
    }

    private boolean isOverInspector(double x, double y) {
        return x >= inspectorX() - 1 && x <= inspectorX() + inspectorWidth() + 1
                && y >= inspectorY() - 1 && y <= inspectorY() + inspectorHeight() + 1;
    }

    private void revealSelection(float x, float y) {
        if (!usesSidebar()) return;
        int[] position = worldToScreen(x, y);
        // Move only a selection that the actual popup rectangle would cover.
        // Nodes above or below it keep their existing position.
        int margin = 24;
        if (position[0] >= inspectorRestX() - margin
                && position[0] <= inspectorRestX() + inspectorWidth() + margin
                && position[1] >= inspectorY() - margin
                && position[1] <= inspectorY() + inspectorHeight() + margin) {
            panX += inspectorRestX() - margin - position[0];
        }
    }

    private void closeInspector() {
        if (!hasSelection() || inspectorClosing) return;
        if (!usesSidebar()) {
            finishClosingInspector();
            return;
        }
        inspectorClosing = true;
        inspectorAnimationStartOffset = inspectorSlideOffset;
        inspectorOpenedAt = Util.getMillis();
        if (unlockButton != null) {
            unlockButton.active = false;
            unlockButton.setTooltip(null);
        }
        if (inspectorCloseButton != null) {
            inspectorCloseButton.active = false;
            inspectorCloseButton.setTooltip(null);
        }
    }

    private void finishClosingInspector() {
        selectedNode = null;
        selectedAbility = null;
        unlockButton = null;
        inspectorCloseButton = null;
        inspectorSlideOffset = 0;
        inspectorAnimationStartOffset = 0;
        inspectorClosing = false;
        clearWidgets();
        rebuildTreeTabs();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && legendOpen) {
            legendOpen = false;
            return true;
        }
        if (keyCode == 256 && hasSelection()) {
            closeInspector();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private int renderPrerequisites(GuiGraphics gfx, List<ResourceLocation> links, int x, int y, boolean ability) {
        if (links.isEmpty()) return y;
        gfx.drawString(font, "Required Skills:", x + 16, y, SkillUi.ACCENT, false);
        y += 12;
        for (ResourceLocation id : links) {
            SkillNode node = SkillNodeRegistry.get(id);
            AbilityNode parentAbility = AbilityNodeRegistry.get(id);
            Component name = node != null ? node.getTitle()
                    : parentAbility != null ? parentAbility.getTitle() : Component.literal(id.toString());
            boolean met = SkilltreeClientState.isUnlocked(id)
                    || (ability && SkilltreeClientState.isAbilityUnlocked(id));
            Component line = Component.literal(met ? "+ " : "- ").append(name);
            gfx.drawWordWrap(font, line, x + 16, y, inspectorWidth() - 32, met ? UNLOCKED : BLOCKED);
            y += font.split(line, inspectorWidth() - 32).size() * font.lineHeight + 3;
        }
        return y;
    }

    private void updateUnlockButton(String status, boolean canUnlock) {
        if (unlockButton == null || inspectorClosing) return;
        unlockButton.setTooltip(Tooltip.create(Component.literal(status)));
        unlockButton.active = canUnlock;
    }

    private void rebuildOverlayButtons() {
        rebuildInspectorButtons(false);
    }

    private void rebuildAbilityOverlayButtons() {
        rebuildInspectorButtons(true);
    }

    private void rebuildInspectorButtons(boolean ability) {
        if (unlockButton == null || inspectorClosing) {
            inspectorAnimationStartOffset = unlockButton == null
                    ? (usesSidebar() ? inspectorWidth() + 16 : 0) : inspectorSlideOffset;
            inspectorOpenedAt = Util.getMillis();
            inspectorSlideOffset = inspectorAnimationStartOffset;
        }
        inspectorClosing = false;
        detailScroll = 0;
        maxDetailScroll = 0;
        dragging = false;
        clearUnlockHighlights();
        clearWidgets();
        addLegendButton();
        int x = inspectorX();
        int y = inspectorY();
        unlockButton = addRenderableWidget(SkillUi.button(Component.literal("Unlock"), btn -> {
            if (inspectorClosing) return;
            if (ability ? !prepareAbilityUnlock(selectedAbility) : !prepareSkillUnlock(selectedNode)) return;
            ResourceLocation id = ability ? selectedAbility.getId() : selectedNode.getId();
            PacketHandler.CHANNEL.sendToServer(new ServerboundUnlockNodePacket(id));
            closeInspector();
        }).pos(x + inspectorWidth() - 96, y + inspectorHeight() - 28).size(80, 20).build());
        unlockButton.active = ability
                ? !SkilltreeClientState.isAbilityUnlocked(selectedAbility.getId()) && abilityRequirementsMet(selectedAbility)
                : getNodeColor(selectedNode) == AVAILABLE;
        inspectorCloseButton = addRenderableWidget(SkillUi.button(Component.literal("X"), btn -> closeInspector())
                .pos(x + inspectorWidth() - 23, y + 5).size(18, 16).build());
        inspectorCloseButton.setTooltip(Tooltip.create(Component.literal("Close details (Esc)")));
    }

    private void addLegendButton() {
        Button help = addRenderableWidget(SkillUi.button(Component.literal("?"), btn -> legendOpen = !legendOpen)
                .pos(12, height - 21).size(20, 18).build());
        help.setTooltip(Tooltip.create(Component.literal("Show or hide the legend")));
    }

    private boolean isOverLegend(double x, double y) {
        return x >= 12 && x <= 12 + Math.min(300, width - 24)
                && y >= height - 158 && y <= height - 30;
    }

    private void renderLegend(GuiGraphics gfx) {
        int x = 12;
        int y = height - 158;
        SkillUi.panel(gfx, x, y, Math.min(300, width - 24), 128);
        gfx.drawString(font, "Node borders and paths", x + 12, y + 10, SkillUi.TEXT, false);
        String[] labels = {"Unlocked", "Available to unlock",
                "Unavailable: requirements or points missing", "Blocked: mutually exclusive"};
        int[] colors = {UNLOCKED, AVAILABLE, UNAVAILABLE, BLOCKED};
        for (int i = 0; i < labels.length; i++) {
            int rowY = y + 30 + i * 15;
            gfx.fill(x + 12, rowY, x + 20, rowY + 8, colors[i]);
            gfx.drawString(font, labels[i], x + 26, rowY, SkillUi.TEXT, false);
        }
        gfx.drawString(font, "White outline: hover or selection", x + 12, y + 110, SkillUi.MUTED, false);
    }

    private int[] worldToScreen(float wx, float wy) {
        double sx = width / 2.0 + panX + wx * zoom;
        double sy = height / 2.0 + panY - wy * zoom;
        return new int[]{(int) sx, (int) sy};
    }

    private SkillNode findNodeAt(double mouseX, double mouseY) {
        int radius = getRenderedNodeRadius(NODE_RADIUS);
        for (SkillNode node : activeSkillNodes()) {
            int[] pos = worldToScreen(node.getX(), node.getY());
            if (isPointInCircle(mouseX, mouseY, pos[0], pos[1], radius)) {
                return node;
            }
        }
        return null;
    }

    private AbilityNode findAbilityAt(double mouseX, double mouseY) {
        int radius = getRenderedNodeRadius(ABILITY_RADIUS);
        for (AbilityNode ability : AbilityNodeRegistry.all()) {
            int[] pos = worldToScreen(ability.getX(), ability.getY());
            if (isPointInCircle(mouseX, mouseY, pos[0], pos[1], radius)) {
                return ability;
            }
        }
        return null;
    }

    private int getRenderedNodeRadius(int baseRadius) {
        return Math.max(1, Math.round(baseRadius * Math.min(1.0f, zoom)));
    }

    private int getRenderedNodeIconSize() {
        return Math.max(1, Math.round(NODE_ICON_SIZE * Math.min(1.0f, zoom)));
    }

    private boolean isPointInCircle(double mx, double my, int cx, int cy, int radius) {
        return mx >= cx - radius && mx <= cx + radius &&
                my >= cy - radius && my <= cy + radius;
    }

    private boolean isLockedByExclusivity(SkillNode node) {
        return node.getExclusiveWith().stream().anyMatch(SkilltreeClientState::isUnlocked);
    }

    private String skillStatus(SkillNode node) {
        if (SkilltreeClientState.isUnlocked(node.getId())) return "Already unlocked";
        for (ResourceLocation id : node.getExclusiveWith()) {
            if (!SkilltreeClientState.isUnlocked(id)) continue;
            SkillNode blocker = SkillNodeRegistry.get(id);
            return "Blocked by: " + (blocker == null ? id.toString() : blocker.getTitle().getString());
        }
        if (SkilltreeClientState.getCurrentSkillPoints() < Math.max(0, node.getCost())) return "Not enough skill points";
        if (!node.getLinks().stream().allMatch(SkilltreeClientState::isUnlocked)) return "Unlock prerequisites first";
        if (!meetsClientAttributes(node)) return "Attribute requirements not met";
        return "Ready to unlock";
    }

    private int getNodeColor(SkillNode node) {
        if (SkilltreeClientState.isUnlocked(node.getId())) return UNLOCKED;
        if (isLockedByExclusivity(node)) return BLOCKED;
        return node.getLinks().stream().allMatch(SkilltreeClientState::isUnlocked)
                && meetsClientAttributes(node)
                && SkilltreeClientState.getCurrentSkillPoints() >= Math.max(0, node.getCost())
                ? AVAILABLE : UNAVAILABLE;
    }

    private boolean abilityRequirementsMet(AbilityNode ability) {
        return ability.getLinks().stream().allMatch(id -> SkilltreeClientState.isUnlocked(id)
                || SkilltreeClientState.isAbilityUnlocked(id));
    }

    private int abilityPathColor(AbilityNode ability, ResourceLocation parent) {
        boolean parentUnlocked = SkilltreeClientState.isUnlocked(parent) || SkilltreeClientState.isAbilityUnlocked(parent);
        if (parentUnlocked && SkilltreeClientState.isAbilityUnlocked(ability.getId())) return UNLOCKED;
        return parentUnlocked && abilityRequirementsMet(ability) ? AVAILABLE : LOCKED_PATH;
    }

    private boolean prepareSkillUnlock(SkillNode target) {
        clearUnlockHighlights();
        if (SkilltreeClientState.isUnlocked(target.getId()) || isLockedByExclusivity(target)) return false;
        for (ResourceLocation parentId : target.getLinks()) {
            if (SkilltreeClientState.isUnlocked(parentId)) continue;
            SkillNode parent = SkillNodeRegistry.get(parentId);
            highlightedNode = parentId;
            Component parentName = parent != null ? parent.getTitle() : Component.literal(parentId.toString());
            unlockFailureMessage = Component.literal("Unlock ")
                    .append(parentName.copy().withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" first — it is required for "))
                    .append(target.getTitle().copy().withStyle(ChatFormatting.WHITE));
            return false;
        }
        if (!meetsClientAttributes(target)) {
            highlightedNode = target.getId();
            unlockFailureMessage = Component.literal("Attribute requirements are not met for ")
                    .append(target.getTitle().copy().withStyle(ChatFormatting.YELLOW));
            return false;
        }
        if (SkilltreeClientState.getCurrentSkillPoints() < Math.max(0, target.getCost())) {
            highlightSkillPoints = true;
            unlockFailureMessage = Component.literal("Not enough skill points to unlock ")
                    .append(target.getTitle().copy().withStyle(ChatFormatting.YELLOW));
            return false;
        }
        return true;
    }

    private boolean meetsClientAttributes(SkillNode node) {
        if (node.getPrereqAttributes().isEmpty()) return true;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return false;
        return minecraft.player.getCapability(PlayerStatsProvider.PLAYER_STATS)
                .map(stats -> node.getPrereqAttributes().entrySet().stream()
                        .allMatch(entry -> stats.getAttributeLevel(entry.getKey()) >= entry.getValue()))
                .orElse(false);
    }

    private boolean prepareAbilityUnlock(AbilityNode ability) {
        clearUnlockHighlights();
        if (SkilltreeClientState.isAbilityUnlocked(ability.getId())) return false;
        for (ResourceLocation parentId : ability.getLinks()) {
            if (SkilltreeClientState.isUnlocked(parentId)
                    || SkilltreeClientState.isAbilityUnlocked(parentId)) continue;
            SkillNode parentNode = SkillNodeRegistry.get(parentId);
            if (parentNode != null) {
                highlightedNode = parentId;
            } else {
                highlightedAbility = parentId;
            }
            Component parentName = parentNode != null
                    ? parentNode.getTitle()
                    : AbilityNodeRegistry.get(parentId) != null
                    ? AbilityNodeRegistry.get(parentId).getTitle()
                    : Component.literal(parentId.toString());
            unlockFailureMessage = Component.literal("Unlock ")
                    .append(parentName.copy().withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" first — it is required for "))
                    .append(ability.getTitle().copy().withStyle(ChatFormatting.WHITE));
            return false;
        }
        return true;
    }

    private void clearUnlockHighlights() {
        highlightedNode = null;
        highlightedAbility = null;
        highlightSkillPoints = false;
        unlockFailureMessage = null;
    }

    private void renderUnlockFailure(GuiGraphics gfx) {
        if (unlockFailureMessage == null) return;
        int messageWidth = Math.min(width - 40, font.width(unlockFailureMessage) + 20);
        int x = (width - messageWidth) / 2;
        int y = height - 46;
        gfx.fill(x - 2, y - 2, x + messageWidth + 2, y + 18, 0xCC000000);
        gfx.fill(x, y, x + messageWidth, y + 16, 0xE0421717);
        gfx.fill(x, y, x + 3, y + 16, 0xFFFF4545);
        gfx.drawCenteredString(font, unlockFailureMessage, width / 2, y + 4, 0xFFFFFF);
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

    private void drawConnection(GuiGraphics gfx, int x1, int y1, int x2, int y2, int color) {
        drawLine(gfx, x1 + 1, y1 + 2, x2 + 1, y2 + 2, 0x99000000);
        drawLine(gfx, x1, y1, x2, y2, color);
    }

    private Iterable<SkillNode> activeSkillNodes() {
        return activeTree != null ? SkillNodeRegistry.byTree(activeTree) : SkillNodeRegistry.all();
    }

    private void rebuildTreeTabs() {
        addLegendButton();
        List<ResourceLocation> trees = SkillNodeRegistry.trees();
        if (trees.size() <= 1 || selectedNode != null || selectedAbility != null) {
            return;
        }
        int x = 14;
        int y = 44;
        for (ResourceLocation tree : trees) {
            String label = tree.toString();
            int buttonWidth = Math.min(140, Math.max(60, font.width(label) + 16));
            if (x + buttonWidth > width - 14 && x > 14) {
                x = 14;
                y += 26;
            }
            Button button = addRenderableWidget(SkillUi.button(Component.literal(label), btn -> {
                        activeTree = tree;
                        selectedNode = null;
                        selectedAbility = null;
                        clearWidgets();
                        rebuildTreeTabs();
                    })
                    .pos(x, y)
                    .size(buttonWidth, 20)
                    .build());
            button.active = !tree.equals(activeTree);
            x += buttonWidth + 6;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

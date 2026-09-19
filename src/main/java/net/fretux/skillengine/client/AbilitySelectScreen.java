package net.fretux.skillengine.client;

import net.fretux.skillengine.network.PacketHandler;
import net.fretux.skillengine.network.ServerboundBindAbilityPacket;
import net.fretux.skillengine.skilltree.AbilityNode;
import net.fretux.skillengine.skilltree.AbilityNodeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AbilitySelectScreen extends Screen {
    private final Screen parent;
    private final int slot;
    private final List<AbilityNode> unlockedAbilities = new ArrayList<>();
    private int page = 0;
    private int pageSize() {
        return Math.max(1, Math.min(8, (height - 108) / 28));
    }

    private int panelHeight() { return 92 + pageSize() * 28; }
    private static final int PANEL_WIDTH = 280;

    public AbilitySelectScreen(Screen parent, int slot) {
        super(Component.literal("Select Ability"));
        this.parent = parent;
        this.slot = slot;
    }

    @Override
    protected void init() {
        unlockedAbilities.clear();
        for (AbilityNode node : AbilityNodeRegistry.all()) {
            if (SkilltreeClientState.isAbilityUnlocked(node.getId())) {
                unlockedAbilities.add(node);
            }
        }
        page = Math.min(page, Math.max(0, (unlockedAbilities.size() - 1) / pageSize()));
        buildPageButtons();
    }

    private void buildPageButtons() {
        clearWidgets();
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - panelHeight()) / 2;

        int start = page * pageSize();
        int end = Math.min(unlockedAbilities.size(), start + pageSize());
        int rowY = y + 52;
        for (int i = start; i < end; i++) {
            AbilityNode node = unlockedAbilities.get(i);
            String label = node.getTitle().getString();
            boolean alreadyInThisSlot = SkilltreeClientState.getSlotOfAbility(node.getId()) == slot;
            Button btn = SkillUi.button(Component.literal(label), b -> select(node))
                    .pos(x + 16, rowY)
                    .size(PANEL_WIDTH - 32, 24)
                    .build();
            if (alreadyInThisSlot) {
                btn.active = false;
                btn.setMessage(Component.literal(label + " (bound)"));
            }
            addRenderableWidget(btn);
            rowY += 28;
        }
        if (page > 0) {
            addRenderableWidget(SkillUi.button(Component.literal("< Prev"), b -> {
                page--;
                buildPageButtons();
            }).pos(x + 16, y + panelHeight() - 28).size(60, 20).build());
        }
        if ((page + 1) * pageSize() < unlockedAbilities.size()) {
            addRenderableWidget(SkillUi.button(Component.literal("Next >"), b -> {
                page++;
                buildPageButtons();
            }).pos(x + 80, y + panelHeight() - 28).size(60, 20).build());
        }
        addRenderableWidget(SkillUi.button(Component.literal("Back"), b -> back())
                .pos(x + PANEL_WIDTH - 70, y + panelHeight() - 28).size(60, 20).build());
    }

    private void select(AbilityNode node) {
        ResourceLocation id = node.getId();
        SkilltreeClientState.bindAbilityLocal(slot, id);
        PacketHandler.CHANNEL.sendToServer(new ServerboundBindAbilityPacket(id, slot));
        back();
    }

    private void back() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        back();
    }

    @Override
    public void render(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - panelHeight()) / 2;
        gfx.fill(0, 0, width, height, 0xAA000000);
        SkillUi.panel(gfx, x, y, PANEL_WIDTH, panelHeight());
        SkillUi.heading(gfx, font, "CHOOSE ABILITY", "Slot " + slot + "  /  Page " + (page + 1)
                + " of " + Math.max(1, (unlockedAbilities.size() + pageSize() - 1) / pageSize()), x, y);
        if (unlockedAbilities.isEmpty()) {
            gfx.drawString(font, "Unlock an ability in the skill tree.", x + 16, y + 58, SkillUi.MUTED, false);
        }
        super.render(gfx, mouseX, mouseY, partialTick);
    }
}

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
    private static final int PAGE_SIZE = 8;
    private static final int PANEL_WIDTH = 260;
    private static final int PANEL_HEIGHT = 220;

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
        buildPageButtons();
    }

    private void buildPageButtons() {
        clearWidgets();
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - PANEL_HEIGHT) / 2;

        int start = page * PAGE_SIZE;
        int end = Math.min(unlockedAbilities.size(), start + PAGE_SIZE);
        int rowY = y + 40;
        for (int i = start; i < end; i++) {
            AbilityNode node = unlockedAbilities.get(i);
            String label = node.getTitle().getString();
            boolean alreadyInThisSlot = SkilltreeClientState.getSlotOfAbility(node.getId()) == slot;
            Button btn = Button.builder(Component.literal(label), b -> select(node))
                    .pos(x + 15, rowY)
                    .size(PANEL_WIDTH - 30, 20)
                    .build();
            if (alreadyInThisSlot) {
                btn.active = false;
                btn.setMessage(Component.literal(label + " (bound)"));
            }
            addRenderableWidget(btn);
            rowY += 24;
        }
        if (page > 0) {
            addRenderableWidget(Button.builder(Component.literal("< Prev"), b -> {
                page--;
                buildPageButtons();
            }).pos(x + 15, y + PANEL_HEIGHT - 28).size(60, 20).build());
        }
        if ((page + 1) * PAGE_SIZE < unlockedAbilities.size()) {
            addRenderableWidget(Button.builder(Component.literal("Next >"), b -> {
                page++;
                buildPageButtons();
            }).pos(x + 80, y + PANEL_HEIGHT - 28).size(60, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> back())
                .pos(x + PANEL_WIDTH - 70, y + PANEL_HEIGHT - 28).size(60, 20).build());
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
        int y = (height - PANEL_HEIGHT) / 2;
        gfx.fill(0, 0, width, height, 0xAA000000);
        gfx.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xFF222222);
        String title = "Select for slot " + (slot == 1 ? "Y" : slot == 2 ? "X" : "C");
        gfx.drawCenteredString(font, Component.literal(title), x + PANEL_WIDTH / 2, y + 12, 0xFFFFFF);
        super.render(gfx, mouseX, mouseY, partialTick);
    }
}

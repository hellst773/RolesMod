package com.friends.rolesmod.client;

import com.friends.rolesmod.ModNetwork;
import com.friends.rolesmod.network.SelectRolePacket;
import com.friends.rolesmod.role.Role;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RoleSelectScreen extends Screen {
    private final int takenMask;
    private final List<Button> roleButtons = new ArrayList<>();

    public RoleSelectScreen(int takenMask) {
        super(Component.translatable("gui.rolesmod.title"));
        this.takenMask = takenMask;
    }

    @Override
    protected void init() {
        roleButtons.clear();
        int w = 280;
        int x = (this.width - w) / 2;
        int y = Math.max(44, this.height / 2 - 60);
        Role[] roles = Role.values();
        for (int i = 0; i < roles.length; i++) {
            final Role role = roles[i];
            boolean taken = (takenMask & (1 << i)) != 0;
            String label = role.nick + " - «" + role.title + "»" + (taken ? " (занято)" : "");
            Button b = Button.builder(Component.literal(label), btn -> choose(role))
                    .bounds(x, y + i * 28, w, 22)
                    .build();
            b.active = !taken;
            roleButtons.add(b);
            this.addRenderableWidget(b);
        }
    }

    private void choose(Role role) {
        ModNetwork.CHANNEL.sendToServer(new SelectRolePacket(role.ordinal()));
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        g.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xFFFFFF);
        g.drawCenteredString(this.font,
                Component.literal("Наведи на роль, чтобы увидеть характер и задачи. Выбор окончательный!")
                        .withStyle(ChatFormatting.GRAY),
                this.width / 2, 30, 0xAAAAAA);
        super.render(g, mouseX, mouseY, partialTick);

        Role[] roles = Role.values();
        for (int i = 0; i < roleButtons.size(); i++) {
            if (roleButtons.get(i).isHoveredOrFocused()) {
                Role r = roles[i];
                List<Component> tip = new ArrayList<>();
                tip.add(Component.literal(r.title).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                tip.add(Component.literal(r.character).withStyle(ChatFormatting.GRAY));
                tip.add(Component.literal("Задачи:").withStyle(ChatFormatting.YELLOW));
                for (Role.Task t : r.getTasks()) {
                    tip.add(Component.literal(" - " + t.text()).withStyle(ChatFormatting.WHITE));
                }
                g.renderComponentTooltip(this.font, tip, mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

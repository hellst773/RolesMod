package com.friends.rolesmod.role;

import com.friends.rolesmod.ModCapabilities;
import com.friends.rolesmod.ModItems;
import com.friends.rolesmod.ModNetwork;
import com.friends.rolesmod.network.OpenRoleScreenPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

public class TaskManager {

    /** Отправить игроку окно выбора роли. */
    public static void openScreen(ServerPlayer p) {
        int mask = RoleRegistry.get(p.getServer()).takenMask();
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new OpenRoleScreenPacket(mask));
    }

    /** Игрок выбрал роль в GUI. */
    public static void selectRole(ServerPlayer p, int ordinal) {
        PlayerRoleData d = ModCapabilities.get(p);
        if (d == null || d.getRole() != null) {
            return;
        }
        Role[] values = Role.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return;
        }
        Role r = values[ordinal];
        RoleRegistry reg = RoleRegistry.get(p.getServer());
        if (reg.isTakenByOther(r, p.getUUID())) {
            p.sendSystemMessage(Component.literal("Эта роль уже занята, выбери другую.").withStyle(ChatFormatting.RED));
            return;
        }
        reg.take(r, p.getUUID());
        d.setRole(r);
        d.setLastVape(p.level().getGameTime());
        p.refreshDisplayName();

        p.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("* " + p.getGameProfile().getName() + " теперь " + r.nick + ", «" + r.title + "»!")
                        .withStyle(ChatFormatting.GOLD), false);
        if (r == Role.SASHA) {
            p.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("<" + r.nick + "> может меня работа встр"), false);
        }
        p.addItem(new ItemStack(ModItems.QUEST_BOOK.get()));
        sendTaskList(p);
    }

    /** Показать список задач. */
    public static void sendTaskList(ServerPlayer p) {
        PlayerRoleData d = ModCapabilities.get(p);
        if (d == null || d.getRole() == null) {
            p.sendSystemMessage(Component.literal("У тебя нет роли. Напиши /role pick").withStyle(ChatFormatting.RED));
            return;
        }
        Role r = d.getRole();
        p.sendSystemMessage(Component.literal("=== " + r.nick + " - «" + r.title + "» ===")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        p.sendSystemMessage(Component.literal(r.character).withStyle(ChatFormatting.GRAY));
        int i = 1;
        for (Role.Task t : r.getTasks()) {
            boolean ok = d.getDone().contains(t.id());
            String line = (ok ? "[x] " : "[ ] ") + i + ". " + t.text()
                    + (t.manual() && !ok ? "  (когда сделаешь: /role done " + i + ")" : "");
            p.sendSystemMessage(Component.literal(line)
                    .withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.WHITE));
            i++;
        }
        boolean sOk = d.getDone().contains(Role.SUMMIT.id());
        p.sendSystemMessage(Component.literal((sOk ? "[x] " : "[ ] ") + Role.SUMMIT.text())
                .withStyle(sOk ? ChatFormatting.GREEN : ChatFormatting.AQUA));
    }

    /** Засчитать задачу. */
    public static void complete(ServerPlayer p, String taskId) {
        PlayerRoleData d = ModCapabilities.get(p);
        if (d == null || d.getRole() == null) {
            return;
        }
        Role.Task t = d.getRole().findTask(taskId);
        if (t == null || d.getDone().contains(taskId)) {
            return;
        }
        d.getDone().add(taskId);
        p.sendSystemMessage(Component.literal("Задача выполнена: " + t.text()).withStyle(ChatFormatting.GREEN));
        p.level().playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        p.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("* " + p.getDisplayName().getString() + " выполнил(а): " + t.text())
                        .withStyle(ChatFormatting.YELLOW), false);

        boolean all = d.getDone().contains(Role.SUMMIT.id());
        for (Role.Task rt : d.getRole().getTasks()) {
            if (!d.getDone().contains(rt.id())) {
                all = false;
            }
        }
        if (all) {
            p.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("*** " + p.getDisplayName().getString()
                            + " выполнил ВСЕ задачи! Абсолютный легенда, го в Японию. ***")
                            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), false);
        }
    }
}

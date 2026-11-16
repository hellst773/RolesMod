package com.friends.rolesmod;

import com.friends.rolesmod.role.PlayerRoleData;
import com.friends.rolesmod.role.Role;
import com.friends.rolesmod.role.RoleRegistry;
import com.friends.rolesmod.role.TaskManager;
import com.friends.rolesmod.role.TradeManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class RoleCommands {
    /** Минимальная высота (Y) для «вершины горы». Поменяй при желании. */
    private static final int SUMMIT_MIN_Y = 100;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("role")
                .executes(c -> {
                    TaskManager.sendTaskList(c.getSource().getPlayerOrException());
                    return 1;
                })
                .then(Commands.literal("task").executes(c -> {
                    TaskManager.sendTaskList(c.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("book").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    PlayerRoleData d = ModCapabilities.get(p);
                    if (d == null || d.getRole() == null) {
                        p.sendSystemMessage(Component.literal("Сначала выбери роль: /role pick").withStyle(ChatFormatting.RED));
                        return 0;
                    }
                    p.addItem(new ItemStack(ModItems.QUEST_BOOK.get()));
                    return 1;
                }))
                .then(Commands.literal("pick").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    PlayerRoleData d = ModCapabilities.get(p);
                    if (d != null && d.getRole() == null) {
                        TaskManager.openScreen(p);
                    } else {
                        p.sendSystemMessage(Component.literal("Роль уже выбрана. Сбросить может оператор: /role reset <игрок>")
                                .withStyle(ChatFormatting.RED));
                    }
                    return 1;
                }))
                .then(Commands.literal("done")
                        .then(Commands.argument("n", IntegerArgumentType.integer(1, 20))
                                .executes(c -> done(c.getSource().getPlayerOrException(),
                                        IntegerArgumentType.getInteger(c, "n")))))
                .then(Commands.literal("summit").executes(c -> summit(c.getSource().getPlayerOrException())))
                .then(Commands.literal("trade")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(c -> {
                                    TradeManager.request(c.getSource().getPlayerOrException(),
                                            EntityArgument.getPlayer(c, "player"));
                                    return 1;
                                })))
                .then(Commands.literal("accept").executes(c -> {
                    TradeManager.accept(c.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("reset")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(c -> {
                                    ServerPlayer t = EntityArgument.getPlayer(c, "player");
                                    PlayerRoleData d = ModCapabilities.get(t);
                                    if (d != null) {
                                        if (d.getRole() != null) {
                                            RoleRegistry.get(c.getSource().getServer()).release(d.getRole());
                                        }
                                        d.reset();
                                        t.refreshDisplayName();
                                    }
                                    c.getSource().sendSuccess(() -> Component.literal(
                                            "Роль игрока " + t.getGameProfile().getName() + " сброшена."), true);
                                    return 1;
                                }))));
    }

    private static int done(ServerPlayer p, int n) {
        PlayerRoleData d = ModCapabilities.get(p);
        if (d == null || d.getRole() == null) {
            p.sendSystemMessage(Component.literal("Сначала выбери роль: /role pick").withStyle(ChatFormatting.RED));
            return 0;
        }
        List<Role.Task> tasks = d.getRole().getTasks();
        if (n > tasks.size()) {
            p.sendSystemMessage(Component.literal("Нет задачи с таким номером.").withStyle(ChatFormatting.RED));
            return 0;
        }
        Role.Task t = tasks.get(n - 1);
        if (!t.manual()) {
            p.sendSystemMessage(Component.literal("Эта задача засчитывается автоматически, просто выполни её.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        TaskManager.complete(p, t.id());
        return 1;
    }

    private static int summit(ServerPlayer p) {
        PlayerRoleData d = ModCapabilities.get(p);
        if (d == null || d.getRole() == null) {
            p.sendSystemMessage(Component.literal("Сначала выбери роль: /role pick").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (p.getY() < SUMMIT_MIN_Y) {
            p.sendSystemMessage(Component.literal("Это не вершина. Нужна высота Y >= " + SUMMIT_MIN_Y
                    + " (сейчас " + (int) p.getY() + ").").withStyle(ChatFormatting.RED));
            return 0;
        }
        ServerLevel level = p.serverLevel();
        Set<Role> present = new HashSet<>();
        List<ServerPlayer> group = new java.util.ArrayList<>();
        for (ServerPlayer o : level.players()) {
            PlayerRoleData od = ModCapabilities.get(o);
            if (od != null && od.getRole() != null && o.distanceToSqr(p) <= 24.0 * 24.0) {
                present.add(od.getRole());
                group.add(o);
            }
        }
        if (present.size() < Role.values().length) {
            p.sendSystemMessage(Component.literal("Рядом (в 24 блоках) должны быть все четверо. Сейчас: "
                    + present.size() + "/4.").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (!hasNickSign(level, p.blockPosition())) {
            p.sendSystemMessage(Component.literal("Рядом (в 6 блоках) нет таблички, где написаны все ники: "
                    + "Саня, Ролик, Саша, Илюха.").withStyle(ChatFormatting.RED));
            return 0;
        }
        for (ServerPlayer o : group) {
            TaskManager.complete(o, Role.SUMMIT.id());
        }
        return 1;
    }

    private static boolean hasNickSign(ServerLevel level, BlockPos center) {
        for (int dx = -6; dx <= 6; dx++) {
            for (int dy = -6; dy <= 6; dy++) {
                for (int dz = -6; dz <= 6; dz++) {
                    BlockEntity be = level.getBlockEntity(center.offset(dx, dy, dz));
                    if (be instanceof SignBlockEntity sign) {
                        StringBuilder sb = new StringBuilder();
                        for (Component line : sign.getFrontText().getMessages(false)) {
                            sb.append(line.getString()).append(' ');
                        }
                        for (Component line : sign.getBackText().getMessages(false)) {
                            sb.append(line.getString()).append(' ');
                        }
                        String text = sb.toString().toLowerCase();
                        if (text.contains("саня") && text.contains("ролик")
                                && text.contains("саша") && text.contains("илюха")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}

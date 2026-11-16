package com.friends.rolesmod.role;

import com.friends.rolesmod.ModCapabilities;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Простой обмен предметами между игроками: /role trade <игрок> и /role accept. */
public class TradeManager {
    private record Request(UUID from, long expiresAt) {}

    private static final Map<UUID, Request> PENDING = new HashMap<>();

    public static void request(ServerPlayer from, ServerPlayer to) {
        if (from.getUUID().equals(to.getUUID())) {
            from.sendSystemMessage(Component.literal("Сам с собой торговать нельзя.").withStyle(ChatFormatting.RED));
            return;
        }
        PlayerRoleData a = ModCapabilities.get(from);
        PlayerRoleData b = ModCapabilities.get(to);
        if (a == null || b == null || a.getRole() == null || b.getRole() == null) {
            from.sendSystemMessage(Component.literal("Обе стороны должны выбрать роль.").withStyle(ChatFormatting.RED));
            return;
        }
        PENDING.put(to.getUUID(), new Request(from.getUUID(), from.level().getGameTime() + 1200));
        from.sendSystemMessage(Component.literal("Предложение обмена отправлено игроку "
                + to.getDisplayName().getString() + ". Предмет в твоей основной руке пойдёт в обмен.")
                .withStyle(ChatFormatting.YELLOW));
        to.sendSystemMessage(Component.literal(from.getDisplayName().getString()
                + " предлагает обмен. Возьми свой предмет в основную руку и нажми: ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("[ПРИНЯТЬ]").withStyle(s -> s
                        .withColor(ChatFormatting.GREEN)
                        .withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/role accept")))));
    }

    public static void accept(ServerPlayer target) {
        Request req = PENDING.remove(target.getUUID());
        if (req == null || req.expiresAt() < target.level().getGameTime()) {
            target.sendSystemMessage(Component.literal("Нет активных предложений обмена.").withStyle(ChatFormatting.RED));
            return;
        }
        ServerPlayer from = target.getServer().getPlayerList().getPlayer(req.from());
        if (from == null) {
            target.sendSystemMessage(Component.literal("Второй игрок вышел.").withStyle(ChatFormatting.RED));
            return;
        }
        if (from.level() != target.level() || from.distanceToSqr(target) > 100) {
            msgBoth(from, target, "Вы слишком далеко друг от друга (нужно в пределах 10 блоков).");
            return;
        }
        ItemStack a = from.getMainHandItem().copy();
        ItemStack b = target.getMainHandItem().copy();
        if (a.isEmpty() || b.isEmpty()) {
            msgBoth(from, target, "У обоих должен быть предмет в основной руке.");
            return;
        }
        from.setItemInHand(InteractionHand.MAIN_HAND, b);
        target.setItemInHand(InteractionHand.MAIN_HAND, a);
        msgBoth(from, target, "Обмен состоялся! Хорошая сделка.");

        PlayerRoleData dFrom = ModCapabilities.get(from);
        PlayerRoleData dTarget = ModCapabilities.get(target);
        dFrom.getTradePartners().add(target.getUUID().toString());
        dTarget.getTradePartners().add(from.getUUID().toString());
        checkRolik(from, dFrom, a, b);
        checkRolik(target, dTarget, b, a);
    }

    private static void checkRolik(ServerPlayer p, PlayerRoleData d, ItemStack gave, ItemStack got) {
        if (d.getRole() != Role.ROLIK) {
            return;
        }
        if (d.getTradePartners().size() >= 3) {
            TaskManager.complete(p, "trade3");
        }
        if (gave.is(Items.DIAMOND) && got.is(Items.EMERALD) && got.getCount() == 1) {
            TaskManager.complete(p, "sell_diamond");
        }
    }

    private static void msgBoth(ServerPlayer a, ServerPlayer b, String text) {
        a.sendSystemMessage(Component.literal(text).withStyle(ChatFormatting.AQUA));
        b.sendSystemMessage(Component.literal(text).withStyle(ChatFormatting.AQUA));
    }
}

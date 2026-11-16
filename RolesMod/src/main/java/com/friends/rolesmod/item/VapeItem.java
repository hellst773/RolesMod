package com.friends.rolesmod.item;

import com.friends.rolesmod.ModCapabilities;
import com.friends.rolesmod.role.PlayerRoleData;
import com.friends.rolesmod.role.Role;
import com.friends.rolesmod.role.TaskManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Ашкудика - шуточная одноразка. */
public class VapeItem extends Item {
    private static final String[] PHRASES = {
            "Облако размером с дом. Соседи вызывают МЧС.",
            "Вкус: арбуз-лёд-безысходность.",
            "Затяжка засчитана. Osiris D3 одобряют.",
            "Пых. Теперь можно и в волейбол.",
            "Ашкудика работает. Где-то плачет кальян."
    };

    public VapeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            if (sp.getCooldowns().isOnCooldown(this)) {
                return InteractionResultHolder.fail(stack);
            }
            sp.getCooldowns().addCooldown(this, 40);
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6F, 1.6F);
            ((ServerLevel) level).sendParticles(ParticleTypes.CLOUD,
                    sp.getX(), sp.getEyeY(), sp.getZ(), 15, 0.15, 0.1, 0.15, 0.02);

            PlayerRoleData d = ModCapabilities.get(sp);
            if (d != null && d.getRole() == Role.ILYUHA) {
                puff(sp, d);
            } else {
                sp.displayClientMessage(Component.literal("Это одноразка Илюхи. Ты просто пыхтишь в пустоту.")
                        .withStyle(ChatFormatting.GRAY), true);
            }
            stack.hurtAndBreak(1, sp, e -> e.broadcastBreakEvent(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static void puff(ServerPlayer p, PlayerRoleData d) {
        long now = p.level().getGameTime();
        boolean onTime = !d.isWithdrawal() && (now - d.getLastVape()) <= 2400;
        d.setStreak(onTime ? d.getStreak() + 1 : 1);
        d.setLastVape(now);
        d.setWithdrawal(false);
        p.removeEffect(MobEffects.CONFUSION);
        p.removeEffect(MobEffects.WEAKNESS);
        String phrase = PHRASES[p.getRandom().nextInt(PHRASES.length)];
        p.displayClientMessage(Component.literal("Затяжек подряд: " + d.getStreak() + ". " + phrase)
                .withStyle(ChatFormatting.AQUA), true);
        if (d.getStreak() >= 5) {
            TaskManager.complete(p, "vape");
        }
    }
}

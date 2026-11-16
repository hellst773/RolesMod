package com.friends.rolesmod;

import com.friends.rolesmod.role.PlayerRoleData;
import com.friends.rolesmod.role.Role;
import com.friends.rolesmod.role.RoleProvider;
import com.friends.rolesmod.role.TaskManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.TradeWithVillagerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RolesMod.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(RolesMod.MODID, "role"), new RoleProvider());
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        PlayerRoleData oldData = ModCapabilities.get(event.getOriginal());
        PlayerRoleData newData = ModCapabilities.get(event.getEntity());
        if (oldData != null && newData != null) {
            newData.copyFrom(oldData);
        }
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        PlayerRoleData d = ModCapabilities.get(event.getEntity());
        if (d != null && d.getRole() != null) {
            event.setDisplayname(Component.literal(d.getRole().nick));
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            PlayerRoleData d = ModCapabilities.get(p);
            if (d != null && d.getRole() != null) {
                p.refreshDisplayName();
                if (d.getRole() == Role.ILYUHA) {
                    d.setLastVape(p.level().getGameTime());
                    d.setWithdrawal(false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        RoleCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && event.getDistance() >= 10.0F) {
            PlayerRoleData d = ModCapabilities.get(p);
            if (d != null && d.getRole() == Role.SANYA) {
                TaskManager.complete(p, "trick");
            }
        }
    }

    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer p
                && event.getCrafting().is(ModItems.VAPE.get())) {
            PlayerRoleData d = ModCapabilities.get(p);
            if (d != null && d.getRole() == Role.ROLIK) {
                TaskManager.complete(p, "vapes");
            }
        }
    }

    @SubscribeEvent
    public static void onVillagerTrade(TradeWithVillagerEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && event.getAbstractVillager() instanceof Villager) {
            PlayerRoleData d = ModCapabilities.get(p);
            if (d != null && d.getRole() == Role.ROLIK) {
                TaskManager.complete(p, "village");
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != LogicalSide.SERVER) {
            return;
        }
        if (!(event.player instanceof ServerPlayer p) || p.tickCount % 20 != 0) {
            return;
        }
        PlayerRoleData d = ModCapabilities.get(p);
        if (d == null) {
            return;
        }
        if (d.getRole() == null) {
            if (p.tickCount % 100 == 0) {
                TaskManager.openScreen(p);
            }
            return;
        }
        switch (d.getRole()) {
            case SANYA -> tickSanya(p);
            case SASHA -> tickSasha(p);
            case ILYUHA -> tickIlyuha(p, d);
            default -> { }
        }
    }

    private static void tickSanya(ServerPlayer p) {
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(Items.DIAMOND_SWORD) && s.hasCustomHoverName()
                    && s.getHoverName().getString().trim().equalsIgnoreCase("катана")) {
                TaskManager.complete(p, "katana");
                return;
            }
        }
    }

    private static void tickSasha(ServerPlayer p) {
        int emeralds = 0;
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(Items.EMERALD)) {
                emeralds += s.getCount();
            }
        }
        if (emeralds >= 10) {
            TaskManager.complete(p, "emeralds");
        }
        for (ServerPlayer o : p.serverLevel().players()) {
            if (o == p) {
                continue;
            }
            PlayerRoleData od = ModCapabilities.get(o);
            if (od != null && od.getRole() != null && o.distanceToSqr(p) <= 25.0) {
                TaskManager.complete(p, "meet");
                break;
            }
        }
    }

    private static void tickIlyuha(ServerPlayer p, PlayerRoleData d) {
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(ModItems.OSIRIS_D3.get())) {
                TaskManager.complete(p, "osiris");
                break;
            }
        }
        if (p.getItemBySlot(EquipmentSlot.FEET).is(ModItems.OSIRIS_D3.get())) {
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, true, false, false));
        }
        long since = p.level().getGameTime() - d.getLastVape();
        if (since >= 2200 && since < 2220) {
            p.displayClientMessage(Component.literal("Через 10 секунд начнётся ломка. Где ашкудика?!")
                    .withStyle(ChatFormatting.RED), true);
        }
        if (since > 2400) {
            if (!d.isWithdrawal()) {
                d.setWithdrawal(true);
                d.setStreak(0);
                p.sendSystemMessage(Component.literal("Илюху накрыла ломка. Срочно найти одноразку!")
                        .withStyle(ChatFormatting.DARK_RED));
            }
            p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
            p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
        }
    }
}

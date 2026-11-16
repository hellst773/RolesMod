package com.friends.rolesmod;

import com.friends.rolesmod.role.PlayerRoleData;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class ModCapabilities {
    public static final Capability<PlayerRoleData> ROLE =
            CapabilityManager.get(new CapabilityToken<>() {});

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(PlayerRoleData.class);
    }

    /** Возвращает данные роли игрока или null. */
    public static PlayerRoleData get(Player player) {
        return player.getCapability(ROLE).resolve().orElse(null);
    }
}

package com.friends.rolesmod.client;

import net.minecraft.client.Minecraft;

public class ClientHooks {
    public static void openRoleScreen(int takenMask) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof RoleSelectScreen) {
            return;
        }
        mc.setScreen(new RoleSelectScreen(takenMask));
    }
}

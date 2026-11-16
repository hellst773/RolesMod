package com.friends.rolesmod;

import com.friends.rolesmod.network.OpenRoleScreenPacket;
import com.friends.rolesmod.network.SelectRolePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RolesMod.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(OpenRoleScreenPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenRoleScreenPacket::encode)
                .decoder(OpenRoleScreenPacket::decode)
                .consumerMainThread(OpenRoleScreenPacket::handle)
                .add();

        CHANNEL.messageBuilder(SelectRolePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SelectRolePacket::encode)
                .decoder(SelectRolePacket::decode)
                .consumerMainThread(SelectRolePacket::handle)
                .add();
    }
}

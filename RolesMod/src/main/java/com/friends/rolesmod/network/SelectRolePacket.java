package com.friends.rolesmod.network;

import com.friends.rolesmod.role.TaskManager;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** Клиент -> сервер: игрок выбрал роль. */
public class SelectRolePacket {
    public final int roleOrdinal;

    public SelectRolePacket(int roleOrdinal) {
        this.roleOrdinal = roleOrdinal;
    }

    public static void encode(SelectRolePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.roleOrdinal);
    }

    public static SelectRolePacket decode(FriendlyByteBuf buf) {
        return new SelectRolePacket(buf.readInt());
    }

    public static void handle(SelectRolePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if (sender != null) {
            TaskManager.selectRole(sender, msg.roleOrdinal);
        }
        ctx.get().setPacketHandled(true);
    }
}

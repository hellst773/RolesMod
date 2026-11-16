package com.friends.rolesmod.network;

import com.friends.rolesmod.client.ClientHooks;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/** Сервер -> клиент: открыть окно выбора роли. */
public class OpenRoleScreenPacket {
    public final int takenMask;

    public OpenRoleScreenPacket(int takenMask) {
        this.takenMask = takenMask;
    }

    public static void encode(OpenRoleScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.takenMask);
    }

    public static OpenRoleScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenRoleScreenPacket(buf.readInt());
    }

    public static void handle(OpenRoleScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openRoleScreen(msg.takenMask));
        ctx.get().setPacketHandled(true);
    }
}

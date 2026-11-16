package com.friends.rolesmod.role;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** Мировое хранилище: какая роль кем занята (чтобы роли не повторялись). */
public class RoleRegistry extends SavedData {
    private final Map<String, UUID> owners = new HashMap<>();

    public static RoleRegistry get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(RoleRegistry::load, RoleRegistry::new, "rolesmod_roles");
    }

    public static RoleRegistry load(CompoundTag tag) {
        RoleRegistry reg = new RoleRegistry();
        for (String key : tag.getAllKeys()) {
            reg.owners.put(key, tag.getUUID(key));
        }
        return reg;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        for (Map.Entry<String, UUID> e : owners.entrySet()) {
            tag.putUUID(e.getKey(), e.getValue());
        }
        return tag;
    }

    public boolean isTakenByOther(Role role, UUID asker) {
        UUID owner = owners.get(role.id);
        return owner != null && !owner.equals(asker);
    }

    public void take(Role role, UUID player) {
        owners.put(role.id, player);
        setDirty();
    }

    public void release(Role role) {
        owners.remove(role.id);
        setDirty();
    }

    public int takenMask() {
        int mask = 0;
        for (Role r : Role.values()) {
            if (owners.containsKey(r.id)) {
                mask |= 1 << r.ordinal();
            }
        }
        return mask;
    }
}

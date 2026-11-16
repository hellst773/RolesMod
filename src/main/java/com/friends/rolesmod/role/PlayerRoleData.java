package com.friends.rolesmod.role;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

/** Данные игрока, которые хранятся в Capability. */
public class PlayerRoleData {
    private Role role = null;
    private final Set<String> done = new HashSet<>();
    private final Set<String> tradePartners = new HashSet<>();
    private long lastVape = 0;
    private int streak = 0;
    private boolean withdrawal = false;

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Set<String> getDone() { return done; }
    public Set<String> getTradePartners() { return tradePartners; }
    public long getLastVape() { return lastVape; }
    public void setLastVape(long lastVape) { this.lastVape = lastVape; }
    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }
    public boolean isWithdrawal() { return withdrawal; }
    public void setWithdrawal(boolean withdrawal) { this.withdrawal = withdrawal; }

    public void reset() {
        role = null;
        done.clear();
        tradePartners.clear();
        lastVape = 0;
        streak = 0;
        withdrawal = false;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (role != null) {
            tag.putString("role", role.id);
        }
        ListTag doneList = new ListTag();
        for (String s : done) {
            doneList.add(StringTag.valueOf(s));
        }
        tag.put("done", doneList);
        ListTag partners = new ListTag();
        for (String s : tradePartners) {
            partners.add(StringTag.valueOf(s));
        }
        tag.put("partners", partners);
        tag.putLong("lastVape", lastVape);
        tag.putInt("streak", streak);
        tag.putBoolean("withdrawal", withdrawal);
        return tag;
    }

    public void load(CompoundTag tag) {
        reset();
        if (tag.contains("role")) {
            role = Role.byId(tag.getString("role"));
        }
        ListTag doneList = tag.getList("done", Tag.TAG_STRING);
        for (int i = 0; i < doneList.size(); i++) {
            done.add(doneList.getString(i));
        }
        ListTag partners = tag.getList("partners", Tag.TAG_STRING);
        for (int i = 0; i < partners.size(); i++) {
            tradePartners.add(partners.getString(i));
        }
        lastVape = tag.getLong("lastVape");
        streak = tag.getInt("streak");
        withdrawal = tag.getBoolean("withdrawal");
    }

    public void copyFrom(PlayerRoleData other) {
        load(other.save());
    }
}

package com.friends.rolesmod;

import com.friends.rolesmod.item.QuestBookItem;
import com.friends.rolesmod.item.VapeItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RolesMod.MODID);

    public static final RegistryObject<Item> VAPE = ITEMS.register("ashkudika",
            () -> new VapeItem(new Item.Properties().durability(64)));

    public static final RegistryObject<Item> OSIRIS_D3 = ITEMS.register("osiris_d3",
            () -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final RegistryObject<Item> QUEST_BOOK = ITEMS.register("quest_book",
            () -> new QuestBookItem(new Item.Properties().stacksTo(1)));
}

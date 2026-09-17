package com.sinthspark.mech.events;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.registration.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MECH.MOD_ID)
public class VillageLootEvents {

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation id = event.getName();

        if (isVillageHouse(id)) {
            event.getTable().addPool(LootPool.lootPool()
                    .add(LootItem.lootTableItem(ItemRegistry.RAPESEED.get())
                            .setWeight(3)
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4))))
                    .build());
        }
    }

    private static boolean isVillageHouse(ResourceLocation id) {
        String path = id.getPath();
        return path.startsWith("chests/village/village_") && path.endsWith("_house");
    }
}

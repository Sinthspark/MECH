package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.SteamCompressorBlock;
import com.sinthspark.mech.recipe.CompressingRecipe;
import com.sinthspark.mech.recipe.ModRecipes;
import com.sinthspark.mech.screen.SteamCompressorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Base-tier steam compressor. See {@link AbstractSteamProcessorTile} for the shared logic. */
public class SteamCompressorTile extends AbstractSteamProcessorTile<CompressingRecipe> {
    private static final int TANK_CAPACITY = 4000;
    public SteamCompressorTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STEAM_COMPRESSOR.get(), pos, state, TANK_CAPACITY, 1.0f, 1.0f, false);
    }
    @Override protected RecipeType<CompressingRecipe> recipeType() { return ModRecipes.COMPRESSING_TYPE.get(); }
    @Override protected BooleanProperty activeProperty() { return SteamCompressorBlock.ACTIVE; }
    @Override protected String translationKey() { return "steam_compressor"; }
    @Override protected int steamCost(CompressingRecipe recipe) { return recipe.getSteamCost(); }
    @Override protected int processTime(CompressingRecipe recipe) { return recipe.getProcessTime(); }
    @Override protected ItemStack resultItem(CompressingRecipe recipe, Level level) { return recipe.getResultItem(level.registryAccess()); }
    @Override protected int inputConsumeCount(CompressingRecipe recipe) { return recipe.getInputCount(); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) { return new SteamCompressorMenu(id, playerInv, this); }
}

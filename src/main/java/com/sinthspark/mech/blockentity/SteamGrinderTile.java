package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.SteamGrinderBlock;
import com.sinthspark.mech.recipe.GrindingRecipe;
import com.sinthspark.mech.recipe.ModRecipes;
import com.sinthspark.mech.screen.SteamGrinderMenu;
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

/** Base-tier steam grinder. See {@link AbstractSteamProcessorTile} for the shared logic. */
public class SteamGrinderTile extends AbstractSteamProcessorTile<GrindingRecipe> {

    private static final int TANK_CAPACITY = 4000;

    public SteamGrinderTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STEAM_GRINDER.get(), pos, state, TANK_CAPACITY, 1.0f, 1.0f, true);
    }

    @Override
    protected RecipeType<GrindingRecipe> recipeType() { return ModRecipes.GRINDING_TYPE.get(); }

    @Override
    protected BooleanProperty activeProperty() { return SteamGrinderBlock.ACTIVE; }

    @Override
    protected String translationKey() { return "steam_grinder"; }

    @Override
    protected int steamCost(GrindingRecipe recipe) { return recipe.getSteamCost(); }

    @Override
    protected int processTime(GrindingRecipe recipe) { return recipe.getProcessTime(); }

    @Override
    protected ItemStack resultItem(GrindingRecipe recipe, Level level) { return recipe.getResultItem(level.registryAccess()); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
        return new SteamGrinderMenu(id, playerInv, this);
    }
}

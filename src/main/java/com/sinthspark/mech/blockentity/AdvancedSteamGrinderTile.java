package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.AdvancedSteamGrinderBlock;
import com.sinthspark.mech.recipe.GrindingRecipe;
import com.sinthspark.mech.recipe.ModRecipes;
import com.sinthspark.mech.screen.AdvancedSteamGrinderMenu;
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

/** Advanced-tier steam grinder. See {@link AbstractSteamProcessorTile} for the shared logic. */
public class AdvancedSteamGrinderTile extends AbstractSteamProcessorTile<GrindingRecipe> {

    private static final int TANK_CAPACITY = 16000;

    public AdvancedSteamGrinderTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_STEAM_GRINDER.get(), pos, state, TANK_CAPACITY, 9.0f, 0.5f, false);
    }

    @Override
    protected RecipeType<GrindingRecipe> recipeType() { return ModRecipes.GRINDING_TYPE.get(); }

    @Override
    protected BooleanProperty activeProperty() { return AdvancedSteamGrinderBlock.ACTIVE; }

    @Override
    protected String translationKey() { return "advanced_steam_grinder"; }

    @Override
    protected int steamCost(GrindingRecipe recipe) { return recipe.getSteamCost(); }

    @Override
    protected int processTime(GrindingRecipe recipe) { return recipe.getProcessTime(); }

    @Override
    protected ItemStack resultItem(GrindingRecipe recipe, Level level) { return recipe.getResultItem(level.registryAccess()); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
        return new AdvancedSteamGrinderMenu(id, playerInv, this);
    }
}

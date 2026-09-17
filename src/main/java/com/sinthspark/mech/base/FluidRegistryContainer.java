package com.sinthspark.mech.base;

import com.sinthspark.mech.blocks.SolidifyingFluidBlock;
import com.sinthspark.mech.registration.BlockRegistry;
import com.sinthspark.mech.registration.FluidRegistry;
import com.sinthspark.mech.registration.ItemRegistry;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FluidRegistryContainer {
    public final RegistryObject<FluidType> type;
    public final RegistryObject<? extends Block> block;
    public final RegistryObject<BucketItem> bucket;
    public final RegistryObject<ForgeFlowingFluid.Source> source;
    public final RegistryObject<ForgeFlowingFluid.Flowing> flowing;
    private final ForgeFlowingFluid.Properties properties;

    public FluidRegistryContainer(String name,
                                  FluidType.Properties typeProperties,
                                  Supplier<IClientFluidTypeExtensions> clientExtensions,
                                  @Nullable AdditionalProperties additionalProperties,
                                  BlockBehaviour.Properties blockProperties,
                                  Item.Properties itemProperties,
                                  @Nullable Supplier<? extends Block> solidBlockSupplier,
                                  int solidifyTime) {

        this.type = FluidRegistry.FLUID_TYPES.register(name, () -> new FluidType(typeProperties) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(clientExtensions.get());
            }
        });

        AtomicReference<RegistryObject<ForgeFlowingFluid.Source>> sourceRef = new AtomicReference<>();
        AtomicReference<RegistryObject<ForgeFlowingFluid.Flowing>> flowingRef = new AtomicReference<>();

        this.properties = new ForgeFlowingFluid.Properties(
                this.type,
                () -> sourceRef.get() != null ? sourceRef.get().get() : null,
                () -> flowingRef.get() != null ? flowingRef.get().get() : null
        );

        if (additionalProperties != null) {
            additionalProperties.applyTo(this.properties);
        }

        this.source = FluidRegistry.FLUIDS.register(name, () -> new ForgeFlowingFluid.Source(this.properties));
        this.flowing = FluidRegistry.FLUIDS.register(name + "_flowing",
                () -> new ForgeFlowingFluid.Flowing(this.properties));

        sourceRef.set(this.source);
        flowingRef.set(this.flowing);

        BlockBehaviour.Properties finalProperties = blockProperties.replaceable();

        if (solidBlockSupplier != null) {
            this.block = BlockRegistry.BLOCKS.register(name,
                    () -> new SolidifyingFluidBlock(
                            this.source.get(),
                            solidBlockSupplier.get(),
                            solidifyTime,
                            finalProperties
                    )
            );
        } else {
            this.block = BlockRegistry.BLOCKS.register(name,
                    () -> new LiquidBlock(this.source, finalProperties)
            );
        }

        this.properties.block(() -> (LiquidBlock) this.block.get());
        this.bucket = ItemRegistry.ITEMS.register(name + "_bucket",
                () -> new BucketItem(this.source, itemProperties)
        );
        this.properties.bucket(this.bucket);
    }

    public FluidRegistryContainer(String name,
                                  FluidType.Properties typeProperties,
                                  Supplier<IClientFluidTypeExtensions> clientExtensions,
                                  @Nullable AdditionalProperties additionalProperties,
                                  BlockBehaviour.Properties blockProperties,
                                  Item.Properties itemProperties,
                                  @Nullable Block solidBlock,
                                  int solidifyTime) {
        this(name, typeProperties, clientExtensions, additionalProperties, blockProperties, itemProperties,
                solidBlock != null ? () -> solidBlock : null, solidifyTime);
    }

    public FluidRegistryContainer(String name,
                                  FluidType.Properties typeProperties,
                                  Supplier<IClientFluidTypeExtensions> clientExtensions,
                                  @Nullable AdditionalProperties additionalProperties,
                                  BlockBehaviour.Properties blockProperties,
                                  Item.Properties itemProperties,
                                  @Nullable Block solidBlock) {
        this(name, typeProperties, clientExtensions, additionalProperties, blockProperties, itemProperties, solidBlock, 1200);
    }

    @SuppressWarnings("unused")
    public FluidRegistryContainer(String name,
                                  FluidType.Properties typeProperties,
                                  Supplier<IClientFluidTypeExtensions> clientExtensions,
                                  BlockBehaviour.Properties blockProperties,
                                  Item.Properties itemProperties,
                                  Block solidBlock) {
        this(name, typeProperties, clientExtensions, null, blockProperties, itemProperties, solidBlock, 1200);
    }

    @SuppressWarnings("unused")
    public FluidRegistryContainer(String name,
                                  FluidType.Properties typeProperties,
                                  Supplier<IClientFluidTypeExtensions> clientExtensions,
                                  BlockBehaviour.Properties blockProperties,
                                  Item.Properties itemProperties) {
        this(name, typeProperties, clientExtensions, null, blockProperties, itemProperties, (Block) null, 1200);
    }

    public ForgeFlowingFluid.Properties getProperties() {
        return this.properties;
    }

    @SuppressWarnings("unchecked")
    public RegistryObject<LiquidBlock> getBlock() {
        return (RegistryObject<LiquidBlock>) this.block;
    }

    public static IClientFluidTypeExtensions createExtension(ClientExtensions extensions) {
        return new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getFlowingTexture() {
                return extensions.flowing;
            }

            @Nullable
            @Override
            public ResourceLocation getOverlayTexture() {
                return extensions.overlay;
            }

            @Override
            public ResourceLocation getRenderOverlayTexture(Minecraft minecraft) {
                return extensions.renderOverlay;
            }

            @Override
            public ResourceLocation getStillTexture() {
                return extensions.still;
            }

            @Override
            public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                return extensions.tintFunction == null
                        ? 0xFFFFFFFF
                        : extensions.tintFunction.apply(state, getter, pos);
            }

            @Override
            public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level,
                                                    int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                return extensions.fogColor == null
                        ? IClientFluidTypeExtensions.super.modifyFogColor(camera, partialTick, level,
                        renderDistance, darkenWorldAmount, fluidFogColor)
                        : extensions.fogColor;
            }

            @Override
            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance,
                                        float partialTick, float nearDistance, float farDistance, FogShape shape) {
                RenderSystem.setShaderFogStart(1f);
                RenderSystem.setShaderFogEnd(6f);
            }
        };
    }

    public static class AdditionalProperties {
        private int levelDecreasePerBlock = 1;
        private float explosionResistance = 1;
        private int slopeFindDistance = 4;
        private int tickRate = 5;

        void applyTo(ForgeFlowingFluid.Properties props) {
            props.explosionResistance(explosionResistance)
                    .levelDecreasePerBlock(levelDecreasePerBlock)
                    .slopeFindDistance(slopeFindDistance)
                    .tickRate(tickRate);
        }

        @SuppressWarnings("unused")
        public AdditionalProperties explosionResistance(float resistance) {
            this.explosionResistance = resistance;
            return this;
        }

        @SuppressWarnings("unused")
        public AdditionalProperties levelDecreasePerBlock(int decrease) {
            this.levelDecreasePerBlock = decrease;
            return this;
        }

        @SuppressWarnings("unused")
        public AdditionalProperties slopeFindDistance(int distance) {
            this.slopeFindDistance = distance;
            return this;
        }

        @SuppressWarnings("unused")
        public AdditionalProperties tickRate(int rate) {
            this.tickRate = rate;
            return this;
        }
    }

    public static class ClientExtensions {
        private ResourceLocation still;
        private ResourceLocation flowing;
        private ResourceLocation overlay;
        private ResourceLocation renderOverlay;
        private Vector3f fogColor;
        private TriFunction<FluidState, BlockAndTintGetter, BlockPos, Integer> tintFunction;
        private final String modid;

        public ClientExtensions(String modid, String fluidName) {
            this.modid = modid;
            this.still = stillTexture(fluidName);
            this.flowing = flowingTexture(fluidName);
            this.overlay = overlayTexture(fluidName);
        }

        @SuppressWarnings("unused")
        public ClientExtensions still(String name) {
            this.still = stillTexture(name);
            return this;
        }

        @SuppressWarnings("unused")
        public ClientExtensions still(String name, String folder) {
            this.still = stillTexture(name, folder);
            return this;
        }

        @SuppressWarnings("unused")
        public ClientExtensions flowing(String name) {
            this.flowing = flowingTexture(name);
            return this;
        }

        @SuppressWarnings("unused")
        public ClientExtensions flowing(String name, String folder) {
            this.flowing = flowingTexture(name, folder);
            return this;
        }

        @SuppressWarnings("unused")
        public ClientExtensions overlay(String name) {
            this.overlay = overlayTexture(name);
            return this;
        }

        @SuppressWarnings("unused")
        public ClientExtensions overlay(String name, String folder) {
            this.overlay = overlayTexture(name, folder);
            return renderOverlay(new ResourceLocation(this.modid, "textures/" + folder + "/" + name + "_overlay.png"));
        }

        @SuppressWarnings("unused")
        public ClientExtensions renderOverlay(ResourceLocation path) {
            this.renderOverlay = path;
            return this;
        }

        @SuppressWarnings("unused")
        public ClientExtensions fogColor(float red, float green, float blue) {
            this.fogColor = new Vector3f(red, green, blue);
            return this;
        }

        @SuppressWarnings("unused")
        public ClientExtensions tint(int tint) {
            this.tintFunction = ($0, $1, $2) -> tint;
            return this;
        }

        @SuppressWarnings("unused")
        public ClientExtensions tint(TriFunction<FluidState, BlockAndTintGetter, BlockPos, Integer> tinter) {
            this.tintFunction = tinter;
            return this;
        }

        private ResourceLocation stillTexture(String name) {
            return stillTexture(name, "block");
        }

        private ResourceLocation stillTexture(String name, String folder) {
            return new ResourceLocation(this.modid, folder + "/" + name + "_still");
        }

        private ResourceLocation flowingTexture(String name) {
            return flowingTexture(name, "block");
        }

        private ResourceLocation flowingTexture(String name, String folder) {
            return new ResourceLocation(this.modid, folder + "/" + name + "_flowing");
        }

        private ResourceLocation overlayTexture(String name) {
            return overlayTexture(name, "block");
        }

        private ResourceLocation overlayTexture(String name, String folder) {
            return new ResourceLocation(this.modid, folder + "/" + name + "_overlay");
        }
    }
}
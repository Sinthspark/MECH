package com.sinthspark.mech.compat.jade;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.blockentity.*;
import com.sinthspark.mech.blocks.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        // Серверная часть не нужна
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Информация о проводах
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                BlockEntity be = accessor.getBlockEntity();
                if (be instanceof WireTile wire) {
                    tooltip.add(Component.translatable("jade.mech.wire.status",
                            wire.isEnergized() ? Component.translatable("jade.mech.status.energized") : Component.translatable("jade.mech.status.off")));
                    tooltip.add(Component.translatable("jade.mech.wire.insulation",
                            wire.isInsulated() ? Component.translatable("jade.mech.status.yes") : Component.translatable("jade.mech.status.no")));
                    tooltip.add(Component.translatable("jade.mech.wire.max_transfer", wire.getMaxTransfer()));
                    tooltip.add(Component.translatable("jade.mech.wire.loss", wire.getLossPerBlock()));
                }
            }

            @Override
            public ResourceLocation getUid() {
                return new ResourceLocation(MECH.MOD_ID, "wire_info");
            }
        }, WireBlock.class);

        // Информация о трубах
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                BlockEntity be = accessor.getBlockEntity();
                if (be instanceof PipeTile pipe) {
                    tooltip.add(Component.translatable("jade.mech.pipe.steam",
                            pipe.getFluidAmount(), pipe.getCapacity()));
                    tooltip.add(Component.translatable("jade.mech.pipe.insulation",
                            pipe.isInsulated() ? Component.translatable("jade.mech.status.yes") : Component.translatable("jade.mech.status.no")));
                }
            }

            @Override
            public ResourceLocation getUid() {
                return new ResourceLocation(MECH.MOD_ID, "pipe_info");
            }
        }, PipeBlock.class);

        // Информация о водяном колесе
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                BlockEntity be = accessor.getBlockEntity();
                if (be instanceof WaterWheelTile wheel) {
                    tooltip.add(Component.translatable("jade.mech.kinetic.status",
                            wheel.isActive ? Component.translatable("jade.mech.status.rotating") : Component.translatable("jade.mech.status.stopped")));
                    tooltip.add(Component.translatable("jade.mech.kinetic.power", String.format("%.1f", wheel.power)));
                    tooltip.add(Component.translatable("jade.mech.kinetic.speed", String.format("%.1f", wheel.perTick)));
                }
            }

            @Override
            public ResourceLocation getUid() {
                return new ResourceLocation(MECH.MOD_ID, "water_wheel_info");
            }
        }, WaterWheelBlock.class);

        // Информация о шестерне
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                BlockEntity be = accessor.getBlockEntity();
                if (be instanceof VulcaniteGearTile gear) {
                    tooltip.add(Component.translatable("jade.mech.kinetic.status",
                            gear.isActive ? Component.translatable("jade.mech.status.rotating") : Component.translatable("jade.mech.status.stopped")));
                    tooltip.add(Component.translatable("jade.mech.kinetic.input_power", String.format("%.1f", gear.inputPower)));
                    tooltip.add(Component.translatable("jade.mech.kinetic.speed", String.format("%.1f", gear.perTick)));
                    tooltip.add(Component.translatable("jade.mech.lubrication",
                            gear.lubricated ? Component.translatable("jade.mech.status.yes") : Component.translatable("jade.mech.status.no")));
                }
            }

            @Override
            public ResourceLocation getUid() {
                return new ResourceLocation(MECH.MOD_ID, "gear_info");
            }
        }, VulcaniteGearBlock.class);

        // Информация о подшипнике
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                BlockEntity be = accessor.getBlockEntity();
                if (be instanceof VulcaniteBearingTile bearing) {
                    tooltip.add(Component.translatable("jade.mech.kinetic.status",
                            bearing.isActive ? Component.translatable("jade.mech.status.rotating") : Component.translatable("jade.mech.status.stopped")));
                    tooltip.add(Component.translatable("jade.mech.kinetic.input_power", String.format("%.1f", bearing.inputPower)));
                    tooltip.add(Component.translatable("jade.mech.kinetic.output_power", String.format("%.1f", bearing.outputPower)));
                    tooltip.add(Component.translatable("jade.mech.kinetic.speed", String.format("%.1f", bearing.perTick)));
                    tooltip.add(Component.translatable("jade.mech.lubrication",
                            bearing.lubricated ? Component.translatable("jade.mech.status.yes") : Component.translatable("jade.mech.status.no")));
                }
            }

            @Override
            public ResourceLocation getUid() {
                return new ResourceLocation(MECH.MOD_ID, "bearing_info");
            }
        }, VulcaniteBearingBlock.class);

        // Информация о ручном генераторе
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                BlockEntity be = accessor.getBlockEntity();
                if (be instanceof ManualKineticGeneratorTile gen) {
                    tooltip.add(Component.translatable("jade.mech.kinetic.status",
                            gen.isActive ? Component.translatable("jade.mech.status.working") : Component.translatable("jade.mech.status.stopped")));
                    tooltip.add(Component.translatable("jade.mech.kinetic.power", String.format("%.1f", gen.getPower())));
                    tooltip.add(Component.translatable("jade.mech.kinetic.speed", String.format("%.1f", gen.perTick)));
                }
            }

            @Override
            public ResourceLocation getUid() {
                return new ResourceLocation(MECH.MOD_ID, "manual_generator_info");
            }
        }, ManualKineticGeneratorBlock.class);

        // Информация о прессе
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                BlockEntity be = accessor.getBlockEntity();
                if (be instanceof PressTile press) {
                    tooltip.add(Component.translatable("jade.mech.progress",
                            String.format("%.0f%%", (press.progress / press.maxProgress) * 100)));
                    tooltip.add(Component.translatable("jade.mech.kinetic.power", String.format("%.1f", press.inputPower)));
                    tooltip.add(Component.translatable("jade.mech.lubrication",
                            press.lubricated ? Component.translatable("jade.mech.status.yes") : Component.translatable("jade.mech.status.no")));
                }
            }

            @Override
            public ResourceLocation getUid() {
                return new ResourceLocation(MECH.MOD_ID, "press_info");
            }
        }, PressBlock.class);

        // Информация о маслопрессе
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                BlockEntity be = accessor.getBlockEntity();
                if (be instanceof OilPressTile oilPress) {
                    tooltip.add(Component.translatable("jade.mech.progress",
                            String.format("%.0f%%", (oilPress.progress / oilPress.maxProgress) * 100)));
                    tooltip.add(Component.translatable("jade.mech.oil",
                            oilPress.getFluidTank().getFluidAmount(), oilPress.getFluidTank().getCapacity()));
                    tooltip.add(Component.translatable("jade.mech.kinetic.power", String.format("%.1f", oilPress.inputPower)));
                    tooltip.add(Component.translatable("jade.mech.lubrication",
                            oilPress.lubricated ? Component.translatable("jade.mech.status.yes") : Component.translatable("jade.mech.status.no")));
                }
            }

            @Override
            public ResourceLocation getUid() {
                return new ResourceLocation(MECH.MOD_ID, "oil_press_info");
            }
        }, OilPressBlock.class);

        // Информация о жерновах
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                BlockEntity be = accessor.getBlockEntity();
                if (be instanceof QuernTile quern) {
                    tooltip.add(Component.translatable("jade.mech.progress",
                            String.format("%.0f%%", (quern.progress / quern.maxProgress) * 100)));
                    tooltip.add(Component.translatable("jade.mech.kinetic.power", String.format("%.1f", quern.inputPower)));
                    tooltip.add(Component.translatable("jade.mech.lubrication",
                            quern.lubricated ? Component.translatable("jade.mech.status.yes") : Component.translatable("jade.mech.status.no")));
                }
            }

            @Override
            public ResourceLocation getUid() {
                return new ResourceLocation(MECH.MOD_ID, "quern_info");
            }
        }, QuernBlock.class);

        // Информация о лесопилке
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                BlockEntity be = accessor.getBlockEntity();
                if (be instanceof SawmillTile sawmill) {
                    tooltip.add(Component.translatable("jade.mech.progress",
                            String.format("%.0f%%", (sawmill.progress / sawmill.maxProgress) * 100)));
                    tooltip.add(Component.translatable("jade.mech.kinetic.power", String.format("%.1f", sawmill.inputPower)));
                    tooltip.add(Component.translatable("jade.mech.lubrication",
                            sawmill.lubricated ? Component.translatable("jade.mech.status.yes") : Component.translatable("jade.mech.status.no")));
                }
            }

            @Override
            public ResourceLocation getUid() {
                return new ResourceLocation(MECH.MOD_ID, "sawmill_info");
            }
        }, SawmillBlock.class);

        // Универсальная информация об энергии для всех блоков
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                BlockEntity be = accessor.getBlockEntity();
                if (be != null) {
                    be.getCapability(ForgeCapabilities.ENERGY, null).resolve().ifPresent(storage -> {
                        tooltip.add(Component.translatable("jade.mech.energy",
                                storage.getEnergyStored(), storage.getMaxEnergyStored()));
                    });
                }
            }

            @Override
            public ResourceLocation getUid() {
                return new ResourceLocation(MECH.MOD_ID, "energy_info");
            }
        }, Block.class);
    }
}

package com.sinthspark.mech.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {

    public static class Client {
        public final ForgeConfigSpec.BooleanValue doubleVisionEnabled;

        Client(ForgeConfigSpec.Builder builder) {
            builder.push("client");

            doubleVisionEnabled = builder
                    .comment(
                            "Enable double vision effect when shocked.",
                            "WARNING: This effect may cause seizures in people with photosensitive epilepsy.",
                            "Disable this if you or someone watching has photosensitivity.",
                            "Default: true"
                    )
                    .define("doubleVisionEnabled", true);

            builder.pop();
        }
    }

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        CLIENT = new Client(builder);
        CLIENT_SPEC = builder.build();
    }
}

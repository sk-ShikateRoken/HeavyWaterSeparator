package com.shikateroken.heavy_water_separator;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // tank size
    public static final ForgeConfigSpec.IntValue inputTANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue outputTANK_CAPACITY;
    static {
        BUILDER.push("Machine Settings");

        // 項目名、デフォルト値、最小値、最大値を設定
        inputTANK_CAPACITY = BUILDER.comment("Capacity of the DTWHS tanks in mB (1000mB = 1 Bucket)")
                .defineInRange("inputtankCapacity", 16000000, 2000000, Integer.MAX_VALUE);
        outputTANK_CAPACITY = BUILDER.comment("Capacity of the DTWHS tanks in mB (1000mB = 1 Bucket)")
                .defineInRange("outputtankCapacity", 8000, 1000, Integer.MAX_VALUE);
        BUILDER.pop();
    }
    // Energy
    public static final ForgeConfigSpec.IntValue EnergyStorage_CAPACITY;
    public static final ForgeConfigSpec.IntValue EnergyStorage_INPUTRATE;
    public static final ForgeConfigSpec.IntValue EnergyStorage_OUTPUTRATE;
    public static final ForgeConfigSpec.IntValue EnergyCost;
    static {
        BUILDER.push("Machine Settings");
        EnergyStorage_CAPACITY = BUILDER.comment("Capacity of the DTWHS energy storage in FE")
                .defineInRange("EnergyStorageCapacity",800000,8000, Integer.MAX_VALUE);
        EnergyStorage_INPUTRATE = BUILDER.comment("Input rate of the DTWHS energy storage in FE")
                .defineInRange("EnergyStorageInputRate", Integer.MAX_VALUE, 1000,Integer.MAX_VALUE);
        EnergyStorage_OUTPUTRATE = BUILDER.comment("Output rate of the DTWHS energy storage in FE")
                .defineInRange("EnergyStorageInputRate", Integer.MAX_VALUE, 1,Integer.MAX_VALUE);
        EnergyCost = BUILDER.comment("Energy Cost of the DTHWS in FE")
                .defineInRange("EnergyCost", 400,0,Integer.MAX_VALUE);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
    }





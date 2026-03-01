package com.shikateroken.heavy_water_separator.tile;

import net.minecraft.util.StringRepresentable;

    public enum SideConfig implements StringRepresentable {
        NONE("none"),      // 無効
        INPUT("input"),    // 入力 (赤)
        OUTPUT("output"),  // 出力 (青)
        ENERGY("energy");  // 電力 (緑) - 必要なら

        private final String name;

        SideConfig(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        // 次の設定に切り替えるメソッド
        public SideConfig cycle() {
            return switch (this) {
                case NONE -> INPUT;
                case INPUT -> OUTPUT;
                case OUTPUT -> ENERGY;
                case ENERGY -> NONE;
            };
        }
}

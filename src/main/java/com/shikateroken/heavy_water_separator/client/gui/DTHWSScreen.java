package com.shikateroken.heavy_water_separator.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.shikateroken.heavy_water_separator.menu.DTHWSMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DTHWSScreen extends AbstractContainerScreen<DTHWSMenu> {
    //GUI 背景画像
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("hws","textures/gui/dthws_gui.png");
    public DTHWSScreen(DTHWSMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        // 画面タイトルなどの位置調整が必要ならここに書く
    }

    // 1. 背景と「動くゲージ」の描画
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        // 画面中央の座標を計算
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 背景画像を描画
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // --- ここからMekanism風のゲージ描画 ---

        // 1. エネルギーバー (左端に配置)
        // データ取得
        int energy = this.menu.getEnergy();
        int maxEnergy = this.menu.getMaxEnergy();
        // 高さ計算 (最大50pxとして計算)
        int energyHeight = (maxEnergy > 0) ? (energy * 50 / maxEnergy) : 0;

        // 枠線(黒)と中身(緑)を描画
        graphics.fill(x + 8, y + 20, x + 18, y + 70, 0xFF000000); // 背景(黒)
        graphics.fill(x + 9, y + 70 - energyHeight, x + 17, y + 70, 0xFF00FF00); // 中身(緑)

        // 2. 入力タンク (左側に配置)
        int fluidIn = this.menu.getFluidInAmount(); // ※Menuに追加が必要
        int maxFluidIn = this.menu.getMaxFluidInAmount(); // ※Menuに追加が必要
        int fluidInHeight = (maxFluidIn > 0) ? (fluidIn * 50 / maxFluidIn) : 0;

        graphics.fill(x + 30, y + 20, x + 46, y + 70, 0xFF000000); // 背景
        graphics.fill(x + 31, y + 70 - fluidInHeight, x + 45, y + 70, 0xFF0000FF); // 中身(青)

        // 3. プログレスバー (中央の矢印)
        int progress = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();
        int arrowWidth = (maxProgress > 0) ? (progress * 24 / maxProgress) : 0;

        // 矢印の代わりに白い四角が伸びるようにする (画像がないため)
        graphics.fill(x + 78, y + 40, x + 78 + arrowWidth, y + 56, 0xFFFFFFFF);

        // 4. 出力タンク (右側に配置)
        int fluidOut = this.menu.getFluidOutAmount(); // ※Menuに追加が必要
        int maxFluidOut = this.menu.getMaxFluidOutAmount(); // ※Menuに追加が必要
        int fluidOutHeight = (maxFluidOut > 0) ? (fluidOut * 50 / maxFluidOut) : 0;

        graphics.fill(x + 130, y + 20, x + 146, y + 70, 0xFF000000); // 背景
        graphics.fill(x + 131, y + 70 - fluidOutHeight, x + 145, y + 70, 0xFFFF8800); // 中身(オレンジ)
    }

    // 2. 文字とツールチップの描画
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        // マウスカーソルを合わせたときに数値を表示する処理
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // エネルギーバーにカーソルがあったら
        if (isHovering(8, 20, 10, 50, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"), mouseX, mouseY);
        }
    }
}

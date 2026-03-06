package com.shikateroken.heavy_water_separator.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.shikateroken.heavy_water_separator.menu.DTHWSMenu;
import com.shikateroken.heavy_water_separator.tile.SideConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DTHWSScreen extends AbstractContainerScreen<DTHWSMenu> {
    //GUI 背景画像
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("heavy_water_separator","textures/gui/dthws_gui.png");
    public DTHWSScreen(DTHWSMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 205;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

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

        // ゲージ描画

        // 1. エネルギーバー (左端に配置)
        // データ取得
        int energy = this.menu.getEnergy();
        int maxEnergy = this.menu.getMaxEnergy();
        // 高さ計算 (最大50px)
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

        // 矢印の代わりに白い四角が伸びる
        graphics.fill(x + 78, y + 40, x + 78 + arrowWidth, y + 56, 0xFFFFFFFF);

        // 4. 出力タンク (右側に配置)
        int fluidOut = this.menu.getFluidOutAmount(); // ※Menuに追加が必要
        int maxFluidOut = this.menu.getMaxFluidOutAmount(); // ※Menuに追加が必要
        int fluidOutHeight = (maxFluidOut > 0) ? (fluidOut * 50 / maxFluidOut) : 0;

        graphics.fill(x + 130, y + 20, x + 146, y + 70, 0xFF000000); // 背景
        graphics.fill(x + 131, y + 70 - fluidOutHeight, x + 145, y + 70, 0xFFFF8800); // 中身(オレンジ)
    }
    //搬出入設定ボタン位置
    private static final int CONFIG_X = 170; // 配置するX座標の基準
    private static final int CONFIG_Y = 20;  // 配置するY座標の基準
    private static final int BTN_SIZE = 10;  // ボタンのサイズ

    // 文字とツールチップの描画
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        // マウスカーソルを合わせたときに数値を表示する処理
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 各バーにカーソルがあったら
        if (isHovering(8, 20, 10, 50, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"), mouseX, mouseY);
        }
        if (isHovering(30, 20, 10, 50, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(this.menu.getFluidInAmount() + " / " + this.menu.getMaxFluidInAmount() + "mB"), mouseX, mouseY);
        }
        if (isHovering(130, 20, 10, 50, mouseX, mouseY)){
            graphics.renderTooltip(font, Component.literal(this.menu.getFluidOutAmount() + " / " + this.menu.getMaxFluidOutAmount() + "mB"), mouseX, mouseY);
        }
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        //     [U]
        //   [W][N][E]
        //      [D]
        //      [S] (背面は一番下など)

        // 各方向についてボタンを描画
        // 引数: (Graphics, Direction, X, Y, MouseX, MouseY)
        drawConfigButton(graphics, Direction.UP,    CONFIG_X + BTN_SIZE, CONFIG_Y,              mouseX, mouseY);
        drawConfigButton(graphics, Direction.WEST,  CONFIG_X,            CONFIG_Y + BTN_SIZE,   mouseX, mouseY);
        drawConfigButton(graphics, Direction.NORTH, CONFIG_X + BTN_SIZE, CONFIG_Y + BTN_SIZE,   mouseX, mouseY);
        drawConfigButton(graphics, Direction.EAST,  CONFIG_X + BTN_SIZE*2, CONFIG_Y + BTN_SIZE, mouseX, mouseY);
        drawConfigButton(graphics, Direction.DOWN,  CONFIG_X + BTN_SIZE, CONFIG_Y + BTN_SIZE*2, mouseX, mouseY);
        drawConfigButton(graphics, Direction.SOUTH, CONFIG_X + BTN_SIZE, CONFIG_Y + BTN_SIZE*3, mouseX, mouseY);
        // ツールチップなどを最後に描画
        this.renderTooltip(graphics, mouseX, mouseY);
    }
    private void drawConfigButton(GuiGraphics gui, Direction dir, int x, int y, int mouseX, int mouseY) {
        // Menu経由で現在の設定値を取得
        int ordinal = this.menu.getSideConfigOrdinal(dir);
        // Enumに変換 (範囲チェック含む)
        SideConfig config = SideConfig.values()[ordinal % SideConfig.values().length];

        // 色の決定 (SideConfigの定義コメントに基づく)
        int color;
        switch (config) {
            case INPUT ->  color = 0xFF651D26; // 赤 (ARGB)
            case OUTPUT -> color = 0xFF334464; // 青
            case ENERGY -> color = 0xFF418D45; // 緑
            default ->     color = 0xFF555555; // NONE (灰色)
        }

        // 四角形の描画 (塗りつぶし)
        gui.fill(leftPos + x, topPos + y, leftPos + x + BTN_SIZE, topPos + y + BTN_SIZE, color);

        // 枠線の描画 (黒)
        gui.renderOutline(leftPos + x, topPos + y, BTN_SIZE, BTN_SIZE, 0xFF000000);

        // マウスホバー時のツールチップ表示
        if (isHovering(x, y, BTN_SIZE, BTN_SIZE, mouseX, mouseY)) {
            Component text = Component.literal(dir.getName() + ": " + config.name());
            gui.renderTooltip(this.font, text, mouseX, mouseY);
        }
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 各ボタンの判定
        if (checkButtonClick(Direction.UP,    CONFIG_X + BTN_SIZE, CONFIG_Y,              mouseX, mouseY)) return true;
        if (checkButtonClick(Direction.WEST,  CONFIG_X,            CONFIG_Y + BTN_SIZE,   mouseX, mouseY)) return true;
        if (checkButtonClick(Direction.NORTH, CONFIG_X + BTN_SIZE, CONFIG_Y + BTN_SIZE,   mouseX, mouseY)) return true;
        if (checkButtonClick(Direction.EAST,  CONFIG_X + BTN_SIZE*2, CONFIG_Y + BTN_SIZE, mouseX, mouseY)) return true;
        if (checkButtonClick(Direction.DOWN,  CONFIG_X + BTN_SIZE, CONFIG_Y + BTN_SIZE*2, mouseX, mouseY)) return true;
        if (checkButtonClick(Direction.SOUTH, CONFIG_X + BTN_SIZE, CONFIG_Y + BTN_SIZE*3, mouseX, mouseY)) return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    // クリック処理ヘルパー
    private boolean checkButtonClick(Direction dir, int x, int y, double mouseX, double mouseY) {
        // isHoveringはGUIの左上からの相対座標(x, y)とサイズで判定します
        if (isHovering(x, y, BTN_SIZE, BTN_SIZE, mouseX, mouseY)) {
            // サーバーに通知 (MenuのclickMenuButtonを発火させる)
            // IDは 10 + Directionのordinal (0~5) = 10~15
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 10 + dir.ordinal());
                return true;
            }
        }
        return false;
    }
}

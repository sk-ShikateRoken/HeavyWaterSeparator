package com.shikateroken.heavy_water_separator.menu;

import com.shikateroken.heavy_water_separator.registry.HwsBlocks;
import com.shikateroken.heavy_water_separator.tile.TileEntityDTHWS;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class DTHWSMenu extends AbstractContainerMenu {
    public final TileEntityDTHWS tile;
    private final ContainerData data;

    public DTHWSMenu(int windowId, Inventory inv, FriendlyByteBuf extraData) {
        this(windowId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(8));
    }

    // サーバー側（データ同期用）のコンストラクタ
    public DTHWSMenu(int windowId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(HwsMenus.DTHWS_MENU.get(), windowId);
        this.tile = (TileEntityDTHWS) entity;
        this.data = data;

        // タイルエンティティの数値データを同期設定
        addDataSlots(data);

        // ---------------------------------------------------
        // 1. アップグレードスロットの追加 (Mekanism風に配置)
        // ---------------------------------------------------
        this.tile.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            // Speed Upgrade (左上に配置してみる)
            this.addSlot(new SlotItemHandler(handler, 0, 152, 8)); // 座標 x:152, y:8
            // Energy Upgrade (その下)
            this.addSlot(new SlotItemHandler(handler, 1, 152, 26)); // 座標 x:152, y:26
        });

        // ---------------------------------------------------
        // 2. プレイヤーのインベントリ (定型文)
        // ---------------------------------------------------
        layoutPlayerInventorySlots(inv, 8, 84);
    }

    // プレイヤーのインベントリを配置するメソッド
    private void layoutPlayerInventorySlots(Inventory inv, int leftCol, int topRow) {
        // プレイヤーのメインインベントリ (3行9列)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, leftCol + col * 18, topRow + row * 18));
            }
        }
        // プレイヤーのホットバー (最下段)
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, leftCol + col * 18, topRow + 58));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(tile.getLevel(), tile.getBlockPos()), player, HwsBlocks.DTHWS.get());
    }

    // Shiftクリック時のアイテム移動ロジック (必須)
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();

            // アップグレードスロットからプレイヤーインベントリへ
            if (index < 2) {
                if (!this.moveItemStackTo(stack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリからアップグレードスロットへ
            else if (!this.moveItemStackTo(stack, 0, 2, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    // 画面側から今の数値を取得するためのゲッター
    public int getProgress() { return data.get(0); }
    public int getMaxProgress() { return data.get(1); }
    public int getEnergy() { return data.get(2); }
    public int getMaxEnergy() { return data.get(3); }
    // タンク情報などは別途同期するか、簡単な数値だけ送るならここに追加

    }

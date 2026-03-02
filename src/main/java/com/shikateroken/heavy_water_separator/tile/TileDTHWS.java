package com.shikateroken.heavy_water_separator.tile;

import com.shikateroken.heavy_water_separator.registry.HwsBlockEntity;
import com.shikateroken.heavy_water_separator.registry.HwsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class TileDTHWS extends Block implements EntityBlock {
    public TileDTHWS(Properties properties) {
        super(properties);
    }

    // このブロックが設置されたときにBlockEntityを生成する
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityDTHWS(pos, state);
    }

    // サーバー側で毎Tick処理(tickメソッド)を呼び出すための登録
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null; // クライアント側では処理しない
        }
        //
        return createTickerHelper(type, HwsBlockEntity.TE_DTHWS.get(),
                (lvl, pos, st, blockEntity) -> ((TileEntityDTHWS) blockEntity).tick());
    }

    // Tickerの型合わせのためのヘルパーメソッド（Forge公式推奨の書き方）
    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> pServerType, BlockEntityType<E> pClientType, BlockEntityTicker<? super E> pTicker) {
        return pClientType == pServerType ? (BlockEntityTicker<A>) pTicker : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);

        // MekanismのConfiguratorを持っているか、あるいはスニーク中か判定
        // "mekanism:configurator" というIDで簡易判定
        boolean isConfigurator = heldItem.getItem().getDescriptionId().contains("configurator");

        // スニークしながら右クリックで設定切り替え (またはConfigurator使用)
        if (player.isCrouching() || isConfigurator) {
            if (!level.isClientSide()) {
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof TileEntityDTHWS tile) {
                    // クリックされた面の設定を切り替え
                    Direction clickedFace = hit.getDirection();
                    tile.cycleConfig(clickedFace);

                    // チャットで今の設定を教えてあげる
                    SideConfig newConfig = tile.getConfig(clickedFace);
                    player.sendSystemMessage(Component.literal("Side " + clickedFace.getName() + ": " + newConfig.name()));
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof TileEntityDTHWS) {
                // GUIを開く
                NetworkHooks.openScreen((ServerPlayer) player, (TileEntityDTHWS) entity, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}

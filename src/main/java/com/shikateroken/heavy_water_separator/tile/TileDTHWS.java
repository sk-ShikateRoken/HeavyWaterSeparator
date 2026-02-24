package com.shikateroken.heavy_water_separator.tile;

import com.shikateroken.heavy_water_separator.registry.HwsBlockEntity;
import com.shikateroken.heavy_water_separator.registry.HwsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

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
                (lvl, pos, st, blockEntity) -> ((TileEntityDTHWS)blockEntity).tick());
    }

    // Tickerの型合わせのためのヘルパーメソッド（Forge公式推奨の書き方）
    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> pServerType, BlockEntityType<E> pClientType, BlockEntityTicker<? super E> pTicker) {
        return pClientType == pServerType ? (BlockEntityTicker<A>) pTicker : null;
    }
}

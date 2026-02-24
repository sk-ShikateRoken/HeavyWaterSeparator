package com.shikateroken.heavy_water_separator.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import com.shikateroken.heavy_water_separator.Config;
import com.shikateroken.heavy_water_separator.registry.HwsBlockEntity;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityDTHWS extends BlockEntity{

    //タンク容量の指定
    public final FluidTank inputTank = new FluidTank(Config.inputTANK_CAPACITY.get());

    public final FluidTank outputTank = new FluidTank(Config.outputTANK_CAPACITY.get());
private final LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.of(() -> inputTank);
public TileEntityDTHWS(BlockPos pos, BlockState state){
    super(HwsBlockEntity.TE_DTHWS.get(),pos, state);
}
//液体取り扱い
//@Override
//public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
//    if (cap == ForgeCapabilities.FLUID_HANDLER) {
//        // 面(Direction)によって入力タンクを返すか出力タンクを返すか分けることができます
//        // 例: 上面なら入力、下面なら出力など。ここではシンプルにlazyFluidHandlerを返しています。
//        return lazyFluidHandler.cast();
//    }
//    return super.getCapability(cap, side);
//}
private final LazyOptional<IFluidHandler> inputHandler = LazyOptional.of(() -> inputTank);
    private final LazyOptional<IFluidHandler> outputHandler = LazyOptional.of(() -> outputTank);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            // ブロックの「下面(DOWN)」からアクセスされた場合は、出力タンクを返す
            if (side == Direction.DOWN) {
                return outputHandler.cast();
            }
            // 上面や側面など、下面以外からのアクセスは入力タンクを返す
            return inputHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    // 両方のハンドラーを無効化するように修正
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inputHandler.invalidate();
        outputHandler.invalidate();
    }

    // ブロックが破壊されたりアンロードされた際にメモリリークを防ぐための処理
//    @Override
//    public void invalidateCaps() {
//        super.invalidateCaps();
//        lazyFluidHandler.invalidate();
//    }

    // 毎Tick呼ばれる
    public void tick() {
        if (level == null || level.isClientSide()) return;
        // 【デバッグ用】1秒（20Tick）に1回だけコンソールに出力する
        if (level.getGameTime() % 20 == 0) {
            System.out.println("========== DTHWS Debug ==========");
            System.out.println("Input Tank : " + inputTank.getFluidAmount() + " mB");
            if (!inputTank.isEmpty()) {
                System.out.println("Input Fluid: " + inputTank.getFluid().getTranslationKey());
            }
            System.out.println("Output Tank: " + outputTank.getFluidAmount() + " mB");
        }

        // 入力タンクの液体を減らし、出力タンクの液体を増やす量
        int processAmount = 100;

        // 入力タンクに十分な水があるかチェック
        boolean hasInput = inputTank.getFluidAmount() >= processAmount && inputTank.getFluid().getFluid() == Fluids.WATER;

        // 【重要】水がある場合のみ、以下の変換・移動処理を行う
        if (hasInput) {
            // 生成する液体を定義（null ではなく、ここで直接宣言して初期化します）
            FluidStack resultFluid = new FluidStack(Fluids.LAVA, processAmount);

            // 出力タンクに空き容量があるかシミュレート
            int acceptedAmount = outputTank.fill(resultFluid, IFluidHandler.FluidAction.SIMULATE);

            // 出力タンクが全量を受け入れられる場合のみ処理を実行
            if (acceptedAmount == processAmount) {

                // 入力タンクから100mB減らす
                inputTank.drain(processAmount, IFluidHandler.FluidAction.EXECUTE);
                // 出力タンクに100mB増やす
                outputTank.fill(resultFluid, IFluidHandler.FluidAction.EXECUTE);

                // データの変更をゲームに通知
                setChanged();
            }
        }
    }
    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        // NBTタグの中に "InputTank" というデータがあれば読み込む
        if (nbt.contains("InputTank")) {
            inputTank.readFromNBT(nbt.getCompound("InputTank"));
        }
        // "OutputTank" も同様に読み込む
        if (nbt.contains("OutputTank")) {
            outputTank.readFromNBT(nbt.getCompound("OutputTank"));
        }
    }

    // ワールド保存時にデータを書き込むメソッド
    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        // それぞれのタンクの中身をNBTに変換して保存する
        nbt.put("InputTank", inputTank.writeToNBT(new CompoundTag()));
        nbt.put("OutputTank", outputTank.writeToNBT(new CompoundTag()));
    }
}



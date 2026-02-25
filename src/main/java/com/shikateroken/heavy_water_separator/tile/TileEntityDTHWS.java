package com.shikateroken.heavy_water_separator.tile;

import com.shikateroken.heavy_water_separator.recipe.FluidToFluidrecipe;
import com.shikateroken.heavy_water_separator.registry.HwsRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
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
//FE define
    public final EnergyStorage energyStorage = new EnergyStorage(Config.EnergyStorage_CAPACITY.get(),Config.EnergyStorage_INPUTRATE.get(),0);
//FE handler
private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energyStorage);

//Fluid handler
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
        if (cap == ForgeCapabilities.ENERGY){
            return energyHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    // 両方のハンドラーを無効化するように修正ブロックが破壊されたりアンロードされた際にメモリリークを防ぐための処理
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inputHandler.invalidate();
        outputHandler.invalidate();
        energyHandler.invalidate();
    }

    // 毎Tick呼ばれる
    public void tick() {
        if (level == null || level.isClientSide()) return;
        // 【デバッグ用】1秒（20Tick）に1回だけコンソールに出力する
//        if (level.getGameTime() % 20 == 0) {
//            System.out.println("========== DTHWS Debug ==========");
//            System.out.println("Input Tank : " + inputTank.getFluidAmount() + " mB");
//            if (!inputTank.isEmpty()) {
//                System.out.println("Input Fluid: " + inputTank.getFluid().getTranslationKey());
//            }
//            System.out.println("Output Tank: " + outputTank.getFluidAmount() + " mB");
//        }
        // Energy cost
        int energyCost =Config.EnergyCost.get();
        if (energyStorage.getEnergyStored() < energyCost){
            return;
        }
        // 1. 入力タンクが空なら何もしない（負荷軽減）
        if (inputTank.isEmpty()) return;

        // 2. 現在の入力タンクの液体に一致するレシピを探す
        FluidToFluidrecipe currentRecipe = null;

        // 登録されている "fluid_to_fluid" タイプの全レシピを取得してループで確認
        for (FluidToFluidrecipe recipe : level.getRecipeManager().getAllRecipesFor(HwsRecipes.FLUID_TO_FLUID_TYPE.get())) {

            // ステップ1で作った独自の matches メソッドで、液体の種類と必要量が足りているかチェック
            if (recipe.getInput().getFluid() == inputTank.getFluid().getFluid() &&
                    inputTank.getFluidAmount() >= recipe.getInput().getAmount()) {
                currentRecipe = recipe;
                break; // 見つかったらループを抜ける
            }
        }

        // 3. 一致するレシピが見つかった場合の処理
        if (currentRecipe != null) {
            // レシピから消費量と生成する液体を取得（copy() で安全に複製する）
            int consumeAmount = currentRecipe.getInput().getAmount();
            FluidStack resultFluid = currentRecipe.getOutput().copy();

            // 出力タンクに空き容量があるかシミュレート
            int acceptedAmount = outputTank.fill(resultFluid, IFluidHandler.FluidAction.SIMULATE);

            // 出力タンクが全量を受け入れられる場合のみ処理を実行
            if (acceptedAmount == resultFluid.getAmount()) {

                // 入力タンクからレシピの指定量（consumeAmount）を減らす
                inputTank.drain(consumeAmount, IFluidHandler.FluidAction.EXECUTE);

                // 出力タンクにレシピの指定量（resultFluid）を増やす
                outputTank.fill(resultFluid, IFluidHandler.FluidAction.EXECUTE);

                //use energy
                energyStorage.extractEnergy(energyCost,false);

                // データの変更をゲームに通知
                setChanged();
            }
        }
    }
    // ワールド保存時にデータを書き込むメソッド
    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        // それぞれのタンクの中身をNBTに変換して保存する
        nbt.put("InputTank", inputTank.writeToNBT(new CompoundTag()));
        nbt.put("OutputTank", outputTank.writeToNBT(new CompoundTag()));
        nbt.put("Energy",energyStorage.serializeNBT());
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
        //energy
        if (nbt.contains("Energy")){
            energyStorage.deserializeNBT(nbt.getCompound("Energy"));
        }

    }


}



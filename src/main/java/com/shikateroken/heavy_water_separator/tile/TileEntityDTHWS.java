package com.shikateroken.heavy_water_separator.tile;

import com.shikateroken.heavy_water_separator.recipe.FluidToFluidrecipe;
import com.shikateroken.heavy_water_separator.registry.HwsRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class TileEntityDTHWS extends BlockEntity{
    public static class DynamicEnergyStorage extends EnergyStorage {
        public DynamicEnergyStorage(int capacity, int maxReceive, int maxExtract) {
            super(capacity, maxReceive, maxExtract);
        }

        // 容量を強制的に変更するメソッドを追加
        public void setCapacity(int newCapacity) {
            this.capacity = newCapacity;
            // もし現在の電力が新容量を超えていたら、溢れさせる（切り捨てる）
            if (this.energy > newCapacity) {
                this.energy = newCapacity;
            }
        }
        // 受入速度変更
        public void setMaxReceive(int newMaxReceive) {
            this.maxReceive = newMaxReceive;
        }
    }
    //タンク容量の指定
    public final FluidTank inputTank = new FluidTank(Config.inputTANK_CAPACITY.get());

    public final FluidTank outputTank = new FluidTank(Config.outputTANK_CAPACITY.get());
private final LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.of(() -> inputTank);
public TileEntityDTHWS(BlockPos pos, BlockState state){
    super(HwsBlockEntity.TE_DTHWS.get(),pos, state);
}
//FE define
    public final DynamicEnergyStorage energyStorage = new DynamicEnergyStorage(Config.EnergyStorage_CAPACITY.get(),Config.EnergyStorage_INPUTRATE.get(),Config.EnergyStorage_OUTPUTRATE.get());
//FE handler
private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energyStorage);

//Fluid handler
private final LazyOptional<IFluidHandler> inputHandler = LazyOptional.of(() -> inputTank);
    private final LazyOptional<IFluidHandler> outputHandler = LazyOptional.of(() -> outputTank);
    // cache
    public int progress = 0;
    private int maxProgress = 20; // 基本の処理時間（20Tick = 1秒）
    private int cachedEnergyCost = Config.EnergyCost.get();

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {

        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }

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

    public final ItemStackHandler upgradeInventory = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {

            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (id == null) return false;
           //  スロット0はSpeed、スロット1はEnergyアップグレードのみ受け入れる
            if (slot == 0) return id.toString().equals("mekanism:upgrade_speed");
            if (slot == 1) return id.toString().equals("mekanism:upgrade_energy");
            return false;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 8; // Mekanismのアップグレードは最大8枚まで
        }

        @Override
        protected void onContentsChanged(int slot) {
            recalculateUpgrades();
            setChanged();
        }
    };

    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> upgradeInventory);



    // 両方のハンドラーを無効化する ブロックが破壊されたりアンロードされた際にメモリリークを防ぐ
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        inputHandler.invalidate();
        outputHandler.invalidate();
        energyHandler.invalidate();
    }
    private void recalculateUpgrades() {

        int speedCount = upgradeInventory.getStackInSlot(0).getCount();
        int energyCount = upgradeInventory.getStackInSlot(1).getCount();
        // 基本値 (Configから取得)
        int baseTicks = 20;//基本速度
        int baseEnergyCost = Config.EnergyCost.get();
        int baseCapacity = Config.EnergyStorage_CAPACITY.get();
        int baseReceive = Config.EnergyStorage_INPUTRATE.get();
        //Speed Upgradeの計算
        double speedMultiplier = Math.pow(10, speedCount / 8.0);
        //処理時間の計算
        this.maxProgress = Math.max(1,(int)(baseTicks / (2* speedMultiplier)));
        // 消費電力を計算: 基本電力 * (速度倍率のさらに倍)
        // Mekanismは速度を上げると燃費が悪くなる仕様です (10^(2*枚数/8))
        double energyMultiplier = Math.pow(10, (2 * speedCount) / 8.0);
        this.cachedEnergyCost = (int) (baseEnergyCost * energyMultiplier);
        // 2. Energy Upgrade の計算 (容量増加 & 消費電力軽減)
        // 容量倍率 = 10の(枚数/8)乗
        double capacityMultiplier = Math.pow(10, energyCount / 8.0);

        // Energy UpgradeでのSpeed Upgradeによる消費電力増加を緩和
        // 緩和率 = 10の(-枚数/8)乗
        double energyReduction = Math.pow(10, -energyCount / 8.0);

        // 最終的な消費電力の適用
        this.cachedEnergyCost = (int) (this.cachedEnergyCost * energyReduction);
        // ※最低でも1FEは消費するようにする
        if (this.cachedEnergyCost < 1) this.cachedEnergyCost = 1;

        // 容量と受入速度の適用
        int newCapacity = (int) (baseCapacity * capacityMultiplier);
        int newReceive = (int) (baseReceive * capacityMultiplier);

        // バッテリーの設定を更新
        energyStorage.setCapacity(newCapacity);
        energyStorage.setMaxReceive(newReceive);


    }
    @Override
    public void onLoad() {
        super.onLoad();
        recalculateUpgrades();
    }

    // 毎Tick呼ばれる
    public void tick() {
        if (level == null || level.isClientSide()) return;
//        // 【デバッグ用】1秒（20Tick）に1回だけコンソールに出力する
//        if (level.getGameTime() % 20 == 0) {
//            System.out.println("========== DTHWS Debug ==========");
//            System.out.println("Input Tank : " + inputTank.getFluidAmount() + " mB");
//            if (!inputTank.isEmpty()) {
//                System.out.println("Input Fluid: " + inputTank.getFluid().getTranslationKey());
//            }
//            System.out.println("Output Tank: " + outputTank.getFluidAmount() + " mB");
//            System.out.println("energyStorage : " + energyStorage.getEnergyStored() + "FE");
//        }
        //入力タンクがからなら進捗を0として終了
        if (inputTank.isEmpty()){
            progress = 0;
            return;
        }
//        //アップグレード枚数を取得
//        int speedUpgreads = upgradeInventory.getStackInSlot(0).getCount();
//        int energyUpgreads = upgradeInventory.getStackInSlot(1).getCount();
//
//        //アップグレード計算

//        double dmp = Math.max(1,20 - (19.0 / 64) *(this.maxProgress )*(this.maxProgress ));
//        int currentMaxProgress = (int) Math.floor(dmp);
//
//     double dec = Config.EnergyCost.get() * Math.pow(10,(2.0 * speedUpgreads - energyUpgreads)/8);
//        int currentEnergyCost = (int) Math.floor(dec);
        //エネルギーが足りなければ停止
        if (energyStorage.getEnergyStored() < cachedEnergyCost){
            return;
        }
        // Energy cost
//        int energyCost =Config.EnergyCost.get();
//        if (energyStorage.getEnergyStored() < energyCost){
//            return;
//        }
        // 1. 入力タンクが空なら何もしない
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

                // 1. 毎Tick電力を消費する
                energyStorage.extractEnergy(cachedEnergyCost, false);

                // 2. プログレス(処理時間)を1進める
                progress++;

                // 3. プログレスが目標値（完了）に達したかチェック
                if (progress >= maxProgress) {
                    // 変換処理を実行
                    inputTank.drain(consumeAmount, IFluidHandler.FluidAction.EXECUTE);
                    outputTank.fill(resultFluid, IFluidHandler.FluidAction.EXECUTE);

                    // プログレスをリセットして次の処理へ
                    progress = 0;
                }
//旧加工ロジック
//                // 入力タンクからレシピの指定量（consumeAmount）を減らす
//                inputTank.drain(consumeAmount, IFluidHandler.FluidAction.EXECUTE);
//
//                // 出力タンクにレシピの指定量（resultFluid）を増やす
//                outputTank.fill(resultFluid, IFluidHandler.FluidAction.EXECUTE);
//
//                //use energy
//                energyStorage.extractEnergy(energyCost,false);

                // データの変更をゲームに通知
                setChanged();
            }else {
                progress = 0;
            }
        }else {
            progress = 0;
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
        nbt.put("Upgrades", upgradeInventory.serializeNBT());
        nbt.putInt("Progress", progress);


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
        //upgrade
        if (nbt.contains("Upgrades")) {
            upgradeInventory.deserializeNBT(nbt.getCompound("Upgrades"));
        }
        progress = nbt.getInt("Progress");

    }


}



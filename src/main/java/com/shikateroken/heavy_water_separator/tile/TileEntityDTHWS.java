package com.shikateroken.heavy_water_separator.tile;

import com.shikateroken.heavy_water_separator.menu.DTHWSMenu;
import com.shikateroken.heavy_water_separator.recipe.FluidToFluidrecipe;
import com.shikateroken.heavy_water_separator.registry.HwsRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
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

import java.util.HashMap;
import java.util.Map;


public class TileEntityDTHWS extends BlockEntity implements MenuProvider {
    // 同期するデータをまとめる
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress; // キャッシュした最大時間
                case 2 -> energyStorage.getEnergyStored();
                case 3 -> energyStorage.getMaxEnergyStored();
                case 4 -> inputTank.getFluidAmount();
                case 5 -> inputTank.getCapacity();
                case 6 -> outputTank.getFluidAmount();
                case 7 -> outputTank.getCapacity();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                case 2 -> { /* エネルギーはクライアント側でセット不要 */ }
                case 3 -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 8; // データの総数
        }
    };

    // MenuProviderの実装: GUIのタイトル
    @Override
    public Component getDisplayName() {
        return Component.literal("Heavy Water Separator");
    }

    // MenuProviderの実装: サーバー側でMenuを開く処理
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new DTHWSMenu(id, inventory, this, this.data);
    }

    public static class DynamicEnergyStorage extends EnergyStorage {
        public DynamicEnergyStorage(int capacity, int maxReceive, int maxExtract) {
            super(capacity, maxReceive, maxExtract);
        }

        public void setEnergy(int energy) {
            this.energy = energy;
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


    //FE define
    public final DynamicEnergyStorage energyStorage = new DynamicEnergyStorage(Config.EnergyStorage_CAPACITY.get(), Config.EnergyStorage_INPUTRATE.get(), Config.EnergyStorage_OUTPUTRATE.get());
    //FE handler
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energyStorage);
    public net.minecraftforge.energy.IEnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }


    //Fluid handler
    private final LazyOptional<IFluidHandler> inputHandler = LazyOptional.of(() -> inputTank);
    private final LazyOptional<IFluidHandler> outputHandler = LazyOptional.of(() -> outputTank);
    // cache
    public int progress = 0;
    private int maxProgress = 20; // 基本の処理時間（20Tick = 1秒）
    private int cachedEnergyCost = Config.EnergyCost.get();

    @Override
    public void onLoad() {
        super.onLoad();
        recalculateUpgrades();
    }

    //面設定
    private final Map<Direction, SideConfig> sideConfigs = new HashMap<>();

    public TileEntityDTHWS(BlockPos pos, BlockState state) {
        super(HwsBlockEntity.TE_DTHWS.get(), pos, state);
        //初期化
        for (Direction dir : Direction.values()) {
            sideConfigs.put(dir, SideConfig.NONE);
        }
    }

    //設定変更
    public void cycleConfig(Direction side) {
        SideConfig current = sideConfigs.get(side);
        SideConfig next = current.cycle();
        sideConfigs.put(side, next);
        setChanged(); // 保存フラグ

        // クライアント同期用 (後で実装)
        // level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    // 現在の設定を取得
    public SideConfig getConfig(Direction side) {
        return sideConfigs.get(side);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (side == null) {
            return super.getCapability(cap, side);
        }
        SideConfig config = sideConfigs.get(side);
        //アイテムハンドラー
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }

        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            // input設定なら入力タンクへのアクセスのみ
            if (config == SideConfig.INPUT) {
                return LazyOptional.of(() -> inputTank).cast();
            }
            //output設定なら出力タンクのみ
            if (config == SideConfig.OUTPUT) {
                return LazyOptional.of(() -> outputTank).cast();
            }
            //他ならアクセス不可
            return LazyOptional.empty();
        }
        //energy
        if (cap == ForgeCapabilities.ENERGY) {
            if (config == SideConfig.ENERGY) {
                return energyHandler.cast();
            }
            return LazyOptional.empty();
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
            setChanged();
            recalculateUpgrades();
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
        this.maxProgress = Math.max(1, (int) (baseTicks / (2 * speedMultiplier)));
        // 消費電力を計算: 基本電力 * (速度倍率のさらに倍)
        // 10^(2*枚数/8)
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
        // 最低でも1FEは消費するようにする
        if (this.cachedEnergyCost < 1) this.cachedEnergyCost = 1;

        // 容量と受入速度の適用
        int newCapacity = (int) (baseCapacity * capacityMultiplier);
        int newReceive = (int) (baseReceive * capacityMultiplier);

        // バッテリーの設定を更新
        energyStorage.setCapacity(newCapacity);
        energyStorage.setMaxReceive(newReceive);
        // 【デバッグ用】 計算結果の確認
//        System.out.println("New MaxProgress: " + this.maxProgress);
//        System.out.println("New EnergyCost: " + this.cachedEnergyCost);
//        System.out.println("New Capacity: " + newCapacity);
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
        if (inputTank.isEmpty()) {
            progress = 0;
            return;
        }
//
        //エネルギーが足りなければ停止
        if (energyStorage.getEnergyStored() < cachedEnergyCost) {
            return;
        }

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
                break;
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
                // データの変更をゲームに通知
                setChanged();
            } else {
                progress = 0;
            }
        } else {
            progress = 0;
        }
    }


    // ワールド保存時にデータを書き込むメソッド
    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
//            System.out.println("Saving TileEntity...");
//            // インベントリのデータを保存
//            CompoundTag upgradesTag = upgradeInventory.serializeNBT();
//            nbt.put("Upgrades", upgradesTag);
//           // 保存しようとしているデータの中身を確認
//           System.out.println("Saving 'Upgrades' tag: " + upgradesTag);
        // 各面の設定を保存
        CompoundTag configTag = new CompoundTag();
        for (Direction dir : Direction.values()) {
            configTag.putString(dir.getName(), sideConfigs.get(dir).getSerializedName());
        }
        nbt.put("SideConfigs", configTag);

        // それぞれのタンクの中身をNBTに変換して保存する
        nbt.put("InputTank", inputTank.writeToNBT(new CompoundTag()));
        nbt.put("OutputTank", outputTank.writeToNBT(new CompoundTag()));
        nbt.putInt("Energy", energyStorage.getEnergyStored());
        nbt.put("Upgrades", upgradeInventory.serializeNBT());
        nbt.putInt("Progress", progress);
    }

    @Override//nbt読み込み
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
        if (nbt.contains("Energy")) {
            int savedEnergy = nbt.getInt("Energy");
            energyStorage.setEnergy(savedEnergy);
        }
        //upgrade
        if (nbt.contains("Upgrades")) {
            upgradeInventory.deserializeNBT(nbt.getCompound("Upgrades"));
            recalculateUpgrades();
        }
        progress = nbt.getInt("Progress");
        if (nbt.contains("Upgrades")) {
            System.out.println("Found 'Upgrades' tag! Loading inventory..."); // ログ
            upgradeInventory.deserializeNBT(nbt.getCompound("Upgrades"));

            // 読み込んだ中身を確認
//            System.out.println("Loaded Item in Slot 0: " + upgradeInventory.getStackInSlot(0));
//        } else {
//            System.out.println("WARNING: 'Upgrades' tag NOT found in NBT."); // ログ
        }
        //読み込む
        if (nbt.contains("SideConfigs")) {
            CompoundTag configTag = nbt.getCompound("SideConfigs");
            for (Direction dir : Direction.values()) {
                String name = configTag.getString(dir.getName());
                // 名前からEnumに戻す
                SideConfig config = SideConfig.NONE;
                for (SideConfig c : SideConfig.values()) {
                    if (c.getSerializedName().equals(name)) config = c;
                }
                sideConfigs.put(dir, config);
            }
        }

    }
    // MenuからアクセスするためのGetter
    public ItemStackHandler getUpgradeInventory() {
        return this.upgradeInventory;
    }
}



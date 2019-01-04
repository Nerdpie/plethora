package org.squiddev.plethora.gameplay.tiny;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.BlockBase;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.client.tile.RenderTinyTurtle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class BlockTinyTurtle extends BlockBase<TileTinyTurtle> {
	private static final AxisAlignedBB BOX = new AxisAlignedBB(
		0.3125, 0.3125, 0.3125,
		0.6875, 0.6875, 0.6875
	);
	static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final int DEFAULT_COLOUR = 0xC6C6C6;

	private static final String NAME = "tiny_turtle";

	public BlockTinyTurtle() {
		super(NAME, Material.IRON, TileTinyTurtle.class);
		setDefaultState(getBlockState().getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	public void harvestBlock(@Nonnull World world, EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable TileEntity te, @Nonnull ItemStack stack) {
		List<ItemStack> items = Collections.singletonList(getItem(te instanceof TileTinyTurtle ? (TileTinyTurtle) te : null));
		ForgeEventFactory.fireBlockHarvesting(items, world, pos, state, 0, 1, true, player);
		for (ItemStack item : items) spawnAsEntity(world, pos, item);
	}

	@Nonnull
	@Override
	@Deprecated
	public ItemStack getItem(World world, BlockPos pos, @Nonnull IBlockState state) {
		return getItem(getTile(world, pos));
	}

	@Nonnull
	private ItemStack getItem(@Nullable TileTinyTurtle tile) {

		ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
		if (tile != null) ItemTinyTurtle.setup(stack, tile.getId(), tile.getLabel(), tile.getColour());
		return stack;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return 0;
	}

	//region Properties
	@Nullable
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileTinyTurtle();
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	@Deprecated
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}

	@Override
	@Deprecated
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
	}

	@Override
	@Deprecated
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	@Deprecated
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	@Nonnull
	@Deprecated
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	@Deprecated
	public boolean isSideSolid(IBlockState base_state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	@Deprecated
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BOX;
	}

	@Nonnull
	@Deprecated
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}
	//endregion

	//region Registry
	@Override
	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().register(new ItemTinyTurtle().setRegistryName(new ResourceLocation(Plethora.RESOURCE_DOMAIN, name)));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
		super.clientInit();

		ClientRegistry.bindTileEntitySpecialRenderer(TileTinyTurtle.class, new RenderTinyTurtle());

		ItemTinyTurtle item = (ItemTinyTurtle) Item.getItemFromBlock(this);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler((stack, tintIndex) -> {
			if (tintIndex == 0) {
				int colour = item.getColour(stack);
				return colour == -1 ? DEFAULT_COLOUR : colour;
			}

			return 0xFFFFFF;
		}, item);
	}
	//endregion
}

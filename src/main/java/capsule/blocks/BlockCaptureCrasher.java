package capsule.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class BlockCaptureCrasher extends Block {
    public BlockCaptureCrasher() {
        super(Block.Properties.of().mapColor(MapColor.STONE)
                .sound(SoundType.STONE));
    }

    @Override
    public BlockState playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(worldIn, pos, state, player);
        throw new RuntimeException("testing purpose");
    }
}

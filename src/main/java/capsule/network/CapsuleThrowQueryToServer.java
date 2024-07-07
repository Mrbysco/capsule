package capsule.network;

import capsule.CapsuleMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CapsuleThrowQueryToServer(boolean instant, BlockPos pos) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(CapsuleMod.MODID, "throw_query");

    public CapsuleThrowQueryToServer(FriendlyByteBuf buf) {
        this(buf.readBoolean(), getPos(buf));
    }

    public CapsuleThrowQueryToServer(BlockPos pos, boolean instant) {
        this(instant, pos);
    }

    private static BlockPos getPos(FriendlyByteBuf buf) {
        boolean hasPos = buf.readBoolean();
        if (hasPos) {
            return BlockPos.of(buf.readLong());
        }
        return null;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(instant);
        boolean hasPos = pos != null;
        buf.writeBoolean(hasPos);
        if (hasPos) {
            buf.writeLong(pos.asLong());
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String toString() {
        return getClass().toString();
    }
}
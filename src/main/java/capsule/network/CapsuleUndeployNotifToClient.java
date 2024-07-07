package capsule.network;

import capsule.CapsuleMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CapsuleUndeployNotifToClient(BlockPos posFrom, BlockPos posTo, int size, String templateName) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(CapsuleMod.MODID, "undeploy_notif");

    public CapsuleUndeployNotifToClient(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readBlockPos(), buf.readShort(), buf.readUtf());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(posFrom);
        buf.writeBlockPos(posTo);
        buf.writeShort(size);
        buf.writeUtf(templateName == null ? "" : templateName);
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
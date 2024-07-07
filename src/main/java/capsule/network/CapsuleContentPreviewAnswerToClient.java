package capsule.network;

import capsule.CapsuleMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * This Network Message is sent from the client to the server
 */
public record CapsuleContentPreviewAnswerToClient(String structureName, List<AABB> boundingBoxes) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(CapsuleMod.MODID, "content_preview_answer");

    protected static final Logger LOGGER = LogManager.getLogger(CapsuleContentPreviewAnswerToClient.class);


    public CapsuleContentPreviewAnswerToClient(List<AABB> boundingBoxes, String structureName) {
        this(structureName, boundingBoxes);
    }

    public CapsuleContentPreviewAnswerToClient(FriendlyByteBuf buf) {
        this(buf.readUtf(), getAABBList(buf));
    }

    private static List<AABB> getAABBList(FriendlyByteBuf buf) {
        int size = buf.readShort();
        List<AABB> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            boolean isSingleBlock = buf.readBoolean();
            if (isSingleBlock) {
                BlockPos p = BlockPos.of(buf.readLong());
                list.add(new AABB(p));
            } else {
                list.add(new AABB(Vec3.atLowerCornerOf(BlockPos.of(buf.readLong())), Vec3.atLowerCornerOf(BlockPos.of(buf.readLong()))));
            }
        }
        return list;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(structureName);
        int size = Math.min(boundingBoxes.size(), Short.MAX_VALUE);
        buf.writeShort(size);
        for (int i = 0; i < size; i++) {
            AABB bb = boundingBoxes.get(i);
            boolean isSingleBlock = bb.getSize() == 0;
            buf.writeBoolean(isSingleBlock);
            buf.writeLong(BlockPos.containing(bb.minX, bb.minY, bb.minZ).asLong());
            if (!isSingleBlock) {
                buf.writeLong(BlockPos.containing(bb.maxX, bb.maxY, bb.maxZ).asLong());
            }
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
package capsule.network;

import capsule.CapsuleMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CapsuleLeftClickQueryToServer() implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(CapsuleMod.MODID, "left_click_query");

    public CapsuleLeftClickQueryToServer(FriendlyByteBuf buf) {
        this();
    }

    public void write(FriendlyByteBuf buf) {
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
package capsule.network;

import capsule.CapsuleMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * This Network Message is sent from the client to the server
 */
public record LabelEditedMessageToServer(String label) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(CapsuleMod.MODID, "label_edited_message");

    public LabelEditedMessageToServer(FriendlyByteBuf buf) {
        this(buf.readUtf(32767));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.label);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String toString() {
        return getClass().toString() + "[label=" + label() + "]";
    }

}
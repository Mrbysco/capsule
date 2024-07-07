package capsule.network;

import capsule.CapsuleMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CapsuleContentPreviewQueryToServer(String structureName) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(CapsuleMod.MODID, "content_preview_query");

	public CapsuleContentPreviewQueryToServer(FriendlyByteBuf buf) {
		this(buf.readUtf());
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeUtf(structureName);
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
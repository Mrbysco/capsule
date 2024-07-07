package capsule.network.handler;

import capsule.client.CapsulePreviewHandler;
import capsule.client.render.CapsuleTemplateRenderer;
import capsule.helpers.Capsule;
import capsule.network.CapsuleContentPreviewAnswerToClient;
import capsule.network.CapsuleContentPreviewQueryToServer;
import capsule.network.CapsuleFullContentAnswerToClient;
import capsule.network.CapsuleUndeployNotifToClient;
import capsule.network.LabelEditedMessageToServer;
import capsule.structure.CapsuleTemplate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class ClientPayloadHandler {
	private static final ClientPayloadHandler INSTANCE = new ClientPayloadHandler();

	public static ClientPayloadHandler getInstance() {
		return INSTANCE;
	}

	public void handleContentPreviewAnswer(final CapsuleContentPreviewAnswerToClient data, final PlayPayloadContext context) {
		context.workHandler().submitAsync(() -> {
					synchronized (capsule.client.CapsulePreviewHandler.currentPreview) {
						capsule.client.CapsulePreviewHandler.currentPreview.put(data.structureName(), data.boundingBoxes());
					}
				})
				.exceptionally(e -> {
					// Handle exception
					context.packetHandler().disconnect(Component.translatable("capsule.networking.content_preview_answer.failed", e.getMessage()));
					return null;
				});
	}

	public void handleFullContentAnswer(final CapsuleFullContentAnswerToClient data, final PlayPayloadContext context) {
		context.workHandler().submitAsync(() -> {
					synchronized (capsule.client.CapsulePreviewHandler.currentFullPreview) {
						String structureName = data.structureName();
						CapsuleTemplate template = data.template();
						capsule.client.CapsulePreviewHandler.currentFullPreview.put(structureName, template);
						if (capsule.client.CapsulePreviewHandler.cachedFullPreview.containsKey(structureName)) {
							capsule.client.CapsulePreviewHandler.cachedFullPreview.get(structureName).setWorldDirty();
						} else {
							capsule.client.CapsulePreviewHandler.cachedFullPreview.put(structureName, new CapsuleTemplateRenderer());
						}
					}
				})
				.exceptionally(e -> {
					// Handle exception
					context.packetHandler().disconnect(Component.translatable("capsule.networking.full_content_answer.failed", e.getMessage()));
					return null;
				});
	}

	public void handleUndeployNotif(final CapsuleUndeployNotifToClient data, final PlayPayloadContext context) {
		context.workHandler().submitAsync(() -> {
					BlockPos posFrom = data.posFrom();
					BlockPos posTo = data.posTo();
					int size = data.size();
					String templateName = data.templateName();
					Capsule.showUndeployParticules(Minecraft.getInstance().level, posFrom, posTo, size);
					if (!StringUtil.isNullOrEmpty(templateName)) {
						// remove templates because they are dirty and must be redownloaded
						CapsulePreviewHandler.currentPreview.remove(templateName);
						CapsulePreviewHandler.currentFullPreview.remove(templateName);
						// ask a preview refresh
						PacketDistributor.SERVER.noArg().send(new CapsuleContentPreviewQueryToServer(templateName));
					}
				})
				.exceptionally(e -> {
					// Handle exception
					context.packetHandler().disconnect(Component.translatable("capsule.networking.undeploy_notif.failed", e.getMessage()));
					return null;
				});
	}
}

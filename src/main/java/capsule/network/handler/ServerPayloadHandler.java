package capsule.network.handler;

import capsule.Config;
import capsule.StructureSaver;
import capsule.helpers.Capsule;
import capsule.helpers.Spacial;
import capsule.items.CapsuleItem;
import capsule.network.CapsuleContentPreviewAnswerToClient;
import capsule.network.CapsuleContentPreviewQueryToServer;
import capsule.network.CapsuleFullContentAnswerToClient;
import capsule.network.CapsuleLeftClickQueryToServer;
import capsule.network.CapsuleThrowQueryToServer;
import capsule.network.CapsuleUndeployNotifToClient;
import capsule.network.LabelEditedMessageToServer;
import capsule.structure.CapsuleTemplate;
import capsule.structure.CapsuleTemplateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

import static capsule.items.CapsuleItem.CapsuleState.DEPLOYED;
import static capsule.items.CapsuleItem.CapsuleState.EMPTY;

public class ServerPayloadHandler {
	protected static final Logger LOGGER = LogManager.getLogger(ServerPayloadHandler.class);

	public static final ServerPayloadHandler INSTANCE = new ServerPayloadHandler();

	public static ServerPayloadHandler getInstance() {
		return INSTANCE;
	}

	public void handleLabel(final LabelEditedMessageToServer data, final PlayPayloadContext context) {
		context.workHandler().submitAsync(() -> {
					if (context.player().isPresent()) {
						Player sendingPlayer = context.player().get();
						ItemStack serverStack = sendingPlayer.getMainHandItem();
						if (serverStack.getItem() instanceof CapsuleItem) {
							// of the player didn't swap item during ui opening
							CapsuleItem.setLabel(serverStack, data.label());
						}
					} else {
						LOGGER.error("ServerPlayer was null when LabelEditedMessageToServer was received");
					}
				})
				.exceptionally(e -> {
					// Handle exception
					context.packetHandler().disconnect(Component.translatable("capsule.networking.label_edited_message.failed", e.getMessage()));
					return null;
				});
	}

	public void handleContentPreviewQuery(final CapsuleContentPreviewQueryToServer data, final PlayPayloadContext context) {
		context.workHandler().submitAsync(() -> {
					if (context.player().isPresent()) {
						Player sendingPlayer = context.player().get();
						// read the content of the template and send it back to the client
						ItemStack heldItem = sendingPlayer.getMainHandItem();
						if (!(heldItem.getItem() instanceof CapsuleItem) || CapsuleItem.getStructureName(heldItem) == null) {
							return;
						}

						ServerLevel serverworld = (ServerLevel) sendingPlayer.level();
						Pair<CapsuleTemplateManager, CapsuleTemplate> templatepair = StructureSaver.getTemplate(heldItem, serverworld);
						CapsuleTemplate template = templatepair.getRight();

						if (template != null) {
//							ctx.get().enqueueWork(() -> {
								List<AABB> blockspos = Spacial.mergeVoxels(template.getPalette());
								context.replyHandler().send(new CapsuleContentPreviewAnswerToClient(blockspos, data.structureName()));
//							});
							context.replyHandler().send(new CapsuleFullContentAnswerToClient(template, data.structureName()));
						} else if (heldItem.hasTag()) {
							//noinspection ConstantConditions
							String structureName = heldItem.getTag().getString("structureName");
							sendingPlayer.sendSystemMessage(Component.translatable("capsule.error.templateNotFound", structureName));
						}
					} else {
						LOGGER.error("ServerPlayer was null when CapsuleContentPreviewQueryToServer was received");
					}
				})
				.exceptionally(e -> {
					// Handle exception
					context.packetHandler().disconnect(Component.translatable("capsule.networking.content_preview_query.failed", e.getMessage()));
					return null;
				});
	}

	public void handleThrowQuery(final CapsuleThrowQueryToServer data, final PlayPayloadContext context) {
		context.workHandler().submitAsync(() -> {
					if (context.player().isPresent()) {
						Player sendingPlayer = context.player().get();
						ItemStack heldItem = sendingPlayer.getMainHandItem();
						ServerLevel serverLevel = (ServerLevel) sendingPlayer.level();
						if (heldItem.getItem() instanceof CapsuleItem) {
							BlockPos pos = data.pos();
							boolean instant = data.instant();
							if (instant && pos != null) {
								int size = CapsuleItem.getSize(heldItem);
								int extendLength = (size - 1) / 2;
								// instant capsule initial capture
								if (CapsuleItem.hasState(heldItem, EMPTY)) {
									boolean captured = Capsule.captureAtPosition(heldItem, sendingPlayer.getUUID(), size, serverLevel, pos);
									if (captured) {
										BlockPos center = pos.offset(0, size / 2, 0);
										PacketDistributor.NEAR.with(new PacketDistributor.TargetPoint(center.getX(), center.getY(), center.getZ(), 200 + size, sendingPlayer.level().dimension()))
												.send(new CapsuleUndeployNotifToClient(center, sendingPlayer.blockPosition(), size, CapsuleItem.getStructureName(heldItem)));
									}
								}
								// instant deployment
								else {
									boolean deployed = Capsule.deployCapsule(heldItem, pos.offset(0, -1, 0), sendingPlayer.getUUID(), extendLength, serverLevel);
									if (deployed) {
										CapsuleItem.setUndeployDelay(heldItem, sendingPlayer);
										serverLevel.playSound(null, pos, SoundEvents.ARROW_SHOOT, SoundSource.BLOCKS, 0.4F, 0.1F);
										Capsule.showDeployParticules(serverLevel, pos, size);
									}
									if (deployed && CapsuleItem.isOneUse(heldItem)) {
										heldItem.shrink(1);
									}
								}
							}
							if (!instant) {
								Capsule.throwCapsule(heldItem, sendingPlayer, pos);
							}
						}
					} else {
						LOGGER.error("ServerPlayer was null when CapsuleThrowQueryToServer was received");
					}
				})
				.exceptionally(e -> {
					// Handle exception
					context.packetHandler().disconnect(Component.translatable("capsule.networking.throw_query.failed", e.getMessage()));
					return null;
				});
	}

	public void handleLeftClickQuery(final CapsuleLeftClickQueryToServer data, final PlayPayloadContext context) {
		context.workHandler().submitAsync(() -> {
					if (context.player().isPresent()) {
						Player sendingPlayer = context.player().get();
						// read the content of the template and send it back to the client
						ItemStack stack = sendingPlayer.getMainHandItem();
						if (stack.getItem() instanceof CapsuleItem && CapsuleItem.isBlueprint(stack) && CapsuleItem.hasState(stack, DEPLOYED)) {
							// Reload if no missing materials
							ServerLevel serverLevel = (ServerLevel) sendingPlayer.level();
							Map<StructureSaver.ItemStackKey, Integer> missing = Capsule.reloadBlueprint(stack, serverLevel, sendingPlayer);
							if (missing != null && missing.size() > 0) {
								MutableComponent message = Component.literal("Missing :");
								for (Map.Entry<StructureSaver.ItemStackKey, Integer> entry : missing.entrySet()) {
									message.append("\n* " + entry.getValue() + " ");
									message.append(entry.getKey().itemStack.getItem().getName(entry.getKey().itemStack));
								}
								sendingPlayer.sendSystemMessage(message);
							}
						} else if (stack.getItem() instanceof CapsuleItem && CapsuleItem.canRotate(stack)) {
							StructurePlaceSettings placement = CapsuleItem.getPlacement(stack);
							if (sendingPlayer.isShiftKeyDown()) {
								if (Config.allowMirror) {
									switch (placement.getMirror()) {
										case FRONT_BACK:
											placement.setMirror(Mirror.LEFT_RIGHT);
											break;
										case LEFT_RIGHT:
											placement.setMirror(Mirror.NONE);
											break;
										case NONE:
											placement.setMirror(Mirror.FRONT_BACK);
											break;
									}
									sendingPlayer.sendSystemMessage(Component.translatable("[ ]: " + Capsule.getMirrorLabel(placement)));
								} else {
									sendingPlayer.sendSystemMessage(Component.translatable("Mirroring disabled by config"));
								}
							} else {
								placement.setRotation(placement.getRotation().getRotated(Rotation.CLOCKWISE_90));
								sendingPlayer.sendSystemMessage(Component.translatable("⟳: " + Capsule.getRotationLabel(placement)));
							}
							CapsuleItem.setPlacement(stack, placement);
						}
					} else {
						LOGGER.error("ServerPlayer was null when CapsuleLeftClickQueryToServer was received");
					}
				})
				.exceptionally(e -> {
					// Handle exception
					context.packetHandler().disconnect(Component.translatable("capsule.networking.left_click_query.failed", e.getMessage()));
					return null;
				});
	}
}

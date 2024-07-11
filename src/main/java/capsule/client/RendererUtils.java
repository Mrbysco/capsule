package capsule.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public class RendererUtils {
    public static void doPositionPrologue(Camera camera, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        poseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        poseStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
    }

    public static void doPositionEpilogue(PoseStack poseStack) {
        poseStack.popPose();
    }

    public static void doOverlayPrologue() {
//        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
    }

    public static void doOverlayEpilogue() {
//        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void doWirePrologue() {
        RenderSystem.disableCull();
//        RenderSystem.disableTexture();
        RenderSystem.lineWidth(3.0F);
    }

    public static void doWireEpilogue() {
        RenderSystem.lineWidth(1.0F);
//        RenderSystem.enableTexture();
        RenderSystem.enableCull();

    }

    public static void drawCube(final BlockPos pos, final float sizeOffset, final VertexConsumer buffer, int r, int g, int b, int a) {
        drawCube(pos.getX() - sizeOffset, pos.getY() - sizeOffset, pos.getZ() - sizeOffset,
                pos.getX() + 1 + sizeOffset, pos.getY() + 1 + sizeOffset, pos.getZ() + 1 + sizeOffset,
                buffer, r, g, b, a);
    }

    public static void drawCube(final double minX, final double minY, final double minZ,
                                final double maxX, final double maxY, final double maxZ,
                                final VertexConsumer buffer, int r, int g, int b, int a) {
        drawPlaneNegX(minX, minY, maxY, minZ, maxZ, buffer, r, g, b, a);
        drawPlanePosX(maxX, minY, maxY, minZ, maxZ, buffer, r, g, b, a);
        drawPlaneNegY(minY, minX, maxX, minZ, maxZ, buffer, r, g, b, a);
        drawPlanePosY(maxY, minX, maxX, minZ, maxZ, buffer, r, g, b, a);
        drawPlaneNegZ(minZ, minX, maxX, minY, maxY, buffer, r, g, b, a);
        drawPlanePosZ(maxZ, minX, maxX, minY, maxY, buffer, r, g, b, a);
    }

    public static void drawPlaneNegX(final double x, final double minY, final double maxY,
                                     final double minZ, final double maxZ, final VertexConsumer buffer,
                                     int r, int g, int b, int a) {
        buffer.vertex(x, minY, minZ).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        buffer.vertex(x, minY, maxZ).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        buffer.vertex(x, maxY, maxZ).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        buffer.vertex(x, maxY, minZ).color(r, g, b, a).normal(-1, 0, 0).endVertex();
    }

    public static void drawPlanePosX(final double x, final double minY, final double maxY,
                                     final double minZ, final double maxZ, final VertexConsumer buffer,
                                     int r, int g, int b, int a) {
        buffer.vertex(x, minY, minZ).color(r, g, b, a).normal(1, 0, 0).endVertex();
        buffer.vertex(x, maxY, minZ).color(r, g, b, a).normal(1, 0, 0).endVertex();
        buffer.vertex(x, maxY, maxZ).color(r, g, b, a).normal(1, 0, 0).endVertex();
        buffer.vertex(x, minY, maxZ).color(r, g, b, a).normal(1, 0, 0).endVertex();
    }

    public static void drawPlaneNegY(final double y, final double minX, final double maxX,
                                     final double minZ, final double maxZ, final VertexConsumer buffer,
                                     int r, int g, int b, int a) {
        buffer.vertex(minX, y, minZ).color(r, g, b, a).normal(0, -1, 0).endVertex();
        buffer.vertex(maxX, y, minZ).color(r, g, b, a).normal(0, -1, 0).endVertex();
        buffer.vertex(maxX, y, maxZ).color(r, g, b, a).normal(0, -1, 0).endVertex();
        buffer.vertex(minX, y, maxZ).color(r, g, b, a).normal(0, -1, 0).endVertex();
    }

    public static void drawPlanePosY(final double y, final double minX, final double maxX,
                                     final double minZ, final double maxZ, final VertexConsumer buffer,
                                     int r, int g, int b, int a) {
        buffer.vertex(minX, y, minZ).color(r, g, b, a).normal(0, 1, 0).endVertex();
        buffer.vertex(minX, y, maxZ).color(r, g, b, a).normal(0, 1, 0).endVertex();
        buffer.vertex(maxX, y, maxZ).color(r, g, b, a).normal(0, 1, 0).endVertex();
        buffer.vertex(maxX, y, minZ).color(r, g, b, a).normal(0, 1, 0).endVertex();
    }

    public static void drawPlaneNegZ(final double z, final double minX, final double maxX,
                                     final double minY, final double maxY, final VertexConsumer buffer,
                                     int r, int g, int b, int a) {
        buffer.vertex(minX, minY, z).color(r, g, b, a).normal(0, 0, -1).endVertex();
        buffer.vertex(minX, maxY, z).color(r, g, b, a).normal(0, 0, -1).endVertex();
        buffer.vertex(maxX, maxY, z).color(r, g, b, a).normal(0, 0, -1).endVertex();
        buffer.vertex(maxX, minY, z).color(r, g, b, a).normal(0, 0, -1).endVertex();
    }

    public static void drawPlanePosZ(final double z, final double minX, final double maxX,
                                     final double minY, final double maxY, final VertexConsumer buffer,
                                     int r, int g, int b, int a) {
        buffer.vertex(minX, minY, z).color(r, g, b, a).normal(0, 0, 1).endVertex();
        buffer.vertex(maxX, minY, z).color(r, g, b, a).normal(0, 0, 1).endVertex();
        buffer.vertex(maxX, maxY, z).color(r, g, b, a).normal(0, 0, 1).endVertex();
        buffer.vertex(minX, maxY, z).color(r, g, b, a).normal(0, 0, 1).endVertex();
    }

    public static void drawCapsuleCube(AABB boundingBox, VertexConsumer bufferBuilder, int r, int g, int b, int a) {
        drawPlaneNegX(boundingBox.minX, boundingBox.minY, boundingBox.maxY, boundingBox.minZ, boundingBox.maxZ, bufferBuilder, r, g, b, a);
        drawPlanePosX(boundingBox.maxX, boundingBox.minY, boundingBox.maxY, boundingBox.minZ, boundingBox.maxZ, bufferBuilder, r, g, b, a);
        drawPlaneNegY(boundingBox.minY, boundingBox.minX, boundingBox.maxX, boundingBox.minZ, boundingBox.maxZ, bufferBuilder, r, g, b, a);
        drawPlanePosY(boundingBox.maxY, boundingBox.minX, boundingBox.maxX, boundingBox.minZ, boundingBox.maxZ, bufferBuilder, r, g, b, a);
        drawPlaneNegZ(boundingBox.minZ, boundingBox.minX, boundingBox.maxX, boundingBox.minY, boundingBox.maxY, bufferBuilder, r, g, b, a);
        drawPlanePosZ(boundingBox.maxZ, boundingBox.minX, boundingBox.maxX, boundingBox.minY, boundingBox.maxY, bufferBuilder, r, g, b, a);
    }

    public static float[] prevColor = new float[4];

    public static void pushColor(final int rgb, final int alpha) {
        prevColor = RenderSystem.getShaderColor();
        final int r = (rgb >> 16) & 0xFF;
        final int g = (rgb >> 8) & 0xFF;
        final int b = rgb & 0xFF;

        final float af = alpha / 255f;
        final float rf = r / 255f;
        final float gf = g / 255f;
        final float bf = b / 255f;
        RenderSystem.setShaderColor(rf, gf, bf, af);
    }

    public static void popColor() {
        if (prevColor != null && prevColor.length == 4)
            RenderSystem.setShaderColor(prevColor[0], prevColor[1], prevColor[2], prevColor[3]);
    }
}

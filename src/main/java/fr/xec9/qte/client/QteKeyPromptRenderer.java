package fr.xec9.qte.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.xec9.qte.QteEngine;
import fr.xec9.qte.domain.QteStatus;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

final class QteKeyPromptRenderer {
    private static final ResourceLocation KEY = sprite("qte_key");
    private static final ResourceLocation KEY_PRESSED = sprite("qte_key_pressed");
    private static final ResourceLocation MOUSE_BASE = sprite("qte_mouse_base");
    private static final ResourceLocation KEY_FONT = ResourceLocation.fromNamespaceAndPath(
        QteEngine.MOD_ID, "qte_key_compact"
    );
    private static final int ACTIVE = 0xFFF1F4F4;
    private static final int SUCCESS = 0xFF70E08C;
    private static final int FAILURE = 0xFFFF795F;
    private static final int LABEL = 0xFFF4F5F5;

    private QteKeyPromptRenderer() {}

    static void render(
        GuiGraphics graphics,
        Font font,
        QteClient.ClientSession session,
        int centerX,
        int centerY,
        double remainingFraction,
        double entryProgress,
        boolean showMechanicProgress
    ) {
        QteStatus status = session.judge().status();
        boolean terminal = status.terminal();
        double feedbackProgress = QteHudModel.clampProgress(session.lingerTicks() / 20.0);
        double scale = QteHudModel.entryScale(entryProgress);
        int alpha = QteHudModel.alpha(entryProgress, 255);
        int shake = 0;
        if (terminal) {
            alpha = QteHudModel.feedbackAlpha(session.lingerTicks());
            if (status == QteStatus.SUCCESS) {
                scale *= QteHudModel.successScale(feedbackProgress);
            } else {
                shake = QteHudModel.failureShake(session.lingerTicks());
            }
        }

        graphics.pose().pushPose();
        graphics.pose().translate(centerX + shake, centerY, 0);
        graphics.pose().scale((float) scale, (float) scale, 1);
        graphics.pose().translate(-centerX, -centerY, 0);

        int ringColor = withAlpha(statusColor(status), QteHudModel.countdownAlpha(alpha));
        drawRing(
            graphics,
            centerX,
            centerY,
            QteHudModel.RING_INNER_RADIUS,
            QteHudModel.RING_OUTER_RADIUS,
            terminal ? 1 : remainingFraction,
            ringColor
        );
        if (showMechanicProgress && !terminal) {
            drawRing(
                graphics,
                centerX,
                centerY,
                QteHudModel.SUCCESS_PROGRESS_INNER_RADIUS,
                QteHudModel.SUCCESS_PROGRESS_OUTER_RADIUS,
                session.judge().progress(),
                withAlpha(
                    QteHudModel.SUCCESS_INDICATOR_COLOR,
                    QteHudModel.successIndicatorAlpha(alpha)
                )
            );
        }

        String label = QteHudModel.keyLabel(session.definition().keys().getFirst());
        drawKeycap(graphics, font, label, centerX, centerY, QteHudModel.KEY_SIZE, session.primaryKeyDown() || terminal, alpha);
        graphics.pose().popPose();
    }

    static void drawKeycap(
        GuiGraphics graphics,
        Font font,
        String label,
        int centerX,
        int centerY,
        int size,
        boolean pressed,
        int alpha
    ) {
        drawKeycap(graphics, font, label, centerX, centerY, size, pressed, alpha, KEY_FONT);
    }

    private static void drawKeycap(
        GuiGraphics graphics,
        Font font,
        String label,
        int centerX,
        int centerY,
        int size,
        boolean pressed,
        int alpha,
        ResourceLocation labelFont
    ) {
        int x = centerX - size / 2;
        int y = centerY - size / 2;
        String mouseSprite = QteHudModel.mousePromptSprite(label);
        if (mouseSprite != null) {
            RenderSystem.setShaderColor(1, 1, 1, alpha / 255.0F);
            graphics.blitSprite(MOUSE_BASE, x, y, size, size);
            int blinkAlpha = QteHudModel.mouseBlinkAlpha(Util.getMillis(), pressed);
            int highlightAlpha = (alpha * blinkAlpha + 127) / 255;
            RenderSystem.setShaderColor(1, 1, 1, highlightAlpha / 255.0F);
            graphics.blitSprite(sprite(mouseSprite), x, y, size, size);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            return;
        }

        RenderSystem.setShaderColor(1, 1, 1, alpha / 255.0F);
        graphics.blitSprite(pressed ? KEY_PRESSED : KEY, x, y, size, size);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        Component text = Component.literal(label).withStyle(style -> style.withFont(labelFont));
        int maxTextWidth = Math.max(8, size - 9);
        float textScale = Math.min(1, (float) maxTextWidth / Math.max(1, font.width(text)));
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY + 1 + QteHudModel.keyLabelYOffset(pressed), 0);
        graphics.pose().scale(textScale, textScale, 1);
        graphics.drawString(
            font,
            text,
            -font.width(text) / 2,
            -font.lineHeight / 2,
            withAlpha(LABEL, alpha),
            false
        );
        graphics.pose().popPose();
    }

    static void drawRing(
        GuiGraphics graphics,
        float centerX,
        float centerY,
        float innerRadius,
        float outerRadius,
        double fraction,
        int color
    ) {
        drawArc(graphics, centerX, centerY, innerRadius, outerRadius, 0, fraction, color);
    }

    static void drawArc(
        GuiGraphics graphics,
        float centerX,
        float centerY,
        float innerRadius,
        float outerRadius,
        double startFraction,
        double fraction,
        int color
    ) {
        double clamped = QteHudModel.clampProgress(fraction);
        if (clamped <= 0 || ((color >>> 24) & 0xFF) == 0) {
            return;
        }

        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f pose = graphics.pose().last().pose();
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        int alpha = color >>> 24;
        int segmentCount = QteHudModel.ringSegments(clamped);
        double start = -Math.PI / 2 + startFraction * Math.PI * 2;
        double end = start + Math.PI * 2 * clamped;
        for (int index = 0; index < segmentCount; index++) {
            double angle0 = start + (end - start) * index / segmentCount;
            double angle1 = start + (end - start) * (index + 1) / segmentCount;
            float innerX0 = centerX + (float) Math.cos(angle0) * innerRadius;
            float innerY0 = centerY + (float) Math.sin(angle0) * innerRadius;
            float outerX0 = centerX + (float) Math.cos(angle0) * outerRadius;
            float outerY0 = centerY + (float) Math.sin(angle0) * outerRadius;
            float innerX1 = centerX + (float) Math.cos(angle1) * innerRadius;
            float innerY1 = centerY + (float) Math.sin(angle1) * innerRadius;
            float outerX1 = centerX + (float) Math.cos(angle1) * outerRadius;
            float outerY1 = centerY + (float) Math.sin(angle1) * outerRadius;
            buffer.addVertex(pose, innerX0, innerY0, 0).setColor(red, green, blue, alpha);
            buffer.addVertex(pose, outerX0, outerY0, 0).setColor(red, green, blue, alpha);
            buffer.addVertex(pose, outerX1, outerY1, 0).setColor(red, green, blue, alpha);
            buffer.addVertex(pose, innerX1, innerY1, 0).setColor(red, green, blue, alpha);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    static int withAlpha(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0xFFFFFF);
    }

    private static int statusColor(QteStatus status) {
        return switch (status) {
            case ACTIVE -> ACTIVE;
            case SUCCESS -> SUCCESS;
            case FAILURE, TIMEOUT -> FAILURE;
        };
    }

    private static ResourceLocation sprite(String path) {
        return ResourceLocation.fromNamespaceAndPath(QteEngine.MOD_ID, path);
    }
}

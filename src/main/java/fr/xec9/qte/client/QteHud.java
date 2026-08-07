package fr.xec9.qte.client;

import fr.xec9.qte.QteEngine;
import fr.xec9.qte.domain.QteStatus;
import fr.xec9.qte.domain.QteType;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

final class QteHud {
    private static final ResourceLocation FRAME = sprite("qte_hud_frame");
    private static final ResourceLocation KEYCAP = sprite("qte_keycap");
    private static final ResourceLocation MARKER = sprite("qte_marker");

    private static final int INK = 0xFF101820;
    private static final int PANEL = 0xFF172630;
    private static final int EDGE = 0xFF29414C;
    private static final int SIGNAL = 0xFF52D6C8;
    private static final int WARNING = 0xFFFFBE55;
    private static final int SUCCESS = 0xFF74DF8B;
    private static final int FAILURE = 0xFFFF6577;
    private static final int TEXT = 0xFFEAF7F5;
    private static final int MUTED = 0xFF8DA6AA;

    private QteHud() {}

    static void render(GuiGraphics graphics, QteClient.ClientSession session) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        QteHudModel.Layout layout = QteHudModel.layout(graphics.guiWidth(), graphics.guiHeight());
        int x = layout.x();
        int y = layout.y();
        int width = layout.width();

        graphics.fill(x + 3, y + 4, x + width + 3, y + layout.height() + 4, 0x62000000);
        graphics.blitSprite(FRAME, x, y, width, layout.height());

        double remainingFraction = (double) (session.definition().durationTicks() - session.elapsed())
            / session.definition().durationTicks();
        if (session.judge().status() == QteStatus.ACTIVE
            && QteHudModel.urgency(remainingFraction) == QteHudModel.Urgency.URGENT
            && (session.elapsed() / 3) % 2 == 0) {
            graphics.fill(x + 5, y + 2, x + width - 5, y + 3, WARNING);
        }

        drawHeader(graphics, font, session, x, y, width);

        int mechanicLeft = x + 12;
        if (drawCustomTexture(graphics, session, x, y, width)) {
            mechanicLeft = x + 64;
        }
        int mechanicRight = x + width - 12;
        drawMechanic(graphics, font, session, mechanicLeft, mechanicRight, y);
        drawFooter(graphics, font, session, x, y, width);
    }

    private static void drawHeader(
        GuiGraphics graphics,
        Font font,
        QteClient.ClientSession session,
        int x,
        int y,
        int width
    ) {
        int color = statusColor(session.judge().status());
        graphics.fill(x + 10, y + 9, x + 13, y + 16, color);
        graphics.fill(x + 8, y + 11, x + 15, y + 14, color);
        graphics.drawString(
            font,
            Component.translatable("qte.type." + session.definition().type().name().toLowerCase(Locale.ROOT)),
            x + 20,
            y + 9,
            TEXT,
            false
        );

        String time = String.format(Locale.ROOT, "%.1fs", session.timeRemaining());
        int timeColor = session.timeRemaining() <= session.definition().durationSeconds() * 0.25
            ? WARNING
            : TEXT;
        graphics.drawString(font, time, x + width - 11 - font.width(time), y + 9, timeColor, false);
        graphics.fill(x + 10, y + 22, x + width - 10, y + 23, EDGE);
        int durationWidth = width - 20;
        int remainingWidth = (int) Math.round(durationWidth * QteHudModel.clampProgress(
            (double) (session.definition().durationTicks() - session.elapsed()) / session.definition().durationTicks()
        ));
        graphics.fill(x + 10, y + 22, x + 10 + remainingWidth, y + 23, timeColor);
    }

    private static boolean drawCustomTexture(
        GuiGraphics graphics,
        QteClient.ClientSession session,
        int x,
        int y,
        int width
    ) {
        if (session.definition().texture() == null || width < 280) {
            return false;
        }
        ResourceLocation texture = ResourceLocation.tryParse(session.definition().texture());
        if (texture == null) {
            return false;
        }
        graphics.fill(x + 9, y + 28, x + 57, y + 68, INK);
        graphics.blit(texture, x + 13, y + 28, 0, 0, 40, 40, 40, 40);
        graphics.fill(x + 9, y + 28, x + 10, y + 68, SIGNAL);
        return true;
    }

    private static void drawMechanic(
        GuiGraphics graphics,
        Font font,
        QteClient.ClientSession session,
        int left,
        int right,
        int panelY
    ) {
        QteStatus status = session.judge().status();
        if (status.terminal()) {
            drawResult(graphics, font, status, left, right, panelY + 36);
            return;
        }

        switch (QteHudModel.mechanic(session.definition().type())) {
            case TIMING -> drawTiming(graphics, font, session, left, right, panelY);
            case PRECISION -> drawPrecision(graphics, font, session, left, right, panelY);
            case AIM -> drawAim(graphics, font, session, left, right, panelY);
            case TRACKING -> drawTracking(graphics, font, session, left, right, panelY);
            case SEQUENCE -> drawSequence(graphics, font, session, left, right, panelY);
            case HOLD -> drawHold(graphics, font, session, left, right, panelY);
            case MASH -> drawMash(graphics, font, session, left, right, panelY);
            case SINGLE -> drawSingle(graphics, font, session, left, right, panelY);
        }
    }

    private static void drawTiming(
        GuiGraphics graphics,
        Font font,
        QteClient.ClientSession session,
        int left,
        int right,
        int y
    ) {
        drawAction(graphics, font, "qte.action.timing", left, right, y + 29);
        int barY = y + 46;
        graphics.fill(left, barY, right, barY + 8, INK);
        graphics.fill(left + 2, barY + 2, right - 2, barY + 6, EDGE);
        int width = right - left - 4;
        QteHudModel.Band band = QteHudModel.timingSuccessBand(width);
        int targetLeft = left + 2 + band.left();
        int targetRight = left + 2 + band.right();
        graphics.fill(targetLeft, barY, targetRight, barY + 8, SUCCESS);
        graphics.fill(targetLeft, barY - 2, targetLeft + 2, barY + 10, INK);
        graphics.fill(targetRight - 2, barY - 2, targetRight, barY + 10, INK);
        graphics.drawCenteredString(
            font,
            Component.translatable("qte.zone.success"),
            (targetLeft + targetRight) / 2,
            y + 57,
            SUCCESS
        );
        double phase = QteHudModel.clampProgress((double) session.elapsed() / session.definition().durationTicks());
        int marker = left + 2 + (int) Math.round(width * phase);
        graphics.blitSprite(MARKER, marker - 2, barY - 2, 5, 11);
    }

    private static void drawPrecision(
        GuiGraphics graphics,
        Font font,
        QteClient.ClientSession session,
        int left,
        int right,
        int y
    ) {
        drawAction(graphics, font, "qte.action.precision", left, right, y + 29);
        int barY = y + 46;
        graphics.fill(left, barY, right, barY + 8, INK);
        graphics.fill(left + 2, barY + 2, right - 2, barY + 6, EDGE);
        int center = (left + right) / 2;
        graphics.fill(center - 13, barY, center + 13, barY + 8, SUCCESS);
        graphics.fill(center - 13, barY - 2, center - 11, barY + 10, INK);
        graphics.fill(center + 11, barY - 2, center + 13, barY + 10, INK);
        graphics.fill(center - 1, barY, center + 1, barY + 8, INK);
        int travel = Math.max(1, (right - left - 10) / 2);
        int marker = center + (int) Math.round(session.markerDistance() * travel);
        graphics.blitSprite(MARKER, marker - 2, barY - 2, 5, 11);
    }

    private static void drawAim(
        GuiGraphics graphics,
        Font font,
        QteClient.ClientSession session,
        int left,
        int right,
        int y
    ) {
        String key = QteHudModel.keyLabel(session.definition().keys().getFirst());
        graphics.drawCenteredString(
            font,
            Component.translatable("qte.action.aim", key),
            (left + right) / 2,
            y + 27,
            MUTED
        );
        drawPointerField(graphics, session, left, right, y + 39, false);
    }

    private static void drawTracking(
        GuiGraphics graphics,
        Font font,
        QteClient.ClientSession session,
        int left,
        int right,
        int y
    ) {
        String key = QteHudModel.keyLabel(session.definition().keys().getFirst());
        graphics.drawCenteredString(
            font,
            Component.translatable("qte.action.tracking", key),
            (left + right) / 2,
            y + 27,
            MUTED
        );
        drawPointerField(graphics, session, left, right, y + 39, true);
    }

    private static void drawPointerField(
        GuiGraphics graphics,
        QteClient.ClientSession session,
        int left,
        int right,
        int top,
        boolean tracking
    ) {
        int bottom = top + 24;
        graphics.fill(left, top, right, bottom, INK);
        graphics.fill(left + 1, top + 1, right - 1, bottom - 1, PANEL);
        for (int tick = left + 16; tick < right; tick += 16) {
            graphics.fill(tick, top + 2, tick + 1, bottom - 2, 0xFF203640);
        }
        int centerY = (top + bottom) / 2;
        graphics.fill(left + 2, centerY, right - 2, centerY + 1, 0xFF203640);

        int targetX = normalizedX(session.pointerTarget().x(), left + 5, right - 5);
        int targetY = normalizedY(session.pointerTarget().y(), top + 4, bottom - 4);
        int radius = tracking ? 4 : 5;
        graphics.fill(targetX - radius, targetY - 1, targetX + radius + 1, targetY + 2, SUCCESS);
        graphics.fill(targetX - 1, targetY - radius, targetX + 2, targetY + radius + 1, SUCCESS);
        graphics.fill(targetX - 1, targetY - 1, targetX + 2, targetY + 2, INK);

        int pointerX = normalizedX(session.pointer().x(), left + 3, right - 3);
        int pointerY = normalizedY(session.pointer().y(), top + 3, bottom - 3);
        graphics.fill(pointerX - 3, pointerY, pointerX + 4, pointerY + 1, WARNING);
        graphics.fill(pointerX, pointerY - 3, pointerX + 1, pointerY + 4, WARNING);

        if (tracking) {
            int dwell = (int) Math.round((right - left - 2) * session.judge().progress());
            graphics.fill(left + 1, bottom - 2, left + 1 + dwell, bottom - 1, SIGNAL);
        }
    }

    private static int normalizedX(double value, int left, int right) {
        return left + (int) Math.round((value + 1) * 0.5 * (right - left));
    }

    private static int normalizedY(double value, int top, int bottom) {
        return top + (int) Math.round((value + 1) * 0.5 * (bottom - top));
    }

    private static void drawSequence(
        GuiGraphics graphics,
        Font font,
        QteClient.ClientSession session,
        int left,
        int right,
        int y
    ) {
        String action = session.definition().type() == QteType.MEMORY
            ? "qte.action.memory"
            : "qte.action.sequence";
        drawAction(graphics, font, action, left, right, y + 27);

        List<String> keys = session.definition().keys();
        int available = right - left;
        int maxVisible = available < 180 ? 4 : 5;
        int visible = Math.min(maxVisible, keys.size());
        int start = Math.max(0, Math.min(session.judge().currentIndex() - visible / 2, keys.size() - visible));
        boolean hideMemory = session.definition().type() == QteType.MEMORY
            && session.elapsed() > session.definition().durationTicks() * 0.35;

        int[] widths = new int[visible];
        int totalWidth = 0;
        for (int offset = 0; offset < visible; offset++) {
            String label = hideMemory ? "?" : QteHudModel.keyLabel(keys.get(start + offset));
            widths[offset] = Math.max(22, Math.min(46, font.width(label) + 10));
            totalWidth += widths[offset];
        }
        totalWidth += Math.max(0, visible - 1) * 4;
        int drawX = left + Math.max(0, (available - totalWidth) / 2);
        for (int offset = 0; offset < visible; offset++) {
            int index = start + offset;
            String label = hideMemory ? "?" : QteHudModel.keyLabel(keys.get(index));
            int keyColor = index < session.judge().currentIndex()
                ? SUCCESS
                : index == session.judge().currentIndex() ? WARNING : MUTED;
            drawKeycap(graphics, font, label, drawX, y + 41, widths[offset], 20, keyColor,
                index == session.judge().currentIndex());
            drawX += widths[offset] + 4;
        }
    }

    private static void drawHold(
        GuiGraphics graphics,
        Font font,
        QteClient.ClientSession session,
        int left,
        int right,
        int y
    ) {
        drawAction(graphics, font, "qte.action.hold", left, right, y + 27);
        String label = QteHudModel.keyLabel(session.definition().keys().getFirst());
        int keyWidth = Math.max(38, font.width(label) + 14);
        int keyX = (left + right - keyWidth) / 2;
        drawKeycap(graphics, font, label, keyX, y + 40, keyWidth, 21, WARNING, true);
        int fill = (int) Math.round((keyWidth - 6) * QteHudModel.clampProgress(session.judge().progress()));
        graphics.fill(keyX + 3, y + 58, keyX + 3 + fill, y + 60, SIGNAL);
    }

    private static void drawMash(
        GuiGraphics graphics,
        Font font,
        QteClient.ClientSession session,
        int left,
        int right,
        int y
    ) {
        drawAction(graphics, font, "qte.action.mash", left, right, y + 27);
        String label = QteHudModel.keyLabel(session.definition().keys().getFirst());
        int keyWidth = Math.max(38, font.width(label) + 14);
        int keyX = (left + right - keyWidth) / 2;
        boolean pulse = (session.elapsed() / 2) % 2 == 0;
        drawKeycap(graphics, font, label, keyX, y + 40, keyWidth, 21, pulse ? WARNING : TEXT, pulse);
        String percent = Math.round(session.judge().progress() * 100) + "%";
        graphics.drawString(font, percent, Math.min(right - font.width(percent), keyX + keyWidth + 7), y + 47, SIGNAL, false);
    }

    private static void drawSingle(
        GuiGraphics graphics,
        Font font,
        QteClient.ClientSession session,
        int left,
        int right,
        int y
    ) {
        drawAction(graphics, font, "qte.action.press", left, right, y + 27);
        List<String> keys = session.definition().keys();
        int visible = Math.min(3, keys.size());
        int totalWidth = 0;
        int[] widths = new int[visible];
        for (int index = 0; index < visible; index++) {
            String label = QteHudModel.keyLabel(keys.get(index));
            widths[index] = Math.max(34, Math.min(58, font.width(label) + 16));
            totalWidth += widths[index];
        }
        totalWidth += Math.max(0, visible - 1) * 6;
        int drawX = left + Math.max(0, (right - left - totalWidth) / 2);
        for (int index = 0; index < visible; index++) {
            drawKeycap(graphics, font, QteHudModel.keyLabel(keys.get(index)), drawX, y + 40,
                widths[index], 22, WARNING, true);
            drawX += widths[index] + 6;
        }
    }

    private static void drawKeycap(
        GuiGraphics graphics,
        Font font,
        String label,
        int x,
        int y,
        int width,
        int height,
        int color,
        boolean active
    ) {
        graphics.blitSprite(KEYCAP, x, y, width, height);
        if (active) {
            graphics.fill(x + 4, y + 2, x + width - 4, y + 3, color);
        }
        String visible = label.length() > 8 ? label.substring(0, 8) : label;
        graphics.drawString(font, visible, x + (width - font.width(visible)) / 2, y + 7, color, false);
    }

    private static void drawResult(
        GuiGraphics graphics,
        Font font,
        QteStatus status,
        int left,
        int right,
        int y
    ) {
        boolean success = status == QteStatus.SUCCESS;
        Component result = Component.translatable(success ? "qte.result.success" : "qte.result.failure");
        int color = success ? SUCCESS : FAILURE;
        int center = (left + right) / 2;
        graphics.fill(center - 34, y - 6, center + 34, y + 16, INK);
        graphics.fill(center - 34, y - 6, center - 31, y + 16, color);
        graphics.drawCenteredString(font, result, center + 2, y + 1, color);
    }

    private static void drawFooter(
        GuiGraphics graphics,
        Font font,
        QteClient.ClientSession session,
        int x,
        int y,
        int width
    ) {
        int railLeft = x + 10;
        int railRight = x + width - 10;
        int railY = y + 74;
        graphics.fill(railLeft, railY, railRight, railY + 5, INK);
        graphics.fill(railLeft + 1, railY + 1, railRight - 1, railY + 4, PANEL);
        int fill = (int) Math.round((railRight - railLeft - 2) * QteHudModel.clampProgress(session.judge().progress()));
        graphics.fill(railLeft + 1, railY + 1, railLeft + 1 + fill, railY + 4, statusColor(session.judge().status()));
        for (int tick = railLeft + 18; tick < railRight; tick += 18) {
            graphics.fill(tick, railY + 1, tick + 1, railY + 4, INK);
        }
    }

    private static void drawAction(
        GuiGraphics graphics,
        Font font,
        String translationKey,
        int left,
        int right,
        int y
    ) {
        graphics.drawCenteredString(font, Component.translatable(translationKey), (left + right) / 2, y, MUTED);
    }

    private static int statusColor(QteStatus status) {
        return switch (status) {
            case ACTIVE -> SIGNAL;
            case SUCCESS -> SUCCESS;
            case FAILURE, TIMEOUT -> FAILURE;
        };
    }

    private static ResourceLocation sprite(String path) {
        return ResourceLocation.fromNamespaceAndPath(QteEngine.MOD_ID, path);
    }
}

package fr.aicha.freshsmooth.qte.client;

import fr.aicha.freshsmooth.qte.domain.QteStatus;
import fr.aicha.freshsmooth.qte.domain.QteType;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

final class QteHud {
    private static final int WIDTH = 250;
    private static final int HEIGHT = 76;

    private QteHud() {}

    static void render(GuiGraphics graphics, QteClient.ClientSession session) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int x = (graphics.guiWidth() - WIDTH) / 2;
        int y = graphics.guiHeight() - HEIGHT - 34;

        graphics.fill(x - 2, y - 2, x + WIDTH + 2, y + HEIGHT + 2, 0xB0000000);
        graphics.fill(x, y, x + WIDTH, y + 2, statusColor(session.judge().status()));

        if (session.definition().texture() != null) {
            ResourceLocation texture = ResourceLocation.tryParse(session.definition().texture());
            if (texture != null) {
                graphics.blit(texture, x + 6, y + 8, 0, 0, 48, 48, 48, 48);
            }
        }

        int contentX = x + (session.definition().texture() == null ? 10 : 62);
        graphics.drawString(
            font,
            Component.translatable("qte.type." + session.definition().type().name().toLowerCase(Locale.ROOT)),
            contentX,
            y + 9,
            0xFFFFFF
        );
        graphics.drawString(font, String.format(Locale.ROOT, "%.1fs", session.timeRemaining()), x + WIDTH - 37, y + 9, 0xFFE18A);

        drawMechanic(graphics, font, session, contentX, y + 27, x + WIDTH - 10);

        int progressWidth = (int) ((WIDTH - 20) * session.judge().progress());
        graphics.drawString(
            font,
            Component.translatable("qte.keyboard_layout", QteClient.keyboardLayout()),
            x + 10,
            y + HEIGHT - 24,
            0x9EA8B8
        );
        graphics.fill(x + 10, y + HEIGHT - 12, x + WIDTH - 10, y + HEIGHT - 7, 0xFF343A46);
        graphics.fill(x + 10, y + HEIGHT - 12, x + 10 + progressWidth, y + HEIGHT - 7, statusColor(session.judge().status()));
    }

    private static void drawMechanic(
        GuiGraphics graphics,
        Font font,
        QteClient.ClientSession session,
        int left,
        int y,
        int right
    ) {
        QteStatus status = session.judge().status();
        if (status.terminal()) {
            Component result = status == QteStatus.SUCCESS
                ? Component.translatable("qte.result.success")
                : Component.translatable("qte.result.failure");
            graphics.drawString(font, result, left, y + 7, status == QteStatus.SUCCESS ? 0x71E58B : 0xFF6B6B);
            return;
        }

        QteType type = session.definition().type();
        if (type == QteType.TIMING || type == QteType.DIALOGUE_TIMING) {
            drawTimingBar(graphics, session, left, y, right);
        } else if (isPrecision(type)) {
            drawPrecisionBar(graphics, session, left, y, right);
        } else {
            boolean hideMemory = type == QteType.MEMORY
                && session.elapsed() > session.definition().durationTicks() * 0.35;
            String inputs = hideMemory ? "?  ?  ?" : formattedPattern(session);
            graphics.drawString(font, inputs, left, y + 7, 0xE8EDF7);
        }
    }

    private static void drawTimingBar(
        GuiGraphics graphics,
        QteClient.ClientSession session,
        int left,
        int y,
        int right
    ) {
        graphics.fill(left, y + 8, right, y + 14, 0xFF343A46);
        int width = right - left;
        graphics.fill(left + (int) (width * 0.58), y + 7, left + (int) (width * 0.82), y + 15, 0xFF2C9F60);
        double phase = Math.min(1, (double) session.elapsed() / session.definition().durationTicks());
        int marker = left + (int) (width * phase);
        graphics.fill(marker - 1, y + 4, marker + 2, y + 18, 0xFFFFFFFF);
    }

    private static void drawPrecisionBar(
        GuiGraphics graphics,
        QteClient.ClientSession session,
        int left,
        int y,
        int right
    ) {
        graphics.fill(left, y + 8, right, y + 14, 0xFF343A46);
        int center = (left + right) / 2;
        graphics.fill(center - 10, y + 6, center + 10, y + 16, 0xFF2C9F60);
        int marker = center + (int) (session.markerDistance() * (right - left - 8) / 2);
        graphics.fill(marker - 2, y + 3, marker + 2, y + 19, 0xFFFFFFFF);
    }

    private static String formattedPattern(QteClient.ClientSession session) {
        StringBuilder text = new StringBuilder();
        for (int index = 0; index < session.definition().keys().size(); index++) {
            if (index > 0) {
                text.append("  ");
            }
            String key = session.definition().keys().get(index)
                .replace("key.keyboard.", "")
                .replace("key.localized.", "")
                .replace("key.mouse.", "MOUSE ")
                .toUpperCase(Locale.ROOT);
            if (index == session.judge().currentIndex()) {
                text.append('[').append(key).append(']');
            } else {
                text.append(key);
            }
        }
        return text.toString();
    }

    private static boolean isPrecision(QteType type) {
        return switch (type) {
            case ANALOG_PRECISION, AIM, TRACKING, BALANCE -> true;
            default -> false;
        };
    }

    private static int statusColor(QteStatus status) {
        return switch (status) {
            case ACTIVE -> 0xFF4BA3FF;
            case SUCCESS -> 0xFF43D17A;
            case FAILURE, TIMEOUT -> 0xFFFF5D67;
        };
    }
}

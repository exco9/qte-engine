package fr.xec9.qte.client;

import fr.xec9.qte.domain.QteBalanceModel;
import fr.xec9.qte.domain.QteStatus;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

final class QteHud {
    private static final int ACTIVE = 0xFFF1F4F4;
    private static final int SUCCESS = 0xFF70E08C;
    private static final int FAILURE = 0xFFFF795F;
    private static final int POINTER_MARGIN = 24;

    private QteHud() {}

    static void render(GuiGraphics graphics, QteClient.ClientSession session, float partialTick) {
        Font font = Minecraft.getInstance().font;
        QteHudModel.Layout layout = QteHudModel.layout(graphics.guiWidth(), graphics.guiHeight());
        int centerX = layout.x() + layout.width() / 2;
        int centerY = layout.y() + layout.height() / 2;
        double remaining = QteHudModel.remainingFraction(
            session.elapsed(), session.definition().durationTicks(), partialTick
        );
        double entry = QteHudModel.entryProgress(session.elapsed(), partialTick);

        switch (QteHudModel.mechanic(session.definition().type())) {
            case SINGLE -> QteKeyPromptRenderer.render(graphics, font, session, centerX, centerY, remaining, entry, true);
            case HOLD, MASH -> QteKeyPromptRenderer.render(graphics, font, session, centerX, centerY, remaining, entry, true);
            case SEQUENCE -> drawSequence(graphics, font, session, centerX, centerY, remaining, entry);
            case BALANCE -> drawBalance(graphics, font, session, centerX, centerY, remaining, entry, partialTick);
            case AIM, TRACKING -> drawPointerMechanic(graphics, font, session, centerX, centerY, remaining, entry);
        }
        drawOptionalTexture(graphics, session, centerX, centerY, entry);
    }

    private static void drawSequence(
        GuiGraphics graphics,
        Font font,
        QteClient.ClientSession session,
        int centerX,
        int centerY,
        double remaining,
        double entry
    ) {
        List<String> keys = session.definition().keys();
        int visible = Math.min(5, keys.size());
        int start = Math.max(0, Math.min(session.judge().currentIndex() - visible / 2, keys.size() - visible));
        int size = 24;
        int gap = 4;
        int x = centerX - (visible * size + Math.max(0, visible - 1) * gap) / 2;
        boolean terminal = session.judge().status().terminal();
        int alpha = renderAlpha(session, entry);
        int currentCenterX = centerX;
        for (int offset = 0; offset < visible; offset++) {
            int index = start + offset;
            boolean current = index == session.judge().currentIndex() || terminal && offset == visible - 1;
            if (current) {
                currentCenterX = x + size / 2;
            }
            QteKeyPromptRenderer.drawKeycap(
                graphics,
                font,
                QteHudModel.keyLabel(keys.get(index)),
                x + size / 2,
                centerY,
                size,
                index < session.judge().currentIndex() || current && session.primaryKeyDown(),
                current ? alpha : Math.max(90, alpha * 2 / 3)
            );
            x += size + gap;
        }
        int ringColor = session.judge().status() == QteStatus.SUCCESS ? SUCCESS : terminal ? FAILURE : ACTIVE;
        QteKeyPromptRenderer.drawRing(
            graphics,
            currentCenterX,
            centerY,
            QteHudModel.SEQUENCE_DURATION_INNER_RADIUS,
            QteHudModel.SEQUENCE_DURATION_OUTER_RADIUS,
            terminal ? 1 : remaining,
            QteKeyPromptRenderer.withAlpha(ringColor, QteHudModel.countdownAlpha(alpha))
        );
        if (!terminal) {
            QteKeyPromptRenderer.drawRing(
                graphics,
                currentCenterX,
                centerY,
                QteHudModel.SEQUENCE_SUCCESS_INNER_RADIUS,
                QteHudModel.SEQUENCE_SUCCESS_OUTER_RADIUS,
                session.judge().progress(),
                QteKeyPromptRenderer.withAlpha(
                    QteHudModel.SUCCESS_INDICATOR_COLOR,
                    QteHudModel.successIndicatorAlpha(alpha)
                )
            );
        }
    }

    private static void drawBalance(
        GuiGraphics graphics,
        Font font,
        QteClient.ClientSession session,
        int centerX,
        int centerY,
        double remaining,
        double entry,
        float partialTick
    ) {
        int alpha = renderAlpha(session, entry);
        boolean terminal = session.judge().status().terminal();
        int stateColor = session.judge().status() == QteStatus.SUCCESS ? SUCCESS : terminal ? FAILURE : ACTIVE;
        QteKeyPromptRenderer.drawRing(
            graphics,
            centerX,
            centerY,
            QteHudModel.BALANCE_DURATION_INNER_RADIUS,
            QteHudModel.BALANCE_DURATION_OUTER_RADIUS,
            terminal ? 1 : remaining,
            QteKeyPromptRenderer.withAlpha(stateColor, QteHudModel.countdownAlpha(alpha))
        );
        QteKeyPromptRenderer.drawRing(
            graphics,
            centerX,
            centerY,
            QteHudModel.BALANCE_TRACK_INNER_RADIUS,
            QteHudModel.BALANCE_TRACK_OUTER_RADIUS,
            1,
            QteKeyPromptRenderer.withAlpha(
                QteHudModel.BALANCE_TRACK_COLOR,
                QteHudModel.countdownAlpha(alpha)
            )
        );
        double target = QteBalanceModel.targetPhase(session.sessionSeed());
        QteKeyPromptRenderer.drawArc(
            graphics,
            centerX,
            centerY,
            QteHudModel.BALANCE_TARGET_INNER_RADIUS,
            QteHudModel.BALANCE_TARGET_OUTER_RADIUS,
            target - QteBalanceModel.SUCCESS_HALF_WIDTH,
            QteBalanceModel.SUCCESS_HALF_WIDTH * 2,
            QteKeyPromptRenderer.withAlpha(QteHudModel.BALANCE_TARGET_COLOR, alpha)
        );
        QteKeyPromptRenderer.drawKeycap(
            graphics,
            font,
            QteHudModel.keyLabel(session.definition().keys().getFirst()),
            centerX,
            centerY,
            QteHudModel.BALANCE_KEY_SIZE,
            session.primaryKeyDown() || terminal,
            alpha
        );
        if (!terminal) {
            double needle = QteBalanceModel.needlePhase(
                session.elapsed() + Math.max(0, partialTick), session.definition().durationTicks()
            );
            QteKeyPromptRenderer.drawArc(
                graphics, centerX, centerY, 14, 28, needle - 0.005, 0.010,
                QteKeyPromptRenderer.withAlpha(ACTIVE, alpha)
            );
        }
    }

    private static void drawPointerMechanic(
        GuiGraphics graphics,
        Font font,
        QteClient.ClientSession session,
        int promptX,
        int promptY,
        double remaining,
        double entry
    ) {
        int alpha = renderAlpha(session, entry);
        QteHudModel.ScreenPoint target = QteHudModel.screenPoint(
            session.pointerTarget().x(), session.pointerTarget().y(),
            graphics.guiWidth(), graphics.guiHeight(), POINTER_MARGIN
        );
        QteHudModel.ScreenPoint pointer = QteHudModel.screenPoint(
            session.pointer().x(), session.pointer().y(),
            graphics.guiWidth(), graphics.guiHeight(), POINTER_MARGIN
        );
        boolean tracking = session.definition().type() == fr.xec9.qte.domain.QteType.TRACKING;
        boolean terminal = session.judge().status().terminal();
        int stateColor = session.judge().status() == QteStatus.SUCCESS
            ? SUCCESS
            : terminal ? FAILURE : QteHudModel.ACCENT_COLOR;

        QteKeyPromptRenderer.drawRing(
            graphics, target.x(), target.y(), 0, 10, 1,
            QteKeyPromptRenderer.withAlpha(QteHudModel.TRACK_COLOR, alpha / 2)
        );
        if (tracking) {
            QteKeyPromptRenderer.drawRing(
                graphics, target.x(), target.y(), 0, 9, session.judge().progress(),
                QteKeyPromptRenderer.withAlpha(
                    QteHudModel.SUCCESS_INDICATOR_COLOR,
                    QteHudModel.successIndicatorAlpha(alpha)
                )
            );
        }
        QteKeyPromptRenderer.drawRing(
            graphics, target.x(), target.y(), 10, 12, 1,
            QteKeyPromptRenderer.withAlpha(stateColor, QteHudModel.countdownAlpha(alpha))
        );
        QteKeyPromptRenderer.drawRing(
            graphics, pointer.x(), pointer.y(), 2, 4, 1,
            QteKeyPromptRenderer.withAlpha(ACTIVE, QteHudModel.countdownAlpha(alpha))
        );

        QteKeyPromptRenderer.render(
            graphics, font, session, promptX, promptY, remaining, entry, true
        );
    }

    private static void drawOptionalTexture(
        GuiGraphics graphics,
        QteClient.ClientSession session,
        int centerX,
        int centerY,
        double entry
    ) {
        if (session.definition().texture() == null) {
            return;
        }
        ResourceLocation texture = ResourceLocation.tryParse(session.definition().texture());
        if (texture != null) {
            int alpha = renderAlpha(session, entry);
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1, 1, 1, alpha / 255.0F);
            graphics.blit(texture, centerX - 58, centerY - 12, 0, 0, 24, 24, 24, 24);
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }

    private static int renderAlpha(QteClient.ClientSession session, double entry) {
        return session.judge().status().terminal()
            ? QteHudModel.feedbackAlpha(session.lingerTicks())
            : QteHudModel.alpha(entry, 255);
    }
}

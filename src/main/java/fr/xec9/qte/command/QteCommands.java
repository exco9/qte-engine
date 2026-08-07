package fr.xec9.qte.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.xec9.qte.domain.QteDefinition;
import fr.xec9.qte.domain.QteCommandSchema;
import fr.xec9.qte.domain.QteType;
import fr.xec9.qte.server.QteSavedData;
import fr.xec9.qte.server.QteSessions;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class QteCommands {
    private QteCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("qte")
            .then(Commands.literal("create")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("id", StringArgumentType.word())
                    .then(Commands.argument("type", StringArgumentType.word()).suggests(QteCommands::suggestTypes)
                        .then(Commands.argument(QteCommandSchema.INPUTS_ARGUMENT, StringArgumentType.string())
                            .suggests(QteCommands::suggestInputs)
                            .then(Commands.argument("duration", DoubleArgumentType.doubleArg(0.1, 300))
                                .then(Commands.argument(QteCommandSchema.SUCCESS_RESULT_ARGUMENT, StringArgumentType.string())
                                  .then(Commands.argument(QteCommandSchema.FAILURE_RESULT_ARGUMENT, StringArgumentType.string())
                                    .executes(context -> create(context, false, false, null))
                                    .then(Commands.argument("exclusive_input", BoolArgumentType.bool())
                                        .executes(context -> create(
                                            context,
                                            BoolArgumentType.getBool(context, "exclusive_input"),
                                            false,
                                            null
                                        ))
                                        .then(Commands.argument(QteCommandSchema.HIDE_HUD_ARGUMENT, BoolArgumentType.bool())
                                            .executes(context -> create(
                                                context,
                                                BoolArgumentType.getBool(context, "exclusive_input"),
                                                BoolArgumentType.getBool(context, QteCommandSchema.HIDE_HUD_ARGUMENT),
                                                null
                                            ))
                                            .then(Commands.argument("texture", StringArgumentType.string())
                                                .executes(context -> create(
                                                    context,
                                                    BoolArgumentType.getBool(context, "exclusive_input"),
                                                    BoolArgumentType.getBool(context, QteCommandSchema.HIDE_HUD_ARGUMENT),
                                                    StringArgumentType.getString(context, "texture")
                                                ))))))))))))
            .then(Commands.literal("edit")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("id", StringArgumentType.word()).suggests(QteCommands::suggestIds)
                    .then(Commands.argument("type", StringArgumentType.word()).suggests(QteCommands::suggestTypes)
                        .then(Commands.argument(QteCommandSchema.INPUTS_ARGUMENT, StringArgumentType.string())
                            .suggests(QteCommands::suggestInputs)
                            .then(Commands.argument("duration", DoubleArgumentType.doubleArg(0.1, 300))
                                .then(Commands.argument(QteCommandSchema.SUCCESS_RESULT_ARGUMENT, StringArgumentType.string())
                                  .then(Commands.argument(QteCommandSchema.FAILURE_RESULT_ARGUMENT, StringArgumentType.string())
                                    .executes(context -> edit(context, false, false, null))
                                    .then(Commands.argument("exclusive_input", BoolArgumentType.bool())
                                        .executes(context -> edit(
                                            context,
                                            BoolArgumentType.getBool(context, "exclusive_input"),
                                            false,
                                            null
                                        ))
                                        .then(Commands.argument(QteCommandSchema.HIDE_HUD_ARGUMENT, BoolArgumentType.bool())
                                            .executes(context -> edit(
                                                context,
                                                BoolArgumentType.getBool(context, "exclusive_input"),
                                                BoolArgumentType.getBool(context, QteCommandSchema.HIDE_HUD_ARGUMENT),
                                                null
                                            ))
                                            .then(Commands.argument("texture", StringArgumentType.string())
                                                .executes(context -> edit(
                                                    context,
                                                    BoolArgumentType.getBool(context, "exclusive_input"),
                                                    BoolArgumentType.getBool(context, QteCommandSchema.HIDE_HUD_ARGUMENT),
                                                    StringArgumentType.getString(context, "texture")
                                                ))))))))))))
            .then(Commands.literal("play")
                .then(Commands.argument("id", StringArgumentType.word()).suggests(QteCommands::suggestIds)
                    .executes(QteCommands::play)))
            .then(Commands.literal("remove")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("id", StringArgumentType.word()).suggests(QteCommands::suggestIds)
                    .executes(QteCommands::remove)))
            .then(Commands.literal("list").executes(QteCommands::list))
            .then(Commands.literal("types").executes(QteCommands::types))
        );
    }

    private static int create(
        CommandContext<CommandSourceStack> context,
        boolean exclusiveInput,
        boolean hideHud,
        String texture
    ) {
        try {
            QteDefinition definition = readDefinition(context, exclusiveInput, hideHud, texture);
            QteSavedData data = data(context);
            if (!data.add(definition)) {
                context.getSource().sendFailure(Component.translatable("command.qte.duplicate", definition.id()));
                return 0;
            }
            context.getSource().sendSuccess(() -> Component.translatable("command.qte.created", definition.id()), true);
            return 1;
        } catch (IllegalArgumentException error) {
            context.getSource().sendFailure(Component.literal(error.getMessage()));
            return 0;
        }
    }

    private static int edit(
        CommandContext<CommandSourceStack> context,
        boolean exclusiveInput,
        boolean hideHud,
        String texture
    ) {
        try {
            QteDefinition definition = readDefinition(context, exclusiveInput, hideHud, texture);
            if (!data(context).replace(definition)) {
                context.getSource().sendFailure(Component.translatable("command.qte.missing", definition.id()));
                return 0;
            }
            context.getSource().sendSuccess(() -> Component.translatable("command.qte.edited", definition.id()), true);
            return 1;
        } catch (IllegalArgumentException error) {
            context.getSource().sendFailure(Component.literal(error.getMessage()));
            return 0;
        }
    }

    private static QteDefinition readDefinition(
        CommandContext<CommandSourceStack> context,
        boolean exclusiveInput,
        boolean hideHud,
        String texture
    ) {
        return QteDefinition.create(
            StringArgumentType.getString(context, "id"),
            QteType.parse(StringArgumentType.getString(context, "type")),
            StringArgumentType.getString(context, QteCommandSchema.INPUTS_ARGUMENT),
            DoubleArgumentType.getDouble(context, "duration"),
            StringArgumentType.getString(context, QteCommandSchema.SUCCESS_RESULT_ARGUMENT),
            StringArgumentType.getString(context, QteCommandSchema.FAILURE_RESULT_ARGUMENT),
            exclusiveInput,
            hideHud,
            texture
        );
    }

    private static int play(CommandContext<CommandSourceStack> context) {
        String id = StringArgumentType.getString(context, "id").toLowerCase(Locale.ROOT);
        QteDefinition definition = data(context).registry().find(id).orElse(null);
        if (definition == null) {
            context.getSource().sendFailure(Component.translatable("command.qte.missing", id));
            return 0;
        }
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            QteSessions.start(player, definition);
            context.getSource().sendSuccess(() -> Component.translatable("command.qte.started", id), false);
            return 1;
        } catch (Exception error) {
            context.getSource().sendFailure(Component.translatable("command.qte.player_only"));
            return 0;
        }
    }

    private static int remove(CommandContext<CommandSourceStack> context) {
        String id = StringArgumentType.getString(context, "id").toLowerCase(Locale.ROOT);
        if (!data(context).remove(id)) {
            context.getSource().sendFailure(Component.translatable("command.qte.missing", id));
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.translatable("command.qte.removed", id), true);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        java.util.List<String> ids = data(context).registry().ids();
        context.getSource().sendSuccess(
            () -> ids.isEmpty()
                ? Component.translatable("command.qte.list.empty")
                : Component.translatable("command.qte.list", String.join(", ", ids)),
            false
        );
        return ids.size();
    }

    private static int types(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal(QteType.names()), false);
        return QteType.values().length;
    }

    private static CompletableFuture<Suggestions> suggestTypes(
        CommandContext<CommandSourceStack> context,
        SuggestionsBuilder builder
    ) {
        return SharedSuggestionProvider.suggest(
            java.util.Arrays.stream(QteType.values()).map(type -> type.name().toLowerCase(Locale.ROOT)),
            builder
        );
    }

    private static CompletableFuture<Suggestions> suggestInputs(
        CommandContext<CommandSourceStack> context,
        SuggestionsBuilder builder
    ) {
        return SharedSuggestionProvider.suggest(QteCommandSchema.inputSuggestions(builder.getRemaining()), builder);
    }

    private static CompletableFuture<Suggestions> suggestIds(
        CommandContext<CommandSourceStack> context,
        SuggestionsBuilder builder
    ) {
        return SharedSuggestionProvider.suggest(data(context).registry().ids(), builder);
    }

    private static QteSavedData data(CommandContext<CommandSourceStack> context) {
        return QteSavedData.get(context.getSource().getServer());
    }
}

package ru.randspawn.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Comparator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import ru.randspawn.data.PlayerSpawnData;
import ru.randspawn.data.RandSpawnSavedData;
import ru.randspawn.config.RandSpawnConfig;
import ru.randspawn.spawn.SpawnManager;

public final class RandSpawnCommand {
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("randspawn")
                .executes(RandSpawnCommand::infoOrClaim)
                .then(Commands.literal("help").executes(RandSpawnCommand::help))
                .then(Commands.literal("info").executes(RandSpawnCommand::infoSelf)
                        .then(Commands.argument("nickname", StringArgumentType.word())
                                .requires(source -> source.hasPermission(2)).executes(RandSpawnCommand::infoNamed)))
                .then(Commands.literal("tp").requires(source -> source.hasPermission(2))
                        .executes(RandSpawnCommand::teleportSelf)
                        .then(Commands.argument("nickname", StringArgumentType.word()).executes(RandSpawnCommand::teleportNamed)))
                .then(Commands.literal("set").requires(source -> source.hasPermission(2))
                        .executes(RandSpawnCommand::setSelf)
                        .then(Commands.argument("nickname", StringArgumentType.word()).executes(RandSpawnCommand::setNamed)))
                .then(Commands.literal("regenerate").requires(source -> source.hasPermission(2))
                        .executes(RandSpawnCommand::regenerateSelf)
                        .then(Commands.argument("nickname", StringArgumentType.word()).executes(RandSpawnCommand::regenerateNamed)))
                .then(Commands.literal("remove").requires(source -> source.hasPermission(2))
                        .then(Commands.argument("nickname", StringArgumentType.word()).executes(RandSpawnCommand::removeNamed)))
                .then(Commands.literal("list").requires(source -> source.hasPermission(2)).executes(RandSpawnCommand::list))
                .then(Commands.literal("reload").requires(source -> source.hasPermission(2)).executes(context -> {
                    context.getSource().sendSuccess(() -> Component.translatable("command.randspawn.reload.restart"), false);
                    return 1;
                })));
    }

    private static int help(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.translatable("command.randspawn.help"), false);
        return 1;
    }

    private static int infoSelf(CommandContext<CommandSourceStack> context) {
        ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (Exception exception) {
            return fail(context, "command.randspawn.player_required");
        }
        return show(context, RandSpawnSavedData.get(context.getSource().getServer()).get(player.getUUID()).orElse(null));
    }

    private static int infoOrClaim(CommandContext<CommandSourceStack> context) {
        ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (Exception exception) {
            return fail(context, "command.randspawn.player_required");
        }
        PlayerSpawnData existing = RandSpawnSavedData.get(player.getServer()).get(player.getUUID()).orElse(null);
        if (existing != null || RandSpawnConfig.AUTOMATIC_ASSIGNMENT.get()) {
            return show(context, existing);
        }
        return switch (SpawnManager.claim(player, System.currentTimeMillis())) {
            case SUCCESS -> success(context, "command.randspawn.claim.success");
            case SEARCH_FAILED -> fail(context, "command.randspawn.search_failed");
            case NOT_AVAILABLE -> fail(context, "command.randspawn.claim.unavailable");
        };
    }

    private static int infoNamed(CommandContext<CommandSourceStack> context) {
        return show(context, named(context));
    }

    private static int show(CommandContext<CommandSourceStack> context, PlayerSpawnData spawn) {
        if (spawn == null) {
            return fail(context, "command.randspawn.not_found");
        }
        BlockPos pos = spawn.position();
        context.getSource().sendSuccess(() -> Component.translatable("command.randspawn.info", spawn.nickname(),
                spawn.uuid().toString(), spawn.dimension().location().toString(), pos.getX(), pos.getY(), pos.getZ()), false);
        return 1;
    }

    private static int teleportSelf(CommandContext<CommandSourceStack> context) {
        return teleport(context, null);
    }

    private static int teleportNamed(CommandContext<CommandSourceStack> context) {
        return teleport(context, StringArgumentType.getString(context, "nickname"));
    }

    private static int teleport(CommandContext<CommandSourceStack> context, String nickname) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            PlayerSpawnData spawn = nickname == null
                    ? RandSpawnSavedData.get(player.getServer()).get(player.getUUID()).orElse(null) : named(context);
            if (spawn == null) {
                return fail(context, "command.randspawn.not_found");
            }
            SpawnManager.teleport(player, spawn);
            return 1;
        } catch (Exception exception) {
            return fail(context, "command.randspawn.player_required");
        }
    }

    private static int setSelf(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            return setAtSource(context, player.getUUID(), player.getGameProfile().getName());
        } catch (Exception exception) {
            return fail(context, "command.randspawn.player_required");
        }
    }

    private static int setNamed(CommandContext<CommandSourceStack> context) {
        PlayerSpawnData target = named(context);
        if (target == null) {
            return fail(context, "command.randspawn.not_found");
        }
        return setAtSource(context, target.uuid(), target.nickname());
    }

    private static int setAtSource(CommandContext<CommandSourceStack> context, java.util.UUID uuid, String nickname) {
        try {
            ServerPlayer source = context.getSource().getPlayerOrException();
            PlayerSpawnData spawn = new PlayerSpawnData(uuid, nickname, source.level().dimension(), source.blockPosition());
            RandSpawnSavedData.get(source.getServer()).put(spawn);
            context.getSource().sendSuccess(() -> Component.translatable("command.randspawn.updated", nickname), true);
            return 1;
        } catch (Exception exception) {
            return fail(context, "command.randspawn.player_required");
        }
    }

    private static int regenerateSelf(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            return regenerate(context, new PlayerSpawnData(player.getUUID(), player.getGameProfile().getName(),
                    player.level().dimension(), player.blockPosition()));
        } catch (Exception exception) {
            return fail(context, "command.randspawn.player_required");
        }
    }

    private static int regenerateNamed(CommandContext<CommandSourceStack> context) {
        return regenerate(context, named(context));
    }

    private static int regenerate(CommandContext<CommandSourceStack> context, PlayerSpawnData target) {
        if (target == null) {
            return fail(context, "command.randspawn.not_found");
        }
        if (SpawnManager.regenerate(context.getSource().getServer(), target.uuid(), target.nickname()).isEmpty()) {
            return fail(context, "command.randspawn.search_failed");
        }
        context.getSource().sendSuccess(() -> Component.translatable("command.randspawn.updated", target.nickname()), true);
        return 1;
    }

    private static int removeNamed(CommandContext<CommandSourceStack> context) {
        PlayerSpawnData target = named(context);
        if (target == null || !RandSpawnSavedData.get(context.getSource().getServer()).remove(target.uuid())) {
            return fail(context, "command.randspawn.not_found");
        }
        context.getSource().sendSuccess(() -> Component.translatable("command.randspawn.removed", target.nickname()), true);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        RandSpawnSavedData.get(context.getSource().getServer()).all().stream()
                .sorted(Comparator.comparing(PlayerSpawnData::nickname, String.CASE_INSENSITIVE_ORDER))
                .forEach(spawn -> context.getSource().sendSuccess(() -> Component.literal(spawn.nickname() + " — "
                        + spawn.position().toShortString() + " — " + spawn.dimension().location()), false));
        return 1;
    }

    private static PlayerSpawnData named(CommandContext<CommandSourceStack> context) {
        String nickname = StringArgumentType.getString(context, "nickname");
        return RandSpawnSavedData.get(context.getSource().getServer()).findByNickname(nickname).orElse(null);
    }

    private static int fail(CommandContext<CommandSourceStack> context, String key) {
        context.getSource().sendFailure(Component.translatable(key));
        return 0;
    }

    private static int success(CommandContext<CommandSourceStack> context, String key) {
        context.getSource().sendSuccess(() -> Component.translatable(key), false);
        return 1;
    }

    private RandSpawnCommand() {
    }
}

package ru.randspawn;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import ru.randspawn.command.RandSpawnCommand;
import ru.randspawn.config.RandSpawnConfig;
import ru.randspawn.event.PlayerEventHandler;

@Mod(RandSpawn.MOD_ID)
public final class RandSpawn {
    public static final String MOD_ID = "randspawn";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RandSpawn(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, RandSpawnConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(PlayerEventHandler::onLogin);
        NeoForge.EVENT_BUS.addListener(PlayerEventHandler::onRespawnPosition);
        NeoForge.EVENT_BUS.addListener(RandSpawnCommand::onRegisterCommands);
    }
}

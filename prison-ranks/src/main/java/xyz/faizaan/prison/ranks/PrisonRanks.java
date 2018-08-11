/*
 * Prison is a Minecraft plugin for the prison game mode.
 * Copyright (C) 2018 The Prison Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package xyz.faizaan.prison.ranks;

import xyz.faizaan.prison.Prison;
import xyz.faizaan.prison.convert.ConversionManager;
import xyz.faizaan.prison.integration.IntegrationType;
import xyz.faizaan.prison.localization.LocaleManager;
import xyz.faizaan.prison.localization.Localizable;
import xyz.faizaan.prison.modules.Module;
import xyz.faizaan.prison.modules.ModuleStatus;
import xyz.faizaan.prison.output.Output;
import xyz.faizaan.prison.ranks.commands.CommandCommands;
import xyz.faizaan.prison.ranks.commands.LadderCommands;
import xyz.faizaan.prison.ranks.commands.RankUpCommand;
import xyz.faizaan.prison.ranks.commands.RanksCommands;
import xyz.faizaan.prison.ranks.data.RankLadder;
import xyz.faizaan.prison.ranks.managers.LadderManager;
import xyz.faizaan.prison.ranks.managers.PlayerManager;
import xyz.faizaan.prison.ranks.managers.RankManager;
import xyz.faizaan.prison.store.Collection;
import xyz.faizaan.prison.store.Database;

import java.io.IOException;
import java.util.Optional;

/**
 * @author Faizaan A. Datoo
 */
public class PrisonRanks extends Module {

    /*
     * Fields & Constants
     */

    private static PrisonRanks instance;
    private RankManager rankManager;
    private LadderManager ladderManager;
    private PlayerManager playerManager;
    private LocaleManager localeManager;

    private Database database;

    /*
     * Constructor
     */

    public PrisonRanks(String version) {
        super("Ranks", version, 3);
    }

    /*
     * Methods
     */

    public static PrisonRanks getInstance() {
        return instance;
    }

    @Override public void enable() {
        instance = this;

        if (!Prison.get().getIntegrationManager().hasForType(IntegrationType.ECONOMY)) {
            getStatus().setStatus(ModuleStatus.Status.FAILED);
            getStatus().setMessage("no economy plugin");
            return;
        }

        Optional<Database> databaseOptional = Prison.get().getPlatform().getStorage().getDatabase("ranksDb");
        if (!databaseOptional.isPresent()) {
            Prison.get().getPlatform().getStorage().createDatabase("ranks");
            databaseOptional = Prison.get().getPlatform().getStorage().getDatabase("ranks");
        }
        this.database = databaseOptional.get();

        this.localeManager = new LocaleManager(this, "lang/ranks");

        // Load up the ranks

        rankManager = new RankManager(initCollection("ranks"));
        try {
            rankManager.loadRanks();
        } catch (IOException e) {
            Output.get().logError("A rank file failed to load.", e);
        }

        // Load up the ladders


        ladderManager = new LadderManager(initCollection("ladders"));
        try {
            ladderManager.loadLadders();
        } catch (IOException e) {
            Output.get().logError("A ladder file failed to load.", e);
        }
        createDefaultLadder();

        // Load up the players


        playerManager = new PlayerManager(initCollection("players"));
        try {
            playerManager.loadPlayers();
        } catch (IOException e) {
            Output.get().logError("A player file failed to load.", e);
        }

        // Load up the commands

        Prison.get().getCommandHandler().registerCommands(new RankUpCommand());
        Prison.get().getCommandHandler().registerCommands(new CommandCommands());
        Prison.get().getCommandHandler().registerCommands(new RanksCommands());
        Prison.get().getCommandHandler().registerCommands(new LadderCommands());

        // Load up all else

        new FirstJoinHandler();
        new ChatHandler();
        ConversionManager.getInstance().registerConversionAgent(new RankConversionAgent());

    }

    private Collection initCollection(String collName) {
        Optional<Collection> collectionOptional = database.getCollection(collName);
        if (!collectionOptional.isPresent()) {
            database.createCollection(collName);
            collectionOptional = database.getCollection(collName);
        }

        return collectionOptional.orElseThrow(RuntimeException::new);
    }

    /**
     * A default ladder is absolutely necessary on the server, so let's create it if it doesn't exist.
     */
    private void createDefaultLadder() {
        if (!ladderManager.getLadder("default").isPresent()) {
            Optional<RankLadder> rankLadderOptional = ladderManager.createLadder("default");

            if (!rankLadderOptional.isPresent()) {
                Output.get().logError("Could not create the default ladder.");
                super.getStatus().toFailed("&cNo default ladder found.");
                return;
            }

            try {
                ladderManager.saveLadder(rankLadderOptional.get());
            } catch (IOException e) {
                Output.get().logError("Could not save the default ladder.", e);
                super.getStatus().toFailed("&cNo default ladder found.");
            }
        }
    }

    /*
     * Getters & Setters
     */

    @Override public void disable() {
        try {
            rankManager.saveRanks();
        } catch (IOException e) {
            Output.get().logError("A ranks file failed to save.", e);
        }
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public LadderManager getLadderManager() {
        return ladderManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public LocaleManager getRanksMessages() {
        return localeManager;
    }

    public RankLadder getDefaultLadder() {
        return getLadderManager().getLadder("default").orElseThrow(IllegalStateException::new);
    }

    public Database getDatabase() {
        return database;
    }

    public static Localizable loc(String key) {
        return instance.getRanksMessages().getLocalizable(key);
    }

}
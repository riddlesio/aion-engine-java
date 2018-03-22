/*
 *  Copyright 2018 riddles.io (developers@riddles.io)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 *
 *      For the full copyright and license information, please view the LICENSE
 *      file that was distributed with this source code.
 */

package io.riddles.aion.engine;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

import io.riddles.aion.Aion;
import io.riddles.aion.game.AionSerializer;
import io.riddles.aion.game.field.AionField;
import io.riddles.aion.game.player.AionPlayer;
import io.riddles.aion.game.processor.AionProcessor;
import io.riddles.aion.game.state.AionPlayerState;
import io.riddles.aion.game.state.AionState;
import io.riddles.aion.game.state.AionStateSerializer;
import io.riddles.javainterface.configuration.Configuration;
import io.riddles.javainterface.engine.AbstractEngine;
import io.riddles.javainterface.engine.GameLoopInterface;
import io.riddles.javainterface.engine.SimpleGameLoop;
import io.riddles.javainterface.game.AbstractGameSerializer;
import io.riddles.javainterface.game.player.PlayerProvider;
import io.riddles.javainterface.io.IOInterface;
import io.riddles.javainterface.serialize.AbstractSerializer;

/**
 * io.riddles.aion.engine.AionEngine - Created on 7-3-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class AionEngine extends AbstractEngine<AionProcessor, AionPlayer, AionState> {

    public static JSONObject fieldData;

    public AionEngine(PlayerProvider<AionPlayer> playerProvider, IOInterface ioHandler) {
        super(playerProvider, ioHandler);
    }

    @Override
    protected Configuration getDefaultConfiguration() {
        Configuration configuration = new Configuration();

        configuration.put("dataFile", "/data.json");
        configuration.put("transactionSpeed", 2);
        configuration.put("initialTransactions", 4);
        configuration.put("transactionRate", 2);
        configuration.put("initialCoins", 500);
        configuration.put("maxRounds", 100);
        configuration.put("stakeAmount", 100);
        configuration.put("winAmount", 1000);
        configuration.put("maxFee", 3);
        configuration.put("seed", UUID.randomUUID().toString());

        return configuration;
    }

    @Override
    protected GameLoopInterface createGameLoop() {
        return new SimpleGameLoop();
    }

    @Override
    protected AbstractGameSerializer createGameSerializer() {
        return new AionSerializer();
    }

    @Override
    protected AbstractSerializer createStateSerializer() {
        return new AionStateSerializer();
    }

    @Override
    protected AionPlayer createPlayer(int id) {
        return new AionPlayer(id);
    }

    @Override
    protected AionProcessor createProcessor() {
        return new AionProcessor(this.playerProvider);
    }

    @Override
    protected void loadData() {
        try {
            InputStream fileInputStream;
            String filePath = configuration.getString("dataFile");

            try {
                fileInputStream = new FileInputStream(filePath);
            } catch (FileNotFoundException ex) {
                fileInputStream = Aion.class.getResourceAsStream(filePath);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));

            String line;
            StringBuilder file = new StringBuilder();
            while ((line = br.readLine()) != null) {
                file.append(line);
            }

            fieldData = new JSONObject(file.toString().trim());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.toString(), ex);
            System.exit(1);
        }
    }

    @Override
    protected void sendSettingsToPlayer(AionPlayer player) {
        player.sendSetting("your_botid", player.getId());
        player.sendSetting("transaction_speed", configuration.getInt("transactionSpeed"));
        player.sendSetting("transaction_rate", configuration.getInt("transactionRate"));
        player.sendSetting("initial_coins", configuration.getInt("initialCoins"));
        player.sendSetting("win_amount", configuration.getInt("winAmount"));
        player.sendSetting("stake_amount", configuration.getInt("stakeAmount"));
        player.sendSetting("max_fee", configuration.getInt("maxFee"));
        player.sendSetting("max_rounds", configuration.getInt("maxRounds"));
        player.sendSetting("field", new AionField(fieldData).toString());
    }

    @Override
    protected void initializeGame() {
        // nothing
    }

    @Override
    protected AionState getInitialState() {
        AionField field = new AionField(fieldData);
        field.spawnTransactions(AionEngine.configuration.getInt("initialTransactions"));

        int initialCoins = configuration.getInt("initialCoins");
        ArrayList<AionPlayerState> playerStates = new ArrayList<>();
        for (AionPlayer player : this.playerProvider.getPlayers()) {
            playerStates.add(new AionPlayerState(player.getId(), initialCoins));
        }

        return new AionState(playerStates, field);
    }
}

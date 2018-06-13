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

package io.riddles.aion.game.processor;

import java.util.ArrayList;

import io.riddles.aion.engine.AionEngine;
import io.riddles.aion.game.field.AionField;
import io.riddles.aion.game.move.ActionType;
import io.riddles.aion.game.move.AionMove;
import io.riddles.aion.game.move.AionMoveDeserializer;
import io.riddles.aion.game.player.AionPlayer;
import io.riddles.aion.game.state.AionPlayerState;
import io.riddles.aion.game.state.AionState;
import io.riddles.javainterface.game.move.AbstractMoveDeserializer;
import io.riddles.javainterface.game.player.PlayerProvider;
import io.riddles.javainterface.game.processor.SimpleProcessor;

/**
 * io.riddles.aion.game.processor.AionProcessor - Created on 7-3-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class AionProcessor extends SimpleProcessor<AionState, AionPlayer> {

    public AionProcessor(PlayerProvider<AionPlayer> playerProvider) {
        super(playerProvider);
    }

    @Override
    public AionState createNextState(AionState inputState, int roundNumber) {
        AionState nextState = inputState.createNextState(roundNumber);
        AionField field = nextState.getField();

        field.spawnTransactions(AionEngine.configuration.getInt("transactionRate"));

        sendUpdates(nextState);
        processMoves(nextState);

        field.removeCompleteTransactions();
        field.moveTransactions(nextState);

        return nextState;
    }

    private void sendUpdates(AionState state) {
        for (AionPlayer player : this.playerProvider.getPlayers()) {
            player.sendUpdate("round", state.getRoundNumber());
            player.sendUpdate("bridges", state.getField().bridgesToString());
            player.sendUpdate("transactions", state.getField().transactionsToString());

            for (AionPlayerState playerState : state.getPlayerStates()) {
                AionPlayer otherPlayer = getPlayer(playerState.getPlayerId());

                player.sendUpdate("coins", otherPlayer, playerState.getAvailableCoins());
                player.sendUpdate("value", otherPlayer, playerState.getTotalCoins());
            }
        }
    }

    private void processMoves(AionState state) {
        for (AionPlayerState playerState : state.getPlayerStates()) {
            AionPlayer player = getPlayer(playerState.getPlayerId());
            AionMove move = (AionMove) getPlayerMove(player, ActionType.MOVE);

            state.getField().processMove(move, playerState);

            if (move.isInvalid()) {
                player.sendWarning(move.getException().getMessage());
            }
        }
    }

    @Override
    public boolean hasGameEnded(AionState state) {
        int maxRounds = AionEngine.configuration.getInt("maxRounds");
        ArrayList<AionPlayerState> winners = state.getWinningPlayers();

        return state.getRoundNumber() >= maxRounds || winners.size() >= 1;
    }

    @Override
    public Integer getWinnerId(AionState state) {
        ArrayList<AionPlayerState> winners = state.getWinningPlayers();

        // Winner by getting the amount above treshold
        if (winners.size() > 1) {
            return null;
        } else if (winners.size() == 1) {
            return winners.get(0).getPlayerId();
        }

        // Winner by most amount, but below treshold
        int maxCoins = -1;
        for (AionPlayerState playerState : state.getPlayerStates()) {
            if (playerState.getTotalCoins() > maxCoins) {
                maxCoins = playerState.getTotalCoins();
                winners.clear();
                winners.add(playerState);
            } else if (playerState.getTotalCoins() == maxCoins) {
                winners.add(playerState);
            }
        }

        if (winners.size() == 1) {
            return winners.get(0).getPlayerId();
        }

        // Draw if players have equal amounts
        return null;
    }

    @Override
    public double getScore(AionState state) {
        return state.getRoundNumber();
    }

    @Override
    public AbstractMoveDeserializer createMoveDeserializer() {
        return new AionMoveDeserializer();
    }
}

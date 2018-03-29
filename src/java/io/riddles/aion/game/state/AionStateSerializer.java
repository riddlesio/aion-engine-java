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

package io.riddles.aion.game.state;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.stream.Collectors;

import io.riddles.aion.game.field.Bridge;
import io.riddles.aion.game.field.Network;
import io.riddles.aion.game.field.Transaction;
import io.riddles.javainterface.serialize.AbstractSerializer;

/**
 * io.riddles.aion.game.state.AionStateSerializer - Created on 8-3-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class AionStateSerializer extends AbstractSerializer<AionState> {

    @Override
    public JSONObject visitObject(AionState state) {
        AionPlayerStateSerializer playerStateSerializer = new AionPlayerStateSerializer();
        JSONObject stateObject = new JSONObject();

        JSONArray players = new JSONArray();
        for (AionPlayerState playerState : state.getPlayerStates()) {
            players.put(playerStateSerializer.traverseToJson(playerState));
        }

        stateObject.put("round", state.getRoundNumber());
        stateObject.put("players", players);
        stateObject.put("bridges", visitBridges(state.getField().getBridges()));
        stateObject.put("transactions", visitTransactions(state.getField().getTransactions()));

        return stateObject;
    }

    private JSONArray visitBridges(ArrayList<Bridge> bridges) {
        JSONArray bridgeArray = new JSONArray();

        for (Bridge bridge : bridges) {
            JSONObject bridgeObject = new JSONObject();

            bridgeObject.put("player", bridge.getPlayerId());
            bridgeObject.put("fee", bridge.getFee());
            bridgeObject.put("sides", visitSides(bridge.getSides()));

            bridgeArray.put(bridgeObject);
        }

        return bridgeArray;
    }

    private JSONArray visitSides(ArrayList<Network> sides) {
        JSONArray sidesArray = new JSONArray();

        ArrayList<String> sortedCodes = sides.stream()
                .map(Network::getCode)
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));

        for (String code : sortedCodes) {
            sidesArray.put(code);
        }

        return sidesArray;
    }

    private JSONArray visitTransactions(ArrayList<Transaction> transactions) {
        JSONArray transactionArray = new JSONArray();

        for (Transaction transaction : transactions) {
            JSONObject transactionObject = new JSONObject();

            String current = transaction.getCurrentNetwork() != null
                    ? transaction.getCurrentNetwork().getCode()
                    : transaction.getCurrentBridges().get(0).toSimpleString();

            transactionObject.put("from", transaction.getFrom().getCode());
            transactionObject.put("to", transaction.getTo().getCode());
            transactionObject.put("current", current);

            if (!transaction.getCurrentBridges().isEmpty()) {
                transactionObject.put("travel", transaction.getTravelCompletion());
                transactionObject.put("previous", transaction.getPreviousNetwork().getCode());
            }

            transactionArray.put(transactionObject);
        }

        return transactionArray;
    }
}

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

package io.riddles.aion.game.field;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

import io.riddles.aion.engine.AionEngine;
import io.riddles.aion.game.move.AionMove;
import io.riddles.aion.game.move.MoveType;
import io.riddles.aion.game.state.AionPlayerState;
import io.riddles.aion.game.state.AionState;
import io.riddles.javainterface.exception.InvalidMoveException;

/**
 * io.riddles.aion.game.field.AionField - Created on 8-3-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class AionField {

    private int width;
    private int height;
    private int transactionCount;
    private int bridgeCount;
    private ArrayList<Network> networks;
    private ArrayList<Bridge> bridges;
    private ArrayList<Transaction> transactions;

    public AionField(JSONObject fieldData) {
        JSONObject field = fieldData.getJSONObject("field");
        JSONArray networks = fieldData.getJSONArray("networks");

        this.width = field.getInt("width");
        this.height = field.getInt("height");
        this.transactionCount = 0;
        this.bridgeCount = 0;
        this.networks = parseNetworks(networks);
        this.bridges = new ArrayList<>();
        this.transactions = new ArrayList<>();
    }

    public AionField(AionField field) {
        this.width = field.width;
        this.height = field.height;
        this.transactionCount = field.transactionCount;
        this.bridgeCount = field.bridgeCount;

        // ordering of cloning below is important
        this.networks = field.networks.stream()
                .map(Network::new)
                .collect(Collectors.toCollection(ArrayList::new));
        this.bridges = field.bridges.stream()
                .map(bridge -> new Bridge(bridge, this))
                .collect(Collectors.toCollection(ArrayList::new));
        this.transactions = field.transactions.stream()
                .map(transaction -> new Transaction(transaction, this))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void spawnTransactions(int amount) {
        for (int i = 0; i < amount; i++) {
            ArrayList<Network> networks = new ArrayList<>(this.networks);

            Network from = networks.remove(AionEngine.random.nextInt(networks.size()));
            Network to = networks.remove(AionEngine.random.nextInt(networks.size()));

            addTransaction(from, to);
        }
    }

    public void removeCompleteTransactions() {
        this.transactions = this.transactions.stream()
                .filter(t -> !t.isCompleted())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void moveTransactions(AionState state) {
        ShortestPathHandler shortestPathHandler = new ShortestPathHandler(this.networks);

        for (Transaction transaction : this.transactions) {

            // Start moving transaction along correct bridge
            if (transaction.getCurrentNetwork() != null) {
                ArrayList<Network> shortestPath = shortestPathHandler.getShortestPath(
                        transaction.getCurrentNetwork(), transaction.getTo()
                );

                if (shortestPath == null) continue; // No path, so do nothing

                Network nextNetwork = shortestPath.get(0);
                Network currentNetwork = transaction.getCurrentNetwork();
                ArrayList<Bridge> bridges = currentNetwork.getCheapestBridgesTo(nextNetwork);

                transaction.setCurrentBridges(bridges);
                transaction.setCurrentNetwork(null);
                transaction.setPreviousNetwork(currentNetwork);
            }

            ArrayList<Bridge> bridges = transaction.getCurrentBridges();

            // Move transaction along bridge
            if (!transaction.moveAlongBridge()) continue;

            // Transaction reached other side of the bridge
            for (Bridge bridge : bridges) {
                AionPlayerState playerState = state.getPlayerStateById(bridge.getPlayerId());
                playerState.receiveFee(bridge.getFee() / bridges.size());
            }

            if (transaction.getCurrentNetwork() == transaction.getTo()) { // Goal reached
                transaction.setCompleted();
            }
        }
    }

    public void processMove(AionMove move, AionPlayerState playerState) {
        if (move.isInvalid() || move.getMoveType() == MoveType.PASS) return;

        Network side1 = getMoveNetwork(move.getSide1(), move);
        Network side2 = getMoveNetwork(move.getSide2(), move);

        if (side1 == null || side2 == null) return;

        if (move.getMoveType() == MoveType.BUILD) {
            processBuildMove(move, side1, side2, playerState);
        } else {
            processRemoveMove(move, playerState);
        }
    }

    private void processBuildMove(AionMove move, Network side1, Network side2, AionPlayerState playerState) {
        Bridge bridge = findBridge(side1.getCode(), side2.getCode(), playerState.getPlayerId());
        int stakeAmount = AionEngine.configuration.getInt("stakeAmount");
        String exception = null;

        if (bridge != null) {
            exception = "This bridge already exists";
        } else if (playerState.getAvailableCoins() < stakeAmount) {
            exception = "Not enough coins to build a bridge";
        }

        if (exception != null) {
            move.setException(new InvalidMoveException(exception));
            return;
        }

        playerState.payStake(stakeAmount);

        Bridge newBridge = new Bridge(
                this.bridgeCount++, playerState.getPlayerId(), move.getFee(), side1, side2
        );
        this.bridges.add(newBridge);
    }

    private void processRemoveMove(AionMove move, AionPlayerState playerState) {
        String side1 = move.getSide1();
        String side2 = move.getSide2();
        Bridge bridge = findBridge(side1, side2, playerState.getPlayerId());

        if (bridge == null) {
            move.setException(new InvalidMoveException(
                    String.format("Can't find your bridge '%s-%s'", side1, side2)
            ));
            return;
        }

        playerState.receiveStake(AionEngine.configuration.getInt("stakeAmount"));

        bridge.getSides().get(0).removeBridge(bridge);
        bridge.getSides().get(1).removeBridge(bridge);
        removeTransactions(bridge);
        this.bridges.remove(bridge);
    }

    private void removeTransactions(Bridge bridge) {
        ArrayList<Transaction> floatingTransactions = new ArrayList<>();

        for (Transaction transaction : this.transactions) {
            if (!transaction.getCurrentBridges().contains(bridge)) continue;

            transaction.getCurrentBridges().remove(bridge);

            // Remove transaction if it no longer sits on any bridge
            if (transaction.getCurrentBridges().size() <= 0) {
                floatingTransactions.add(transaction);
            }
        }

        floatingTransactions.forEach(t -> this.transactions.remove(t));
    }

    private Network getMoveNetwork(String code, AionMove move) {
        try {
            return findNetwork(code);
        } catch (RuntimeException ignored) {
            move.setException(new InvalidMoveException(
                    String.format("Network '%s' doesn't exist", code)
            ));
            return null;
        }
    }

    private ArrayList<Network> parseNetworks(JSONArray input) {
        ArrayList<Network> networks = new ArrayList<>();

        for (int i = 0; i < input.length(); i++) {
            JSONObject networkInput = input.getJSONObject(i);
            JSONObject positionInput = networkInput.getJSONObject("position");

            String name = networkInput.getString("name");
            String code = networkInput.getString("code");
            int x = positionInput.getInt("x");
            int y = positionInput.getInt("y");

            networks.add(new Network(name, code, new Point(x, y)));
        }

        return networks;
    }

    @Override
    public String toString() {
        return this.networks.stream()
                .map(Network::toString)
                .collect(Collectors.joining(";"));
    }

    public String transactionsToString() {
        if (this.transactions.size() <= 0) {
            return "null";
        }

        return this.transactions.stream()
                .map(Transaction::toString)
                .collect(Collectors.joining(";"));
    }

    public String bridgesToString() {
        if (this.bridges.size() <= 0) {
            return "null";
        }

        return this.bridges.stream()
                .map(Bridge::toString)
                .collect(Collectors.joining(";"));
    }

    public Network findNetwork(String code) {
        return this.networks.stream()
                .filter(n -> n.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        String.format("Network '%s' not found", code)
                ));
    }

    public Bridge findBridge(int id) {
        return this.bridges.stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        String.format("Bridge '%s' not found", id)
                ));
    }

    private Bridge findBridge(String code1, String code2, int playerId) {
        return this.bridges.stream()
                .filter(b -> b.getPlayerId() == playerId)
                .filter(b -> {
                    String c1 = b.getSides().get(0).getCode();
                    String c2 = b.getSides().get(1).getCode();

                    return (c1.equals(code1) && c2.equals(code2))
                            || (c1.equals(code2) && c2.equals(code1));
                })
                .findFirst()
                .orElse(null);
    }

    public void addBridge(int playerId, int fee, String code1, String code2) {
        Bridge bridge = new Bridge(
                this.bridgeCount++, playerId, fee, findNetwork(code1), findNetwork(code2)
        );
        this.bridges.add(bridge);
    }

    private void addTransaction(Network from, Network to) {
        this.transactions.add(new Transaction(this.transactionCount++, from, to));
    }

    public ArrayList<Network> getNetworks() {
        return this.networks;
    }

    public ArrayList<Bridge> getBridges() {
        return this.bridges;
    }

    public ArrayList<Transaction> getTransactions() {
        return this.transactions;
    }
}

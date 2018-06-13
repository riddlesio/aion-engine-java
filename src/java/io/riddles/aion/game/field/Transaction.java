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

import java.util.ArrayList;
import java.util.stream.Collectors;

import io.riddles.aion.engine.AionEngine;

/**
 * io.riddles.aion.game.field.Transaction - Created on 14-3-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class Transaction {

    private int id;
    private Network from;
    private Network to;
    private ArrayList<Bridge> currentBridges; // possibly on bridges owned by different players
    private Network currentNetwork;
    private Network previousNetwork;
    private double travelCompletion;
    private boolean completed;

    public Transaction(int id, Network from, Network to) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.currentBridges = new ArrayList<>();
        this.currentNetwork = from;
        this.previousNetwork = null;
        this.travelCompletion = 0;
        this.completed = false;
    }

    public Transaction(Transaction transaction, AionField field) {
        this.id = transaction.id;
        this.from = field.findNetwork(transaction.from.getCode());
        this.to = field.findNetwork(transaction.to.getCode());
        this.currentNetwork = transaction.currentNetwork != null
                ? field.findNetwork(transaction.currentNetwork.getCode())
                : null;
        this.currentBridges = transaction.currentBridges.size() == 0
                ? new ArrayList<>()
                : transaction.currentBridges.stream()
                    .map(b -> field.findBridge(b.getId()))
                    .collect(Collectors.toCollection(ArrayList::new));
        this.previousNetwork = transaction.previousNetwork != null
                ? field.findNetwork(transaction.previousNetwork.getCode())
                : null;
        this.travelCompletion = transaction.travelCompletion;
        this.completed = transaction.completed;
    }

    public boolean moveAlongBridge() {
        if (this.currentBridges.size() == 0) {
            return false;
        }

        int speed = AionEngine.configuration.getInt("transactionSpeed");
        double step = speed / this.currentBridges.get(0).getDistance();
        this.travelCompletion = this.travelCompletion + step;

        if (this.travelCompletion < 1) {
            return false;
        }

        // Reached other side of the bridge
        this.currentNetwork = this.currentBridges.get(0).getSides().stream()
                .filter(s -> s != this.previousNetwork)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can't find new current network"));
        this.previousNetwork = null;
        this.currentBridges = new ArrayList<>();

        return true;
    }

    @Override
    public String toString() {
        String fromTo = String.format("%s>%s", this.from.getCode(), this.to.getCode());

        if (this.currentNetwork != null) {
            return String.format("%s:%s", fromTo, this.currentNetwork.getCode());
        }

        String side1 = this.currentBridges.get(0).getSides().get(0).getCode();
        String side2 = this.currentBridges.get(0).getSides().get(1).getCode();
        return String.format("%s:%s-%s", fromTo, side1, side2);
    }

    public int getId() {
        return this.id;
    }

    public Network getFrom() {
        return this.from;
    }

    public Network getTo() {
        return this.to;
    }

    public void setCurrentNetwork(Network network) {
        this.currentNetwork = network;
    }

    public void setCurrentBridges(ArrayList<Bridge> bridges) {
        this.currentBridges = bridges;
        this.travelCompletion = 0;
    }

    public void setPreviousNetwork(Network network) {
        this.previousNetwork = network;
    }

    public Network getCurrentNetwork() {
        return this.currentNetwork;
    }

    public ArrayList<Bridge> getCurrentBridges() {
        return this.currentBridges;
    }

    public double getTravelCompletion() {
        return this.travelCompletion;
    }

    public Network getPreviousNetwork() {
        return this.previousNetwork;
    }

    public void setCompleted() {
        this.completed = true;
    }

    public boolean isCompleted() {
        return this.completed;
    }
}

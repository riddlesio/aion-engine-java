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

/**
 * io.riddles.aion.game.field.Bridge - Created on 14-3-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class Bridge {

    private int id;
    private int playerId;
    private ArrayList<Network> sides;
    private int fee;
    private double distance;

    public Bridge(int id, int playerId, int fee, Network side1, Network side2) {
        this.id = id;
        this.playerId = playerId;
        this.fee = fee;
        this.sides = new ArrayList<>();
        this.distance = side1.getPosition().distance(side2.getPosition());

        addSide(side1);
        addSide(side2);
    }

    public Bridge(Bridge bridge, AionField field) {
        this.id = bridge.id;
        this.playerId = bridge.playerId;
        this.fee = bridge.fee;
        this.distance = bridge.distance;
        this.sides = new ArrayList<>();

        // Searches sides from networks that should be already cloned
        bridge.sides.forEach(side -> addSide(field.findNetwork(side.getCode())));
    }

    private void addSide(Network side) {
        this.sides.add(side);
        side.addBridge(this);
    }

    public String toString() {
        String side1 = this.sides.get(0).getCode();
        String side2 = this.sides.get(1).getCode();
        return String.format("%s-%s:%d-%d", side1, side2, this.playerId, this.fee);
    }

    public String toSimpleString() {
        return String.format("%s-%s", this.sides.get(0).getCode(), this.sides.get(1).getCode());
    }

    public int getId() {
        return this.id;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public ArrayList<Network> getSides() {
        return this.sides;
    }

    public int getFee() {
        return this.fee;
    }

    public double getDistance() {
        return this.distance;
    }
}

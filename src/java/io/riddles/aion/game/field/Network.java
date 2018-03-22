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

import java.awt.*;
import java.util.ArrayList;

/**
 * io.riddles.aion.game.field.NetWork - Created on 8-3-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class Network {

    private String name;
    private String code;
    private Point position;
    private ArrayList<Bridge> bridges;

    public Network(String name, String code, Point position) {
        this.name = name;
        this.code = code;
        this.position = position;
        this.bridges = new ArrayList<>();
    }

    public Network(Network network) {
        this.name = network.name;
        this.code = network.code;
        this.position = new Point(network.position);
        this.bridges = new ArrayList<>(); // bridges added when cloning bridges
    }

    public void addBridge(Bridge bridge) {
        this.bridges.add(bridge);
    }

    public void removeBridge(Bridge bridge) {
        this.bridges.remove(bridge);
    }

    public ArrayList<Bridge> getCheapestBridgesTo(Network network) {
        ArrayList<Bridge> cheapestBridges = new ArrayList<>();

        Bridge minBridge = null;
        for (Bridge bridge : this.bridges) {
            if (!bridge.getSides().contains(network)) continue;

            if (minBridge == null || bridge.getFee() < minBridge.getFee()) {
                cheapestBridges.clear();
                minBridge = bridge;
                cheapestBridges.add(bridge);
            } else if (minBridge.getFee() == bridge.getFee()) {
                cheapestBridges.add(bridge);
            }
        }

        if (cheapestBridges.size() <= 0) {
            throw new RuntimeException(
                    String.format("Bridge '%s-%s' not found", this.code, network.getCode())
            );
        }

        return cheapestBridges;
    }

    public String getCode() {
        return this.code;
    }

    public Point getPosition() {
        return this.position;
    }

    public ArrayList<Bridge> getBridges() {
        return this.bridges;
    }

    @Override
    public String toString() {
        return String.format("%s:%d,%d", this.code, this.position.x, this.position.y);
    }
}

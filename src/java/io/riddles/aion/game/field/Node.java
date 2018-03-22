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
 * io.riddles.aion.game.field.Node - Created on 14-3-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class Node implements Comparable<Node> {

    private Network network;
    private ArrayList<Node> neighbors;

    private int distance;
    private Node predecessor;

    Node(Network network, ShortestPathHandler handler) {
        this.network = network;
        this.neighbors = new ArrayList<>();

        for (Bridge bridge : network.getBridges()) {
            for (Network side : bridge.getSides()) {
                if (side == network) continue;

                Node node = handler.findNode(side);
                if (node == null) continue;

                addNeighbor(node);
            }
        }
    }

    private void addNeighbor(Node neighbor) {
        if (neighbor == null) {
            throw new RuntimeException("Can't add neighor because it doesn't exist yet");
        }

        this.neighbors.add(neighbor);
        neighbor.getNeighbors().add(this);
    }

    public void initialize() {
        this.distance = Integer.MAX_VALUE;
        this.predecessor = null;
    }

    @Override
    public int compareTo(Node node) {
        return Double.compare(this.distance, node.distance);
    }

    public Network getNetwork() {
        return this.network;
    }

    public ArrayList<Node> getNeighbors() {
        return this.neighbors;
    }

    public void setDistance(int score) {
        this.distance = score;
    }

    public int getDistance() {
        return this.distance;
    }

    public void setPredecessor(Node predecessor) {
        this.predecessor = predecessor;
    }

    public Node getPredecessor() {
        return this.predecessor;
    }
}

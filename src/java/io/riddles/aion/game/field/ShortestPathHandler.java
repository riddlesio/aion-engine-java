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
import java.util.Collections;

/**
 * io.riddles.aion.game.field.ShortestPathHandler - Created on 14-3-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class ShortestPathHandler {

    private ArrayList<Node> graph;

    public ShortestPathHandler(ArrayList<Network> networks) {
        this.graph = new ArrayList<>();

        for (Network network : networks) {
            this.graph.add(new Node(network, this));
        }
    }

    /**
     * Dijkstra's algorithm
     * @param start Starting point
     * @param goal Goal point
     * @return List of all networks that need to be traveled to get to goal.
     */
    public ArrayList<Network> getShortestPath(Network start, Network goal) {

        // Initialize
        Node startNode = findNode(start);
        ArrayList<Node> openSet = new ArrayList<>();
        ArrayList<Node> closedSet = new ArrayList<>();

        for (Node node : this.graph) {
            node.initialize();
        }

        openSet.add(startNode);
        startNode.setDistance(0);

        // Perform algorithm
        while (openSet.size() > 0) {

            // Get the node with the lowest distance
            Collections.sort(openSet);
            Node current = openSet.remove(0);

            // Found the goal node
            if (current.getNetwork() == goal) {
                return reconstructShortestPath(current, startNode);
            }

            // Add to closed set so we don't visit again
            closedSet.add(current);

            // Set the distance for current node's neighbors
            for (Node neighbor : current.getNeighbors()) {

                if (closedSet.contains(neighbor)) continue; // Already visited

                Network network = current.getNetwork();
                Bridge bridge = network.getCheapestBridgesTo(neighbor.getNetwork()).get(0);
                int distance = current.getDistance() + bridge.getFee();

                if (distance >= neighbor.getDistance()) continue; // Not a better path

                neighbor.setDistance(distance);
                neighbor.setPredecessor(current);

                if (!openSet.contains(neighbor) && !closedSet.contains(neighbor)) {
                    openSet.add(neighbor);
                }
            }
        }

        // Path can't be found
        return null;
    }

    public Node findNode(Network network) {
        return this.graph.stream()
                .filter(n -> n.getNetwork() == network)
                .findFirst()
                .orElse(null);
    }

    private ArrayList<Network> reconstructShortestPath(Node current, Node start) {
        ArrayList<Network> shortestPath = new ArrayList<>();
        shortestPath.add(current.getNetwork());

        Node predecessor = current.getPredecessor();
        while (predecessor != null && predecessor.getNetwork() != start.getNetwork()) {
            shortestPath.add(predecessor.getNetwork());
            predecessor = predecessor.getPredecessor();
        }

        Collections.reverse(shortestPath);
        return shortestPath;
    }
}

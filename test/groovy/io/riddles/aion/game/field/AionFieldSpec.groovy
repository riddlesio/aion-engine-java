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

package io.riddles.aion.game.field

import io.riddles.aion.engine.AionEngine
import io.riddles.aion.game.state.AionState
import io.riddles.javainterface.game.player.PlayerProvider
import io.riddles.javainterface.io.FileIOHandler
import spock.lang.Specification

/**
 * io.riddles.aion.game.field.AionFieldSpec - Created on 16-3-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
class AionFieldSpec extends Specification {

    def "test shortest path handler no bridges"() {

        setup:
        FileIOHandler ioHandler = new FileIOHandler("./test/resources/wrapper.txt")
        AionEngine engine = new AionEngine(new PlayerProvider<>(), ioHandler)
        AionState initialState = engine.willRun()
        AionField field = initialState.getField()
        Network eth = field.findNetwork("ETH")
        Network btc = field.findNetwork("BTC")

        when:
        ShortestPathHandler handler = new ShortestPathHandler(field.getNetworks())
        ArrayList<Network> path = handler.getShortestPath(eth, btc)

        then:
        path == null
    }

    def "test shortest path handler 1 bridge"() {

        setup:
        FileIOHandler ioHandler = new FileIOHandler("./test/resources/wrapper.txt")
        AionEngine engine = new AionEngine(new PlayerProvider<>(), ioHandler)
        AionState initialState = engine.willRun()
        AionField field = initialState.getField()
        Network eth = field.findNetwork("ETH")
        Network btc = field.findNetwork("BTC")

        when:
        field.addBridge(0, 1, "ETH", "BTC")
        field.addBridge(0, 3, "ETH", "BTC")
        ShortestPathHandler handler = new ShortestPathHandler(field.getNetworks())
        ArrayList<Network> path = handler.getShortestPath(eth, btc)

        then:
        path.size() == 1
        path.get(0).getCode() == "BTC"
    }

    def "test shortest path handler multiple bridges"() {

        setup:
        FileIOHandler ioHandler = new FileIOHandler("./test/resources/wrapper.txt")
        AionEngine engine = new AionEngine(new PlayerProvider<>(), ioHandler)
        AionState initialState = engine.willRun()
        AionField field = initialState.getField()
        Network eth = field.findNetwork("ETH")
        Network btc = field.findNetwork("BTC")

        when:
        field.addBridge(0, 4, "ETH", "BTC")
        field.addBridge(0, 1, "ETH", "ADA")
        field.addBridge(0, 1, "ADA", "NEO")
        field.addBridge(0, 3, "ADA", "NEO")
        field.addBridge(0, 1, "NEO", "BTC")
        ShortestPathHandler handler = new ShortestPathHandler(field.getNetworks())
        ArrayList<Network> path = handler.getShortestPath(eth, btc)

        then:
        path.size() == 3
        path.get(0).getCode() == "ADA"
        path.get(1).getCode() == "NEO"
        path.get(2).getCode() == "BTC"
    }

    def "test shortest path handler non connected start node"() {

        setup:
        FileIOHandler ioHandler = new FileIOHandler("./test/resources/wrapper.txt")
        AionEngine engine = new AionEngine(new PlayerProvider<>(), ioHandler)
        AionState initialState = engine.willRun()
        AionField field = initialState.getField()
        Network eth = field.findNetwork("ETH")
        Network btc = field.findNetwork("NEO")

        when:
        field.addBridge(0, 1, "NEO", "XRP")
        ShortestPathHandler handler = new ShortestPathHandler(field.getNetworks())
        ArrayList<Network> path = handler.getShortestPath(eth, btc)

        then:
        path == null
    }
}

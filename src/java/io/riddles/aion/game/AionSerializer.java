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

package io.riddles.aion.game;

import org.json.JSONObject;

import io.riddles.aion.engine.AionEngine;
import io.riddles.aion.game.processor.AionProcessor;
import io.riddles.aion.game.state.AionState;
import io.riddles.aion.game.state.AionStateSerializer;
import io.riddles.javainterface.game.AbstractGameSerializer;

/**
 * io.riddles.aion.game.AionSerializer - Created on 7-3-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class AionSerializer extends AbstractGameSerializer<AionProcessor, AionState, AionStateSerializer> {

    @Override
    protected JSONObject visitGame(
            AionProcessor processor,
            AionState initialState,
            AionStateSerializer stateSerializer
    ) {
        JSONObject game = super.visitGame(processor, initialState, stateSerializer);

        game.put("field", AionEngine.fieldData.getJSONObject("field"));
        game.put("networks", AionEngine.fieldData.getJSONArray("networks"));

        int transactionSpeed = AionEngine.configuration.getInt("transactionSpeed");
        int initialCoins = AionEngine.configuration.getInt("initialCoins");
        game.getJSONObject("settings").put("transactionSpeed", transactionSpeed);
        game.getJSONObject("settings").put("initialCoins", initialCoins);

        return game;
    }
}

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

import org.json.JSONObject;

import io.riddles.javainterface.serialize.AbstractSerializer;

/**
 * io.riddles.aion.game.state.AionPlayerStateSerializer - Created on 20-3-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class AionPlayerStateSerializer extends AbstractSerializer<AionPlayerState> {

    @Override
    protected JSONObject visitObject(AionPlayerState playerState) {
        JSONObject playerStateObject = new JSONObject();

        playerStateObject.put("id", playerState.getPlayerId());
        playerStateObject.put("coins", playerState.getAvailableCoins());
        playerStateObject.put("value", playerState.getTotalCoins());

        return playerStateObject;
    }
}

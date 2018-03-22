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

import java.util.ArrayList;
import java.util.stream.Collectors;

import io.riddles.aion.engine.AionEngine;
import io.riddles.aion.game.field.AionField;
import io.riddles.javainterface.game.state.AbstractState;

/**
 * io.riddles.aion.game.state.AionState - Created on 7-3-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class AionState extends AbstractState<AionPlayerState> {

    private AionField field;

    // For initial state only
    public AionState(ArrayList<AionPlayerState> playerStates, AionField field) {
        super(null, playerStates, 0);
        this.field = field;
    }

    public AionState(AionState previousState, ArrayList<AionPlayerState> playerStates, int roundNumber) {
        super(previousState, playerStates, roundNumber);
        this.field = new AionField(previousState.field);
    }

    @Override
    public AionState createNextState(int roundNumber) {
        return new AionState(this, clonePlayerStates(), roundNumber);
    }

    public ArrayList<AionPlayerState> getWinningPlayers() {
        return this.playerStates.stream()
                .filter(p -> p.getTotalCoins() >= AionEngine.configuration.getInt("winAmount"))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public AionField getField() {
        return this.field;
    }
}

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

import io.riddles.aion.game.move.AionMove;
import io.riddles.javainterface.game.state.AbstractPlayerState;

/**
 * io.riddles.aion.game.state.AionPlayerState - Created on 7-3-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class AionPlayerState extends AbstractPlayerState<AionMove> {

    private int totalCoins;  // Total coin value for player
    private int availableCoins;  // Coins available for bridge staking

    public AionPlayerState(int playerId, int initialCoins) {
        super(playerId);
        this.totalCoins = initialCoins;
        this.availableCoins = initialCoins;
    }

    public AionPlayerState(AionPlayerState playerState) {
        super(playerState.playerId);
        this.totalCoins = playerState.totalCoins;
        this.availableCoins = playerState.availableCoins;
    }

    @Override
    public AionPlayerState clone() {
        return new AionPlayerState(this);
    }

    public void payStake(int stake) {
        this.availableCoins -= stake;
    }

    public void receiveStake(int stake) {
        this.availableCoins += stake;
    }

    public void receiveFee(int fee) {
        this.totalCoins += fee;
        this.availableCoins += fee;
    }

    public int getTotalCoins() {
        return this.totalCoins;
    }

    public int getAvailableCoins() {
        return this.availableCoins;
    }
}

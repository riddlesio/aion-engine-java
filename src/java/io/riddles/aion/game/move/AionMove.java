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

package io.riddles.aion.game.move;

import io.riddles.javainterface.exception.InvalidInputException;
import io.riddles.javainterface.game.move.AbstractMove;

/**
 * io.riddles.aion.game.move.AionMove - Created on 7-3-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class AionMove extends AbstractMove {

    private String side1;
    private String side2;
    private Integer fee;
    private MoveType moveType;

    public AionMove() {
        this.moveType = MoveType.PASS;
    }

    public AionMove(String side1, String side2, int fee) {
        this.side1 = side1;
        this.side2 = side2;
        this.fee = fee;
        this.moveType = MoveType.BUILD;
    }

    public AionMove(String side1, String side2) {
        this.side1 = side1;
        this.side2 = side2;
        this.moveType = MoveType.REMOVE;
    }

    public AionMove(InvalidInputException exception) {
        super(exception);
    }

    public String getSide1() {
        return this.side1;
    }

    public String getSide2() {
        return this.side2;
    }

    public Integer getFee() {
        return this.fee;
    }

    public MoveType getMoveType() {
        return this.moveType;
    }
}

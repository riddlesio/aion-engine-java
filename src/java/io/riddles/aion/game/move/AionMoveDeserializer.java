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

import io.riddles.aion.engine.AionEngine;
import io.riddles.javainterface.exception.InvalidInputException;
import io.riddles.javainterface.game.move.AbstractMoveDeserializer;

/**
 * io.riddles.aion.game.move.AionMoveDeserializer - Created on 7-3-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class AionMoveDeserializer extends AbstractMoveDeserializer<AionMove> {

    @Override
    protected AionMove createExceptionMove(InvalidInputException exception) {
        return new AionMove(exception);
    }

    @Override
    protected AionMove visitMove(String input) throws InvalidInputException {
        String[] split = input.split(" ");
        MoveType moveType = parseMoveType(split[0]);

        if (moveType == MoveType.PASS) {
            return new AionMove();
        }

        if (moveType == MoveType.BUILD && split.length != 3) {
            throw new InvalidInputException("Move doesn't split into 3 parts");
        }

        if (moveType == MoveType.REMOVE && split.length != 2) {
            throw new InvalidInputException("Move doesn't split into 2 parts");
        }

        String[] sides = parseSides(split[1]);

        if (moveType == MoveType.REMOVE) {
            return new AionMove(sides[0], sides[1]);
        }

        int fee = parseFee(split[2]);

        return new AionMove(sides[0], sides[1], fee);
    }

    private MoveType parseMoveType(String input) throws InvalidInputException {
        MoveType moveType = MoveType.fromString(input);

        if (moveType == null) {
            throw new InvalidInputException("Cannot parse move type");
        }

        return moveType;
    }

    private String[] parseSides(String input) throws InvalidInputException {
        String[] sides = input.split("-");

        if (sides.length != 2) {
            throw new InvalidInputException("Cannot parse bridge");
        }

        if (sides[0].equals(sides[1])) {
            throw new InvalidInputException("Two sides of bridge can't be the same");
        }

        return sides;
    }

    private int parseFee(String input) throws InvalidInputException {
        int fee;
        int maxFee = AionEngine.configuration.getInt("maxFee");

        try {
            fee = Integer.parseInt(input);
        } catch (Exception e) {
            throw new InvalidInputException("Cannot parse fee");
        }

        if (fee < 1) {
            throw new InvalidInputException("Fee can't be lower than 1");
        }

        if (fee > maxFee) {
            throw new InvalidInputException(String.format("Fee can't be higher than %d", maxFee));
        }

        return fee;
    }
}

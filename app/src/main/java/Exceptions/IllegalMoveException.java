package Exceptions;

import android.util.Log;

import net.john.magiclifecounter.PlayerMove;

/**
 * Created by John on 8/25/2015.
 * Thrown when an illegal move is created and then attempted to be applied
 */
public class IllegalMoveException extends Exception {
    PlayerMove move;

    public IllegalMoveException(PlayerMove move){
        this.move = move;
        Log.wtf("IllegalMoveExcetion", move.toString());
    }

}

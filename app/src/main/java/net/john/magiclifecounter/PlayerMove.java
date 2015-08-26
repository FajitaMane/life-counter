package net.john.magiclifecounter;

import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by John on 8/21/2015.
 */
public class PlayerMove {
    private Player player_enum;
    private int increment;


    public PlayerMove(Player player, int increment){
        this.player_enum = player;
        this.increment = increment;
    }
/*
    //assume damage is to opposite player in two player game if no affected player provided
    public PlayerMove(Player player, int increment){
        player_affected = (player_enum == Player.PLAYER_ONE) ? Player.PLAYER_TWO : Player.PLAYER_ONE;
        this.player_enum = player_enum;
        this.increment = increment;
    }
    */

    @Nullable
    protected static PlayerMove getMoveFromId(int id){
        Log.d("move", "getMoveFromId received id " + id);
        switch (id){
            case R.id.player_1_add_one:
                return new PlayerMove(Player.PLAYER_ONE, 1);
            case R.id.player_1_add_five:
                return new PlayerMove(Player.PLAYER_ONE, 5);
            case R.id.player_1_minus_one:
                return new PlayerMove(Player.PLAYER_ONE, -1);
            case R.id.player_1_minus_five:
                return new PlayerMove(Player.PLAYER_ONE, -5);
            case R.id.player_2_add_one:
                return new PlayerMove(Player.PLAYER_TWO, 1);
            case R.id.player_2_add_five:
                return new PlayerMove(Player.PLAYER_TWO, 5);
            case R.id.player_2_minus_one:
                return new PlayerMove(Player.PLAYER_TWO, -1);
            case R.id.player_2_minus_five:
                return new PlayerMove(Player.PLAYER_TWO, -5);
        }
        Log.wtf("id", "bad id fed to switch in PlayerMove");
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerMove that = (PlayerMove) o;

        if (increment != that.increment) return false;
        return player_enum == that.player_enum;

    }

    @Override
    public int hashCode() {
        int result = player_enum.hashCode();
        result = 31 * result + increment;
        return result;
    }

    @Override
    public String toString(){
        return player_enum.name() + " takes " + increment + " damage";
    }

    public int getIncrement() {
        return increment;
    }

    public Player getplayer_enum() {
        return player_enum;
    }
}

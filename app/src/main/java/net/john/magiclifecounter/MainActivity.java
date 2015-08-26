package net.john.magiclifecounter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final int STARTING_LIFE = 20;
    private int player_count = 2;
    private int[] player_life = new int[2];
    private TextView player_1_life;
    private TextView player_2_life;
    private Player[] player_array;

    //back and forward buttons and state
    private ArrayList<PlayerMove> move_list;
    private ImageView forward_button;
    private ImageView back_button;
    private boolean forwardIsPossible;
    private boolean backIsPossible;
    private ArrayList<PlayerMove> reverted_move_list;

    //enumeration to determine which player an id belongs to
    private int[] player_1_id_array = new int[4];
    private int[] player_2_id_array = new int[4];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (int i = 0; i < player_life.length; i++){
            player_life[i] = 20; //set all players' life to 20
        }
        move_list = new ArrayList<PlayerMove>(); //instantiate move_list when activity starts
        reverted_move_list = new ArrayList<PlayerMove>();
        player_1_life = (TextView) findViewById(R.id.player_1_life);
        player_2_life = (TextView) findViewById(R.id.player_2_life);
        //instantiate the array of n Players here
        player_array = new Player[2];
        player_array[0] = Player.PLAYER_ONE;
        player_array[1] = Player.PLAYER_TWO;

        back_button = (ImageView) findViewById(R.id.back_button);
        forward_button = (ImageView) findViewById(R.id.forward_button);
        forwardIsPossible = false; //can't go forward without any moves
        backIsPossible = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void incrementerClick(View v){
        Log.d("Buttons", "click received" + v.getId());
        int id = v.getId();
        PlayerMove move = PlayerMove.getMoveFromId(id);
        if (move == null){
            Log.d("move", "move is null");
            return;
        }
        Log.d("move", move.toString());
        if (move.getplayer_enum() == Player.PLAYER_ONE){
            player_life[0]+=move.getIncrement();
        } else {
            player_life[1]+=move.getIncrement();
        }
        move_list.add(move);
        drawUI();
        //Log.d("Buttons", "after IncrementorClick player 1 life " + player_life[0]);
        printHistory();
    }

    private void drawUI(){
        player_1_life.setText("" + player_life[0]);
        player_2_life.setText("" + player_life[1]);
        if (move_list.size() == 0){
            forwardIsPossible = false;
            backIsPossible = false;
        } else {

        }
    }

    private void printHistory(){
        for (int i = 0; i < move_list.size(); i++){
            Log.d("history", "move #" + i + move_list.get(i).toString());
        }
    }

    public void onBackButtonClick(View v){
        if (move_list.size() == 0 || move_list == null){
            Log.wtf("move_list", "move_list undo called when null or empty");
            return;
        }
        for (int i = 0; i < player_count; i++){
            if (player_array[i] == move_list.get(move_list.size() - 1).getplayer_enum()){ //if this is the player whose move was reversed
                player_life[i]+=move_list.get(move_list.size() - 1).getIncrement();
                //remove last from move_list and add it to reverted move_list
                reverted_move_list.add(move_list.get(move_list.size() - 1));
                move_list.remove(move_list.size() - 1);
                break;
            }
        }
        //drawUI is always the last call
        drawUI();
    }

    public void onForwardButtonClick(View v){
        if (reverted_move_list.size() == 0 || reverted_move_list == null){
            Log.wtf("reverted_move_list", "reverted_move_list redo called when null or empty");
            return;
        }
        //drawUI is always the last call
        drawUI();
    }
}

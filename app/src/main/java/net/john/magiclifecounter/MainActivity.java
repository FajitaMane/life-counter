package net.john.magiclifecounter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;

import Exceptions.IllegalMoveException;

public class MainActivity extends AppCompatActivity {
    private final int STARTING_LIFE = 20;
    private int player_count;
    private boolean keep_screen_on;
    private int[] player_life = new int[2];
    private RelativeLayout main_layout;
    private TextView player_1_life;
    private TextView player_2_life;
    private Player[] player_array;

    //back and forward buttons and state
    private ArrayList<PlayerMove> move_list;
    private ImageView forward_button;
    private ImageView back_button;
    private ImageView refresh_button;
    private boolean forwardIsPossible;
    private boolean backIsPossible;
    private ArrayList<PlayerMove> reverted_move_list;

    //enumeration to determine which player an id belongs to
    private int[] player_1_id_array = new int[4];
    private int[] player_2_id_array = new int[4];

    //wakelock keeps the scree on
    protected PowerManager.WakeLock mWakeLock;

    //milliseconds between commands to merge similar moves
    private final long MERGE_MILLIS = 3 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get preferences before making any UI
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        player_count = Integer.getInteger(prefs.getString("num_players", "2"));
        keep_screen_on = prefs.getBoolean("keep_screen_on", true);

        setContentView(R.layout.activity_main);
        main_layout = (RelativeLayout) findViewById(R.id.main_layout);
        back_button = (ImageView) findViewById(R.id.back_button);
        forward_button = (ImageView) findViewById(R.id.forward_button);
        refresh_button = (ImageView) findViewById(R.id.refresh_button);

        //set the wake lock
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MagicLifeCounter");
        mWakeLock.acquire();

        Display mDisplay = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        mDisplay.getMetrics(metrics);
        float dpHeight = metrics.heightPixels / metrics.density;
        int[] p = new int[2];
        main_layout.getLocationOnScreen(p);
        float remaining_dp_space = dpHeight - p[1] - main_layout.getHeight();

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

        forwardIsPossible = false; //can't go forward without any moves
        backIsPossible = false;
        drawUI();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mWakeLock.release();
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

        if (id == R.id.preferences){
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
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
        try {
            applyPlayerMove(move); //only makes the move if it's legal
            drawUI();
        } catch (IllegalMoveException ex){
            ex.printStackTrace();
        }
    }

    private void applyPlayerMove(PlayerMove move) throws IllegalMoveException {
        int offset = 0;
        boolean player_found = false;
        for (int i = 0; i < player_count; i++){
            if (player_array[i] == move.getplayer_enum()){
                offset = i;
                player_found = true;
            }
        }
        if (!player_found) throw new IllegalMoveException(move); //player not found
        if (player_life[offset] + move.getIncrement() < 0){
            throw new IllegalMoveException(move);
        }
        player_life[offset] += move.getIncrement();
        //check for null move_list
        if (move_list.size() != 0){
            if (System.currentTimeMillis() - move_list.get(move_list.size() - 1).getTimestamp() <
                    MERGE_MILLIS){ //if last command was within merge range
                float diff = System.currentTimeMillis() - move_list.get(move_list.size() - 1).getTimestamp() / 1000;
                Log.d("merge", "merging with diff of " + diff + "s");
                move_list.get(move_list.size() - 1).mergeWithMove(move);
            } else {
                move_list.add(move); //TODO fix confusing logic. Move logic to new class?
            }
        } else {
            move_list.add(move);
        }
        //check if the merged move created a 0 increment
        if (move_list.get(move_list.size() - 1).getIncrement() == 0){
            move_list.remove(move_list.size() - 1); //remove if so
        }
    }

    /**
     * This method should be the last call of controller methods
     */
    private void drawUI(){
        //set player life first
        player_1_life.setText("" + player_life[0]);
        player_2_life.setText("" + player_life[1]);

        //determine button visibility
        backIsPossible = (move_list.size() == 0) ? false : true;
        forwardIsPossible = (reverted_move_list.size() == 0) ? false : true;
        int back_button_xml = (backIsPossible) ? View.VISIBLE : View.INVISIBLE;
        int forward_button_xml = (forwardIsPossible) ? View.VISIBLE : View.INVISIBLE;
        int refresh_button_xml = (move_list.isEmpty()) ? View.INVISIBLE : View.VISIBLE;

        //apply visibility and clickability to buttons
        back_button.setVisibility(back_button_xml);
        back_button.setClickable(backIsPossible);
        forward_button.setVisibility(forward_button_xml);
        forward_button.setClickable(forwardIsPossible);
        refresh_button.setVisibility(refresh_button_xml);
        refresh_button.setClickable(!move_list.isEmpty());
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
                player_life[i]-=move_list.get(move_list.size() - 1).getIncrement();
                //remove last from move_list and add it to reverted move_list
                Toast.makeText(MainActivity.this,
                        "Undid move: " + move_list.get(move_list.size() - 1).toString() ,
                        Toast.LENGTH_SHORT).show();
                reverted_move_list.add(new PlayerMove(move_list.get(move_list.size() - 1)));
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
        for (int i = 0; i < player_count; i++){
            if (player_array[i] == reverted_move_list.get(i).getplayer_enum()){
                player_life[i]+=reverted_move_list.get(reverted_move_list.size() - 1).getIncrement();//last item in the list
                move_list.add(new PlayerMove(reverted_move_list.get(reverted_move_list.size() - 1)));
                Toast.makeText(MainActivity.this,
                        "Redid move: " + move_list.get(move_list.size() - 1).toString() ,
                        Toast.LENGTH_SHORT).show();
                //remove reverted move from list
                reverted_move_list.remove(reverted_move_list.size() - 1);
                break;
            }
        }
        //drawUI is always the last call
        drawUI();
    }

    public void onRefreshButtonClick(View v){
        new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.reset_prompt))
            .setPositiveButton(getResources().getString(R.string.confirm_reset), new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    move_list.clear();
                    reverted_move_list.clear();
                    for (int i = 0; i < player_life.length; i++){
                        player_life[i] = STARTING_LIFE;
                    }
                    drawUI();
                }
            })
            .setNegativeButton(getResources().getString(R.string.deny_reset), new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which) {
                    //do nothing
                }
            }).show();
    }
}

package com.gagaryn.play.commands;

import com.gagaryn.play.MainActivity;
import com.gagaryn.play.R;

public interface Command {

    public static final int COMMAND_PLAY = R.id.player_play_button;
    public static final int COMMAND_PAUSE = 2;
    public static final int COMMAND_NEXT = R.id.player_next_button;
    public static final int COMMAND_PREVIOUS = R.id.player_back_button;
    public static final int COMMAND_SHUFFLE = R.id.player_shuffle_button;

    public void execute(MainActivity activity);
}

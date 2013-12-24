package com.gagaryn.play.commands;

import com.gagaryn.play.MainActivity;

public class CommandShuffle implements Command {

    @Override
    public void execute(MainActivity activity) {
        activity.shuffle();
    }
}

package com.gagaryn.play.commands;

import com.gagaryn.play.MainActivity;

public class CommandPause implements Command {

    @Override
    public void execute(MainActivity activity) {
        activity.pause();
    }
}

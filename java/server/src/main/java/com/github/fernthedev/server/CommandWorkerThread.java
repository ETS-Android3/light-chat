package com.github.fernthedev.server;

import com.github.fernthedev.server.command.Command;
import com.github.fernthedev.server.command.CommandSender;

public class CommandWorkerThread implements Runnable {

    private final Command serverCommand;
    private final String[] args;
    private final CommandSender commandSender;

    public CommandWorkerThread(CommandSender commandSender, Command command, String[] args) {
        this.serverCommand = command;
        this.args = args;
        this.commandSender = commandSender;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        serverCommand.onCommand(commandSender,args);
    }
}

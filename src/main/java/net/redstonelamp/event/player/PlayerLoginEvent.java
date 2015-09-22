package net.redstonelamp.event.player;

import lombok.Getter;
import net.redstonelamp.Player;
import net.redstonelamp.event.Cancellable;
import net.redstonelamp.event.Event;

public class PlayerLoginEvent extends Event implements Cancellable {
    @Getter private Player player;
    private boolean cancelled = false;
    
    public PlayerLoginEvent(Player player) {
        this.player = player;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
package net.redstonelamp.ticker;

import net.redstonelamp.Server;
import net.redstonelamp.utils.AntiSpam;

import java.util.ArrayList;
import java.util.List;

/**
 * The ticker used by RedstoneLamp.
 * <br>
 * It is based on the one used in BlockServer (https://github.com/BlockServerProject/BlockServer)
 *
 * @author RedstoneLamp Team
 */
public class RedstoneTicker {
    private static final String ANTISPAM_LOAD_MEASURE_TOO_HIGH = "net.redstonelamp.ticker.RedstoneTicker.LoadMeasureTooHigh";
    private final Server server;
    private long sleep;
    private long tick = -1L;
    private boolean running = false;
    private boolean lastTickDone = false;
    private long lastTickMilli;
    private double loadMeasure = 0D;
    private long startTime;
    private final List<RegisteredTask> tasks = new ArrayList<>();

    /**
     * Create a new <code>RedstoneTicker</code> belonging to the specified <code>Server</code>
     * @param server The Server this ticker belongs to.
     * @param sleepNanos The amount of nanoseconds to sleep for. (default 50)
     */
    public RedstoneTicker(Server server, int sleepNanos) {
        this.server = server;
        sleep = sleepNanos;
    }

    /**
     * Start this ticker. This method actually runs the ticker too, therefor it blocks.
     */
    public void start() {
        if(running){
            throw new IllegalStateException("Ticker is already running");
        }
        running = true;
        server.getLogger().debug("Ticker is now running.");
        startTime = System.currentTimeMillis();
        while(running){
            lastTickMilli = System.currentTimeMillis();
            tick++;
            tick();
            // calculate server load
            long now = System.currentTimeMillis();
            long diff = now - lastTickMilli;
            loadMeasure = diff * 100D /  sleep;
            if(loadMeasure > 80D){
                AntiSpam.act(() -> server.getLogger().warning("The server load is too high! (%f / 100)", loadMeasure), ANTISPAM_LOAD_MEASURE_TOO_HIGH, 5000);
                continue;
            }
            long need = sleep - diff;
            try{
                Thread.sleep(need);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        synchronized(tasks){
            for(RegisteredTask task: tasks){
                task.getTask().onFinalize();
            }
        }
        lastTickDone = true;
    }

    private void tick(){
        RegisteredTask[] taskArray;
        synchronized(tasks){
            taskArray = new RegisteredTask[tasks.size()];
            tasks.toArray(taskArray);
        }
        for(RegisteredTask task : taskArray){
            task.check(tick);
        }
    }

    /**
     * Stop this ticker. This method will block until the last tick is done.
     */
    public void stop(){
        if(!running){
            throw new IllegalStateException("Ticker is not running and cannot be stopped");
        }
        running = false;
        while(!lastTickDone);
    }

    public synchronized void addDelayedTask(Task task, int delay){
        synchronized(tasks){
            tasks.add(RegisteredTask.delay(task, delay));
        }
    }

    public synchronized void addRepeatingTask(Task task, int repeatInterval){
        synchronized(tasks){
            tasks.add(RegisteredTask.repeat(task, repeatInterval));
        }
    }

    public synchronized void addDelayedRepeatingTask(Task task, int delay, int repeatInterval){
        synchronized(tasks){
            tasks.add(RegisteredTask.delayAndRepeat(task, delay, repeatInterval));
        }
    }

    /**
     * Cancel a task.
     * @param task The task to be canceled.
     * @return If the task was removed.
     */
    public synchronized boolean cancelTask(Task task){
        RegisteredTask corr = null;
        synchronized(tasks){
            for(RegisteredTask rt : tasks){
                if(rt.getTask().equals(task)){
                    corr = rt;
                    break;
                }
            }
            return tasks.remove(corr);
        }
    }

    public long getStartTime(){
        return startTime;
    }
}

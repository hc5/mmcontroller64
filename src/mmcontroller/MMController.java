/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmcontroller;

import java.util.concurrent.ConcurrentLinkedQueue;
import net.java.games.input.*;
import net.java.games.input.Component.Identifier;
/**
 *
 * @author yzhou53
 */
public class MMController extends Thread {

    Controller core;
    private ConcurrentLinkedQueue<Event> queue;
    private PollingThread poller = new PollingThread();
    boolean safeToStart = false;

    public MMController() {
        for (Controller c : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
            if (c.getName().contains("RumblePad")) {
                core = c;
            }
        }
        safeToStart = true;
        queue = new ConcurrentLinkedQueue<>();
    }

    public void run() {
        long oldTime = -1;
        float oldValue = Float.NEGATIVE_INFINITY;
        Component.Identifier oldCompId = null;
        while (!safeToStart);
        poller.start();
        while (true) {
            Event e = queue.poll();
            if (e == null) {
                continue;
            }
            Component comp = e.getComponent();
            Component.Identifier compId = comp.getIdentifier();
            float value = e.getValue();
            long time = e.getNanos();
            if (time == oldTime && value == oldValue && compId == oldCompId) {
                continue; // dup, ignore it.
            } else if (time > oldTime || compId != oldCompId || value != oldValue) {
                oldTime = time;
                oldValue = value;
                oldCompId = compId;
            }
            dispatch(e);
        }
    }

    public static void main(String[] args) {
        MMController mc = new MMController();
        mc.start();
    }

    void LX(double value){};
    void LY(double value){};
    void RX(double value){};
    void RY(double value){};
    void LShoulder(boolean down){};
    void RShoulder(boolean down){};
    void ZShoulder(boolean down){};
    
    private void dispatch(Event e) {
        if(e.getComponent().getIdentifier().equals(Component.Identifier.Axis.X)){
            LX(e.getValue());
        }
        else if(e.getComponent().getIdentifier().equals(Component.Identifier.Axis.Y)){
            LY(e.getValue());
        }
        else if(e.getComponent().getIdentifier().equals(Component.Identifier.Axis.Z)){
            RX(e.getValue());
        }
        else if(e.getComponent().getIdentifier().equals(Component.Identifier.Axis.RZ)){
            RY(e.getValue());
        }
        else if(e.getComponent().getIdentifier().equals(Component.Identifier.Button._7)){
            LShoulder(e.getValue() == 1.0);
        }
        else if(e.getComponent().getIdentifier().equals(Component.Identifier.Button._6)){
            RShoulder(e.getValue() == 1.0);
        }
        else if(e.getComponent().getIdentifier().equals(Component.Identifier.Button._5)){
            ZShoulder(e.getValue() == 1.0);
        }
    }

    class PollingThread extends Thread {

        public void run() {
            while (!safeToStart);
            while (true) {
                core.poll();
                EventQueue localQueue = core.getEventQueue();
                Event e = new Event();
                while (localQueue.getNextEvent(e)) {
                    queue.add(e);
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}

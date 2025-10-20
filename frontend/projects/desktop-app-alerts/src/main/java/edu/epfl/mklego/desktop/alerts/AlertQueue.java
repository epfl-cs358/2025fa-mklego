package edu.epfl.mklego.desktop.alerts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.epfl.mklego.desktop.alerts.exceptions.AlertAlreadyExistsException;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class AlertQueue {
    private final Lock operationsLock = new ReentrantLock(); 

    private final List<Alert> innerQueue = new ArrayList<>();
    private final Set<AlertQueueListener> listeners
         = Collections.synchronizedSet( new HashSet<>() );
    
    public static interface AlertQueueListener {
        public void onPushBack (Alert object);
        public void onPop (int index, Alert object);
    }

    public void addListener (AlertQueueListener listener) {
        listeners.add(listener);
    }

    /**
     * Push back an Alert to the queue
     * @param alert the alert to add to the queue
     * @return a runnable that removes the object from the queue
     */
    public Runnable pushBack (Alert alert) throws AlertAlreadyExistsException {
        try {
            operationsLock.lock();
            for (int idx = 0; idx < innerQueue.size(); idx ++)
                if (innerQueue.get(idx) == alert)
                    throw new AlertAlreadyExistsException();

            innerQueue.add(alert);

            Platform.runLater(() -> {
                try {
                    operationsLock.lock();
                    
                    for (AlertQueueListener listener : listeners)
                        listener.onPushBack(alert);
                } finally {
                    operationsLock.unlock();
                }
            });

            final BooleanProperty stillInQueue = new SimpleBooleanProperty(true);
            Runnable remove = () -> {
                try {
                    operationsLock.lock();

                    if (stillInQueue.get()) {
                        stillInQueue.set(false);
                        
                        for (int idx = 0; idx < innerQueue.size(); idx ++) {
                            if (innerQueue.get(idx) == alert) {
                                innerQueue.remove(idx);

                                for (AlertQueueListener listener : listeners)
                                    listener.onPop(idx, alert);
                                
                                break ;
                            }
                        }
                    }
                } finally {
                    operationsLock.unlock();
                }
            };

            alert.setRemove(remove);
            return remove;
        } finally {
            operationsLock.unlock();
        }
    }
}

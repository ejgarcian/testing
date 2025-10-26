/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edd;

import javax.swing.SwingUtilities;

/**
 * Esta clase define un objeto de tipo cola. Contiene una lista de los elementos encolados.
 * 
 * @version 27/10/2024
 * @author Michelle García
 */
public class Cola {
    
    private Lista queue = new Lista();
    private Lista listeners = new Lista();

    /**
     * Procedimiento para encolar un objeto
     * 
     * @param j Objeto a encolar
     */
    public synchronized void enqueue(Object j) {
        
        if (getQueue().contains(j)){
            return;
        }
        getQueue().add(j);
        notifyListeners();
    }

    
    /**
     * Procedimiento para desencolar el primer elemento de la cola
     * 
     * @return Primer elemento de la cola
     */
    public synchronized Object dequeue() {
        if (getQueue().count() > 0) {
            Object value = getQueue().get(0);
            getQueue().remove(0);
            notifyListeners();
            return value;
        }
        return null;
    }

    /**
     * Función para obtener la cantidad de elementos encolados en una cola
     * 
     * @return Cantidad de elementos encolados en la cola actual
     */
    public synchronized int getCount() {
        return getQueue().count();
    }

    /**
     * Función para obtener un objeto según su posición
     * 
     * @param index Posición del objeto dentro de la cola
     * @return Objeto según la posición
     */
    public synchronized Object get(int index) {
        return getQueue().get(index);
    }

    /**
     * Función para obtener la cola actual
     * 
     * @return Cola actual
     */
    public Lista getQueue() {
        return queue;
    }
    
    public synchronized boolean getContains(Object value) {
        return getQueue().contains(value);
    }
    
    // ---------------- Listener API (uses Lista, not java.util) ----------------

    /**
     * Add a listener that will be notified when this Cola changes.
     *
     * @param l the listener to add (ignored if null)
     */
    public synchronized void addListener(QueueChangeListener l) {
        if (l == null) return;
        // avoid duplicates (simple linear scan)
        for (int i = 0; i < listeners.count(); i++) {
            Object existing = listeners.get(i);
            if (existing == l) {
                return; // already registered
            }
        }
        listeners.add(l);
    }

    /**
     * Remove a previously added listener.
     *
     * @param l the listener to remove (ignored if not found or null)
     */
    public synchronized void removeListener(QueueChangeListener l) {
        if (l == null) return;
        int n = listeners.count();
        for (int i = 0; i < n; i++) {
            Object o = listeners.get(i);
            if (o == l || (o != null && o.equals(l))) {
                // use Lista.remove(index)
                listeners.remove(i);
                return;
            }
        }
    }

    /**
     * Notify all registered listeners that this queue changed.
     * Note: keep private but expose fireChange() for controlled public triggering.
     *
     * Important change: listeners are invoked on the Swing Event Dispatch Thread (EDT)
     * so UI code updating JScrollPanes / Swing components runs safely and repaints properly.
     */
    private void notifyListeners() {
        // Take a snapshot of current listeners to avoid holding locks while notifying
        int n = listeners.count();
        Object[] snap = new Object[n];
        for (int i = 0; i < n; i++) {
            snap[i] = listeners.get(i);
        }

        // Schedule each listener call on the EDT. This prevents UI updates from happening
        // on scheduler/background threads where Swing is not thread-safe.
        for (int i = 0; i < n; i++) {
            final Object o = snap[i];
            if (o instanceof QueueChangeListener) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ((QueueChangeListener) o).queueChanged(Cola.this);
                        } catch (Exception ex) {
                            // avoid listener exceptions breaking queue logic; print on EDT so stack trace is coherent
                            System.err.println("Queue listener threw: " + ex);
                            ex.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    /**
     * Public method to trigger listeners when external code manipulates the
     * internal Lista directly (use sparingly). Prefer using Cola APIs instead.
     */
    public synchronized void fireChange() {
        notifyListeners();
    }
    
    /**
     * Remove item at index (wraps Lista.remove) and notifies listeners.
     *
     * @param index index to remove
     */
    public synchronized void removeAt(int index) {
        getQueue().remove(index);
        notifyListeners();
    }

    /**
     * Remove the first occurrence of an object from the queue (if found).
     * Returns true if removed.
     *
     * @param value value to remove
     * @return true if removed
     */
    public synchronized boolean removeValue(Object value) {
        if (value == null) return false;
        // search for equality by scanning
        int n = getQueue().count();
        for (int i = 0; i < n; i++) {
            Object o = getQueue().get(i);
            if (o == value || (o != null && o.equals(value))) {
                getQueue().remove(i);
                notifyListeners();
                return true;
            } else {
                // also support matching by PCB id / Proceso equality cases
            }
        }
        return false;
    }

    /**
     * Swap two indices in the internal Lista and notify listeners.
     * This is a thread-safe wrapper around Lista.swap().
     *
     * @param i first index
     * @param j second index
     */
    public synchronized void swap(int i, int j) {
        getQueue().swap(i, j);
        notifyListeners();
    }

    /**
     * Take an atomic snapshot of the current queue contents and return as an Object[].
     * Use this for safe UI iteration.
     *
     * @return array copy of current elements (never null)
     */
    public synchronized Object[] snapshot() {
        int n = getQueue().count();
        Object[] arr = new Object[n];
        for (int i = 0; i < n; i++) {
            arr[i] = getQueue().get(i);
        }
        return arr;
    }

}

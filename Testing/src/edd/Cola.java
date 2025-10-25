/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edd;

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
    public void enqueue(Object j) {
        getQueue().add(j);
        notifyListeners();
    }

    
    /**
     * Procedimiento para desencolar el primer elemento de la cola
     * 
     * @return Primer elemento de la cola
     */
    public Object dequeue() {
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
    public int getCount() {
        return getQueue().count();
    }

    /**
     * Función para obtener un objeto según su posición
     * 
     * @param index Posición del objeto dentro de la cola
     * @return Objeto según la posición
     */
    public Object get(int index) {
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
    
    public boolean getContains(Object value) {
        return getQueue().contains(value);
    }
    
    // ---------------- Listener API (uses Lista, not java.util) ----------------

    /**
     * Add a listener that will be notified when this Cola changes.
     *
     * @param l the listener to add (ignored if null)
     */
    public void addListener(QueueChangeListener l) {
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
    public void removeListener(QueueChangeListener l) {
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
     */
    private void notifyListeners() {
        int n = listeners.count();
        // iterate by index to avoid using java.util
        for (int i = 0; i < n; i++) {
            Object o = listeners.get(i);
            if (o instanceof QueueChangeListener) {
                try {
                    ((QueueChangeListener) o).queueChanged(this);
                } catch (Exception ex) {
                    // avoid listener exceptions breaking queue logic
                    System.err.println("Queue listener threw: " + ex);
                    ex.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Remove item at index (wraps Lista.remove) and notifies listeners.
     *
     * @param index index to remove
     */
    public void removeAt(int index) {
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
    public boolean removeValue(Object value) {
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
                // Lista.indexOf already handles some comparisons; use it if present
                // but keep the explicit scan above for general objects
            }
        }
        return false;
    }

}

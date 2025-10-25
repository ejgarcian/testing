/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edd;

/**
 *
 * @author Eddy
 */
public class Semaforo {
    private Cola queue;
    private int mutex;

    public Semaforo() {
        this.queue = new Cola();
        this.mutex = 1;
    }

    public int getMutex() {
        return mutex;
    }

    public void setMutex(int mutex) {
        this.mutex = mutex;
    }
    
    public boolean isAvailable(){
        return getMutex()==0;
    }
    
    public boolean semWait(Object j){
        int update = getMutex() - 1;
        setMutex(update);
        
        if (!isAvailable()){
            queue.enqueue(j);
            return false;
        }
        return true;
    }
    
    public Object semSignal(){
        int update = getMutex() + 1;
        setMutex(update);
        
        if (!isAvailable()) {
            Object next = queue.dequeue();
            return next;
        } else {
            return null;
        }
    }
}

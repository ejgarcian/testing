/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edd;

/**
 *
 * @author Eddy
 */
public class Proceso extends Thread {
    private PCB pcb;
    private String bound;
    private int instructions;
    private double processingTime; //50ms por cada instruccion
    private int timeSpent = 0;
    private int totalTimeSpent = 0;
    private int memorySpace = instructions * 4;
    private int ioCicles;
    private int satisfyCicles;
    private int interruptAt;
    private int deviceToUse;



    public Proceso(int id, String name, String bound, int instructions, int ioCicles, int satisfyCicles, int deviceToUse, int priority) {
        this.pcb = new PCB(id, name);
        this.pcb.setPriorityFSS(priority);
        this.bound = bound;
        this.instructions = instructions;
        this.ioCicles = ioCicles;
        this.satisfyCicles = satisfyCicles;
        this.interruptAt = (int)(instructions/2);
        this.deviceToUse = deviceToUse;
        this.processingTime = instructions;
        this.memorySpace = instructions * 4;
    }

    
    
    public PCB getPcb() {
        return pcb;
    }

    public String getBound() {
        return bound;
    }

    public void setBound(String bound) {
        this.bound = bound;
    }

    public int getInstructions() {
        return instructions;
    }

    public void setInstructions(int instructions) {
        this.instructions = instructions;
    }
    
    /**
     * @return the processingTime
     */
    public double getProcessingTime() {
        return processingTime;
    }

    /**
     * @return the timeSpent
     */
    public int getTimeSpent() {
        return timeSpent;
    }
    
    /**
     * @return the timeSpent
     */
    public int getMemorySpace() {
        return memorySpace;
    }

    @Override //La clase thread ya tiene una clase run
    public void run(){
        timeSpent = 0;
        pcb.setPc(pcb.getPc()+1);
        pcb.setMar(pcb.getMar()+1);
        while (getTimeSpent() < getProcessingTime()){
            timeSpent++;
            totalTimeSpent++;
            pcb.setPc(pcb.getPc()+1);
            pcb.setMar(pcb.getMar()+1);

            /*if (this.getTimeSpent() == 3 || this.getProcessingTime() <= this.getTotalTimeSpent()){
                System.out.println("desactivando");
                /*this.getPcb().setStatus("ready");
                this.interrupt();
                break;
            }
            if (this.getPcb().getPc()-1 == this.getInterruptAt()){
                System.out.println("bloqueado");
                try {
                    System.out.println("esperando io");
                    this.sleep(this.getIoCicles());
                    this.interrupt();
                    break;
                } 
                catch(InterruptedException e) {
                     // this part is executed when an exception (in this example InterruptedException) occurs
                     System.out.println("en catch" + e);
                }

            }*/

            try {
                Thread.sleep(1000); // pausa de 1 s
            } catch (InterruptedException e) {
                System.out.println("Hilo interrumpido");
                synchronized (this) {
                try {
                    // El hilo se pone en estado WAITING
                    this.wait(); 
                    timeSpent = 0;


                } catch (InterruptedException er) {
                    // Esto ocurre cuando el Scheduler llama a notify() o notifyAll()
                    // El hilo es despertado y puede salir del bucle while() o continuar.
                    Thread.currentThread().interrupt(); // Se mantiene el estado de interrupción
                }
            }
            }
        }
    }
    
    
    
    
    
    // proceso.start() ----> Empieza a ejecutar el proceso. Este método hará que se ejecute el run() dentro de la clase
    // ¿Cómo podemos simular la ejecución de un proceso? ¿Qué podemos meter en run?

    public int getIoCicles() {
        return ioCicles;
    }

    public void setIoCicles(int ioCicles) {
        this.ioCicles = ioCicles;
    }

    public int getSatisfyCicles() {
        return satisfyCicles;
    }

    public void setSatisfyCicles(int satisfyCicles) {
        this.satisfyCicles = satisfyCicles;
    }

    public int getInterruptAt() {
        return interruptAt;
    }

    public int getDeviceToUse() {
        return deviceToUse;
    }
    
    /**
     * @param timeSpent the timeSpent to set
     */
    public void setTimeSpent(int timeSpent) {
        this.timeSpent = timeSpent;
    }

    /**
     * @return the totalTimeSpent
     */
    public int getTotalTimeSpent() {
        return totalTimeSpent;
    }

    /**
     * @param totalTimeSpent the totalTimeSpent to set
     */
    public void setTotalTimeSpent(int totalTimeSpent) {
        this.totalTimeSpent = totalTimeSpent;
    }
    
    
}

/*

El syncronized es para manejar la sección crítica

class Recurso {
    public void usar() {
        synchronized (this) {
            System.out.println(Thread.currentThread().getName() + " está usando el recurso");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
    }
}

*/

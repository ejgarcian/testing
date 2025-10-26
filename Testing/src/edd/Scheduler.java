/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edd;

/**
 *
 * @author miche_ysmoa6e
 */
public class Scheduler {
    
    // Tomando que 1 ciclo de ejecución del CPU son 0.00001ms
    
    private Lista processList; //Lista en la cual se guardan todos los procesos a ejecutar.
    private int memoryAvaiable;
    private Lista deviceTable = new Lista();
    private int remainingSpace = memoryAvaiable;
                                     //Se agregan desde la interfaz

    public Scheduler(Lista processList, int memorySpace, Lista deviceTable) {
        this.processList = processList;
        this.memoryAvaiable = memorySpace;
        this.deviceTable = deviceTable;
        this.remainingSpace = memoryAvaiable;
    }
    
    
    // quantum medido en ms
    public void RoundRobin (int setQuantum, Cola readyQueue, Dispatcher dispatcher, Cola blockedQueue, Lista terminatedProcessList){
        int quantum = setQuantum;
         
        if (readyQueue.getCount() <= 0){
            return;
        }

        // Extraer el próximo PCB de la cola (FIFO)
        Object dequeued = readyQueue.dequeue();
        if (!(dequeued instanceof PCB)) {
            return;
        }
        PCB pcbOfActiveProcess = (PCB) dequeued;

        // activar si no está corriendo
        if (!"running".equals(pcbOfActiveProcess.getStatus())){
            dispatcher.activate(pcbOfActiveProcess, getProcessList()); // ready ---> running
        }
        
        Proceso toRun = dispatcher.getActiveProcess(getProcessList());
        if (toRun == null) return;

        // Ciclo de ejecución controlado por el scheduler (quantum y condiciones de I/O/terminación)
        while("running".equals(toRun.getPcb().getStatus())) {
            // honor interruption from the scheduler thread (UI changed planification)
            if (Thread.currentThread().isInterrupted()) return;

            // Si ya consumió su quantum o terminó -> desactivar y salir del tick
            if (toRun.getTimeSpent() >= quantum || toRun.getProcessingTime() <= toRun.getTotalTimeSpent()){
                dispatcher.deactivate(toRun);   // running --> ready
                break;
            }
            
            // Si debe bloquearse por I/O lo movemos a blocked
            if ("I/O Bound".equals(toRun.getBound()) && toRun.getPcb().getPc()-1 == toRun.getInterruptAt()){
                toRun.getPcb().setStatus("blocked");
                blockedQueue.enqueue(toRun.getPcb());
                try {
                    Thread.sleep((long)toRun.getIoCicles()*1000);
                    accessDevice(toRun, dispatcher, blockedQueue);
                } 
                catch(InterruptedException e) {
                     Thread.currentThread().interrupt();
                     return;
                }                
            }                
            try {
                Thread.sleep(1000);
            } 
            catch(InterruptedException e) {
                 Thread.currentThread().interrupt();
                 return;
            }
        }
        // gestionar resultado (terminado o reencolar)
        if (toRun.getProcessingTime() <= toRun.getTotalTimeSpent()) {
            toRun.getPcb().setStatus("terminated");
            // Wake up the thread if it's waiting so it can finish cleanly
            try {
                synchronized (toRun) { toRun.notify(); } 
            } catch (IllegalMonitorStateException ignored) {}
            try { toRun.interrupt(); } catch (SecurityException ignored) {}
            terminatedProcessList.add(toRun);
            // asegurarnos de que no quede referencia en ready
            readyQueue.removeValue(toRun.getPcb());
        } else {
            // Reinsertar al final de la cola
            readyQueue.enqueue(toRun.getPcb());
        }
    }
    
    
    public void SPN(Cola readyQueue, Dispatcher dispatcher, Cola blockedQueue, Lista terminatedProcessList){
        if (readyQueue.getCount() > 0){
            var processToActivate = readyQueue.get(0);
            if (!(processToActivate instanceof PCB)) return;
            PCB pcbOfActiveProcess = ((PCB)processToActivate);
            
            // verificar si el proceso ya está activado y si no lo está, activarlo
            if (!"running".equals(pcbOfActiveProcess.getStatus())){
                dispatcher.activate(pcbOfActiveProcess, getProcessList()); // ready ---> running
            }
            
            // while running
            Proceso toRun = dispatcher.getActiveProcess(processList);
            if (toRun == null) return;
            
            while(toRun != null && "running".equals(toRun.getPcb().getStatus())){
                if (Thread.currentThread().isInterrupted()) return;

                if (toRun.getProcessingTime() == toRun.getTimeSpent()){
                    dispatcher.deactivate(toRun);
                    readyQueue.dequeue();
                    break;
                }
                if ("I/O Bound".equals(toRun.getBound()) && toRun.getPcb().getPc()-1 == toRun.getInterruptAt()){
                    toRun.getPcb().setStatus("blocked");
                    blockedQueue.enqueue(toRun.getPcb());
                    try {
                        Thread.sleep(toRun.getIoCicles()*1000);
                        accessDevice(toRun, dispatcher, blockedQueue);
                    } 
                    catch(InterruptedException e) {
                         Thread.currentThread().interrupt();
                         return;
                    }
                }
                try {
                    Thread.sleep(1000);
                } 
                catch(InterruptedException e) {
                     Thread.currentThread().interrupt();
                     return;
                }
            }
            if (toRun.getProcessingTime() <= toRun.getTotalTimeSpent()) {
                toRun.getPcb().setStatus("terminated");
                terminatedProcessList.add(toRun);
            } else {
                readyQueue.enqueue(toRun.getPcb());
            }
        }
    }
    
    
    public void PriorityPlanification(int quantum, Cola readyQueue, Dispatcher dispatcher, Lista priorityList, Cola blockedQueue, Lista terminatedProcessList){
        // Recorremos las colas de prioridad en orden (0 = mayor prioridad)
        reorganicePriorityPlanification(readyQueue, priorityList);
        for (int i = 0; i < priorityList.count(); i++){
            // check interruption on top of loops
            if (Thread.currentThread().isInterrupted()) return;

            Object bucketObj = priorityList.get(i);
            if (!(bucketObj instanceof Cola)) continue;
            Cola act = (Cola) bucketObj;

            // Si la cola de ese nivel está vacía, saltar
            if (act.getCount() <= 0) continue;

            // obtener el siguiente PCB de ese bucket
            Object dequeued = act.dequeue();
            if (!(dequeued instanceof PCB)) continue;
            PCB pcbOfActiveProcess = (PCB) dequeued;

            // eliminar cualquier referencia residual en la cola global de listos
            readyQueue.removeValue(pcbOfActiveProcess);

            if (!"running".equals(pcbOfActiveProcess.getStatus())){
                dispatcher.activate(pcbOfActiveProcess, processList);
            }

            // mientras el proceso esté en running, controlar quantum / IO / terminación
            Proceso toRun = dispatcher.getActiveProcess(processList);
            if (toRun == null) continue;

            while("running".equals(toRun.getPcb().getStatus())) {
                if (Thread.currentThread().isInterrupted()) return;

                if (toRun.getTimeSpent() >= quantum || toRun.getProcessingTime() <= toRun.getTotalTimeSpent()){
                    dispatcher.deactivate(toRun);   // running --> ready
                    break;
                }
                if ("I/O Bound".equals(toRun.getBound()) && toRun.getPcb().getPc()-1 == toRun.getInterruptAt()){
                    toRun.getPcb().setStatus("blocked");
                    blockedQueue.enqueue(toRun.getPcb());
                    try {
                        Thread.sleep(toRun.getIoCicles()*1000);
                        accessDevice(toRun, dispatcher, blockedQueue);
                    } 
                    catch(InterruptedException e) {
                         Thread.currentThread().interrupt();
                         return;
                    }                
                }                

                try {
                    Thread.sleep(1000);
                } 
                catch(InterruptedException e) {
                     Thread.currentThread().interrupt();
                     return;
                }
            }

            if (toRun.getProcessingTime() <= toRun.getTotalTimeSpent()) {
                toRun.getPcb().setStatus("terminated");
                terminatedProcessList.add(toRun);
            } else {
                // reencolar en el mismo bucket y en la cola global de listos
                act.enqueue(toRun.getPcb());
                readyQueue.enqueue(toRun.getPcb());
            }
        }
    }
    
    
    public void Feedback(int setQuantum, Cola readyQueue, Lista readyQueueList, Dispatcher dispatcher, Cola blockedQueue, Lista terminatedProcessList) {
        System.out.println("sssssssswqdqwfqfqfqfqfq");
        int quantum = setQuantum;
        if (Thread.currentThread().isInterrupted()) return;

        PCB processToActivate = null;
        // Buscar el primer bucket no vacío (nivel de mayor prioridad)
        int i = 0;
        while (i < readyQueueList.count()){
            if (((Cola)readyQueueList.get(i)).getCount() > 0){
                System.out.println("activando");
                processToActivate = (PCB)(((Cola)readyQueueList.get(i)).dequeue());
                System.out.println("id del proceso" + processToActivate.getId());
                dispatcher.activate(processToActivate, processList);
                System.out.println("status del pta" + processToActivate.getStatus());
                readyQueue.getQueue().remove(readyQueue.getQueue().indexOf(processToActivate));
                break;
            }
            i++;
        }
        
        Proceso toRun = dispatcher.getActiveProcess(processList);
        if (toRun == null) return;
        if (Thread.currentThread().isInterrupted()) return;

        toRun.getPcb().setTimesIn(toRun.getPcb().getTimesIn()+1);
        while ("running".equals(toRun.getPcb().getStatus())) {
            if (Thread.currentThread().isInterrupted()) return;

            if (toRun.getTimeSpent() >= quantum || toRun.getProcessingTime() <= toRun.getTotalTimeSpent()){
                dispatcher.deactivate(toRun);   // running --> ready
                break;
            }
            if ("I/O Bound".equals(toRun.getBound()) && toRun.getPcb().getPc()-1 == toRun.getInterruptAt()){
                toRun.getPcb().setStatus("blocked");
                blockedQueue.enqueue(toRun.getPcb());
                try {
                    Thread.sleep(toRun.getIoCicles()*1000);
                    accessDevice(toRun, dispatcher, blockedQueue);
                } 
                catch(InterruptedException e) {
                     Thread.currentThread().interrupt();
                     return;
                }
            }
            try {
                Thread.sleep(1000);
            } 
            catch(InterruptedException e) {
                 Thread.currentThread().interrupt();
                 return;
            }
        }

        if (toRun.getProcessingTime() <= toRun.getTotalTimeSpent()) {
            toRun.getPcb().setStatus("terminated");
            terminatedProcessList.add(toRun);
        } else {
            // Demote / reenqueue according to timesIn
            int timesIn = toRun.getPcb().getTimesIn();
            if (timesIn < readyQueueList.count()) {
                Object obj = readyQueueList.get(timesIn);
                ((Cola) obj).enqueue(toRun.getPcb());
                readyQueue.enqueue(toRun.getPcb());
                
                /*
                if (obj instanceof Cola) {
                    ((Cola) obj).enqueue(toRun.getPcb());
                    readyQueue.enqueue(toRun.getPcb());
                } else {
                    Cola aux = new Cola();
                    readyQueueList.add(aux);
                    aux.enqueue(toRun.getPcb());
                    readyQueue.enqueue(toRun.getPcb());
                }*/
                
            } else {
                Cola aux = new Cola();
                readyQueueList.add(aux);
                System.out.println(timesIn);
                ((Cola) readyQueueList.get(timesIn)).enqueue(toRun.getPcb());
                readyQueue.enqueue(toRun.getPcb());
            }
        }
    }

    public void FSS(int setQuantum, Cola readyQueue, Dispatcher dispatcher, Cola blockedQueue, Lista terminatedProcessList) {
        int quantum = setQuantum;
        if (readyQueue.getCount() > 0) {
            
            var processToActivate = readyQueue.dequeue();
            dispatcher.activate(((PCB)(processToActivate)), getProcessList());
            
            Proceso toRun = dispatcher.getActiveProcess(getProcessList());
            if (toRun == null) return;

            while ("running".equals(toRun.getPcb().getStatus())){
                if (Thread.currentThread().isInterrupted()) return;

                if (toRun.getTimeSpent() >= quantum || toRun.getProcessingTime() <= toRun.getTotalTimeSpent()){
                    dispatcher.deactivate(toRun);
                    recalculateFSS(readyQueue, toRun.getPcb().getPriority());
                    reorganiceFSS(readyQueue);// running --> ready
                }
                if ("I/O Bound".equals(toRun.getBound()) && toRun.getPcb().getPc()-1 == toRun.getInterruptAt()){
                    toRun.getPcb().setStatus("blocked");
                    blockedQueue.enqueue(toRun.getPcb());
                    try {
                        Thread.sleep(toRun.getIoCicles()*1000);
                        accessDevice(toRun, dispatcher, blockedQueue);
                    } 
                    catch(InterruptedException e) {
                         Thread.currentThread().interrupt();
                         return;
                    }                
                }                
                try {
                    Thread.sleep(1000);
                } 
                catch(InterruptedException e) {
                     Thread.currentThread().interrupt();
                     return;
                }
            }
            if (toRun.getProcessingTime() <= toRun.getTotalTimeSpent()) {
                toRun.getPcb().setStatus("terminated");
                terminatedProcessList.add(toRun);
            } else {
                readyQueue.enqueue(toRun.getPcb());
            }
        }
    }
    
    public void SRT (Cola readyQueue, Dispatcher dispatcher, Cola blockedQueue, Lista terminatedProcessList) {
        // Shortest Remaining Time (preemptive approximation):
        // - Seleccionar el PCB con menor remaining = processingTime - totalTimeSpent
        // - Activarlo y durante su ejecución comprobar si aparece otro con remaining menor => preempt.
        if (readyQueue.getCount() <= 0) return;

        // Buscar índice del PCB con menor remaining
        int minIndex = -1;
        double minRemaining = Double.MAX_VALUE;
        for (int i = 0; i < readyQueue.getCount(); i++) {
            Object o = readyQueue.get(i);
            if (!(o instanceof PCB)) continue;
            PCB pcb = (PCB) o;
            Proceso p = findProcessByPCB(pcb);
            if (p == null) continue;
            double remaining = p.getProcessingTime() - p.getTotalTimeSpent();
            if (remaining < minRemaining) {
                minRemaining = remaining;
                minIndex = i;
            }
        }
        if (minIndex == -1) return;

        // Extraer el PCB elegido
        Object chosen = readyQueue.get(minIndex);
        if (!(chosen instanceof PCB)) return;
        PCB chosenPCB = (PCB) chosen;
        // Quitarlo de la cola global de ready
        readyQueue.removeValue(chosenPCB);

        // Activar
        dispatcher.activate(chosenPCB, getProcessList());
        Proceso toRun = dispatcher.getActiveProcess(getProcessList());
        if (toRun == null) return;

        while ("running".equals(toRun.getPcb().getStatus())) {
            if (Thread.currentThread().isInterrupted()) return;

            // cond de terminación
            if (toRun.getProcessingTime() <= toRun.getTotalTimeSpent()){
                dispatcher.deactivate(toRun);
                break;
            }

            // comprobar si existe preempción: algún PCB en ready con remaining < current remaining
            double currentRemaining = toRun.getProcessingTime() - toRun.getTotalTimeSpent();
            boolean shouldPreempt = false;
            for (int i = 0; i < readyQueue.getCount(); i++) {
                Object o = readyQueue.get(i);
                if (!(o instanceof PCB)) continue;
                PCB pcb = (PCB) o;
                Proceso p = findProcessByPCB(pcb);
                if (p == null) continue;
                double rem = p.getProcessingTime() - p.getTotalTimeSpent();
                if (rem < currentRemaining) {
                    shouldPreempt = true;
                    break;
                }
            }

            if (shouldPreempt) {
                // Preemptar y reencolar el proceso actual (su remaining ya actualizado por run())
                dispatcher.deactivate(toRun);
                readyQueue.enqueue(toRun.getPcb());
                break;
            }

            // I/O handling
            if ("I/O Bound".equals(toRun.getBound()) && toRun.getPcb().getPc()-1 == toRun.getInterruptAt()){
                toRun.getPcb().setStatus("blocked");
                blockedQueue.enqueue(toRun.getPcb());
                try {
                    Thread.sleep(toRun.getIoCicles()*1000);
                    accessDevice(toRun, dispatcher, blockedQueue);
                } 
                catch(InterruptedException e) {
                     Thread.currentThread().interrupt();
                     return;
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        // Tras salir, si termina, marcar; si no, asegurar que esté en ready
        if (toRun.getProcessingTime() <= toRun.getTotalTimeSpent()) {
            toRun.getPcb().setStatus("terminated");
            terminatedProcessList.add(toRun);
        } else {
            // si no se reencoló ya, encolar
            if (!readyQueue.getContains(toRun.getPcb())) {
                readyQueue.enqueue(toRun.getPcb());
            }
        }
    }
    
    //La lista de prioridades es una lista que contiene las prioridades
    public Lista reorganicePriorityPlanification(Cola readyQueue, Lista priorityList){
        
        // Creando la cantidad de colas necesarias segun las prioridades existentes
        if (priorityList.count() < 1){
            int maxpriority = getPriorities(readyQueue);
            
            for (int i = 0; i < maxpriority; i++){
                Cola aux = new Cola();
                priorityList.add(aux);
            }
        } 
        
        // Agregar cada proceso a su lista de prioridad correspondiente
        for (int j=0; j < readyQueue.getCount(); j++){
            Object obj = readyQueue.get(j);
            if (!(obj instanceof PCB)) continue;
            PCB aux = (PCB)obj;
            
            for (int x=0; x < priorityList.count(); x++){
                Cola act = (Cola)priorityList.get(x);
                
                // usar == para prioridad numérica está bien, pero evitar & bitwise
                if(aux.getPriority() == x && !act.getQueue().contains(aux)){
                    act.enqueue(aux);
                }
            }
        }
    
        return priorityList;
    }
    
    public void reorganiceSPN(Cola readyQueue){
        if (readyQueue == null) return;
        if (readyQueue.getQueue() == null) return;
        if (readyQueue.getCount() < 2) return;
        
        synchronized (readyQueue.getQueue()) {
            for (int pass = 0; pass < readyQueue.getCount() - 1; pass++) {
                boolean swapped = false;
                for (int i = 0; i < readyQueue.getCount() - 1 - pass; i++) {
                    Object o1 = readyQueue.getQueue().get(i);
                    Object o2 = readyQueue.getQueue().get(i + 1);

                    if (!(o1 instanceof PCB) || !(o2 instanceof PCB)) {
                        continue;
                    }

                    PCB pcb1 = (PCB) o1;
                    PCB pcb2 = (PCB) o2;
                    Proceso p1 = findProcessByPCB(pcb1);
                    Proceso p2 = findProcessByPCB(pcb2);

                    // If either Proceso is missing, skip this pair
                    if (p1 == null || p2 == null) continue;

                    // Compare processing time and swap underlying list if out of order
                    if (p1.getProcessingTime() > p2.getProcessingTime()) {
                        readyQueue.getQueue().swap(i, i + 1);
                        swapped = true;
                    }
                }
                if (!swapped) break; // already sorted
            }
        }
    }
    
    
    public void reorganiceFeedback (Cola readyQueue, Lista readyQueueList) {

        if (readyQueueList.count() < getTimesIn(readyQueue)){
            int maxTimesIn = getTimesIn(readyQueue);
            
            int i = readyQueueList.count();
            
            while (i < maxTimesIn) {
                Cola aux = new Cola();
                readyQueueList.add(aux);
                i++;
            }
        }
        int i = 0;
        while (i < readyQueue.getCount()) {
            int j = 0;
            int index = ((PCB)readyQueue.get(i)).getTimesIn();
            while (j < readyQueueList.count()){
                if ( index == j && !(((Cola)readyQueueList.get(j)).getContains(readyQueue.get(i)))) {
                    ((Cola)readyQueueList.get(j)).enqueue(readyQueue.get(i));
                }
                j++;
            }
            i++;
        }
    }
    
    public void reorganiceFSS (Cola readyQueue){
        int i = 0;
        int n = readyQueue.getCount();
        while (i < n){
            PCB aux = (PCB)readyQueue.get(i);
            int j = i + 1;
            
            while (j < n){
                PCB aux2 = (PCB)readyQueue.get(j);
                if (aux.getPriorityFSS()>aux2.getPriorityFSS()){
                    readyQueue.getQueue().swap(i, j);
                }
                j++;
            }
            i++;
        }
    }
    
    public void recalculateFSS (Cola readyQueue, int priority) {
        int i = 0;
        int n = readyQueue.getCount();
        int timesInTotal = 0;
        int priorityCount = 0;
        while (i < n) {
            if (((PCB)readyQueue.get(i)).getPriority() == priority) {
                timesInTotal = ((PCB)readyQueue.get(i)).getTimesIn() + timesInTotal;
                priorityCount++;
            }
            i++;
        }
        i = 0;
        while (i < n) {
            if (((PCB)readyQueue.get(i)).getPriority() == priority) {
                int priorityNode = ((PCB)readyQueue.get(i)).getPriority();
                int timesIn = ((PCB)readyQueue.get(i)).getTimesIn();
                
                float newPriorityFSS = priorityNode + timesIn + (timesInTotal/priorityCount);
                
                ((PCB)readyQueue.get(i)).setPriorityFSS(newPriorityFSS);
            }
            i++;
        }
    }
    
    
    public void reorganiceSRT (Cola readyQueue){
        int i = 0;
        int n = readyQueue.getCount();
        while (i < n){
            
            PCB aux = (PCB) readyQueue.get(i);

            int j = i + 1;
            
            while (j < n){
                PCB aux2 = (PCB)readyQueue.get(j);
                if (aux.getPriorityFSS() > aux2.getPriorityFSS()){

                    readyQueue.getQueue().swap(i, j);

                }
                j++;
            }
            i++;
        }
    }
    
    
    public int getPriorities(Cola readyQueue){
        Lista priorities = new Lista();
        
        for (int i = 0; i < readyQueue.getCount(); i++){
            PCB aux = (PCB)readyQueue.get(i);
            if (!priorities.contains(aux.getPriority())){
                priorities.add(aux.getPriority());
            }
        }
        
        return priorities.count();
    }

    public int getTimesIn(Cola readyQueue){
        Lista timesIn = new Lista();
        
        for (int i = 0; i < readyQueue.getCount(); i++){
            var aux = (PCB)readyQueue.get(i);
            
            if (!timesIn.contains(aux.getTimesIn())){
                timesIn.add(aux.getTimesIn());
            }
        }
        return timesIn.count();
    }
    
    /**
     * @return the processList
     */
    public Lista getProcessList() {
        return processList;
    }

    /**
     * @param processList the processList to set
     */
    public void setProcessList(Lista processList) {
        this.processList = processList;
    }

    /**
     * @return the memoryAvaiable
     */
    public int getMemoryAvaiable() {
        return memoryAvaiable;
    }

    /**
     * @param memoryAvaiable the memoryAvaiable to set
     */
    public void setMemoryAvaiable(int memoryAvaiable) {
        this.memoryAvaiable = memoryAvaiable;
    }

    /**
     * @return the remainingSpace
     */
    public int getRemainingSpace() {
        return remainingSpace;
    }

    /**
     * @param remainingSpace the remainingSpace to set
     */
    public void setRemainingSpace(int remainingSpace) {
        this.remainingSpace = remainingSpace;
    }
    
    
    public void accessDevice(Proceso blockedProcess, Dispatcher dispatcher, Cola blockedQueue){
        int i = 0;
        while (i < deviceTable.count()){
            if (Thread.currentThread().isInterrupted()) return;
            if (blockedProcess.getDeviceToUse() == ((Device)deviceTable.get(i)).getId()){
                try {
                    System.out.println("accediendo al device");
                    ((Device)deviceTable.get(i)).getSemaf().acquire();
                } catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                break;
            }
            i++;
        }
        try {
            blockedProcess.sleep(blockedProcess.getSatisfyCicles()*1000);
            ((Device)deviceTable.get(i)).getSemaf().release();
            dispatcher.deactivate(blockedProcess);
            blockedQueue.getQueue().remove(blockedQueue.getQueue().indexOf(blockedProcess.getPcb()));
        } 
        catch(InterruptedException e) {
             Thread.currentThread().interrupt();
             return;
        }
    }
    
     public Proceso findProcessByPCB(PCB pcb) {
        if (pcb == null) return null;

        int n = (processList == null) ? 0 : processList.count();
        for (int i = 0; i < n; i++) {
            Object o = processList.get(i);
            if (o instanceof Proceso) {
                Proceso p = (Proceso) o;
                PCB ppcb = p.getPcb();
                if (ppcb != null && ppcb.getId() == pcb.getId()) {
                    return p;
                }
            }
        }
        return null;
    }
    
}

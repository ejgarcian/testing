/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edd;

/**
 *
 * @author Eddy
 */
public class OS {

    private Lista processList = new Lista();
    private Lista processTable = new Lista();
    private Lista deviceTable = new Lista();
    private int memorySpace = 4000;
    private Scheduler scheduler;
    private Dispatcher dispatcher = new Dispatcher();
    private int remainingSpace = memorySpace;
    private Lista priorityList = new Lista();
    private Lista feedbackList = new Lista();
    private Cola readyQueue = new Cola();
    private Cola longTermQueue = new Cola();
    private Cola blockedQueue = new Cola();
    private Cola suspendedReadyQueue = new Cola();
    private Cola suspendedBlockedQueue = new Cola();
    private Lista terminatedProcessList = new Lista();
    private int currentPlanification = 0;
    private int quantum;

    
    
    public Cola fillReadyQueue(){
        // Llena readyQueue con los procesos cuyo PCB.status == "ready"
        // Corregido: usar equals en lugar de == y no incrementar i en else (evitaba elementos)
        for (int i = 0; i < getProcessList().count(); i++){
            Object obj = getProcessList().get(i);
            if (!(obj instanceof Proceso)) continue;
            Proceso proc = (Proceso) obj;
            PCB auxProcessPCB = proc.getPcb();
            if ("ready".equals(auxProcessPCB.getStatus())){
                getReadyQueue().enqueue(proc);
            }
        }
        return getReadyQueue();
    }

    public OS(int memorySpace, int quantum) {
        Device first = new Device(0);
        Device second = new Device(1);
        Device third = new Device(2);
        
        deviceTable.add(first);
        deviceTable.add(second);
        deviceTable.add(third);
        
        this.scheduler = new Scheduler(processList, memorySpace, deviceTable);
        this.quantum = quantum;
        
    }
    
    
    public boolean canBeReady(Proceso process){
        // compute potential remaining space without mutating state
        int potential = getRemainingSpace() - process.getMemorySpace();

        // allow exact fit (change to >0 if you prefer strictly positive)
        if (potential >= 0) {
            // commit the subtraction only when we accept the process
            this.setRemainingSpace(potential);
            process.getPcb().setStatus("ready");
            System.out.println("si");
            return true;
        } else {
            System.out.println("no");
            // do NOT change remainingSpace here
            return false;
        }
    }

    public void executePriorityPlanification() {
        scheduler.reorganicePriorityPlanification(readyQueue, priorityList);
        scheduler.PriorityPlanification(quantum, readyQueue, dispatcher, priorityList, blockedQueue, terminatedProcessList);
    }

    public void executeSPN() {
        scheduler.reorganiceSPN(readyQueue);
        scheduler.SPN(readyQueue, dispatcher, blockedQueue, terminatedProcessList);
    }

    public void executeFeedback() {
        scheduler.reorganiceFeedback(readyQueue, priorityList);
        scheduler.Feedback(quantum, readyQueue, feedbackList, dispatcher, blockedQueue, terminatedProcessList);
    }

    public void executeFSS() {
    // Build priority buckets first (this may add Cola objects to priorityList)
    scheduler.reorganicePriorityPlanification(readyQueue, priorityList);

    int priorities = scheduler.getPriorities(readyQueue);
    // iterate deterministically over existing priority buckets
        for (int i = 0; i < priorities; i++) {
            Object bucket = null;
            try {
                bucket = priorityList.get(i);
            } catch (Exception ex) {
                // defensive: if priorityList is shorter than expected, skip the bucket
                continue;
            }
            if (!(bucket instanceof Cola)) continue;
            Cola col = (Cola) bucket;
            if (col.getCount() == 0) continue;

            Object first = col.get(0);
            if (!(first instanceof PCB)) continue;
            int priority = ((PCB) first).getPriority();

            // recompute FSS metrics only for this priority
            scheduler.recalculateFSS(readyQueue, priority);
        }

        // reorder the ready queue using the computed FSS metric
        scheduler.reorganiceFSS(readyQueue);

        // run the FSS scheduling tick
        scheduler.FSS(quantum, readyQueue, dispatcher, blockedQueue, terminatedProcessList);
    }

    public void executeSRT() {
        scheduler.reorganiceSRT(readyQueue);
        scheduler.SRT(readyQueue, dispatcher, blockedQueue, terminatedProcessList);
    }
    
    public void executeRoundRobin(){
        System.out.println(terminatedProcessList);
        scheduler.RoundRobin(quantum, readyQueue, dispatcher, blockedQueue, terminatedProcessList);
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
     * @return the processTable
     */
    public Lista getProcessTable() {
        return processTable;
    }

    /**
     * @param processTable the processTable to set
     */
    public void setProcessTable(Lista processTable) {
        this.processTable = processTable;
    }

    /**
     * @return the deviceTable
     */
    public Lista getDeviceTable() {
        return deviceTable;
    }

    /**
     * @param deviceTable the deviceTable to set
     */
    public void setDeviceTable(Lista deviceTable) {
        this.deviceTable = deviceTable;
    }

    /**
     * @return the scheduler
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * @param scheduler the scheduler to set
     */
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * @return the dispatcher
     */
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * @param dispatcher the dispatcher to set
     */
    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * @return the memorySpace
     */
    public int getMemorySpace() {
        return memorySpace;
    }

    /**
     * @param memorySpace the memorySpace to set
     */
    public void setMemorySpace(int memorySpace) {
        this.memorySpace = memorySpace;
    }

    /**
     * @return the priorityList
     */
    public Lista getPriorityList() {
        return priorityList;
    }

    /**
     * @param priorityList the priorityList to set
     */
    public void setPriorityList(Lista priorityList) {
        this.priorityList = priorityList;
    }

    /**
     * @return the feedbackList
     */
    public Lista getFeedbackList() {
        return feedbackList;
    }

    /**
     * @param feedbackList the feedbackList to set
     */
    public void setFeedbackList(Lista feedbackList) {
        this.feedbackList = feedbackList;
    }

    /**
     * @return the readyQueue
     */
    public Cola getReadyQueue() {
        return readyQueue;
    }

    public int getRemainingSpace() {
        return remainingSpace;
    }

    public void setRemainingSpace(int remainingSpace) {
        this.remainingSpace = remainingSpace;
    }

    public void setReadyQueue(Cola readyQueue) {
        this.readyQueue = readyQueue;
    }

    public Cola getLongTermQueue() {
        return longTermQueue;
    }

    public void setLongTermQueue(Cola longTermQueue) {
        this.longTermQueue = longTermQueue;
    }


    public Cola getBlockedQueue() {
        return blockedQueue;
    }

    public void setBlockedQueue(Cola blockedQueue) {
        this.blockedQueue = blockedQueue;
    }

    public Cola getSuspendedReadyQueue() {
        return suspendedReadyQueue;
    }

    public void setSuspendedReadyQueue(Cola suspendedReadyQueue) {
        this.suspendedReadyQueue = suspendedReadyQueue;
    }

    public Cola getSuspendedBlockedQueue() {
        return suspendedBlockedQueue;
    }

    public void setSuspendedBlockedQueue(Cola suspendedBlockedQueue) {
        this.suspendedBlockedQueue = suspendedBlockedQueue;
    }

    public Lista getTerminatedProcessList() {
        return terminatedProcessList;
    }

    public void setTerminatedProcessList(Lista terminatedProcessList) {
        this.terminatedProcessList = terminatedProcessList;
    }

    public int getCurrentPlanification() {
        return currentPlanification;
    }

    public void setCurrentPlanification(int currentPlanification) {
        this.currentPlanification = currentPlanification;
    }
        
    /**
     * @return the quantum
     */
    public int getQuantum() {
        return quantum;
    }

    /**
     * @param quantum the quantum to set
     */
    public void setQuantum(int quantum) {
        this.quantum = quantum;
    }
    
}
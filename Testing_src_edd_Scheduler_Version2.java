public void FSS(int setQuantum, Cola readyQueue, Dispatcher dispatcher, Cola blockedQueue, Lista terminatedProcessList) {
    int quantum = setQuantum;
    if (readyQueue == null) return;
    if (readyQueue.getCount() <= 0) return;

    // Dequeue next PCB
    Object processToActivate = readyQueue.dequeue();
    if (processToActivate == null) return;

    // Normalize to PCB if needed
    if (!(processToActivate instanceof PCB)) {
        if (processToActivate instanceof Proceso) {
            processToActivate = ((Proceso) processToActivate).getPcb();
        } else {
            return;
        }
    }
    PCB pcb = (PCB) processToActivate;

    // Activate the corresponding Proceso (if not already running)
    if (!"running".equals(pcb.getStatus())) {
        dispatcher.activate(pcb, getProcessList());
    }

    Proceso toRun = dispatcher.getActiveProcess(getProcessList());
    if (toRun == null) return;

    // Run while process reports "running"
    while ("running".equals(toRun.getPcb().getStatus())) {
        if (Thread.currentThread().isInterrupted()) return;

        // Preemption or completion
        if (toRun.getTimeSpent() >= quantum || toRun.getProcessingTime() <= toRun.getTotalTimeSpent()) {
            dispatcher.deactivate(toRun); // running -> ready
            // Stop this quantum immediately: do not rely on status check only
            break;
        }

        // I/O handling
        if ("I/O Bound".equals(toRun.getBound()) && toRun.getPcb().getPc() - 1 == toRun.getInterruptAt()) {
            toRun.getPcb().setStatus("blocked");
            blockedQueue.enqueue(toRun.getPcb());
            try {
                Thread.sleep((long) toRun.getIoCicles() * 1000L); // simulate wait for device
                accessDevice(toRun, dispatcher, blockedQueue);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            // After initiating I/O, stop running in this tick
            break;
        }

        // Simulate CPU tick
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
    }

    // Check completion after running
    if (toRun.getProcessingTime() <= toRun.getTotalTimeSpent()) {
        toRun.getPcb().setStatus("terminated");

        // Wake the thread if it's waiting so it can finish and exit
        try {
            synchronized (toRun) {
                toRun.notify();
            }
        } catch (IllegalMonitorStateException ignored) {}

        try {
            toRun.interrupt();
        } catch (SecurityException ignored) {}

        // Add to terminated list (store Proceso or PCB depending on your design)
        terminatedProcessList.add(toRun);

        // Remove any lingering reference from readyQueue
        readyQueue.removeValue(toRun.getPcb());
    } else {
        // Not finished: requeue for future scheduling
        readyQueue.enqueue(toRun.getPcb());
    }

    // Notify listeners in case other code mutated internal Lista directly
    readyQueue.fireChange();
}
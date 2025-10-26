/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edd;

/**
 *
 * @author Eddy
 */
public class Dispatcher {

    public Dispatcher() {
    }
    
    public void activate(PCB pcb, Lista procesos) {
        int i = 0;
        Proceso procesoAux = null;
        while (i < procesos.count()){
            procesoAux = (Proceso)procesos.get(i);
            if (pcb.getId() == ((Proceso)procesos.get(i)).getPcb().getId()){
                procesoAux = (Proceso)procesos.get(i);
                break;
            } else {
                i++;
            } 
        }
        
        procesoAux.getPcb().setStatus("running");
        System.out.println("status setteado: " + procesoAux.getPcb().getStatus());
        System.out.println("id del proceso setteado: " + procesoAux.getPcb().getId());
        if (procesoAux.getTotalTimeSpent() > 0){
            synchronized (procesoAux) {
            // Notifica al hilo que estaba en wait() para que salga del bloqueo
            procesoAux.notify(); 
            }
        }else{
            procesoAux.start();
        }
    }
    
    public Proceso deactivate(Proceso activeProcess) {
        activeProcess.getPcb().setStatus("ready");
        activeProcess.interrupt();
        return activeProcess;
    }
    
    public Proceso getActiveProcess(Lista procesos){
        if (procesos == null) return null;
        int n = procesos.count();
        for (int i = 0; i < n; i++) {
            Object o = procesos.get(i);
            if (!(o instanceof Proceso)) continue;
                Proceso p = (Proceso) o;
                PCB pcb = p.getPcb();
                if (pcb == null) continue;
            // use .equals to compare string content (safer than ==)
            if ("running".equals(pcb.getStatus())) {
                return p;
            }
        }
        
        //System.out.println(procesos.printListProcess());
        return null;
    }
}

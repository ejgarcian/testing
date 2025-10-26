/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package json;

/**
 *
 * @author miche_ysmoa6e
 */
public class PData {
    
    private String name;
    private String bound; // CPU bound o I/O bound
    private int instructions;
    private int ioCicles; // Ciclos para la excepción (request de E/S)
    private int satisfyCicles; // Ciclos para completar la solicitud de excepción (manejo de E/S)
    private int deviceToUse; // Dispositivo a usar
    private int priority;

    public PData() {}

    public PData(String name, String bound, int instructions, int ioCicles, int satisfyCicles, int deviceToUse, int priority) {
        this.name = name;
        this.bound = bound;
        this.instructions = instructions;
        this.ioCicles = ioCicles;
        this.satisfyCicles = satisfyCicles;
        this.deviceToUse = deviceToUse;
        this.priority = priority;
    }

    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getDeviceToUse() {
        return deviceToUse;
    }

    public void setDeviceToUse(int deviceToUse) {
        this.deviceToUse = deviceToUse;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
    
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package json;

/**
 *
 * @author miche_ysmoa6e
 */
public class Config {
   
    private int cycleDurationMs;

    private PData[] processes;

    public Config() {}

    public Config(int cycleDurationMs, PData[] processes) {
        this.cycleDurationMs = cycleDurationMs;
        this.processes = processes;
    }

    // Getters y Setters
    public int getCycleDurationMs() {
        return cycleDurationMs;
    }

    public void setCycleDurationMs(int cycleDurationMs) {
        this.cycleDurationMs = cycleDurationMs;
    }

    // El getter y setter usan el array
    public PData[] getProcesses() {
        return processes;
    }

    public void setProcesses(PData[] processes) {
        this.processes = processes;
    }
}
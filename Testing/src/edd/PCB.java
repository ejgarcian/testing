/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edd;

/**
 *
 * @author Eddy
 */
public class PCB {
    private int id;
    private String status;
    private String name;
    private int pc;
    private int mar;
    private int priority;
    private int timesIn;
    private float priorityFSS;

    public PCB(int id, String name) {
        this.id = id;
        this.status = "new";
        this.name = name;
        this.pc = 0;
        this.mar = 0;
        this.timesIn = 0;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPc() {
        return pc;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public int getMar() {
        return mar;
    }

    public void setMar(int mar) {
        this.mar = mar;
    }

    public int getPriority() {
        return priority;
    }

    public int getTimesIn() {
        return timesIn;
    }

    public void setTimesIn(int timesIn) {
        this.timesIn = timesIn;
    }

    public float getPriorityFSS() {
        return priorityFSS;
    }

    public void setPriorityFSS(float priorityFSS) {
        this.priorityFSS = priorityFSS;
    }
        
    public void update(String status, int pc, int mar) {
        setStatus(status);
        setPc(pc);
        setMar(mar);
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    
}

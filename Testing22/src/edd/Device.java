/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edd;


import java.util.concurrent.Semaphore;

/**
 *
 * @author miche_ysmoa6e
 */
public class Device {
    private int id;
    private Semaphore semaf;

    public Device(int id) {
        this.id = id;
        this.semaf = new Semaphore(1);
    }

    public Device() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public Semaphore getSemaf() {
        return semaf;
    }
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    
    
}

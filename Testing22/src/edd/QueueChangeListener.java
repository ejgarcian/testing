
package edd;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author miche_ysmoa6e
 */
@FunctionalInterface
public interface QueueChangeListener {
    /**
     * Called when the observed Cola changes.
     *
     * @param queue the queue which changed (never null)
     */
    void queueChanged(Cola queue);
}
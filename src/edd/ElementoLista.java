/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edd;

/**
 * Esta clase define a un elemento de tipo ElementoLista. En ella se guardan la
 * posición de un elemento y su valor.
 *
 * @version 27/10/2024
 * @author Michelle García
 */
public class ElementoLista {

    private int index;
    private Object value;

    /**
     * Función que devuelve el valor del atributo índice.
     *
     * @return El valor actual de índice.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Procedimiento para cambiar el contenido del atributo index.
     *
     * @param index Valor nuevo del índice
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Función que devuelve el contenido del atributo value.
     *
     * @return El contenido actual de value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Procedimiento para cambiar el contenido del atributo value.
     *
     * @param value Objeto nuevo que se contendrá en value.
     */
    public void setValue(Object value) {
        this.value = value;
    }

}

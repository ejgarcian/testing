/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edd;

/**
 * Esta clase define un objeto de tipo lista. Contiene un contador de su
 * cantidad de elementos, una lista donde se almacenan los objetos siguienres y
 * un elemento de tipo ElementoLista.
 *
 * @version 27/10/2024
 * @author Michelle García
 */
public class Lista {

    private int count = 0;
    private Lista next;
    private ElementoLista value;

    /**
     * Procedimiento para agregar un objeto en la lista
     *
     * @param value1 Proceso
     */
    public void add(Object value1) {
        if (value1 != null) {
            ElementoLista newValue = new ElementoLista();
            newValue.setValue(value1);
            newValue.setIndex(count);

            if (this.value == null) {
                this.value = newValue;
            } else {
                Lista actual = this;
                while (actual.next != null) {
                    actual = actual.next;
                }
                actual.next = new Lista();
                actual.next.value = newValue;
            }

            count++;
        }
    }


    /**
     * Función para obtener un elemento según su índice dentro de la lista
     *
     * @param index Índice del elemento a retornar
     * @return Elemento encontrado según su índice (o null si fuera de rango)
     */
    public Object get(int index) {
        if (index < 0) return null;
        Lista current = this;
        int i = 0;
        // Walk nodes until we find the index or run out of nodes
        while (current != null && current.value != null) {
            if (i == index) {
                return current.value.getValue();
            }
            current = current.next;
            i++;
        }
        // index out of range
        return null;
    }

    /**
     * Procedimiento para eliminar un elemento de la lista según su índice
     *
     * @param index Indice del elemento a eliminar
     */
    public void remove(int index) {
        if (index >= 0 && index < count()) {
            if (index == 0) {

                if (this.next == null) {
                    this.value = null;
                } else {
                    this.value = this.next.value;
                    this.next = this.next.next;
                }
            } else {
                Lista actual = this;
                for (int i = 0; i < index - 1; i++) {
                    actual = actual.next;
                }


                actual.next = actual.next.next;
            }


            Lista temp = this;
            int indiceActual = 0;
            while (temp != null) {
                if (temp.value != null) {
                    temp.value.setIndex(indiceActual);
                    indiceActual++;
                }
                temp = temp.next;
            }

            count--;
        }
    }

    /**
     * Función para contar los elementos dentro de la lista
     *
     * @return Cantidad de elementos de la lista
     */
    public int count() {
        // Iterative, defensive count that walks nodes with non-null ElementoLista.value
        int cnt = 0;
        Lista current = this;
        while (current != null && current.value != null) {
            cnt++;
            current = current.next;
        }
        // Keep the internal count field in sync (defensive)
        this.count = cnt;
        return cnt;
    }

    /**
     * Procedimiento para agregar los elementos de una lista externa a la lista
     * actual
     *
     * @param h Lista externa
     */
    public void addRange(Lista h) {

        if (h.count() > 0) {
            Lista actual = h;
            while (actual != null && actual.value != null) {

                if (!this.contains(actual.value.getValue())) {
                    this.add(actual.value.getValue());
                }
                actual = actual.next;
            }
        }
    }

    
    
    public String printListProcess() {
        String txt = "";
        
        for (int i = 0; i < count(); i++) {
            Object o = get(i);
            if (o instanceof Proceso proceso) {
                txt = txt + proceso.getPcb().getName() + "\n"
                        + proceso.getPcb().getStatus() + "\n";
            }
        }
        return txt;
    }
    
    
    
    /**
     * Función para verificar si un objeto se encuentra dentro de la lista.
     *
     * @param value Objeto a encontrar
     * @return Si el objeto se encuentra adentro, true. En caso contrario,
     * false.
     */
    public boolean contains(Object value) {
        if (value == null) return false;
        Lista current = this;
        while (current != null && current.value != null) {
            Object v = current.value.getValue();
            if (v == value || (v != null && v.equals(value))) return true;
            current = current.next;
        }
        return false;
    }

 /**
    /*
    /**
     
    Función para obtener el índice de un elemento dentro de la lista.*
    @param value Objeto a encontrar.
    @return Posición correspondiente al elemento en la lista.*/
    public int indexOf(Object value) {
        if (value == null) return -1;
        Lista current = this;
        while (current != null && current.value != null) {
            Object aux = current.value.getValue();
            if ((value instanceof PCB) && (aux instanceof PCB) && ((PCB)aux).getId() == ((PCB)value).getId()) {
                return current.value.getIndex();
            } else if ((value instanceof Device) && (aux instanceof Device) && ((Device)aux).getId() == ((Device)value).getId()){
                return current.value.getIndex();
            } else if ((value instanceof Proceso) && (aux instanceof Proceso) && ((Proceso)aux).getPcb().getId() == ((Proceso)value).getPcb().getId()) {
                return current.value.getIndex();
            } else if (aux == value || (aux != null && aux.equals(value))) {
                return current.value.getIndex();
            }
            current = current.next;
        }
        return -1;
    }
    
    private Lista getNodeAt(int index) {
        if (index < 0) return null;
        Lista current = this;
        int i = 0;
        while (current != null && current.value != null && i < index) {
            current = current.next;
            i++;
        }
        return current;
    }

    /**
     * Intercambia los valores almacenados en los nodos de índice i y j.
     * Actualiza los índices de ElementoLista para mantener consistencia.
     *
     * @param i índice del primer elemento
     * @param j índice del segundo elemento
     */
    public void swap(int i, int j) {
        if (i == j) return; // nada que hacer
        if (i < 0 || j < 0) return;
        if (i >= count() || j >= count()) return;

        // asegurar que i < j para simplificar
        if (i > j) {
            int tmp = i;
            i = j;
            j = tmp;
        }
        

        Lista nodeI = getNodeAt(i);
        Lista nodeJ = getNodeAt(j);

        if (nodeI == null || nodeJ == null) return;

        // intercambio simple de los valores
        ElementoLista tempValue = nodeI.value;
        nodeI.value = nodeJ.value;
        nodeJ.value = tempValue;

        // recomputar índices (mantener igual comportamiento que remove)
        recomputeIndices();
    }

    /**
     * Recalcula los índices (ElementoLista.index) a partir del inicio de la lista.
     * Llamar esto después de operaciones que cambien el orden o contenido.
     */
    private void recomputeIndices() {
        Lista temp = this;
        int idx = 0;
        while (temp != null) {
            if (temp.value != null) {
                temp.value.setIndex(idx);
                idx++;
            }
            temp = temp.next;
        }
        // update count to match number of non-null values (defensive)
        this.count = idx;
    }
}

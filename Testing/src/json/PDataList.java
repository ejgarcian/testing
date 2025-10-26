/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package json;

/**
 *
 * @author miche_ysmoa6e
 */
public class PDataList {
    private PData[] data;
    private int size;

    public PDataList(int capacity) {
        data = new PData[capacity];
        size = 0;
    }

    public void add(PData item) {
        if (size >= data.length) {
            resize();
        }
        data[size++] = item;
    }

    private void resize() {
        PData[] newData = new PData[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            newData[i] = data[i];
        }
        data = newData;
    }

    public PData get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
        return data[index];
    }

    public int size() {
        return size;
    }

    public PData[] toArray() {
        PData[] result = new PData[size];
        for (int i = 0; i < size; i++) {
            result[i] = data[i];
        }
        return result;
    }
    
    
    
}

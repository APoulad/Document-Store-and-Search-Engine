package edu.yu.cs.com1320.project.impl;


import edu.yu.cs.com1320.project.MinHeap;

import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {
    public MinHeapImpl(){
        this.elements = (E[]) new Comparable[1];
    }
    @Override
    public void reHeapify(E element) {
        int index = getArrayIndex(element);
        if(index == -1) throw new NoSuchElementException();
        downHeap(index);
        upHeap(index);
    }

    @Override
    protected int getArrayIndex(E element) {
        int index = -1;
        for(int i=1; i<elements.length; i++){
            if(element.equals(elements[i])){
                index=i;
                break;
            }
        }
        return index;
    }

    @Override
    protected void doubleArraySize() {
        E[] temp = (E[]) new Comparable[this.elements.length*2];
        System.arraycopy(elements, 0, temp, 0, elements.length);
        elements=temp;
    }
}

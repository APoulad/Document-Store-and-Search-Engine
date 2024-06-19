package edu.yu.cs.com1320.project.impl;

public class StackImpl<T> implements edu.yu.cs.com1320.project.Stack<T>{

    private class Node<T>{
        private Node<T> next;
        private T value;
        public Node(T t){
            this.next = null;
            this.value = t;
        }
        public Node<T> getNext(){
            return next;
        }
        public void setNext(Node<T> v){
            this.next = v;
        }
        public T getValue(){
            return this.value;
        }
    }
    private int size;
    private Node<T> top;
    public StackImpl(){
        this.top=null;
        this.size=0;
    }
    @Override
    public void push(T element) {
        Node<T> bob = new Node<>(element);
        bob.setNext(this.top);
        this.top = bob;
        this.size++;
    }

    @Override
    public T pop() {
        if(this.size==0){
            return null;
        }
        T bob = this.top.getValue();
        this.top = this.top.getNext();
        this.size--;
        return bob;
    }

    @Override
    public T peek() {
        if(this.size==0){
            return null;
        }
        return this.top.getValue();
    }

    @Override
    public int size() {
        return this.size;
    }
}

package edu.yu.cs.com1320.project.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

public class TrieImpl<Value> implements edu.yu.cs.com1320.project.Trie<Value>{
    private int alphabetSize = 62; // 0-9, A-Z, a-z
    private Node<Value> root; // root of trie
    public TrieImpl(){
        this.root = new Node<>();
    }
    private class Node<Value>{
        protected Set<Value> val = new HashSet<>();
        protected Node[] links = new Node[alphabetSize];
    }
    @Override
    public void put(String key, Value val){
        if(key==null) throw new NullPointerException("Null key");
        if(val==null) return;
        key = stripper(key);
        this.root = put(this.root, key, val, 0);
    }
    private String stripper(String key){
        String string = key.replaceAll("[^a-zA-Z0-9]", "").strip();
        return string;
    }
    private Node put(Node x, String key, Value val, int d){
        //create a new node
        if(x == null) x = new Node();
        //we've reached the last node in the key,
        //set the value for the key and return the node
        if (d == key.length()){
            x.val.add(val);
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        int c = getPos(key.charAt(d));
        x.links[c] = this.put(x.links[c], key, val, d + 1);
        return x;
    }
    private int getPos(char c){
        if((48 <= c) && (c <= 57)) return c-48;
        if((65 <= c) && (c <= 90)) return c-55;
        if((97 <= c) && (c <= 122)) return c-61;
        return -1;
    }
    private boolean hasIllegalChars(String s){
        for (int i = 0; i < s.length(); i++) {
            if(getPos(s.charAt(i))==-1) return true;
        }
        return false;
    }
    @Override
    public Set<Value> get(String key) {
        if(hasIllegalChars(key)) return new HashSet<>();
        return get(this.root, (key), 0);
    }
    private Set<Value> get(Node x, String key, int d){
        if(x==null) return new HashSet<>();
        if(d==key.length()) return x.val;
        int c = getPos(key.charAt(d));
        return get(x.links[c], key, d+1);
    }
    private Node getNode(Node x, String key, int d){
        if(x==null) return null;
        if(d==key.length()) return x;
        int c = getPos(key.charAt(d));
        return getNode(x.links[c], key, d+1);
    }
    @Override
    public List<Value> getSorted(String key, Comparator<Value> comparator) {
        List<Value> allVals = new ArrayList<>(get((key)));
        allVals.sort(comparator);
        return allVals;
    }
    private Set<Value> getValForPrefix(String prefix){
        Set<Value> valueSet = new HashSet<>();
        if(hasIllegalChars(prefix)) return valueSet;
        collect(getNode(this.root, prefix, 0), valueSet);
        return valueSet;
    }
    private void collect(Node x, Set<Value> q){
        if(x==null) return;
        if(!x.val.isEmpty()) q.addAll(x.val);
        for (int c = 0; c < alphabetSize; c++) {
            collect(x.links[c], q);
        }
    }
    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
        List<Value> values = new ArrayList<>(getValForPrefix((prefix)));
        values.sort(comparator);
        return values;
    }
    @Override
    public Value delete(String key, Value val) {
        if(hasIllegalChars(key)) return null;
        Node x = getNode(this.root, (key), 0);
        if(x==null || !x.val.contains(val)) return null;
        x.val.remove(val);
        if(x.val.isEmpty()) delete(this.root, (key), 0);
        return val;
    }
    @Override
    public Set<Value> deleteAll(String key) {
        if(hasIllegalChars(key)) return null;
        Node x = getNode(this.root, (key), 0);
        if(x==null) return new HashSet<>();
        Set<Value> values = new HashSet<>(x.val);
        //x.val.clear();
        delete(this.root, (key), 0);
        return values;
    }
    private Node delete(Node x, String key, int d){
        if(x==null) return null;
        if(d==key.length()){
            x.val.clear();
        }else{
            int c = getPos(key.charAt(d));
            x.links[c]= this.delete(x.links[c], key, d+1);
        }
        if(!x.val.isEmpty()) return x;
        for (int c = 0; c < alphabetSize; c++) {
            //if there are nodes below that exist, don't cut this one.
            if(x.links[c]!=null) return x;
        }
        return null;
    }
//    private Node deleteAllBelow(Node x, Set<Value> q){
//        if(x==null) return null;
//        if(!x.val.isEmpty()){
//            q.addAll(x.val);
//            x.val.clear();
//        }
//        for (int c = 0; c < alphabetSize; c++) {
//            deleteAllBelow(x.links[c], q);
//        }
//    }
//    private class LQ<T>{
//        private class LinearNode<T> {
//            private LinearNode<T> next;
//            private T element;
//
//            public LinearNode() {
//                next = null;
//                element = null;
//            }
//
//            public LinearNode(T elem) {
//                next = null;
//                element = elem;
//            }
//
//            public LinearNode<T> getNext() {
//                return next;
//            }
//
//            public void setNext(LinearNode<T> next) {
//                this.next = next;
//            }
//
//            public T getElement() {
//                return element;
//            }
//
//            public void setElement(T element) {
//                this.element = element;
//            }
//        }
//        private int count;
//        private LinearNode<T> head, tail;
//        public LQ(){
//            count=0;
//            head=tail=null;
//        }
//        public void add(T t){
//            LinearNode<T> n = new LinearNode<T>(t);
//            if(count==0) head = n;
//            else tail.setNext(n);
//            tail=n;
//            count++;
//        }
//        public T take(){
//            if(count==0) return null;
//            T returned = head.getElement();
//            head=head.getNext();
//            count--;
//            if(count==0) tail=null;
//            return returned;
//        }
//        public boolean isEmpty(){return this.count!=0;}
//    }
//    private void breadthFirst(Node n){
//        LQ<Node> q = new LQ<>();
//        q.add(n);
//        while(!q.isEmpty()) {
//            Node current = q.take();
//            //System.out.println("Visited node: "+ current.getData());
//            //for(TreeNode<T> child : current.getChildren()) {
//            for (Node child : current.links) {
//                if(child!=null) q.add(child);
//
//            }
//        }
//    }
    @Override
    public Set<Value> deleteAllWithPrefix(String prefix) {
        String prfx = (prefix);
        Set<Value> valueSet = getValForPrefix(prfx);
//        Node before = getNode(this.root, prfx.substring(0, prfx.length()-1), 0);
//        before.links[getPos(prfx.charAt(prfx.length()-1))] = null;
        deleteBelow(getNode(this.root, prfx, 0));
        delete(this.root, prfx, 0);
        return valueSet;
    }
    private void deleteBelow(Node x){
        if(x==null) return;
        x.val.clear();
        for (int c = 0; c < alphabetSize; c++) {
            deleteBelow(x.links[c]);
        }
    }
}

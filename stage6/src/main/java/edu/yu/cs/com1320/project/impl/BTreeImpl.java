package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage6.PersistenceManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value> {
    private static final int MAX = 4;
    private Node root; //root of the B-tree
    //private Node leftMostExternalNode;
    private int height; //height of the B-tree
    private int size; //number of key-value pairs in the B-tree
    private static final class Node{
        private int entryCount; // number of entries
        private Entry[] entries =  new Entry[MAX]; // the array of children
        private Node next;
        private Node previous;
        // create a node with k entries
        private Node(int entryCount){
            this.entryCount = entryCount;
        }
        private void setNext(Node next){
            this.next = next;
        }
        private Node getNext(){
            return this.next;
        }
        private void setPrevious(Node previous){
            this.previous = previous;
        }
        private Node getPrevious(){
            return this.previous;
        }
        private Entry[] getEntries(){
            return Arrays.copyOf(this.entries, this.entryCount);
        }

    }
    //internal nodes: only use key and child
    //external nodes: only use key and value
    private static class Entry{
        private Comparable key;
        private Object val;
        private Node child;

        public Entry(Comparable key, Object val, Node child){
            this.key = key;
            this.val = val;
            this.child = child;
        }
        public Object getValue(){
            return this.val;
        }
        public Comparable getKey(){
            return this.key;
        }
    }
    private PersistenceManager<Key, Value> persistenceManager;
    public BTreeImpl(){//piazza
        this.root=new Node(0);
    }
    @Override
    public Value get(Key k){
        if(k==null) throw new IllegalArgumentException("Null Key");
        Entry e = this.get(this.root,k, this.height);
        if(e==null) return null;
        if(e.val==null){//in disk
            try {
                e.val = persistenceManager.deserialize(k);
                persistenceManager.delete(k);
            }catch (FileNotFoundException ff){

            }
            catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return (Value) e.val;
    }
    private Entry get(Node currentNode, Key key, int height){
        Entry[] entries = currentNode.entries;
        if(height==0){
            for(int j = 0; j < currentNode.entryCount; j++){
               if(isEqual(key, entries[j].key)) return entries[j];
            }
            //key isn't here
            return null;
        }else{
            for(int j = 0; j < currentNode.entryCount; j++){
                if(j + 1 == currentNode.entryCount || less(key, entries[j + 1].key)){
                    return this.get(entries[j].child, key, height - 1);
                }
            }
            return null;
        }
    }
    private boolean less(Comparable k1, Comparable k2){
        return k1.compareTo(k2) < 0;
    }
    private boolean isEqual(Comparable k1, Comparable k2){
        return k1.compareTo(k2) ==0;
    }

    @Override
    public Value put(Key k, Value v) {
        if(k==null)
            throw new IllegalArgumentException("null key");
        Entry alreadyThere = this.get(this.root, k, this.height);
        if(alreadyThere!=null){
            Object toRet= alreadyThere.val;
            if(toRet==null){//in disk
                try {
                    toRet = persistenceManager.deserialize(k);
                    persistenceManager.delete(k);
                }catch (FileNotFoundException ff){

                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            alreadyThere.val=v;
            return (Value) toRet;
        }
        Node newNode = this.put(this.root, k, v, this.height);
        this.size++;
        if(newNode==null)
            return null;
        Node newRoot = new Node(2);
        newRoot.entries[0] = new Entry(this.root.entries[0].key, null, this.root);
        newRoot.entries[1] = new Entry(newNode.entries[0].key, null, newNode);
        this.height++;
        this.root=newRoot;
        return null;
    }
    private Node put(Node currentNode, Key k, Value v, int height){
        int j;
        Entry newEntry = new Entry(k,v,null);
        if(height==0){//external
            for (j = 0; j < currentNode.entryCount; j++) {
                if(less(k, currentNode.entries[j].key))
                    break;
            }
        }else{//internal
            for (j = 0; j < currentNode.entryCount; j++) {
                if((j+1==currentNode.entryCount)||less(k,currentNode.entries[j+1].key)){
                    Node newNode = this.put(currentNode.entries[j++].child, k, v, height-1);
                    if(newNode==null)
                        return null;
                    newEntry.key=newNode.entries[0].key;
                    newEntry.val=null;
                    newEntry.child=newNode;
                    break;
                }
            }
        }
        for(int i = currentNode.entryCount; i>j; i--)
            currentNode.entries[i]=currentNode.entries[i-1];
        currentNode.entries[j]=newEntry;
        currentNode.entryCount++;
        if(currentNode.entryCount<MAX)
            return null;
        else return this.split(currentNode, height);
    }
    private Node split(Node currentNode, int height){
        Node newNode = new Node(MAX/2);
        currentNode.entryCount=MAX/2;
        for (int j = 0; j < MAX/2; j++)
            newNode.entries[j]=currentNode.entries[MAX/2+j];
        if(height==0){
            newNode.setNext(currentNode.getNext());
            newNode.setPrevious(currentNode);
            currentNode.setNext(newNode);
        }
        return newNode;
    }

    @Override
    public void moveToDisk(Key k) throws IOException {
        if(this.persistenceManager==null)
            throw new IllegalStateException("no PM");
        Value toMove= this.get(k);
        if(toMove==null)
            return;
        persistenceManager.serialize(k,toMove);
        this.put(k, null);
    }
    @Override
    public void setPersistenceManager(PersistenceManager<Key, Value> pm) {
        this.persistenceManager=pm;
    }
}

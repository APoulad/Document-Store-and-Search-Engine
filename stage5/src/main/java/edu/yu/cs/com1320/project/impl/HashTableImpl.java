package edu.yu.cs.com1320.project.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
public class HashTableImpl<Key, Value> implements edu.yu.cs.com1320.project.HashTable<Key, Value>{
    private class LinkedList<Key, Value> {
        private Node first;

        private class Node {
            Key k;
            Value v;
            Node next;

            public Node(Key key, Value val, Node next) {
                this.k = key;
                this.v = val;
                this.next = next;
            }
        }

        public Value put(Key k, Value v) {
            if(k==null){
                throw new IllegalArgumentException("Null key");
            }
            if(v==null){
                delete(k);
            }
            for (Node i = first; i != null; i = i.next) {
                if (k.equals(i.k)) {
                    Value old = i.v;
                    i.v = v;
                    return old;
                }
            }
            first = new Node(k, v, first);
            //size++;
            return null;
        }

        public Value get(Key k) {
            for (Node i = first; i != null; i = i.next) {
                if (k.equals(i.k)) {
                    return i.v;
                }
            }
            return null;
        }
        public Value delete(Key k){
            Value theReturn = get(k);
            Node current = this.first;
            while(current.next!=null && current.next.k!=k){
                current=current.next;
            }
            if(current.next!=null){
                current.next=current.next.next;
            }
            return theReturn;
        }

        public Value[] getAllValues(){
            int j=0;
            for(Node i = first; i!=null; i=i.next){
                j++;
            }
            Object[] vals = new Object[j];
            j=0;
            for(Node i = first; i!=null; i=i.next){
                vals[j++] = i.v;
            }
            return (Value[]) vals;
        }
        public Key[] getAllKeys(){
            int j=0;
            for(Node i = first; i!=null; i=i.next){
                j++;
            }
            Object[] keys = new Object[j];
            j=0;
            for(Node i = first; i!=null; i=i.next){
                keys[j++] = i.k;
            }
            return (Key[]) keys;
        }
    }
    private LinkedList<Key, Value>[] ll;
    private int size;
    public HashTableImpl(){
        ll=(LinkedList<Key, Value>[]) new LinkedList[5];
        for(int i=0; i<5; i++) ll[i] = new LinkedList<>();
    }
    private int hashFunction(Key key){
        return (key.hashCode() & 0x7fffffff) % this.ll.length;
    }
    private int hashFunction(Key key, int l){
        return (key.hashCode() & 0x7fffffff) % l;
    }
    @Override
    public Value get(Key k) {
        return this.ll[hashFunction(k)].get(k);
    }
    public Value put(Key k, Value v){
        if(k==null){
            throw new IllegalArgumentException("Null Key");
        }
        if(this.ll.length<(this.size/8)){
            resize(this.ll.length*2);
        }
        if(v==null && this.containsKey(k)){
            size--;
//            if(0<this.ll.length && this.ll.length<=2*this.size){
//                resize(this.ll.length/2);
//            }
        }else if(v!=null && !(this.containsKey(k))){
            size++;
        }
        return this.ll[hashFunction(k)].put(k, v);
    }
    public boolean containsKey(Key k) {
        if(k==null){
            throw new NullPointerException("null key");
        }
        return get(k)!=null;
    }
    public int size() {
        return this.size;
    }
    public Set<Key> keySet(){
        Set<Key> keySet = new HashSet<>();
        for(int i=0; i<ll.length; i++){
            Key[] keys = ll[i].getAllKeys();
            Collections.addAll(keySet, keys);
        }
        return Collections.unmodifiableSet(keySet);
    }
    public Collection<Value> values(){
        Collection<Value> valueList = new ArrayList<>();
        for(int i=0; i<ll.length; i++){
            Value[] values = ll[i].getAllValues();
            Collections.addAll(valueList, values);
        }
        return Collections.unmodifiableCollection(valueList);
    }
//    private void resize(int size){
//        LinkedList<Key, Value>[] oldArr = this.ll;
//        this.ll = (LinkedList<Key, Value>[]) new LinkedList[size];
//        //LinkedList<Key, Value>[] newArr = (LinkedList<Key, Value>[]) new LinkedList[size];
//        for(int i=0; i<5; i++) this.ll[i] = new LinkedList<>();
//        for(int i=0; i<oldArr.length; i++){
//            Key[] keys = oldArr[i].getAllKeys();
//            for(int j=0; j<keys.length; j++){
//                this.ll[hashFunction(keys[j])].put(keys[j], oldArr[i].get(keys[j]));
//            }
//        }
//    }
    private void resize(int size){
        LinkedList<Key,Value>[] newArr = new LinkedList[size];
        for(int i=0; i<size; i++) newArr[i] = new LinkedList<>();
        for(int i=0; i<this.ll.length; i++){
            Key[] keys = this.ll[i].getAllKeys();
            //Value[] values = new Value[keys.length];
            for(int j=0; j<keys.length; j++){
                //values[j] = this.ll[i].get(keys[j]);
                newArr[hashFunction(keys[j],size)].put(keys[j], this.ll[i].get(keys[j]));
            }
        }
        this.ll = newArr;
    }
}

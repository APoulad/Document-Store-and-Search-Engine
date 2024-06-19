package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage5.Document;

import java.net.URI;
import java.util.Arrays;
import java.util.Set;

import static java.lang.System.nanoTime;

public class DocumentImpl implements Document {
    private HashTableImpl<String, String> myMap;
    private HashTableImpl<String, Integer> words;
    private String sdata;
    private byte[] data;
    private URI uri;
    long time;
    public DocumentImpl(URI uri, String txt){
        if(uri==null || txt==null || txt.isEmpty() || uri.toString().isEmpty()){
            throw new IllegalArgumentException("no blanks or nulls");
        }
        this.myMap = new HashTableImpl<>();
        this.sdata = txt;
        this.uri = uri;
        this.time = nanoTime();
        //this.data = txt.getBytes();
        this.words = new HashTableImpl<>();
        startHT(txt);
    }
    private void startHT(String text){
        String ntext = text.replaceAll("[^a-zA-Z0-9 ]", "");
        String[] allText = ntext.split(" ");
        for(String txt : allText){
            if(!this.words.containsKey(txt)){
                this.words.put(txt, 1);
            } else{
              this.words.put(txt, this.words.get(txt)+1);
            }
        }
    }

    @Override
    public Set<String> getWords() {
        //this.time = nanoTime();
        return this.words.keySet();
    }

    @Override
    public long getLastUseTime() {
        return this.time;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        this.time = timeInNanoseconds;
    }

    @Override
    public int wordCount(String word) {
        //this.time = nanoTime();
        Integer integer = this.words.get(word);
        return integer == null ? 0 : integer;
    }

    public DocumentImpl(URI uri, byte[] binaryData){
        if(uri==null || binaryData==null || binaryData.length==0 || uri.toString().isEmpty()){
            throw new IllegalArgumentException("no blanks or nulls");
        }
        this.myMap = new HashTableImpl<>();
        this.data = binaryData;
        this.uri = uri;
        this.time = nanoTime();
    }
    public String setMetadataValue(String key, String value){
        if(key==null || key.isEmpty()){
            throw new IllegalArgumentException("Key cannot be null or blank");
        }
        //String oldKey = this.getMetadataValue(key);
        //this.time = nanoTime();
        return this.myMap.put(key, value);
        //return oldKey;
    }
    public String getMetadataValue(String key){
        if(key==null || key.isEmpty()){
            throw new IllegalArgumentException("Key cannot be null or blank");
        }
        //this.time = nanoTime();
        return this.myMap.get(key);
    }

    public HashTable<String, String> getMetadata() {
        //this.time = nanoTime();
        HashTable<String,String> newMap = new HashTableImpl<>();
        for(String key : this.myMap.keySet()){
            newMap.put(key, this.myMap.get(key));
        }
        return newMap;
        //return (HashTable<String, String>) this.myMap.clone();
    }

    public String getDocumentTxt(){
        //this.time = nanoTime();
        return this.sdata;
    }
    public URI getKey(){
        //this.time = nanoTime();
        return this.uri;
    }
    public byte[] getDocumentBinaryData() {
        //this.time = nanoTime();
        return this.data;
    }
    @Override
    public int hashCode() {
        int result = this.uri.hashCode();
        String text = this.getDocumentTxt();
        byte[] bites = this.getDocumentBinaryData();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(bites);
        return Math.abs(result);
    }
    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(getClass()!=obj.getClass()){
            return false;
        }
        DocumentImpl otherDoc = (DocumentImpl) obj;
        return this.hashCode() == otherDoc.hashCode();
    }

    @Override
    public int compareTo(Document o) {
        return (int) (this.time - o.getLastUseTime());
    }
}
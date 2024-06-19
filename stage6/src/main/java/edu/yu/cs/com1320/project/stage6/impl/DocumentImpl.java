package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.stage6.Document;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.lang.System.nanoTime;

public class DocumentImpl implements edu.yu.cs.com1320.project.stage6.Document {
    private HashMap<String, Integer> words;
    private HashMap<String, String> mdMap;
    private String sdata;
    private byte[] data;
    private URI uri;
    long time;
    public DocumentImpl(URI uri, String txt, Map<String, Integer> wordCountMap){
        if(uri==null || txt==null || txt.isEmpty() || uri.toString().isEmpty()){
            throw new IllegalArgumentException("no blanks or nulls");
        }
        this.mdMap = new HashMap<>();
        this.sdata = txt;
        this.uri = uri;
        this.time = nanoTime();
        //this.data = txt.getBytes();
        this.words = new HashMap<>();
        if(wordCountMap==null) {
            startHT(txt);
        }else{
           this.words = new HashMap<>(wordCountMap);
        }
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
    public HashMap<String, Integer> getWordMap() {
        return (HashMap<String, Integer>) this.words.clone();
    }

    @Override
    public void setWordMap(HashMap<String, Integer> wordMap) {
        this.words = wordMap;
    }

    @Override
    public int wordCount(String word) {
        Integer integer = this.words.get(word);
        return integer == null ? 0 : integer;
    }

    public DocumentImpl(URI uri, byte[] binaryData){
        if(uri==null || binaryData==null || binaryData.length==0 || uri.toString().isEmpty()){
            throw new IllegalArgumentException("no blanks or nulls");
        }
        this.mdMap = new HashMap<>();
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
        return this.mdMap.put(key, value);
        //return oldKey;
    }
    public String getMetadataValue(String key){
        if(key==null || key.isEmpty()){
            throw new IllegalArgumentException("Key cannot be null or blank");
        }
        //this.time = nanoTime();
        return this.mdMap.get(key);
    }

    public HashMap<String, String> getMetadata() {
        return (HashMap<String, String>) this.mdMap.clone();
    }
    @Override
    public void setMetadata(HashMap<String, String> metadata) {
     this.mdMap = metadata;
    }
    public String getDocumentTxt(){
        return this.sdata;
    }
    public URI getKey(){
        return this.uri;
    }
    public byte[] getDocumentBinaryData() {
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
package edu.yu.cs.com1320.project.stage6.impl;


import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DocumentImplTest {
    @Test
    public void sayHello(){
        try{
            URI hi = new URI("HelloName.txt");
            DocumentImpl hello = new DocumentImpl(hi, "Hello", null);
            assertEquals("Hello", hello.getDocumentTxt(), "FAIL");
            assertNull(hello.getDocumentBinaryData());
        }catch(URISyntaxException e){
            System.out.println("Error");
        }
    }
    @Test
    public void setMD(){
        try{
            URI uri = new URI("i Know what this does");
            String test = "Hello!";
            byte[] myBites = test.getBytes();
            DocumentImpl mydoc = new DocumentImpl(uri, myBites);
            assertNull(mydoc.setMetadataValue("Hi", "Hello"));
            assertEquals("Hello", mydoc.getMetadataValue("Hi"));
            assertEquals("Hello", mydoc.setMetadataValue("Hi", "bye"));
            assertThrows(IllegalArgumentException.class, () ->{
                mydoc.setMetadataValue(null, "HI");
            });
            assertThrows(IllegalArgumentException.class, () ->{
                mydoc.setMetadataValue("", "HI");
            });
            assertThrows(IllegalArgumentException.class, () ->{
                mydoc.getMetadataValue(null);
            });
            assertThrows(IllegalArgumentException.class, () ->{
                mydoc.getMetadataValue("");
            });
        }catch(URISyntaxException e){
            System.out.println("Error");
        }
    }
    @Test
    void moreMetadata(){
        try{
            URI uri = new URI("Path/hi");
            DocumentImpl doc1 = new DocumentImpl(uri, "Doc1", null);
            doc1.setMetadataValue("Name", "John");
            doc1.setMetadataValue("Adress", "Manhatan");
            assertEquals("Manhatan", doc1.getMetadataValue("Adress"));
            assertNull(doc1.getMetadataValue("not John"));
        }catch(URISyntaxException e){
            System.out.println("Bad thiinkgs");
        }
    }
    @Test
    void getMD(){
        try{
            URI uri = new URI("MYURI");
            String st1 = "Doc";
            String st2 = "MDDD";
            String st3 = "Val";
            String st4 = "New";
            DocumentImpl doc1 = new DocumentImpl(uri, st1, null);
            doc1.setMetadataValue(st2, st3);
            Set<String> toComp = new HashSet<>();
            toComp.add(st2);
            assertEquals(toComp, doc1.getMetadata().keySet());
            doc1.getMetadata().put(st4, "NEW");
            assertFalse(doc1.getMetadata().containsKey(st4)); //check put doesn't add to the actual object
        }catch (URISyntaxException e){
            throw new RuntimeException();
        }
    }
    @Test
    void wordcount(){
        try{
            DocumentImpl myDoc = new DocumentImpl(new URI("uri"), "Text.", null);
            assertEquals(1, myDoc.wordCount("Text"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void setWordMap(){
        try{
            Map<String, Integer> map = new HashMap<>();
            map.put("Text", 1);
            DocumentImpl myDoc = new DocumentImpl(new URI("uri"), "Text", map);
            assertEquals(1, myDoc.wordCount("Text"));
            assertEquals(map, myDoc.getWordMap());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void setMDMap(){
        try{
            DocumentImpl myDoc = new DocumentImpl(new URI("uri"), "Text.", null);
            HashMap<String, String> map = new HashMap<>();
            map.put("Name:", "Bob");
            map.put("Place:", "YU");
            myDoc.setMetadata(map);
            assertEquals(map, myDoc.getMetadata());
            myDoc.setMetadataValue("Floor:", "Tile");
            HashMap<String, String> map2 = (HashMap<String, String>) map.clone();
            map2.put("Floor:", "Tile");
            assertEquals(map2, myDoc.getMetadata());

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}

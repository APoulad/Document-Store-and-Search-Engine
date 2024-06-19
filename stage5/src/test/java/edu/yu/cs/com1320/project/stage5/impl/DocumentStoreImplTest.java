package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentStoreImplTest {
    DocumentStore store;
    URI uri;
    @BeforeEach
    void setup(){
        this.store=new DocumentStoreImpl();
        try{
            this.uri = new URI("myUri");
            this.store.put(new ByteArrayInputStream("File1".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File2".getBytes()), new URI("myURI"), DocumentStore.DocumentFormat.BINARY);
            this.store.put(new ByteArrayInputStream("File3".getBytes()), new URI("uri"), DocumentStore.DocumentFormat.TXT);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void setMD(){
        assertNull(this.store.setMetadata(uri, "Name", "John"));
        assertEquals("John",this.store.getMetadata(uri, "Name"));
        assertThrows(IllegalArgumentException.class, ()->{
            this.store.setMetadata(new URI(""), "Key", "Val");
        });
    }
    @Test
    void deleteTest(){
        assertTrue(this.store.delete(uri));
        assertThrows(IllegalArgumentException.class, ()->{
            this.store.setMetadata(uri, "Key", "Value");
        });
    }
    @Test
    void deleteUndo(){
        this.store.setMetadata(uri, "Key", "Value");
        this.store.delete(uri);
        this.store.undo();
        assertEquals("Value", this.store.getMetadata(uri, "Key"));
    }
    @Test
    void putUndo(){
        assertNotNull(this.store.get(uri));
        this.store.undo(uri);
        assertNull(this.store.get(uri));
    }
    @Test
    void setMDUndo(){
        this.store.setMetadata(uri, "Key", "Value");
        this.store.setMetadata(uri, "Key2", "Value2");
        this.store.setMetadata(uri, "Key", "NextValue");
        try {
            this.store.put(new ByteArrayInputStream("File3".getBytes()), new URI("uri"), DocumentStore.DocumentFormat.TXT);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        assertEquals("NextValue", this.store.getMetadata(uri, "Key"));
        this.store.undo(uri);
        assertEquals("Value", this.store.getMetadata(uri, "Key"));
        assertEquals("Value2", this.store.getMetadata(uri, "Key2"));
        this.store.undo(uri);
        assertNull(this.store.getMetadata(uri, "Key2"));
    }
    @Test
    void searchDocs(){
        try {
            this.store.put(new ByteArrayInputStream("Lakes Have Many Shores".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
            List<Document> docsR = new ArrayList<>();
            docsR.add(this.store.get(uri));
            assertEquals(docsR, this.store.search("Shores"));
            this.store.undo(uri);
            assertTrue(this.store.search("Have").isEmpty());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    @Test
    void searchCheckOrder(){
        try {
            this.store.put(new ByteArrayInputStream("Lakes".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
            URI uri1 =  new URI("IDKanymore");
            this.store.put(new ByteArrayInputStream("Lakes Lakes Lakes".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("IDK");
            this.store.put(new ByteArrayInputStream("Lakes Lakes".getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
            List<Document> docsR = new ArrayList<>();
            docsR.add(this.store.get(uri1));
            docsR.add(this.store.get(uri2));
            docsR.add(this.store.get(uri));
            assertEquals(docsR, this.store.search("Lakes"));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void prefixDocs(){
        try {
            this.store.put(new ByteArrayInputStream("Lake".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
            URI uri1 =  new URI("IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like Lak ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("IDK");
            this.store.put(new ByteArrayInputStream("Lakes".getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
            List<Document> docsR = new ArrayList<>();
            docsR.add(this.store.get(uri));
            docsR.add(this.store.get(uri1));
            docsR.add(this.store.get(uri2));
            assertEquals(docsR, this.store.searchByPrefix("La"));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void deleteAll(){
        try {
            this.store.put(new ByteArrayInputStream("Lakes Are Fun To Go To ZazA 3pm".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
            URI uri1 =  new URI("IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like Lakes ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("IDK");
            this.store.put(new ByteArrayInputStream("Lakes".getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
            Set<URI> docsR = new HashSet<>();
            docsR.add(uri2);
            docsR.add(uri1);
            docsR.add(uri);
            assertEquals(docsR, this.store.deleteAll("Lakes"));
            assertTrue(this.store.search("Lakes").isEmpty());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void deletePrefix(){
        try {
            this.store.put(new ByteArrayInputStream("Lake".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
            URI uri1 =  new URI("IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like Lak ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("IDK");
            this.store.put(new ByteArrayInputStream("Lakes".getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
            Set<URI> docsR = new HashSet<>();
            docsR.add(uri2);
            docsR.add(uri1);
            docsR.add(uri);
            assertEquals(docsR, this.store.deleteAllWithPrefix("Lak"));
            assertTrue(this.store.searchByPrefix("La").isEmpty());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void searchMetaData(){
        try{
            URI uri1 =  new URI("IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like Lak ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("IDK");
            this.store.put(new ByteArrayInputStream("Lakes".getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
            this.store.setMetadata(uri, "Name:", "Apple");
            this.store.setMetadata(uri2, "Name:", "Bob");
            this.store.setMetadata(uri1, "Name:", "Apple");
            List<Document> docsR = new ArrayList<>();
            docsR.add(this.store.get(uri1));
            docsR.add(this.store.get(uri));
            //docsR.add(this.store.get(uri2));
            Map<String, String> map = new HashMap<>();
            map.put("Name:", "Apple");
            assertEquals(docsR, this.store.searchByMetadata(map));
            map.put("Age:", "13");
            assertTrue(this.store.searchByMetadata(map).isEmpty());
            this.store.setMetadata(uri, "Age:", "13");
            docsR.remove(this.store.get(uri1));
            assertEquals(docsR, this.store.searchByMetadata(map));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void searchKWMD(){
        try{
            URI uri1 =  new URI("IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like Lak ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("IDK");
            this.store.put(new ByteArrayInputStream("Lakes".getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
            this.store.setMetadata(uri, "Name:", "Apple");
            this.store.setMetadata(uri2, "Name:", "Bob");
            this.store.setMetadata(uri1, "Name:", "Apple");
            List<Document> docsR = new ArrayList<>();
            docsR.add(this.store.get(uri1));
            //docsR.add(this.store.get(uri));
            //docsR.add(this.store.get(uri2));
            Map<String, String> map = new HashMap<>();
            map.put("Name:", "Apple");
            assertEquals(docsR, this.store.searchByKeywordAndMetadata("Like", map));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void searchPFXMD(){
        try{
            URI uri1 =  new URI("IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like Lak ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("IDK");
            this.store.put(new ByteArrayInputStream("Lakes".getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
            this.store.setMetadata(uri, "Name:", "Bob");
            this.store.setMetadata(uri2, "Name:", "Apple");
            this.store.setMetadata(uri1, "Name:", "Apple");
            List<Document> docsR = new ArrayList<>();
            docsR.add(this.store.get(uri1));
            //docsR.add(this.store.get(uri));
            docsR.add(this.store.get(uri2));
            Map<String, String> map = new HashMap<>();
            map.put("Name:", "Apple");
            assertEquals(docsR, this.store.searchByPrefixAndMetadata("La", map));
            this.store.setMetadata(uri2, "Age:", "13");
            map.put("Age:", "13");
            docsR.remove(this.store.get(uri1));
            assertEquals(docsR, this.store.searchByPrefixAndMetadata("La", map));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void deleteAllMD(){
        try {
            this.store.put(new ByteArrayInputStream("Lakes Are Fun To Go To ZazA 3pm".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
            URI uri1 =  new URI("IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like Lakes ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("IDK");
            this.store.put(new ByteArrayInputStream("Lakes".getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
            this.store.setMetadata(uri1, "Name:", "Bob");
            this.store.setMetadata(uri1, "Hello", "Hi");
            this.store.setMetadata(uri2, "Hello", "Hi");
            Map<String, String> map = new HashMap<>();
            map.put("Name:", "Bob");
            Set<URI> docsR = new HashSet<>();
            docsR.add(uri1);
            assertEquals(docsR, this.store.deleteAllWithMetadata(map));
            assertTrue(this.store.searchByMetadata(map).isEmpty());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void deleteKWMD(){
        try {
            this.store.put(new ByteArrayInputStream("Lakes Are Fun To Go To ZazA 3pm".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
            URI uri1 =  new URI("IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like Lakes ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("IDK");
            this.store.put(new ByteArrayInputStream("Not A Lake".getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
            this.store.setMetadata(uri1, "Name:", "Bob");
            this.store.setMetadata(uri1, "Hello", "Hi");
            this.store.setMetadata(uri2, "Name:", "Bob");
            Map<String, String> map = new HashMap<>();
            map.put("Name:", "Bob");
            Set<URI> docsR = new HashSet<>();
            docsR.add(uri1);
            assertEquals(docsR, this.store.deleteAllWithKeywordAndMetadata("Lakes",map));
            assertTrue(this.store.searchByKeywordAndMetadata("Lakes", map).isEmpty());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void deletePFXMD(){
        try {
            this.store.put(new ByteArrayInputStream("The Lake Are Fun To Go To ZazA 3pm".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
            URI uri1 =  new URI("IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like Lakes ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("IDK");
            this.store.put(new ByteArrayInputStream("Latkes".getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
            this.store.setMetadata(uri1, "Name:", "Bob");
            this.store.setMetadata(uri1, "Hello", "Hi");
            this.store.setMetadata(uri2, "Name:", "Bob");
            Map<String, String> map = new HashMap<>();
            map.put("Name:", "Bob");
            Set<URI> docsR = new HashSet<>();
            docsR.add(uri1); docsR.add(uri2);
            assertEquals(docsR, this.store.deleteAllWithPrefixAndMetadata("La", map));
            assertEquals(1, this.store.searchByPrefix("L").size());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void checkIllegalChars(){
        try {
            this.store.put(new ByteArrayInputStream("The La$ke Are Fun To Go To ZazA 3pm".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
            URI uri1 =  new URI("IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like L:akes ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("IDK");
            this.store.put(new ByteArrayInputStream("Latkes".getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
            this.store.setMetadata(uri1, "Name:", "Bob");
            this.store.setMetadata(uri1, "Hello", "Hi");
            this.store.setMetadata(uri2, "Name:", "Bob");
            assertTrue(this.store.searchByPrefix("L:").isEmpty());
            Map<String, String> map = new HashMap<>();
            map.put("Name:", "Bob");
            assertTrue(this.store.searchByKeywordAndMetadata("L:", map).isEmpty());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void deleteAllWithNonexistent(){
        assertTrue(this.store.deleteAll("HEYYYY").isEmpty());
    }
    @Test
    void undoOverwriteURI(){
        try {
            this.store.put(new ByteArrayInputStream("New Text".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
            assertEquals("New Text", this.store.get(uri).getDocumentTxt());
            this.store.undo(uri);
            assertEquals("File1", this.store.get(uri).getDocumentTxt());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    @Test
    void overWrite(){
        try {
            this.store.put(new ByteArrayInputStream("New Text".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
            this.store.setMetadata(uri, "Key1", "Value1");
            this.store.put(new ByteArrayInputStream("Newer Texter".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
            this.store.setMetadata(uri, "Key2", "Value2");
            assertTrue(this.store.search("New").isEmpty());
            Map<String, String> map = new HashMap<>();
            map.put("Key1", "Value1");
            assertTrue(this.store.searchByMetadata(map).isEmpty());
            assertEquals("Newer Texter", this.store.get(uri).getDocumentTxt());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void setMaxDocs(){
        try{
            //this.store.setMetadata(uri, "Key", "Value");
            this.store.setMaxDocumentCount(3);
            this.store.put(new ByteArrayInputStream("File4 Is Here".getBytes()), new URI("thefourth"), DocumentStore.DocumentFormat.TXT);
            assertTrue(this.store.search("File1").isEmpty());
            assertNull(this.store.get(uri));
            Map<String, String> map = new HashMap<>();
            map.put("Key", "Value");
            assertTrue(this.store.searchByMetadata(map).isEmpty());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void setMaxDocsWithMD(){
        try{
            this.store.setMetadata(uri, "Key", "Value");
            this.store.get(new URI("myURI"));this.store.search("File3");
            this.store.setMaxDocumentCount(3);
            this.store.put(new ByteArrayInputStream("File4 Is Here".getBytes()), new URI("thefourth"), DocumentStore.DocumentFormat.TXT);
            assertTrue(this.store.search("File1").isEmpty());
            assertNull(this.store.get(uri));
            Map<String, String> map = new HashMap<>();
            map.put("Key", "Value");
            assertTrue(this.store.searchByMetadata(map).isEmpty());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void setMaxBytes(){
        try{
            //this.store.setMetadata(uri, "Key", "Value");
            this.store.setMaxDocumentBytes(20);
            this.store.put(new ByteArrayInputStream("File4 Is Here".getBytes()), new URI("thefourth"), DocumentStore.DocumentFormat.TXT);
            assertTrue(this.store.search("File1").isEmpty());
            assertNull(this.store.get(uri));
            Map<String, String> map = new HashMap<>();
            map.put("Key", "Value");
            assertTrue(this.store.searchByMetadata(map).isEmpty());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void undoIsAlsoDeleted(){
        try{
            this.store.setMaxDocumentBytes(45);
            this.store.put(new ByteArrayInputStream("File4 Is Here".getBytes()), new URI("thefourth"), DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File5, The Fifth File Is Here".getBytes()), new URI("thefifth"), DocumentStore.DocumentFormat.BINARY);
            assertTrue(this.store.search("File1").isEmpty());
            assertNull(this.store.get(uri));
            this.store.undo(uri);
            assertTrue(this.store.search("File1").isEmpty());
            assertNull(this.store.get(uri));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void docTooBig(){
        try{
            this.store.setMaxDocumentBytes(26);
            this.store.put(new ByteArrayInputStream("File4 Is Here".getBytes()), new URI("thefourth"), DocumentStore.DocumentFormat.TXT);
            assertThrows(IllegalArgumentException.class, ()->{
                this.store.put(new ByteArrayInputStream("File5, The Fifth File Is Here".getBytes()),
                        new URI("thefourth"), DocumentStore.DocumentFormat.BINARY);
            });
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void multipleDeletesUndosMaxStorage(){//piazza 431 says he will not test if the file is already deleted, and then we set the max limit
        try{
            this.store.put(new ByteArrayInputStream("The Word you're looking for is absent".getBytes()), new URI("theifth"), DocumentStore.DocumentFormat.BINARY);
            this.store.deleteAllWithPrefix("File");
            this.store.setMaxDocumentBytes(100);
            this.store.put(new ByteArrayInputStream("File4 Is Here".getBytes()), new URI("thefourth"), DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("This is an overwrite".getBytes()), uri, DocumentStore.DocumentFormat.BINARY);
            this.store.put(new ByteArrayInputStream("File5, The Fifth File Is Here".getBytes()), new URI("thefifth"), DocumentStore.DocumentFormat.BINARY);
            this.store.put(new ByteArrayInputStream("Fil 6, The Sixth File Is Here".getBytes()), new URI("thesixth"), DocumentStore.DocumentFormat.BINARY);
            this.store.put(new ByteArrayInputStream("Fil 7, The Seventh File Is Here, and it has so many bytes that the storage will be full".getBytes()),
                    new URI("theseventh"), DocumentStore.DocumentFormat.BINARY);
            this.store.undo(uri);
            assertNull(this.store.get(uri));
            assertTrue(this.store.search("File1").isEmpty());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void searchingHasSameTime(){
        try{
        Document doc1 = this.store.get(uri);
        Document doc2 = this.store.get(new URI("myURI"));
        Document doc3 = this.store.get(new URI("uri"));
        List<Document> docList = this.store.searchByPrefix("File");
        this.store.put(new ByteArrayInputStream("File4 Is Here".getBytes()), new URI("thefourth"), DocumentStore.DocumentFormat.TXT);
        Document doc4 = this.store.get(new URI("thefourth"));
        assertEquals(0, doc1.compareTo(doc3));
        assertTrue(doc4.compareTo(doc1)>0);
        }catch (URISyntaxException | IOException e){
            throw new RuntimeException(e);
        }
    }
    @Test
    void undoUpdatesTime(){
        try{
            Document doc1 = this.store.get(uri);
            Document doc2 = this.store.get(new URI("myURI"));
            Document doc3 = this.store.get(new URI("uri"));
            assertTrue(doc2.compareTo(doc1)>0);
            List<Document> docList = this.store.searchByPrefix("File");
            assertTrue(doc3.compareTo(doc1)==0);
            this.store.delete(new URI("uri"));this.store.delete(new URI("myURI"));this.store.delete(uri);
            assertTrue(doc2.compareTo(doc1)<0);
            this.store.undo(new URI("myURI"));this.store.undo();this.store.undo();
            assertTrue(doc2.compareTo(doc1)<0);
        }catch (URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
    @Test
    void undoTooLarge(){
        try{
            this.store.deleteAllWithPrefix("File");
            this.store.put(new ByteArrayInputStream("This is an overwrite".getBytes()), uri, DocumentStore.DocumentFormat.BINARY);
            this.store.put(new ByteArrayInputStream("File5, The Fifth File Is Here".getBytes()), new URI("thefifth"), DocumentStore.DocumentFormat.BINARY);
            this.store.put(new ByteArrayInputStream("Fil 6, The Sixth File Is Here".getBytes()), new URI("thesixth"), DocumentStore.DocumentFormat.BINARY);
            this.store.put(new ByteArrayInputStream("Fil 7, The Seventh File Is Here, and it has so many bytes that the storage will be full".getBytes()),
                    new URI("theseventh"), DocumentStore.DocumentFormat.TXT);
            this.store.deleteAllWithPrefix("7");
            assertNull(this.store.get(new URI("theseventh")));
            this.store.setMaxDocumentBytes(50);
            this.store.put(new ByteArrayInputStream("File4 Is Here".getBytes()), new URI("thefourth"), DocumentStore.DocumentFormat.TXT);
            this.store.undo(new URI("theseventh"));
            assertNull(this.store.get(new URI("theseventh")));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void undoLargerThanMaxDocs(){
        try{
            this.store.put(new ByteArrayInputStream("File5, The Fifth File Is Here".getBytes()), new URI("thefifth"), DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File6, The Sixth File Is Here".getBytes()), new URI("thesixth"), DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File7, The Seventh File Is Here, and it has so many bytes that the storage will be full".getBytes()),
                    new URI("theseventh"), DocumentStore.DocumentFormat.TXT);
            this.store.deleteAllWithPrefix("File");
            assertNull(this.store.get(new URI("theseventh")));
            this.store.setMaxDocumentCount(4);
            this.store.undo();
            assertEquals(4, this.store.searchByPrefix("File").size());
            this.store.put(new ByteArrayInputStream("Newer File".getBytes()), new URI("ithinkeight"), DocumentStore.DocumentFormat.TXT);
            assertEquals(4, this.store.searchByPrefix("File").size());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void deleteAllBlank(){
        this.store.deleteAllWithPrefix("File");
        this.store.deleteAll("");
    }
}

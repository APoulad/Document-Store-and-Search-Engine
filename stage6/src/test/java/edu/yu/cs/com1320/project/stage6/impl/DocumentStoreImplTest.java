package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.DocumentStore;
import edu.yu.cs.com1320.project.stage6.PersistenceManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DocumentStoreImplTest {
    DocumentStore store;
    URI uri;
    @BeforeEach
    void setup(){
        this.store=new DocumentStoreImpl();
        try{
            this.uri = new URI("http://myUriTheFirst");
            this.store.put(new ByteArrayInputStream("File1".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File2".getBytes()), new URI("http://myURI"), DocumentStore.DocumentFormat.BINARY);
            this.store.put(new ByteArrayInputStream("File3".getBytes()), new URI("http://uri"), DocumentStore.DocumentFormat.TXT);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void setMD(){
        try {
            assertNull(this.store.setMetadata(uri, "Name", "John"));
        
        assertEquals("John",this.store.getMetadata(uri, "Name"));
        assertThrows(IllegalArgumentException.class, ()-> this.store.setMetadata(new URI(""), "Key", "Val"));} catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void deleteTest(){
        assertTrue(this.store.delete(uri));
        assertThrows(IllegalArgumentException.class, ()-> this.store.setMetadata(uri, "Key", "Value"));
    }
    @Test
    void deleteUndo(){
        try {
            this.store.setMetadata(uri, "Key", "Value");
            this.store.delete(uri);
            this.store.undo();
            assertEquals("Value", this.store.getMetadata(uri, "Key"));} catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void putUndo() throws IOException {
        assertNotNull(this.store.get(uri));
        this.store.undo(uri);
        assertNull(this.store.get(uri));
    }
    @Test
    void setMDUndo() throws IOException {
        this.store.setMetadata(uri, "Key", "Value");
        this.store.setMetadata(uri, "Key2", "Value2");
        this.store.setMetadata(uri, "Key", "NextValue");
        try {
            this.store.put(new ByteArrayInputStream("File3".getBytes()), new URI("http://uri"), DocumentStore.DocumentFormat.TXT);
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
            URI uri1 =  new URI("http://IDKanymore");
            this.store.put(new ByteArrayInputStream("Lakes Lakes Lakes".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("http://IDK");
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
            URI uri1 =  new URI("http://IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like Lak ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("http://IDK");
            this.store.put(new ByteArrayInputStream("Lakes".getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
            Set<Document> docsR = new HashSet<>();
            docsR.add(this.store.get(uri));
            docsR.add(this.store.get(uri1));
            docsR.add(this.store.get(uri2));
            assertEquals(docsR, new HashSet<>(this.store.searchByPrefix("La")));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void deleteAll(){
        try {
            this.store.put(new ByteArrayInputStream("Lakes Are Fun To Go To ZazA 3pm".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
            URI uri1 =  new URI("http://IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like Lakes ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("http://IDK");
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
            URI uri1 =  new URI("http://IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like Lak ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("http://IDK");
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
            URI uri1 =  new URI("http://IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like Lak ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("http://IDK");
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
            URI uri1 =  new URI("http://IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like Lak ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("http://IDK");
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
            URI uri1 =  new URI("http://IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like Lak ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("http://IDK");
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
            URI uri1 =  new URI("http://IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like Lakes ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("http://IDK");
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
            URI uri1 =  new URI("http://IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like Lakes ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("http://IDK");
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
            URI uri1 =  new URI("http://IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like Lakes ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("http://IDK");
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
            URI uri1 =  new URI("http://IDKanymore");
            this.store.put(new ByteArrayInputStream("I Like L:akes ".getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            URI uri2 =  new URI("http://IDK");
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
            this.store.put(new ByteArrayInputStream("File4 Is Here".getBytes()), new URI("http://thefourth"), DocumentStore.DocumentFormat.TXT);
            assertFalse(this.store.search("File1").isEmpty());
            assertNotNull(this.store.get(uri));
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
            this.store.get(new URI("http://myURI"));this.store.search("File3");
            this.store.setMaxDocumentCount(3);
            this.store.put(new ByteArrayInputStream("File4 Is Here".getBytes()), new URI("http://thefourth"), DocumentStore.DocumentFormat.TXT);
            assertFalse(this.store.search("File1").isEmpty());
            assertNotNull(this.store.get(uri));
            Map<String, String> map = new HashMap<>();
            map.put("Key", "Value");
            assertFalse(this.store.searchByMetadata(map).isEmpty());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void setMaxBytes(){
        try{
            //this.store.setMetadata(uri, "Key", "Value");
            this.store.setMaxDocumentBytes(20);
            this.store.put(new ByteArrayInputStream("File4 Is Here".getBytes()), new URI("http://thefourth"), DocumentStore.DocumentFormat.TXT);
            assertFalse(this.store.search("File1").isEmpty());
            assertNotNull(this.store.get(uri));
            Map<String, String> map = new HashMap<>();
            map.put("Key", "Value");
            assertTrue(this.store.searchByMetadata(map).isEmpty());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void docTooBig(){
        try{
            this.store.setMaxDocumentBytes(26);
            this.store.put(new ByteArrayInputStream("File4 Is Here".getBytes()), new URI("http://thefourth"), DocumentStore.DocumentFormat.TXT);
            assertThrows(IllegalArgumentException.class, ()-> this.store.put(new ByteArrayInputStream("File5, The Fifth File Is Here".getBytes()),
                    new URI("http://thefourth"), DocumentStore.DocumentFormat.BINARY));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void multipleDeletesUndosMaxStorage(){//piazza 431 says he will not test if the file is already deleted, and then we set the max limit
        try{
            this.store.put(new ByteArrayInputStream("The Word you're looking for is absent".getBytes()), new URI("http://theifth"), DocumentStore.DocumentFormat.BINARY);
            this.store.deleteAllWithPrefix("File");
            this.store.setMaxDocumentBytes(100);
            this.store.put(new ByteArrayInputStream("File4 Is Here".getBytes()), new URI("http://thefourth"), DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("This is an overwrite".getBytes()), uri, DocumentStore.DocumentFormat.BINARY);
            this.store.put(new ByteArrayInputStream("File5, The Fifth File Is Here".getBytes()), new URI("http://thefifth"), DocumentStore.DocumentFormat.BINARY);
            this.store.put(new ByteArrayInputStream("Fil 6, The Sixth File Is Here".getBytes()), new URI("http://thesixth"), DocumentStore.DocumentFormat.BINARY);
            this.store.put(new ByteArrayInputStream("Fil 7, The Seventh File Is Here, and it has so many bytes that the storage will be full".getBytes()),
                    new URI("http://theseventh"), DocumentStore.DocumentFormat.BINARY);
            this.store.undo(uri);
            assertNull(this.store.get(uri));
            assertTrue(this.store.search("File1").isEmpty());
            this.store.delete(new URI("http://theifth"));
            this.store.delete(new URI("http://thesixth"));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void searchingHasSameTime(){
        try{
            Document doc1 = this.store.get(uri);
            Document doc2 = this.store.get(new URI("http://myURI"));
            Document doc3 = this.store.get(new URI("http://uri"));
            List<Document> docList = this.store.searchByPrefix("File");
            this.store.put(new ByteArrayInputStream("File4 Is Here".getBytes()), new URI("http://thefourth"), DocumentStore.DocumentFormat.TXT);
            Document doc4 = this.store.get(new URI("http://thefourth"));
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
            Document doc2 = this.store.get(new URI("http://myURI"));
            Document doc3 = this.store.get(new URI("http://uri"));
            assertTrue(doc2.compareTo(doc1)>0);
            List<Document> docList = this.store.searchByPrefix("File");
            assertEquals(0, doc3.compareTo(doc1));
            this.store.delete(new URI("http://uri"));this.store.delete(new URI("http://myURI"));this.store.delete(uri);
            assertTrue(doc2.compareTo(doc1)<0);
            this.store.undo(new URI("http://myURI"));this.store.undo();this.store.undo();
            assertTrue(doc2.compareTo(doc1)<0);
        }catch (URISyntaxException | IOException e){
            throw new RuntimeException(e);
        }
    }
    @Test
    void undoTooLarge(){
        try{
            this.store.deleteAllWithPrefix("File");
            this.store.put(new ByteArrayInputStream("This is an overwrite".getBytes()), uri, DocumentStore.DocumentFormat.BINARY);
            this.store.put(new ByteArrayInputStream("File5, The Fifth File Is Here".getBytes()), new URI("http://thefifth"), DocumentStore.DocumentFormat.BINARY);
            this.store.put(new ByteArrayInputStream("Fil 6, The Sixth File Is Here".getBytes()), new URI("http://thesixth"), DocumentStore.DocumentFormat.BINARY);
            this.store.put(new ByteArrayInputStream("Fil 7, The Seventh File Is Here, and it has so many bytes that the storage will be full".getBytes()),
                    new URI("http://theseventh"), DocumentStore.DocumentFormat.TXT);
            this.store.deleteAllWithPrefix("7");
            assertNull(this.store.get(new URI("http://theseventh")));
            this.store.setMaxDocumentBytes(50);
            this.store.put(new ByteArrayInputStream("File4 Is Here".getBytes()), new URI("http://thefourth"), DocumentStore.DocumentFormat.TXT);
            assertThrows(IllegalStateException.class, ()-> this.store.undo(new URI("http://theseventh")));
            assertNull(this.store.get(new URI("http://theseventh")));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void undoLargerThanMaxDocs(){
        try{
            this.store.put(new ByteArrayInputStream("File5, The Fifth File Is Here".getBytes()), new URI("http://thefifth"), DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File6, The Sixth File Is Here".getBytes()), new URI("http://thesixth"), DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File7, The Seventh File Is Here, and it has so many bytes that the storage will be full".getBytes()),
                    new URI("http://theseventh"), DocumentStore.DocumentFormat.TXT);
            this.store.deleteAllWithPrefix("File");
            assertNull(this.store.get(new URI("http://theseventh")));
            this.store.setMaxDocumentCount(4);
            this.store.undo();
            assertEquals(5, this.store.searchByPrefix("File").size());
            this.store.put(new ByteArrayInputStream("Newer File".getBytes()), new URI("http://ithinkeight"), DocumentStore.DocumentFormat.TXT);
            assertEquals(6, this.store.searchByPrefix("File").size());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void deleteAllBlank(){
        this.store.deleteAllWithPrefix("File");
        this.store.deleteAll("");
    }
    @Test
    void nullInputStream(){
        try {
            this.store.put(null, uri, DocumentStore.DocumentFormat.TXT);
            assertNull(this.store.get(uri));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void simpleDelete(){
        try {
            this.store.delete( uri);
            assertNull(this.store.get(uri));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void overwrite(){//based on piazza 463
        this.store = new DocumentStoreImpl();
        try{
            this.store.put(new ByteArrayInputStream("File5, The Fifth File Is Here".getBytes()), new URI("http://thefifth"), DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File6, The Sixth File Is Here".getBytes()), new URI("http://thesixth"), DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File7, The Seventh File Is Here, and it has so many bytes".getBytes()),
                    new URI("http://theseventh"), DocumentStore.DocumentFormat.TXT);
            this.store.setMaxDocumentCount(2);
            this.store.put(new ByteArrayInputStream("This is a new one".getBytes()), new URI("http://thefifth"), DocumentStore.DocumentFormat.TXT);
            assertTrue(this.store.search("File5").isEmpty());
            assertEquals("This is a new one", this.store.get(new URI("http://thefifth")).getDocumentTxt());
            this.store.undo();
            assertTrue(this.store.search("is").isEmpty());
            assertEquals(1, this.store.search("File5").size());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void storeWithPath(){
        this.store = new DocumentStoreImpl(new File("newdir/hello"));
        try{
            this.store.put(new ByteArrayInputStream("File5, The Fifth File Is Here".getBytes()), new URI("http://thefifth"), DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File6, The Sixth File Is Here".getBytes()), new URI("http://thesixth"), DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File7, The Seventh File Is Here, and it has so many bytes".getBytes()),
                    new URI("http://theseventh"), DocumentStore.DocumentFormat.TXT);
            this.store.setMaxDocumentCount(2);
            this.store.put(new ByteArrayInputStream("This is a new one".getBytes()), new URI("http://thefifth"), DocumentStore.DocumentFormat.TXT);
            assertTrue(this.store.search("File5").isEmpty());
            assertEquals("This is a new one", this.store.get(new URI("http://thefifth")).getDocumentTxt());
            this.store.undo();
            assertTrue(this.store.search("is").isEmpty());
            assertEquals(1, this.store.search("File5").size());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void deleteByMD(){
        try{
            this.store.put(new ByteArrayInputStream("File5, The Fifth File Is Here".getBytes()), new URI("http://thefifth"), DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File6, The Sixth File Is Here".getBytes()), new URI("http://thesixth"), DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File7, The Seventh File Is Here, and it has so many bytes".getBytes()),
                    new URI("http://theseventh"), DocumentStore.DocumentFormat.TXT);
            this.store.setMetadata(uri, "Name:", "Bob");
            this.store.setMetadata(uri, "Place:", "YU");
            this.store.setMetadata(new URI("http://thefifth"), "Name:", "Bob");
            HashMap<String, String> map = new HashMap<>();
            map.put("Name:", "Bob");
            assertEquals(2, this.store.searchByMetadata(map).size());
            assertEquals(2, this.store.deleteAllWithMetadata(map).size());
            assertEquals(0, this.store.searchByMetadata(map).size());
            this.store.undo();
            assertEquals(2, this.store.searchByMetadata(map).size());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    Document checkContents(URI uri) throws IOException {
        PersistenceManager<URI, Document> pm = new DocumentPersistenceManager(null);
        return pm.deserialize(uri);
    }
    //Stage 6 testing for real

    @Test
    void documentInDiskLimit(){
        try{
            this.store.put(new ByteArrayInputStream("File5, The Fifth File Is Here".getBytes()), new URI("http://thefifth"), DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File6, The Sixth File Is Here".getBytes()), new URI("http://thesixth"), DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File7, The Seventh File Is Here, and it has so many bytes".getBytes()),
                    new URI("http://theseventh"), DocumentStore.DocumentFormat.TXT);
            this.store.setMaxDocumentCount(3);
            assertNotNull(checkContents(uri));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void piazza463(){
        this.store=new DocumentStoreImpl();
        try{
            URI uri1 = new URI("http://fileOne");
            URI uri2 = new URI("http://fileTwo");
            URI uri3 = new URI("http://fileThree");
            this.store.put(new ByteArrayInputStream("File1, The first File Is Here".getBytes()), uri1 , DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File2, The second File Is Here".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File3, The third File Is Here".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri1));
            this.store.setMaxDocumentCount(2);
            assertNotNull(checkContents(uri1));
            this.store.put(new ByteArrayInputStream("Different File than the 1st, an overwrite".getBytes()), uri1 , DocumentStore.DocumentFormat.TXT);
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri1));
            this.store.undo();
            //issue here is that after the overwrite, uri2 goes to disk, and then the undo doesn't bring it back.
            //how to fix... we must track the documents sent to disk by the overwrite...
            assertNotNull(checkContents(uri1));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @AfterEach
    void deleteDocs(){
        this.store.delete(uri);
        this.store.deleteAllWithPrefix("");
    }
    @Test
    void PushToDiskViaUndoDelete(){//1st failed 50pt test
        this.store=new DocumentStoreImpl();
        //test that documents move to and from disk and memory as expected
        // when a doc is deleted
        // then another is added to memory
        // then the delete is undone causing another doc to be pushed out to disk
        try{
            this.store.setMaxDocumentCount(2);
            URI uri1 = new URI("http://fileOne");
            URI uri2 = new URI("http://fileTwo");
            URI uri3 = new URI("http://fileThree");
            this.store.put(new ByteArrayInputStream("File1, The first File Is Here".getBytes()), uri1 , DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File2, The second File Is Here".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            this.store.delete(uri1);

            assertThrows(FileNotFoundException.class, ()-> checkContents(uri1));//bc its deleted
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri2));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri3));

            this.store.put(new ByteArrayInputStream("File3, The third File Is Here".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
            //this doc is now in memory
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri1));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri2));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri3));

            this.store.undo(uri1);
            //undo deleting uri1 which was in memory...
            //uri 1 should be put back into memory, and uri 2 in disk
            assertNotNull(checkContents(uri2));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri1));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri3));

        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void BringBackInViaMetadataSearch(){
        //pushes a document out to the disk via max doc count setting
        // and then tests that it is brought back into memory via a metadata search
        this.store=new DocumentStoreImpl();
        try{
            URI uri1 = new URI("http://fileOne");
            URI uri2 = new URI("http://fileTwo");
            URI uri3 = new URI("http://fileThree");
            this.store.put(new ByteArrayInputStream("File1, The first File Is Here".getBytes()), uri1 , DocumentStore.DocumentFormat.TXT);
            Document doc1 = this.store.get(uri1);
            this.store.setMetadata(uri1, "Job:", "CS");
            this.store.setMetadata(uri1, "Time:", "10");
            this.store.put(new ByteArrayInputStream("File2, The second File Is Here".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            this.store.setMetadata(uri2, "Job:", "CS");
            this.store.setMetadata(uri2, "Time:", "11");
            this.store.put(new ByteArrayInputStream("File3, The third File Is Here".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
            this.store.setMetadata(uri3, "Job:", "Not CS");
            this.store.setMaxDocumentCount(2);

            Map<String, String> map = new HashMap<>();
            map.put("Job:", "CS");map.put("Time:", "10");
            //uri 1 should be sent to disk
            assertNotNull(checkContents(uri1));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri2));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri3));
            List<Document> list = new ArrayList<>(); list.add(doc1);
            assertEquals(list, this.store.searchByMetadata(map));
            //uri 1 should now be back in memory, and uri2 sent to disk
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri1));
            assertNotNull(checkContents(uri2));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri3));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void bytesBringBackInViaMetadataSearch(){
        this.store=new DocumentStoreImpl();
        //pushes a document out to the disk via max bytes setting
        // and then tests that it is brought back into memory via a metadata search
        try{
            URI uri1 = new URI("http://fileOne");
            URI uri2 = new URI("http://fileTwo");
            URI uri3 = new URI("http://fileThree");
            this.store.put(new ByteArrayInputStream("File 1, The first File Is Here".getBytes()), uri1 , DocumentStore.DocumentFormat.BINARY);
            Document doc1 = this.store.get(uri1);
            this.store.setMetadata(uri1, "Jab:", "CS");
            this.store.setMetadata(uri1, "Time:", "10");
            this.store.put(new ByteArrayInputStream("File2, The second File Is Here".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            this.store.setMetadata(uri2, "Job:", "CS");
            this.store.setMetadata(uri2, "Time:", "11");
            this.store.put(new ByteArrayInputStream("File 3, The third File Is Here".getBytes()), uri3, DocumentStore.DocumentFormat.BINARY);
            this.store.setMetadata(uri3, "Job:", "Not CS");
            this.store.setMaxDocumentBytes(70);

            Map<String, String> map = new HashMap<>();
            map.put("Jab:", "CS");map.put("Time:", "10");
            //uri 1 should be sent to disk
            assertNotNull(checkContents(uri1));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri2));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri3));
            List<Document> list = new ArrayList<>(); list.add(doc1);
            assertEquals(list, this.store.searchByMetadata(map));
            //uri 1 should now be back in memory, and uri2 sent to disk
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri1));
            assertNotNull(checkContents(uri2));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri3));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void pushToDiskViaMaxDocCount(){
        this.store=new DocumentStoreImpl();
        try{
            this.store.setMaxDocumentCount(2);
            URI uri1 = new URI("http://juda/fileOne");
            URI uri2 = new URI("http://juda/fileTwo");
            URI uri3 = new URI("http://fileThree");
            this.store.put(new ByteArrayInputStream("File1, The first File Is Here".getBytes()), uri1 , DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("File2, The second File Is Here".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri1));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri2));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri3));
            this.store.put(new ByteArrayInputStream("File3, The third File Is Here".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
            assertNotNull(checkContents(uri1));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri2));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri3));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void bringBackInViaDeleteAndSearch(){
        this.store= new DocumentStoreImpl();
        //test that documents move to and from disk and memory as expected
        // when the maxdoc count is reached and a doc is pushed out to disk
        // and then a doc is deleted
        // and then the doc on disk has to be brought back in because of a search
        try{
            this.store.setMaxDocumentCount(4);
            URI uri1 = new URI("http://teachers/juda/first");
            URI uri2 = new URI("http://assignments/points/second");
            URI uri3 = new URI("http://assignments/points/third");
            URI uri4 = new URI("http://clowns/poulad/fourth");
            URI uri5 = new URI("http://theFifth");
            URI uri6 = new URI("http://clowns/sixth");
            this.store.put(new ByteArrayInputStream("This is the 1st".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("second".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            Document doc2 = this.store.get(uri2);
            this.store.put(new ByteArrayInputStream("This is the 3rd".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("This is the fourth".getBytes()), uri4, DocumentStore.DocumentFormat.TXT);
            this.store.put(new ByteArrayInputStream("Number FIVE".getBytes()), uri5, DocumentStore.DocumentFormat.BINARY);
            this.store.put(new ByteArrayInputStream("Finaly Sixth".getBytes()), uri6, DocumentStore.DocumentFormat.BINARY);
            //now the first two should be in disk
            assertNotNull(checkContents(uri1));
            assertNotNull(checkContents(uri2));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri3));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri4));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri5));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri6));
            this.store.delete(uri4);

            List<Document> list = new ArrayList<>(); list.add(doc2);
            assertEquals(list, this.store.searchByPrefix("second"));

            assertNotNull(checkContents(uri1));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri2));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri3));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri4));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri5));
            assertThrows(FileNotFoundException.class, ()-> checkContents(uri6));

        }catch (IOException|URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
}
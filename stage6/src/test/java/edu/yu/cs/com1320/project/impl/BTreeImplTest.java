package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.PersistenceManager;
import edu.yu.cs.com1320.project.stage6.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage6.impl.DocumentPersistenceManager;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class BTreeImplTest {
    @Test
    void putInt(){
        BTree<Integer, String> intSBTree = new BTreeImpl<>();
        intSBTree.put(10,"ten");
        intSBTree.put(20,"twenty");
        intSBTree.put(30,"thirty");
        intSBTree.put(40,"forty");
        intSBTree.put(50,"fifty");
        intSBTree.put(60,"sixty");
        intSBTree.put(70,"seventy");
        assertEquals("ten", intSBTree.get(10));
    }
    @Test
    void deleteInt(){
        BTree<Integer, String> intSBTree = new BTreeImpl<>();
        intSBTree.put(40,"forty");
        intSBTree.put(20,"twenty");
        intSBTree.put(30,"thirty");
        intSBTree.put(10,"ten");
        intSBTree.put(50,"fifty");
        intSBTree.put(60,"sixty");
        intSBTree.put(70,"seventy");
        assertEquals("ten", intSBTree.get(10));
        assertEquals("forty", intSBTree.get(40));
        assertEquals("seventy", intSBTree.get(70));
        assertEquals("fifty",intSBTree.put(50, null));
        intSBTree.setPersistenceManager(new PersistenceManager<Integer, String>() {
            @Override
            public void serialize(Integer integer, String val) throws IOException {
                throw new IOException();
            }

            @Override
            public String deserialize(Integer integer) throws IOException {
                throw new FileNotFoundException();
            }

            @Override
            public boolean delete(Integer integer) throws IOException {
                return false;
            }
        });
        assertNull(intSBTree.get(50));
    }
    @Test
    void withDocuments(){
        BTree<URI, Document> bTree = new BTreeImpl<>();
        bTree.setPersistenceManager(new DocumentPersistenceManager(null));
        try {
            URI uri1 = new URI("http://www.yu.edu/doc1");
            URI uri2 = new URI("http://www.yu.edu/doc2");
            URI uri3 = new URI("http://www.yu.edu/doc3");
            URI uri4 = new URI("http://www.yu.edu/moredocs/doc4");
            URI uri5 = new URI("http://www.yu.edu/doc/doc5");
            URI uri6 = new URI("http://www.yu.edu/doc6");
            Document doc1 = new DocumentImpl(uri1, "Document one is here", null);
            Document doc2 = new DocumentImpl(uri2, "thisi is some byte".getBytes());
            Document doc3 = new DocumentImpl(uri3, "HELLLLO TO YOU", null);
            Document doc4 = new DocumentImpl(uri4, "HELLLLO TO YOU TWO", null);
            Document doc5 = new DocumentImpl(uri5, "HELLLLO TO YOU THree", null);
            Document doc6 = new DocumentImpl(uri6, "HELLLLO TO YOU".getBytes());
            bTree.put(uri1, doc1);
            bTree.put(uri2, doc2);
            bTree.put(uri3, doc3);
            bTree.put(uri4, doc4);
            bTree.put(uri5, doc5);
            bTree.put(uri6, doc6);
            assertEquals(doc5, bTree.get(uri5));
            assertEquals(doc1, bTree.get(uri1));
            assertEquals(doc3, bTree.get(uri3));
            assertEquals(doc6, bTree.get(uri6));
            assertEquals(doc2, bTree.get(uri2));
            bTree.moveToDisk(uri4);
            assertEquals(doc4, bTree.get(uri4));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

    }
    @Test
    void getAgain(){
        BTree<URI, Document> bTree = new BTreeImpl<>();
        bTree.setPersistenceManager(new DocumentPersistenceManager(null));
        try {
            URI uri1 = new URI("http://www.yu.edu/doc1");
            URI uri2 = new URI("http://www.yu.edu/doc2");
            URI uri3 = new URI("http://www.yu.edu/doc3");
            URI uri4 = new URI("http://www.yu.edu/moredocs/doc4");
            URI uri5 = new URI("http://www.yu.edu/doc/doc5");
            URI uri6 = new URI("http://www.yu.edu/doc6");
            Document doc1 = new DocumentImpl(uri1, "Document one is here", null);
            Document doc2 = new DocumentImpl(uri2, "thisi is some byte".getBytes());
            Document doc3 = new DocumentImpl(uri3, "HELLLLO TO YOU", null);
            Document doc4 = new DocumentImpl(uri4, "HELLLLO TO YOU TWO", null);
            Document doc5 = new DocumentImpl(uri5, "HELLLLO TO YOU THree", null);
            Document doc6 = new DocumentImpl(uri6, "HELLLLO TO YOU".getBytes());
            bTree.put(uri1, doc1);
            bTree.put(uri2, doc2);
            bTree.put(uri3, doc3);
            bTree.put(uri4, doc4);
            bTree.put(uri5, doc5);
            bTree.put(uri6, doc6);
            bTree.moveToDisk(uri5);
            bTree.moveToDisk(uri6);
            assertEquals(doc5, bTree.get(uri5));
            assertEquals(doc6, bTree.get(uri6));
            assertEquals(doc5, bTree.get(uri5));
            assertEquals(doc6, bTree.get(uri6));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
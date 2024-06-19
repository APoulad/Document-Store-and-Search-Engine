package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HashTableImplTest {
    HashTable<String, String> table;
    @BeforeEach
    void createTable(){
        this.table = new HashTableImpl<>();
    }
    @Test
    void putAndGet(){
        assertNull(this.table.put("Name", "John"));
        assertEquals("John", this.table.get("Name"));
        this.table.put("One", "true");
        this.table.put("Two", "false");
        this.table.put("Three", "Math");
        this.table.put("Four", "Mothers");
        this.table.put("Five", "Books");
        this.table.put("Six", "Mishnayot");
        assertEquals("John", this.table.get("Name"));
        assertEquals("Math", this.table.get("Three"));
        assertEquals("Mothers", this.table.get("Four"));
        assertEquals("true", this.table.get("One"));
        assertEquals("Books", this.table.get("Five"));
        assertEquals("Mishnayot", this.table.get("Six"));
        assertEquals("Mothers", this.table.put("Four", null));
        assertNull(this.table.get("Four"));
        assertEquals("John", this.table.get("Name"));
        assertEquals("Math", this.table.get("Three"));
        assertEquals("true", this.table.get("One"));
        assertEquals("Books", this.table.get("Five"));
        assertEquals("Mishnayot", this.table.get("Six"));
        assertTrue(this.table.containsKey("Five"));
        assertFalse(this.table.containsKey("Not here"));
    }
    @Test
    void rewritingValues(){
        this.table.put("One", "true");
        this.table.put("Two", "false");
        this.table.put("Three", "Math");
        this.table.put("Four", "Mothers");
        this.table.put("Five", "Books");
        this.table.put("Six", "Mishnayot");
        this.table.put("Seven", "Week");
        this.table.put("Eight", "Circ");
        assertEquals("Math", this.table.get("Three"));
        assertEquals("Mothers", this.table.get("Four"));
        assertEquals("true", this.table.get("One"));
        assertEquals("Books", this.table.get("Five"));
        assertEquals("Mishnayot", this.table.get("Six"));
        assertEquals("Mothers", this.table.put("Four", "Imahot"));
        assertEquals("Imahot", this.table.get("Four"));
        this.table.put("Eight", "Days Until");
        assertEquals("Days Until", this.table.get("Eight"));
        assertEquals(8, this.table.size());
    }
    @Test
    void checkThrowings(){
        this.table.put("One", "true");
        this.table.put("Two", "false");
        assertThrows(IllegalArgumentException.class, ()->{
            this.table.put(null, "Hello");
        });
        assertThrows(NullPointerException.class, ()->{
            this.table.get(null);
        });
        assertThrows(NullPointerException.class, ()->{
            this.table.containsKey(null);
        });
    }
    @Test
    void unmodifiableChecks(){
        this.table.put("One", "true");
        this.table.put("Two", "false");
        assertThrows(UnsupportedOperationException.class, ()->{
            this.table.keySet().add(null);
        });
        assertThrows(UnsupportedOperationException.class, ()->{
            this.table.values().add(null);
        });
    }
    @Test
    void sizeAndContains(){
        this.table.put("One", "true");
        this.table.put("Two", "false");
        this.table.put("Three", "Math");
        this.table.put("Four", "Mothers");
        this.table.put("Five", "Books");
        this.table.put("Six", "Mishnayot");
        this.table.put("Seven", "Week");
        this.table.put("Eight", "Circ");
        assertEquals(8, this.table.size());
        this.table.put("Five", "Torah");
        this.table.put("Six", "Tractates");
        this.table.put("Seven", "Days");
        this.table.put("Eight", "Days");
        assertEquals(8, this.table.size());
        this.table.put("Six", null);
        assertFalse(this.table.containsKey("Six"));
        assertEquals(7, this.table.size());
    }
    @Test
    void keySetTest(){
        this.table.put("One", "true");
        this.table.put("Two", "false");
        this.table.put("Three", "Math");
        this.table.put("Four", "Mothers");
        this.table.put("Five", "Books");
        this.table.put("Six", "Mishnayot");
        this.table.put("Seven", "Week");
        this.table.put("Eight", "Circ");
        assertEquals("[Eight, Six, Five, One, Four, Seven, Two, Three]",this.table.keySet().toString());
    }
    @Test
    void valuesReturn(){
        this.table.put("One", "true");
        this.table.put("Two", "false");
        this.table.put("Three", "Math");
        this.table.put("Four", "Mothers");
        this.table.put("Five", "Books");
        this.table.put("Six", "Mishnayot");
        this.table.put("Seven", "Week");
        this.table.put("Eight", "Circ");
        assertEquals("[true, Circ, Mothers, Week, Mishnayot, Books, Math, false]", this.table.values().toString());
    }
    @Test
    void resizing(){
        HashTable<Integer,Integer> ntable = new HashTableImpl<>();
        for(int i=0; i<100; i++){
            ntable.put(i,i);
        }
        assertEquals(100, ntable.size());
        assertEquals(90, ntable.get(90));
    }
}

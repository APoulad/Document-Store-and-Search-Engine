package edu.yu.cs.com1320.project.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import edu.yu.cs.com1320.project.Stack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StackImplTest {
    Stack<Integer> stack;
    @BeforeEach
    void setStack(){
        this.stack = new StackImpl<>();
    }
    @Test
    void pushPeek(){
        stack.push(1);
        assertEquals(1, stack.peek());
    }
    @Test
    void pushPop(){
        stack.push(1);
        stack.push(2);
        assertEquals(2, stack.pop());
        assertEquals(1, stack.pop());
        assertNull(stack.pop());
    }
    @Test
    void size(){
        for (int i = 0; i < 50; i++) {
            stack.push(i);
        }
        assertEquals(50, stack.size());
        for (int i = 0; i < 10; i++) {
            stack.pop();
        }
        assertEquals(40, stack.size());
        assertEquals(39, stack.pop());
    }
    @Test
    void checkNullPeek(){
        assertNull(stack.peek());
        stack.push(1);
        stack.pop();
        assertNull(stack.peek());
    }
}

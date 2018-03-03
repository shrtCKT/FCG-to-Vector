package graph;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GraphTest {
  private static class TestVertex implements Vertex {
    String label;
    
    public TestVertex(String label) {
      this.label = label;
    }
    
    @Override
    public String getLabel() {
      return label;
    }
  }
  
  private static class TestEdge {
    public final int source;
    public final int target;
    
    public TestEdge(int source, int target) {
      this.source = source;
      this.target = target;
    }
  }
  
  
  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testAdd() {
    Graph<TestVertex, TestEdge> g = new Graph<>(10);
    
    TestVertex v0 = new TestVertex("0");
    int v0Id = g.addVertex(v0);
    assertEquals(0, v0Id);
    
    TestVertex v1 = new TestVertex("1");
    int v1Id = g.addVertex(v1);
    assertEquals(1, v1Id);
    
    TestVertex v2 = new TestVertex("2");
    int v2Id = g.addVertex(v2);
    assertEquals(2, v2Id);
    
    TestVertex v3 = new TestVertex("3");
    int v3Id = g.addVertex(v3);
    assertEquals(3, v3Id);
    
    TestEdge e1 = new TestEdge(0,1);
    g.addEdge(e1.source, e1.target, e1);
    TestEdge e2 = new TestEdge(0,2);
    g.addEdge(e2.source, e2.target, e2);
    TestEdge e3 = new TestEdge(2,3);
    g.addEdge(e3.source, e3.target, e3);
    TestEdge e4 = new TestEdge(1,2);
    g.addEdge(e4.source, e4.target, e4);
    TestEdge e5 = new TestEdge(1,3);
    g.addEdge(e5.source, e5.target, e5);
    
    // Outgoing edges.
    HashMap<Integer, ArrayList<TestEdge>> outEdgeMap = new HashMap<Integer, ArrayList<TestEdge>>();
    ArrayList<TestEdge> v0OutEdges = new ArrayList<TestEdge>();
    v0OutEdges.add(e1);
    v0OutEdges.add(e2);
    outEdgeMap.put(v0Id, v0OutEdges);
    ArrayList<TestEdge> v1OutEdges = new ArrayList<TestEdge>();
    v1OutEdges.add(e4);
    v1OutEdges.add(e5);
    outEdgeMap.put(v1Id, v1OutEdges);
    ArrayList<TestEdge> v2OutEdges = new ArrayList<TestEdge>();
    v2OutEdges.add(e3);
    outEdgeMap.put(v2Id, v2OutEdges);
    outEdgeMap.put(v3Id, new ArrayList<TestEdge>());
    
    // Incoming edges.
    HashMap<Integer, ArrayList<TestEdge>> inEdgeMap = new HashMap<Integer, ArrayList<TestEdge>>();
    ArrayList<TestEdge> v0InEdges = new ArrayList<TestEdge>();
    inEdgeMap.put(v0Id, v0InEdges);
    ArrayList<TestEdge> v1InEdges = new ArrayList<TestEdge>();
    v1InEdges.add(e1);
    inEdgeMap.put(v1Id, v1InEdges);
    ArrayList<TestEdge> v2InEdges = new ArrayList<TestEdge>();
    v2InEdges.add(e2);
    v2InEdges.add(e4);
    inEdgeMap.put(v2Id, v2InEdges);
    ArrayList<TestEdge> v3InEdges = new ArrayList<TestEdge>();
    v3InEdges.add(e3);
    v3InEdges.add(e5);
    inEdgeMap.put(v3Id, v3InEdges);
    
    // Test add vertex
    assertEquals(4, g.vSize());
    
    assertTrue(g.hasVertex(0));
    assertEquals(v0, g.getVertex(0));
    assertTrue(g.hasVertex(1));
    assertEquals(v1, g.getVertex(1));
    assertTrue(g.hasVertex(2));
    assertEquals(v2, g.getVertex(2));
    assertTrue(g.hasVertex(3));
    assertEquals(v3, g.getVertex(3));
    
    // Test add Edge
    assertEquals(5, g.eSize());
    
    assertTrue(g.hasEdge(0, 1));
    assertEquals(0, g.getEdge(0, 1).source);
    assertEquals(1, g.getEdge(0, 1).target);
    assertTrue(g.hasEdge(0, 2));
    assertEquals(0, g.getEdge(0, 2).source);
    assertEquals(2, g.getEdge(0, 2).target);
    assertTrue(g.hasEdge(2, 3));
    assertEquals(2, g.getEdge(2, 3).source);
    assertEquals(3, g.getEdge(2, 3).target);
    assertTrue(g.hasEdge(1, 2));
    assertEquals(1, g.getEdge(1, 2).source);
    assertEquals(2, g.getEdge(1, 2).target);
    assertTrue(g.hasEdge(1, 3));
    assertEquals(1, g.getEdge(1, 3).source);
    assertEquals(3, g.getEdge(1, 3).target);
    
    assertFalse(g.hasVertex(5));
    assertFalse(g.hasVertex(6));
    assertFalse(g.hasEdge(0, 3));
    assertFalse(g.hasEdge(2, 1));
    
    // Test out edge iterator.
    for (int v : g.getVertices()) {
      assertEquals(outEdgeMap.get(v).size(), g.outDegree(v));
      Iterator<TestEdge> outEdgeIter = g.iteratorOutEdge(v); 
      while (outEdgeIter.hasNext()) {
        TestEdge e = outEdgeIter.next();
        assertTrue(outEdgeMap.get(v).contains(e));
      }
    }
    
    // Test in edge iterator.
    for (int v : g.getVertices()) {
      assertEquals(inEdgeMap.get(v).size(), g.inDegree(v));
      Iterator<TestEdge> inEdgeIter = g.iteratorInEdge(v); 
      while (inEdgeIter.hasNext()) {
        TestEdge e = inEdgeIter.next();
        assertTrue(inEdgeMap.get(v).contains(e));
      }
    }
  }

}

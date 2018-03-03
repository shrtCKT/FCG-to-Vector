package ml.random;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import malware.feature.FunctionMatcher;
import malware.parse.CallGraph;
import malware.parse.CallGraph.FunctionCallEdge;
import malware.parse.CallGraph.FunctionVertex;
import malware.parse.CallGraph.FunctionVertex.FunctionType;
import util.Pair;

public class GraphStateTest {
  CallGraph<String> graph1;
  CallGraph<String> graph2;

  @Before
  public void setUp() throws Exception {
    graph1 = new CallGraph<>(3);
    graph1.addVertexWithID(1, new FunctionVertex<String>("A", FunctionType.External));
    graph1.addVertexWithID(2, new FunctionVertex<String>("A", FunctionType.External));
    graph1.addVertexWithID(3, new FunctionVertex<String>("B", FunctionType.External));
    graph1.addEdge(1, 2, new FunctionCallEdge(1, 2));
    graph1.addEdge(1, 3, new FunctionCallEdge(1, 3));
    graph1.addEdge(2, 3, new FunctionCallEdge(1, 2));

    graph2 = new CallGraph<>(4);
    graph2.addVertexWithID(1, new FunctionVertex<String>("A", FunctionType.External));
    graph2.addVertexWithID(2, new FunctionVertex<String>("C", FunctionType.External));
    graph2.addVertexWithID(3, new FunctionVertex<String>("A", FunctionType.External));
    graph2.addVertexWithID(4, new FunctionVertex<String>("B", FunctionType.External));
    graph2.addEdge(1, 2, new FunctionCallEdge(1, 2));
    graph2.addEdge(2, 3, new FunctionCallEdge(2, 3));
    graph2.addEdge(1, 4, new FunctionCallEdge(1, 4));
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testApplyChange() {
    fail("Not yet implemented");
  }

  @Test
  public void testUndoChange() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetCost() {
    Pair<Integer, Integer>[] vertexMapping = new Pair[Math.max(graph1.vSize(), graph2.vSize())];
    vertexMapping[0] = new Pair<Integer, Integer>(1, 1);
    vertexMapping[1] = new Pair<Integer, Integer>(2, 2);
    vertexMapping[2] = new Pair<Integer, Integer>(3, 3);
    vertexMapping[3] = new Pair<Integer, Integer>(null, 4);

    GraphState<String> state =
        new GraphState<String>(graph1, graph2, vertexMapping, new FunctionMatcher<String>() {

          @Override
          public boolean matching(String function1Feature, String function2Feature,
              float threashold) {
            return function1Feature.equals(function2Feature);
          }

          @Override
          public double similarity(String function1Feature, String function2Feature) {
            return function1Feature.equals(function2Feature) ? 0 : 1;
          }

        });

    assertEquals(1.0, (double) state.getVertexCost(),
        0.001);
    assertEquals(2.0, (double) state.getEdgeCost(), 0.001);
    assertEquals(2.0, (double) state.getRelabelCost(), 0.001);
  }

}

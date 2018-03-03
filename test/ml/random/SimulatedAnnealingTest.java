package ml.random;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import malware.feature.FunctionMatcher;
import malware.parse.CallGraph;
import malware.parse.CallGraph.FunctionCallEdge;
import malware.parse.CallGraph.FunctionVertex;
import malware.parse.CallGraph.FunctionVertex.FunctionType;
import util.Pair;

public class SimulatedAnnealingTest {
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
  public void testSearch() {
    Pair<Integer, Integer>[] vertexMapping = new Pair[Math.max(graph1.vSize(), graph2.vSize())];
    vertexMapping[0] = new Pair<Integer, Integer>(1, 1);
    vertexMapping[1] = new Pair<Integer, Integer>(2, 2);
    vertexMapping[2] = new Pair<Integer, Integer>(3, 3);
    vertexMapping[3] = new Pair<Integer, Integer>(null, 4);

    FunctionMatcher<String> matcher = new FunctionMatcher<String>() {
      @Override
      public boolean matching(String function1Feature, String function2Feature, float threashold) {
        return function1Feature.equals(function2Feature);
      }

      @Override
      public double similarity(String function1Feature, String function2Feature) {
        return function1Feature.equals(function2Feature) ? 0 : 1;
      }
    };

    GraphState<String> initialState =
        new GraphState<String>(graph1, graph2, vertexMapping, matcher);
        
    System.out.println("initialState");
    System.out.println(initialState);

    SimulatedAnnealing algo = new SimulatedAnnealing();

    GraphState<String> finalState =
        (GraphState<String>) algo.search(initialState, 4.0, 10, 0.2, 15, new Random());

    System.out.println("finalState");
    System.out.println(finalState);
  }
  

  @Test
  public void testSearch2() {
    Pair<Integer, Integer>[] vertexMapping = new Pair[Math.max(graph1.vSize(), graph2.vSize())];
    vertexMapping[0] = new Pair<Integer, Integer>(1, 1);
    vertexMapping[1] = new Pair<Integer, Integer>(2, 3);
    vertexMapping[2] = new Pair<Integer, Integer>(3, 4);
    vertexMapping[3] = new Pair<Integer, Integer>(null, 2);

    FunctionMatcher<String> matcher = new FunctionMatcher<String>() {
      @Override
      public boolean matching(String function1Feature, String function2Feature, float threashold) {
        return function1Feature.equals(function2Feature);
      }

      @Override
      public double similarity(String function1Feature, String function2Feature) {
        return function1Feature.equals(function2Feature) ? 0 : 1;
      }
    };

    GraphState<String> initialState =
        new GraphState<String>(graph1, graph2, vertexMapping, matcher);
        
    System.out.println("initialState");
    System.out.println(initialState);

    SimulatedAnnealing algo = new SimulatedAnnealing();

    GraphState<String> finalState =
        (GraphState<String>) algo.search(initialState, 4.0, 10, 0.2, 15, new Random());

    System.out.println("finalState");
    System.out.println(finalState);
  }
  
  @Test
  public void testSearchRandomState() {
    Pair<Integer, Integer>[] vertexMapping = new Pair[Math.max(graph1.vSize(), graph2.vSize())];
    vertexMapping[0] = new Pair<Integer, Integer>(1, 1);
    vertexMapping[1] = new Pair<Integer, Integer>(2, 2);
    vertexMapping[2] = new Pair<Integer, Integer>(3, 3);
    vertexMapping[3] = new Pair<Integer, Integer>(null, 4);

    FunctionMatcher<String> matcher = new FunctionMatcher<String>() {
      @Override
      public boolean matching(String function1Feature, String function2Feature, float threashold) {
        return function1Feature.equals(function2Feature);
      }

      @Override
      public double similarity(String function1Feature, String function2Feature) {
        return function1Feature.equals(function2Feature) ? 0 : 1;
      }
    };

//    GraphState<String> initialState =
//        new GraphState<String>(graph1, graph2, vertexMapping, matcher);
    GraphState<String> initialState = GraphState.randomState(graph1, graph2, matcher, new Random());
        
    System.out.println("initialState");
    System.out.println(initialState);

    SimulatedAnnealing algo = new SimulatedAnnealing();

    GraphState<String> finalState =
        (GraphState<String>) algo.search(initialState, 4.0, 10, 0.2, 15, new Random());

    System.out.println("finalState");
    System.out.println(finalState);
  }

}

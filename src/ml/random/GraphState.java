package ml.random;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import graph.Graph;
import graph.Vertex;
import malware.feature.FunctionMatcher;
import malware.parse.CallGraph;
import malware.parse.CallGraph.FunctionVertex.FunctionType;
import util.Pair;

/***
 * A state represents a bipartite mapping between vertices of two graph.
 * 
 * @author mehadi
 * 
 * @param <F> function feature data type.
 */
public class GraphState<F> implements State {
  CallGraph<F> graph1;
  CallGraph<F> graph2;
  Pair<Integer, Integer>[] vertexMapping;
  FunctionMatcher<F> matcher;
  private Double vertexCost = null;
  private Double edgeCost = null;
  private Double relabelCost = null;
  HashMap<Integer, Integer> forwardMap;
  HashMap<Integer, Integer> backwardMap;

  public GraphState(CallGraph<F> graph1, CallGraph<F> graph2,
      Pair<Integer, Integer>[] vertexMapping, FunctionMatcher<F> matcher) {
    this.graph1 = graph1;
    this.graph2 = graph2;
    this.vertexMapping = vertexMapping;
    this.matcher = matcher;

    forwardMap = new HashMap<Integer, Integer>(vertexMapping.length);
    backwardMap = new HashMap<Integer, Integer>(vertexMapping.length);
    for (Pair<Integer, Integer> p : vertexMapping) {
      if (p.getFirst() != null) {
        forwardMap.put(p.getFirst(), p.getSecond());
      }
      if (p.getSecond() != null) {
        backwardMap.put(p.getSecond(), p.getFirst());
      }
    }
  }

  /***
   * Generates a state of random bijective mapping between the vertices of the two graphs.
   * 
   * @param graph1
   * @param graph2
   * @param matcher A function similarity metric to be used to calculate the vertex relabling cost.
   *        this similarity metric should be in 0 to 1 scale.
   * @param rng a random number generator.
   * @return A random state.
   */
  public static <F> GraphState<F> randomState(CallGraph<F> graph1, CallGraph<F> graph2,
      FunctionMatcher<F> matcher, Random rng) {
    Pair<Integer, Integer>[] vMapping = new Pair[Math.max(graph1.vSize(), graph2.vSize())];

    // Set the first vertex indices.
    int i = 0;
    for (int v : graph1.getVertices()) {
      if (vMapping[i] == null) {
        vMapping[i] = new Pair<Integer, Integer>(null, null);
      }
      vMapping[i].setFirst(v);
      i++;
    }

    // Set the second vertice indices.
    i = 0;
    for (int u : graph2.getVertices()) {
      if (vMapping[i] == null) {
        vMapping[i] = new Pair<Integer, Integer>(null, null);
      }
      vMapping[i].setSecond(u);
      i++;
    }

    // Shuffle the elements.
    for (int j = 0; j < vMapping.length; j++) {
      int k = rng.nextInt(vMapping.length);
      int l = rng.nextInt(vMapping.length);

      // Swap first
      Integer tmpFirst = vMapping[j].getFirst();
      vMapping[j].setFirst(vMapping[k].getFirst());
      vMapping[k].setFirst(tmpFirst);

      // Swap Second
      Integer tmpSecond = vMapping[j].getSecond();
      vMapping[j].setSecond(vMapping[l].getSecond());
      vMapping[l].setSecond(tmpSecond);
    }

    return new GraphState<F>(graph1, graph2, vMapping, matcher);
  }

  /***
   * The next state in the random search.
   * 
   * @param rng random number generator.
   * @return The change needed to move to next state.
   */
  @Override
  public Change neighbourState(Random rng) {
    int k = rng.nextInt(vertexMapping.length);
    int l = rng.nextInt(vertexMapping.length);

    return new GraphStateChange(k, l);
  }

  /***
   * Apply the given changes to the current state to move to next state.
   * 
   * @param change the change to be applied.
   */
  @Override
  public void applyChange(Change change) {
    GraphStateChange gsc = (GraphStateChange) change;
    // Apply the change
    Integer v1Graph1 = vertexMapping[gsc.getFirstIndex()].getFirst();
    Integer v2Graph1 = vertexMapping[gsc.getSecondIndex()].getFirst();
    Integer u1Graph2 = vertexMapping[gsc.getFirstIndex()].getSecond();
    Integer u2Graph2 = vertexMapping[gsc.getSecondIndex()].getSecond();
    
    edgeCost = getEdgeCost();
    double edgeCostBefore = mappedPairEdgeCost(v1Graph1, v2Graph1, graph1, graph2, 
        forwardMap);
    edgeCostBefore += mappedPairEdgeCost(u1Graph2, u2Graph2, graph2, graph1, 
        backwardMap);
    
    vertexMapping[gsc.getFirstIndex()].setSecond(u2Graph2);
    vertexMapping[gsc.getSecondIndex()].setSecond(u1Graph2);

    if (v1Graph1 != null) {
      forwardMap.put(v1Graph1, u2Graph2);
    }
    if (v2Graph1 != null) {
      forwardMap.put(v2Graph1, u1Graph2);
    }

    if (u1Graph2 != null) {
      backwardMap.put(u1Graph2, v2Graph1);
    }
    if (u2Graph2 != null) {
      backwardMap.put(u2Graph2, v1Graph1);
    }

    double edgeCostAfter = mappedPairEdgeCost(v1Graph1, v2Graph1, graph1, graph2, 
        forwardMap);
    edgeCostAfter += mappedPairEdgeCost(u1Graph2, u2Graph2, graph2, graph1, 
        backwardMap);
    
    // Update edgeCost
//    edgeCost = edgeCost();
    edgeCost = (edgeCost - edgeCostBefore) + edgeCostAfter;

    // Update relabelCost
    double oldRelabeling = 0.0;
    oldRelabeling += vertexPairRelabelingCost(v1Graph1, u1Graph2);
    oldRelabeling += vertexPairRelabelingCost(v2Graph1, u2Graph2);
    relabelCost -= oldRelabeling;

    double newRelabeling = 0.0;
    newRelabeling += vertexPairRelabelingCost(v1Graph1, u2Graph2);
    newRelabeling += vertexPairRelabelingCost(v2Graph1, u1Graph2);
    relabelCost += newRelabeling;
  }

  /***
   * Computes the edge cost associated with unpriserved edges in graph2 due to the given pair 
   * of mappings wich mappes (graph1.V -> graph2.V).
   * 
   * @param v1Graph1 vertex in graph1 that is mapped to vertex a in Graph2.
   * @param v2Graph1 vertex in graph1 that is mapped to vertex a in Graph2.
   * @param graph1 input graph 1.
   * @param graph2 input graph 2.
   * @param bipartiteMapping bipartite map, mapping vertices in input graph1 to vertices in 
   * input graph2.
   * @return the edge cost associated with upriserved edges dues to the givved pair of mappings.
   */
  private double mappedPairEdgeCost(Integer v1Graph1, Integer v2Graph1, 
      CallGraph<F> graph1, CallGraph<F> graph2, HashMap<Integer, Integer> bipartiteMapping) {
    double edgeCost = 0.0;
    
    if (v1Graph1 != null) {
      // Incoming Edges to vertex v1Graph1.
      Iterator<CallGraph.FunctionCallEdge> iter = graph1.iteratorInEdge(v1Graph1);
      while (iter.hasNext()) {
        CallGraph.FunctionCallEdge e = iter.next();
        edgeCost += singleMappingEdgeCost(e, graph1, graph2, bipartiteMapping);
      }
      // Outgoing Edges from vertex v1Graph1.
      iter = graph1.iteratorOutEdge(v1Graph1);
      while (iter.hasNext()) {
        CallGraph.FunctionCallEdge e = iter.next();
        edgeCost += singleMappingEdgeCost(e, graph1, graph2, bipartiteMapping);
      }
    }
    
    if (v2Graph1 != null) {
      // Incoming Edges to vertex v2Graph1.
      Iterator<CallGraph.FunctionCallEdge> iter = graph1.iteratorInEdge(v2Graph1);
      while (iter.hasNext()) {
        CallGraph.FunctionCallEdge e = iter.next();
        edgeCost += singleMappingEdgeCost(e, graph1, graph2, bipartiteMapping);
      }
      // Outgoing Edges from vertex v2Graph1.
      iter = graph1.iteratorOutEdge(v2Graph1);
      while (iter.hasNext()) {
        CallGraph.FunctionCallEdge e = iter.next();
        edgeCost += singleMappingEdgeCost(e, graph1, graph2, bipartiteMapping);
      }
    }
    
    return edgeCost;
  }

  /***
   * Undo the changes given to move to previous state.
   * 
   * @param change
   */
  @Override
  public void undoChange(Change change) {
    applyChange(change); // are same beucase it's just swaping vertices.
  }

  /***
   * 
   * 
   * @return
   */
  @Override
  public double getCost() {
    int esizes = graph1.eSize() + graph2.eSize();
    double vc = vertexMapping.length == 0 ? 0 : getVertexCost() / vertexMapping.length;
    double ec = esizes == 0 ? 0 : getEdgeCost() / esizes;
    double rc = vertexMapping.length == 0 ? 0 : getRelabelCost() / vertexMapping.length;
    
    return vc + ec + rc;
  }

  public Double getVertexCost() {
    if (vertexCost == null) {
      vertexCost = vertexCost();
    }
    return vertexCost;
  }

  public Double getEdgeCost() {
    if (edgeCost == null) {
      edgeCost = edgeCost();
    }
    return edgeCost;
  }

  public Double getRelabelCost() {
    if (relabelCost == null) {
      relabelCost = relabelCost();
    }
    return relabelCost;
  }

  /***
   * Calculates the vertex insertion/deletion cost for the current vertex bipartite mapping.
   * 
   * @return vertex cost.
   */
  private double vertexCost() {
    double vCost = 0.0;
    for (Pair<Integer, Integer> p : vertexMapping) {
      if (p.getFirst() == null || p.getSecond() == null) {
        vCost++;
      }
    }
    // return vCost / Math.max(graph1.vSize(), graph2.vSize());
    return vCost;
  }

  /***
   * Calculates the vertex relabeling cost for the current vertex bipartite mapping.
   * 
   * @return vertex relabeling cost.
   */
  private double relabelCost() {
    double rCost = 0.0;
    for (Pair<Integer, Integer> p : vertexMapping) {
      if (p.getFirst() == null || p.getSecond() == null) {
        continue;
      }

      rCost += vertexPairRelabelingCost(p.getFirst(), p.getSecond());
    }

    return rCost;
  }

  /***
   * Computes the relabling cost for a pair of vertices in the two graphs.
   * 
   * @param v1 vertexId in the first graph.
   * @param u1 vertexId in the second graph.
   * @return vertex pair relabelingCost.
   */
  public double vertexPairRelabelingCost(Integer v1, Integer u1) {
    if (v1 == null || u1 == null) {
      return 0.0; // this considerd an insertion or deletion and is handled by vertexCost().
    }
    if (graph1.getVertex(v1).getType() == FunctionType.External
        && graph2.getVertex(u1).getType() == FunctionType.External) {
      // External functions compared based in name.
      if (graph1.getVertex(v1).getFnName().equals(graph2.getVertex(u1).getFnName())) {
        return 0.0;
      } else {
        return 1.0;
      }
    }
    if ((graph1.getVertex(v1).getType() == FunctionType.External
        && graph2.getVertex(u1).getType() == FunctionType.Internal)
        || (graph1.getVertex(v1).getType() == FunctionType.Internal
            && graph2.getVertex(u1).getType() == FunctionType.External)) {
      return 1.0;
    }

    // Cost is 1 - similarity.
    return 1.0 - matcher.similarity(graph1.getVertex(v1).getFeatures(),
        graph2.getVertex(u1).getFeatures());
  }

  /***
   * Calculates the edge cost for the current vertex bipartite mapping.
   * This is the older way of calculating the edge cost.
   * @return edgeCost.
   */
  @Deprecated
  private double edgeCostOld() {
    if (graph1.eSize() + graph2.eSize() == 0) {
      return 0.0;
    }

    // Unpresenrved Edge count;
    int edgeCount = 0;

    // Find the edges that are in G1 but not preserved in G2.
    for (int v1 : graph1.getVertices()) {
      Integer v2 = forwardMap.get(v1);
      if (v2 == null) { // Case 1: v1 is maped to null. So all edges from v1 are not preserved.
        edgeCount += graph1.getAdjacent(v1).size();
        continue;
      }
      for (int u1 : graph1.getAdjacent(v1)) {
        Integer u2 = forwardMap.get(u1);
        if (u2 == null) { // Case 2: u1 is mapped to null, so edge v1-u1 not preserved in G2.
          edgeCount++;
          continue;
        }
        if (!graph2.hasEdge(v2, u2)) { // Case 3: edge v2-u2 is not in G2 so edge v1-u1 not
                                       // preserved.
          edgeCount++;
        }
        // Case 4: edge is preserved don't do anything.
      }
    }

    // Find the edges that are in G2 but not preserved in G1.
    for (int v2 : graph2.getVertices()) {
      Integer v1 = backwardMap.get(v2);
      if (v1 == null) { // Case 1: v2 is mapped to null. So all edges from v2 are not preserved.
        edgeCount += graph2.getAdjacent(v2).size();
        continue;
      }
      for (int u2 : graph2.getAdjacent(v2)) {
        Integer u1 = backwardMap.get(u2);
        if (u1 == null) { // Case 2: u2 is mapped to null, so edge v2-u2 not preserved in G1.
          edgeCount++;
          continue;
        }
        if (!graph1.hasEdge(v1, u1)) { // Case 3: edge v1-u1 is not in G1 so edge v2-u2 not
                                       // preserved.
          edgeCount++;
        }
        // Case 4: edge is preserved don't do anything.
      }
    }

    // return (double) edgeCount / (graph1.eSize() + graph2.eSize());
    return (double) edgeCount;
  }
  
  /***
   * Calculates the edge cost for the current vertex bipartite mapping.
   * 
   * @return edgeCost.
   */
  private double edgeCost() {
    if (graph1.eSize() + graph2.eSize() == 0) {
      return 0.0;
    }

    // Unpresenrved Edge count;
    int edgeCount = 0;
    
    Iterator<CallGraph.FunctionCallEdge> g1EdgeIterator = graph1.iteratorEdge();
    while (g1EdgeIterator.hasNext()) {
      CallGraph.FunctionCallEdge e = g1EdgeIterator.next();
      edgeCount += singleMappingEdgeCost(e, graph1, graph2, forwardMap);
    }
    
    Iterator<CallGraph.FunctionCallEdge> g2EdgeIterator = graph2.iteratorEdge();
    while (g2EdgeIterator.hasNext()) {
      CallGraph.FunctionCallEdge e = g2EdgeIterator.next();
      edgeCount += singleMappingEdgeCost(e, graph2, graph1, backwardMap);
    }
    
    return (double) edgeCount;
  }
  
  /***
   * Checks if the given edge, in graphA, is preserved in graphB based on the bipartite mapping 
   * between vertices from graphA to graphB.
   * @param edge the edge that exists in the first graph.
   * @param graphA the first graph.
   * @param graphB the second graph.
   * @param bipartiteMapping a bipartite mapping of vertices from graphA to graphB.
   * @return returns 1 if the given edge is unpreserved in graph2, and 0 otherwise.
   */
  public int singleMappingEdgeCost(CallGraph.FunctionCallEdge edge, CallGraph<F> graphA, 
      CallGraph<F> graphB, HashMap<Integer, Integer> biPartiteMapping) {
    Integer sourceB = biPartiteMapping.get(edge.getCaller());
    Integer targetB = biPartiteMapping.get(edge.getCallee());
    
    if (sourceB == null || targetB == null || !graphB.hasEdge(sourceB, targetB)) {
      return 1;
    }
    return 0;
  }

  @Override
  public String toString() {
    StringBuilder buff = new StringBuilder();
    buff.append("Mapping:\n");
    for (Pair<Integer, Integer> p : vertexMapping) {
      buff.append(String.format("%s-%s\n", p.getFirst(), p.getSecond()));
    }
    buff.append(String.format("vCost=%.5f, eCost=%.5f, rCost=%.5f\n", getVertexCost(),
        getEdgeCost(), getRelabelCost()));
    return buff.toString();
  }
}

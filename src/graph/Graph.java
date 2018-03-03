package graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/***
 * Represents a general Directed Graph data structure.
 * 
 * @author mehadi
 *
 * @param <V> Vertex data type
 * @param <E> Edge data type.
 */
public class Graph<V extends Vertex, E> {
  /***
   * Maps vertex ID to vertex object.
   */
  Map<Integer, V> vertices;
  /***
   * Maps source vertex ID to second Map(which maps destination vertex to the edge object.) (Out
   * edges).
   */
  Map<Integer, HashMap<Integer, E>> adjList;
  /***
   * Maps the destination vertex to set of source vertices which have edges incident to this
   * destination vertices(In edges).
   */
  Map<Integer, Set<Integer>> incidentVertices;
  /***
   * The number of edges in the graph.
   */
  int edgeSize;

  public Graph(int initialCapacityV) {
    vertices = new HashMap<Integer, V>(initialCapacityV);
    edgeSize = 0;
  }

  /***
   * Adds a vertex to the graph and returns the vertex ID(index).
   * 
   * @param vertex the vertex to be added.
   * @return the vertex ID(index).
   */
  public int addVertex(V vertex) {
    int index = vertices.size();
    vertices.put(index, vertex);
    return index;
  }

  /***
   * Addes the given vertex to the graph with the given vertexID.
   * 
   * @param vertexID the vertex ID as provided by user.
   * @param vertex the vertex to be added.
   */
  public void addVertexWithID(int vertexID, V vertex) {
    vertices.put(vertexID, vertex);
  }

  /***
   * Add edge from source vertex to destination vertex.
   * 
   * @param from vertex ID for the source vertex.
   * @param to vertex ID for the destination vertex.
   */
  public void addEdge(int from, int to, E edge) {
    if (adjList == null) {
      adjList = new HashMap<Integer, HashMap<Integer, E>>();
    }
    // while (adjList.size() <= from) {
    // adjList.add(new HashMap<Integer, E>());
    // }
    //
    HashMap<Integer, E> adjListV = adjList.get(from);
    if (adjListV == null) {
      adjListV = new HashMap<Integer, E>();
      adjList.put(from, adjListV);
    }

    // HashMap<Integer, E> adjListV = adjList.get(from);
    E previousValue = adjListV.put(to, edge);
    // If there was no such edge before.
    if (previousValue == null) {
      edgeSize++;
    }

    // Update the incedent node relations.
    if (incidentVertices == null) {
      incidentVertices = new HashMap<Integer, Set<Integer>>();
    }
    // update incidentNodes
    Set<Integer> incidentNodeTo = incidentVertices.get(to);
    if (incidentNodeTo == null) {
      incidentNodeTo = new HashSet<Integer>();
      incidentVertices.put(to, incidentNodeTo);
    }
    incidentNodeTo.add(from);
  }

  public void removeVertex(int vertexID) {
    if (!hasVertex(vertexID)) {
      return;
    }
    vertices.remove(vertexID);

    // Remove all outgoing edges, if any.
    adjList.remove(vertexID);

    // Remove all incoming edges, if any.
    Set<Integer> inVertices = incidentVertices.get(vertexID);
    if (inVertices == null) {
      return;
    }
    for (int source : inVertices) {
      removeEdge(source, vertexID);
    }
    incidentVertices.remove(vertexID);
  }

  /***
   * Removes the given edge from the graph.
   * 
   * @param from vertex ID for the source vertex.
   * @param to vertex ID for the destination vertex.
   */
  public void removeEdge(int from, int to) {
    if (adjList == null) {
      return;
    }
    // if (adjList.size() <= from) {
    // return;
    // }

    HashMap<Integer, E> adjListV = adjList.get(from);
    if (adjListV == null) {
      return;
    }

    E removed = adjListV.remove(to);

    // If no edge was removed then dont decrement edge count.
    if (removed == null) {
      return;
    }

    edgeSize--;

    // Update incident node relations.
    if (incidentVertices == null) {
      return;
    }
    Set<Integer> incidentNodeTo = incidentVertices.get(to);
    if (incidentNodeTo == null) {
      return;
    }
    incidentNodeTo.remove(from);
  }

  /***
   * Returns the a vertex in the graph identified by vertexID.
   * 
   * @param vertexID vertext Id(index).
   * @return Null if vertexID not in graph. Otherwise returns the vertex.
   */
  public V getVertex(int vertexID) {
    if (!hasVertex(vertexID)) {
      return null;
    }
    return vertices.get(vertexID);
  }

  /***
   * Retrieves an edge of the graph, if it exists. Otherwise returns null.
   * 
   * @param from source vertex.
   * @param to destination vertex.
   * @return an edge(from, to) of the graph, if it exists. Otherwise returns null.
   */
  public E getEdge(int from, int to) {
    if (adjList == null || !adjList.containsKey(from)) {
      return null;
    }
    return adjList.get(from).get(to);
  }

  /***
   * Returns the out degree of vertex. i.e. The number edges wich have source vertex equal to the
   * given vertex ID.
   * 
   * @param source the source vertext Id(index).
   * @return the degree of vertex.
   */
  public int outDegree(int source) {
    if (!hasVertex(source) || adjList == null) {
      return 0;
    }

    if (!adjList.containsKey(source)) {
      return 0;
    }

    return adjList.get(source).size();
  }

  /***
   * Returns the int degree of vertex. i.e. The number edges wich have destination vertex equal to
   * the given vertex ID.
   * 
   * @param target target vertext Id(index).
   * @return the degree of vertex.
   */
  public int inDegree(int target) {
    if (!hasVertex(target) || incidentVertices == null) {
      return 0;
    }

    if (!incidentVertices.containsKey(target)) {
      return 0;
    }

    return incidentVertices.get(target).size();
  }

  /***
   * Returns the number of vertices currently in the graph.
   * 
   * @return
   */
  public int vSize() {
    return vertices.size();
  }

  /***
   * Returns whether a graph is empty or not. A graph is considered empty if it does not contain any
   * vertices.
   * 
   * @return True if a graph is empty, False otherwise.
   */
  public boolean isEmpty() {
    return vertices == null ? true : vertices.isEmpty();
  }

  /***
   * Returns the number of edges currently in the graph.
   * 
   * @return
   */
  public int eSize() {
    return edgeSize;
  }

  /***
   * Returns whether the vertex with the given vertexID is in the graph or not.
   * 
   * @param vertexID given vertexID in in the graph.
   * @return True the vertex with the given vertexID is in the graph, False otherwise.
   */
  public boolean hasVertex(int vertexID) {
    return vertices.containsKey(vertexID);
  }

  /***
   * Returns whether an edge exists from the vertexID from to vertex to in the graph or not.
   * 
   * @param from from vertexID.
   * @param to to vertexID.
   * @return True the the edge exists, False otherwise.
   */
  public boolean hasEdge(int from, int to) {
    if (adjList == null || !adjList.containsKey(from)) {
      return false;
    }
    return adjList.get(from).containsKey(to);
  }

  /***
   * Returns the adjacent vertex id's for the given vertex. Returns an empty Iterable if the vertex
   * doesnt have neighbours.
   * 
   * @param source the source vertex id.
   * @return
   */
  public Set<Integer> getAdjacent(int source) {
    if (!hasVertex(source) || adjList == null || !adjList.containsKey(source)) {
      return Collections.emptySet();
    }

    return adjList.get(source).keySet();
  }

  /***
   * Returns a collection of vertex IDs who have edges to the given targetVertexID.
   * 
   * @param target the target vertex id.
   * @return
   */
  public Set<Integer> getIncidentVertices(int target) {
    if (!hasVertex(target) || incidentVertices == null || !incidentVertices.containsKey(target)) {
      return Collections.emptySet();
    }

    return incidentVertices.get(target);
  }

  /***
   * Returns an iterator over edges originating at this source vertex.
   * 
   * @param sourceVertexID the source vertex ID.
   * @return an iterator over edges originating at this source vertex.
   */
  public Iterator<E> iteratorOutEdge(int source) {

    class OutEdgeIterator implements Iterator<E> {
      Iterator<Integer> adjVertexIterator;
      int source;
      int lastTarget;

      public OutEdgeIterator(int source) {
        this.source = source;
        Set<Integer> adjListSource = getAdjacent(source);
         
        if (adjListSource == null) {
          adjVertexIterator = Collections.emptyIterator();
        } else {
          adjVertexIterator = adjListSource.iterator();
        }
      }

      @Override
      public boolean hasNext() {
        return adjVertexIterator.hasNext();
      }

      @Override
      public E next() {
        lastTarget = adjVertexIterator.next();
        return getEdge(source, lastTarget);
      }

      @Override
      public void remove() {
        removeEdge(source, lastTarget);
      }

    }

    return new OutEdgeIterator(source);
  }

  /***
   * Returns an iterator over the edges incident to the give target vertex.
   * 
   * @param target the target vertex ID.
   * @returnan iterator over the edges incident to the give target vertex.
   */
  public Iterator<E> iteratorInEdge(int target) {
    class InEdgeIterator implements Iterator<E> {
      Iterator<Integer> incidentVertexIterator;
      int target;
      int lastSource;

      public InEdgeIterator(int target) {
        this.target = target;
        Set<Integer> incidentSet = getIncidentVertices(target);
        if (incidentSet == null) {
          incidentVertexIterator = Collections.emptyIterator();
        } else {
          incidentVertexIterator = incidentSet.iterator();
        }
      }

      @Override
      public boolean hasNext() {
        return incidentVertexIterator.hasNext();
      }

      @Override
      public E next() {
        lastSource = incidentVertexIterator.next();
        return getEdge(lastSource, target);
      }

      @Override
      public void remove() {
        removeEdge(lastSource, target);
      }
    }

    return new InEdgeIterator(target);
  }

  public Iterator<E> iteratorEdge() {
    class EdgeIterator implements Iterator<E> {
      Iterator<Integer> vertexIterator;
      Iterator<Integer> adjVertexIterator;
      int currentSource;
      int lastSource;
      int lastTarget;

      public EdgeIterator() {
        vertexIterator = getVertices().iterator();
        if (vertexIterator.hasNext()) {
          currentSource = vertexIterator.next();
          Set<Integer> adjListV = getAdjacent(currentSource);
          adjVertexIterator = adjListV.iterator();
        }
      }

      @Override
      public boolean hasNext() {
        if (adjVertexIterator == null) {
          return false;
        }
        if (adjVertexIterator.hasNext()) {
          return true;
        }

        while (vertexIterator.hasNext()) {
          currentSource = vertexIterator.next();
          Set<Integer> adjListV = getAdjacent(currentSource);
          adjVertexIterator = adjListV.iterator();
          if (adjVertexIterator.hasNext()) {
            return true;
          }
        }

        return false;
      }

      @Override
      public E next() {
        if (adjVertexIterator == null) {
          return null;
        }

        lastSource = currentSource;
        lastTarget = adjVertexIterator.next();

        return getEdge(currentSource, lastTarget);
      }

      @Override
      public void remove() {
        removeEdge(lastSource, lastTarget);
      }
    }

    return new EdgeIterator();
  }

  public Iterable<Integer> getVertices() {
    return vertices.keySet();
  }
}

package util;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import malware.parse.CallGraph;
import malware.parse.AsmParser.Instruction;
import malware.parse.CallGraph.FunctionCallEdge;
import malware.parse.CallGraph.FunctionVertex;
import malware.parse.CallGraph.FunctionVertex.FunctionType;

/***
 * Provides a set of methods for writing a call graph to file in GML format(Graph Modelling
 * Language).
 * 
 * @author mehadi
 *
 */
public class GMLGraphWriter {
  /***
   * Writes the given call graph to file in GML format.
   * 
   * @param graph the graph to be triten to a file.
   * @param out Output writter. Recommended using BufferedWriter for efficiency.
   * @param writeInstructions If true, writes the instruction sequence.
   * @return true on success.
   * @throws IOException
   */
  public static <F> boolean write(CallGraph<F> graph, Writer out, boolean writeInstructions,
      GMLFeatureReaderWriter<F> featWriter) throws IOException {
    String name = graph.getName();
    if (name == null || name.isEmpty()) {
      System.err.println("ERROR: Graph has no name.");
      return false;
    }
    out.write("graph [\n");
    out.write(String.format("\tname \"%s\"\n", name));
    out.write(String.format("\tvsize %d\n", graph.vSize()));

    String indentation = "\t";
    // Write vertices.
    for (int vid : graph.getVertices()) {
      CallGraph.FunctionVertex<F> vertex = graph.getVertex(vid);
      writeVertex(out, vid, vertex, indentation, writeInstructions, featWriter);
    }

    // Write edges.
    for (int vid : graph.getVertices()) {
      for (int u : graph.getAdjacent(vid)) {
        writeEdge(out, graph.getEdge(vid, u), indentation);
      }
    }

    out.write(String.format("]\n"));

    out.flush();

    return true;
  }

  protected static void writeEdge(Writer out, FunctionCallEdge edge, String indentation)
      throws IOException {
    StringBuilder buff = new StringBuilder();

    buff.append(String.format("%sedge [\n", indentation));
    // Source
    buff.append(String.format("%s\tsource %d\n", indentation, edge.getCaller()));
    // Target
    buff.append(String.format("%s\ttarget %d\n", indentation, edge.getCallee()));

    buff.append(String.format("%s]\n", indentation));

    out.write(buff.toString());
  }

  protected static <F> void writeVertex(Writer out, int vid, FunctionVertex<F> vertex,
      String indentation, boolean writeInstructions, GMLFeatureReaderWriter<F> featWriter) throws IOException {
    StringBuilder buff = new StringBuilder();

    buff.append(String.format("%snode [\n", indentation));
    // Vertex ID
    buff.append(String.format("%s\tid %d\n", indentation, vid));
    // Name
    buff.append(String.format("%s\tlabel \"%s\"\n", indentation, vertex.getFnName()));
    // Type
    buff.append(String.format("%s\ttype \"%s\"\n", indentation, vertex.getType().toString()));

    if (vertex.getType() == FunctionType.External) {
      buff.append(String.format("%s]\n", indentation));
      out.write(buff.toString());
      return;
    }

    
    // Feature
    buff.append(featWriter.write(vertex.getFeatures(), indentation));
    
//    buff.append(String.format("%s\tfeatures [\n", indentation));
//    for (int i = 0; i < vertex.getFeatures().length; i++) {
//      buff.append(String.format("%s\t\tval \"", indentation));
//      buff.append(vertex.getFeatures()[i]);
//      buff.append("\"\n");
//    }
//    buff.append(String.format("%s\t]\n", indentation));

    if (writeInstructions && vertex.getInstructions() != null) {
      // Instruction
      buff.append(String.format("%s\tinstructions ", indentation));
      for (int i = 0; i < vertex.getInstructions().size(); i++) {
        if (i > 0) {
          buff.append(",");
        }
        Instruction inst = vertex.getInstructions().get(i);
        buff.append(String.format("\"%s:%s\"", inst.getOpcode() == null ? "" : inst.getOpcode(),
            inst.getAsm() == null ? "" : inst.getAsm()));
      }
      buff.append("\n");
    }

    buff.append(String.format("%s]\n", indentation));

    out.write(buff.toString());
  }
}

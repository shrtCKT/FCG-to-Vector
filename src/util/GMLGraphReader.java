package util;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import malware.parse.CallGraph;
import malware.parse.CallGraph.FunctionCallEdge;
import malware.parse.CallGraph.FunctionVertex;
import malware.parse.CallGraph.FunctionVertex.FunctionType;

public class GMLGraphReader {
  private static Pattern startGraphPat = Pattern.compile("[Gg][Rr][Aa][Pp][Hh]\\s+\\[");
  private static Pattern endPat = Pattern.compile("\\s*\\]\\s*");

  // Graph keys
  private static Pattern graphNamePat = Pattern.compile("\\s*name\\s+\"(\\S+)\"\\s*");
  private static Pattern graphVSizePat = Pattern.compile("\\s*vsize\\s+(\\d+)\\s*");
  private static Pattern graphNodePat = Pattern.compile("\\s*node\\s+\\[\\s*");
  private static Pattern graphEdgePat = Pattern.compile("\\s*edge\\s+\\[\\s*");

  // Node keys
  private static Pattern nodeVidPat = Pattern.compile("\\s*id\\s+(\\d+)\\s*");
  private static Pattern nodeNamePat = Pattern.compile("\\s*label\\s+\"(.*)\"\\s*");
  private static Pattern nodeTypePat = Pattern.compile("\\s*type\\s+\"(\\S+)\"\\s*");
  // private static Pattern nodeFeaturePat = Pattern.compile("\\s*features\\s+(?:,?(\\d+))+\\s*");
  private static Pattern nodeFeaturePat = Pattern.compile("\\s*features\\s+\\[\\s*");

  // Edge keys
  private static Pattern edgeFromPat = Pattern.compile("\\s*source\\s+(\\d+)\\s*");
  private static Pattern edgeToPat = Pattern.compile("\\s*target\\s+(\\d+)\\s*");

  /***
   * Reads the give input stream of GML formated file. If there are multiple graphs in the file then
   * returns the first graph.
   * 
   * @param input input stream of GML formated file. Recommended: use BufferedInputStream.
   * @return CallGraph representation. Otherwise returns null if reading was not successful.
   */
  public static <F> CallGraph<F> read(InputStream input, GMLFeatureReaderWriter<F> featureReader) {
    Scanner in = new Scanner(input);

    while (in.hasNextLine()) {
      String line = in.nextLine();
      Matcher startGraphMat = startGraphPat.matcher(line);

      if (startGraphMat.matches()) {
        // read the graph structure
        CallGraph<F> graph = readCallGraph(in, featureReader);
        return graph;
      }
    }

    return null;
  }

  private static <F> CallGraph<F> readCallGraph(Scanner in,
      GMLFeatureReaderWriter<F> featureReader) {
    int initialCapacityV = 10;
    CallGraph<F> graph = new CallGraph<F>(initialCapacityV);

    while (in.hasNextLine()) {
      String line = in.nextLine();
      Matcher graphNameMat = graphNamePat.matcher(line);
      Matcher graphVSizeMat = graphVSizePat.matcher(line);
      Matcher graphNodeMat = graphNodePat.matcher(line);
      Matcher graphEdgeMat = graphEdgePat.matcher(line);
      Matcher endMat = endPat.matcher(line);

      if (graphNameMat.matches()) {
        graph.setName(graphNameMat.group(1));
      } else if (graphVSizeMat.matches()) {
        // VSize is stored in GML for referance purposes only.
        continue;
      } else if (graphNodeMat.matches()) {
        Pair<Integer, CallGraph.FunctionVertex<F>> vertexPair = readVertex(in, featureReader);
        if (vertexPair != null) {
          graph.addVertexWithID(vertexPair.getFirst(), vertexPair.getSecond());
        }
      } else if (graphEdgeMat.matches()) {
        CallGraph.FunctionCallEdge edge = readEdge(in);
        if (edge != null) {
          graph.addEdge(edge.getCaller(), edge.getCallee(), edge);
        }
      } else if (endMat.matches()) {
        return graph;
      }
    }

    return null;
  }

  private static FunctionCallEdge readEdge(Scanner in) {
    Integer from = null;
    Integer to = null;

    while (in.hasNextLine()) {
      String line = in.nextLine();
      Matcher edgeFromMat = edgeFromPat.matcher(line);
      Matcher edgeToMat = edgeToPat.matcher(line);
      Matcher endMat = endPat.matcher(line);

      if (edgeFromMat.matches()) {
        from = Integer.parseInt(edgeFromMat.group(1));
      } else if (edgeToMat.matches()) {
        to = Integer.parseInt(edgeToMat.group(1));
      } else if (endMat.matches()) {
        if (from != null && to != null) {
          return new FunctionCallEdge(from, to);
        }
      }
    }

    return null;
  }

  private static <F> Pair<Integer, CallGraph.FunctionVertex<F>> readVertex(Scanner in,
      GMLFeatureReaderWriter<F> featureReader) {
    Integer vid = null;
    String name = null;
    FunctionType type = null;
    F features = null;

    String external = FunctionType.External.toString();
    String internal = FunctionType.Internal.toString();

    while (in.hasNextLine()) {
      String line = in.nextLine();
      Matcher nodeVidMat = nodeVidPat.matcher(line);
      Matcher nodeNameMat = nodeNamePat.matcher(line);
      Matcher nodeTypeMat = nodeTypePat.matcher(line);
      Matcher nodeFeatureMat = nodeFeaturePat.matcher(line);
      Matcher endMat = endPat.matcher(line);

      if (nodeVidMat.matches()) {
        vid = Integer.parseInt(nodeVidMat.group(1));
      } else if (nodeNameMat.matches()) {
        name = nodeNameMat.group(1);
      } else if (nodeTypeMat.matches()) {
        if (external.equals(nodeTypeMat.group(1))) {
          type = FunctionType.External;
        } else if (internal.equals(nodeTypeMat.group(1))) {
          type = FunctionType.Internal;
        }
      } else if (nodeFeatureMat.matches()) {
        // features = readLongArray(in);
        features = featureReader.read(in, endPat);
      } else if (endMat.matches()) {
        if (type == FunctionType.External && vid != null && name != null) {
          CallGraph.FunctionVertex<F> vertex = new FunctionVertex<F>(name, type);
          return new Pair<Integer, CallGraph.FunctionVertex<F>>(vid, vertex);
        } else
          if (type == FunctionType.Internal && name != null && vid != null && features != null) {
          CallGraph.FunctionVertex<F> vertex = new FunctionVertex<F>(name, features, type);
          return new Pair<Integer, CallGraph.FunctionVertex<F>>(vid, vertex);
        } else {
          return null;
        }
      }
    }

    return null;
  }

  // private static long[] readLongArray(Scanner in) {
  // List<Long> valueList = new LinkedList<>();
  // while (in.hasNextLine()) {
  // String line = in.nextLine();
  // Matcher nodeFeatureValueMat = nodeFeatureValuePat.matcher(line);
  // Matcher endMat = endPat.matcher(line);
  // if (nodeFeatureValueMat.matches()) {
  // valueList.add(Long.parseLong(nodeFeatureValueMat.group(1)));
  // } else if (endMat.matches()) {
  // long[] valueArray = new long[valueList.size()];
  // int i = 0;
  // for (long l : valueList) {
  // valueArray[i++] = l;
  // }
  // return valueArray;
  // }
  // }
  // return null;
  // }
}

package util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import malware.feature.FeatureExtractor;
import malware.parse.AsmParser.Instruction;
import malware.parse.CallGraph;
import malware.parse.CallGraph.FunctionCallEdge;
import malware.parse.CallGraph.FunctionVertex;
import malware.parse.Function;

public class GMLGraphReaderWriterTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testWrite() throws IOException {
    List<Function> functions = new ArrayList<Function>();
    Function f1 = new Function("F1");
    functions.add(f1);
    f1.addNameAlias("aliasF1");
    f1.addcalledFunction("external1"); // E: 0,4

    Function f2 = new Function("F2");
    functions.add(f2);
    f2.addcalledFunction(f1.getName()); // E: 1,0

    Function f3 = new Function("F3");
    functions.add(f3);
    f3.addcalledFunction("alias" + f1.getName()); // E: 2,0
    f3.addcalledFunction("external2"); // E: 2,5

    Function f4 = new Function("F4");
    functions.add(f4);
    f4.addcalledFunction("external1"); // E: 3,4

    CallGraph<long[]> cg =
        CallGraph.build(functions, new FeatureExtractor<long[], List<Instruction>>() {
          // Dummy FeatureExtractor.
          @Override
          public long[] extractFeatures(List<Instruction> function) {
            long[] arr = {1, 2};
            return arr;
          }
        }, true);

    cg.setName("cg1");

    StringWriter out = new StringWriter();
    GMLGraphWriter.write(cg, out, false, new GMLLongArrayFeatureReaderWriter());
    System.out.println(out.toString());

    CallGraph<long[]> cgCopy = GMLGraphReader.read(
        new ByteArrayInputStream(out.toString().getBytes()), new GMLLongArrayFeatureReaderWriter());

    assertNotNull(cgCopy);
    assertEquals(cg.vSize(), cgCopy.vSize());
    for (int v : cg.getVertices()) {
      compareVertex(cg.getVertex(v), cgCopy.getVertex(v));
      for (int u : cg.getAdjacent(v)) {
        compareEdge(cg.getEdge(v, u), cgCopy.getEdge(v, u));
      }
    }
  }

  private void compareVertex(FunctionVertex<long[]> vertex1, FunctionVertex<long[]> vertex2) {
    if (vertex1 == null) {
      assertNull(vertex2);
      return;
    }
    if (vertex2 == null) {
      assertNull(vertex1);
    }

    assertEquals(vertex1.getFnName(), vertex2.getFnName());
    assertEquals(vertex1.getType(), vertex2.getType());
    assertArrayEquals(vertex1.getFeatures(), vertex2.getFeatures());
  }

  private void compareEdge(FunctionCallEdge edge1, FunctionCallEdge edge2) {
    if (edge1 == null) {
      assertNull(edge2);
      return;
    }
    if (edge2 == null) {
      assertNull(edge1);
    }

    assertEquals(edge1.getCaller(), edge2.getCaller());
    assertEquals(edge1.getCallee(), edge2.getCallee());
  }

}

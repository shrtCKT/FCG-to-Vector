

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import malware.parse.CallGraph;
import malware.parse.CallGraph.FunctionVertex.FunctionType;
import ml.cluster.CosineLSHCluster;
import ml.cluster.LSHCluster;
import ml.cluster.LSHCluster.SecondLevelHash;
import ml.cluster.MinhashCluster;
import util.CommandLineArguments;
import util.FileUtil;
import util.GMLBitSetFeatureReaderWriter;
import util.GMLFeatureReaderWriter;
import util.GMLLongArrayFeatureReaderWriter;

/***
 * Converts a call graph representation into a vector representations. This feature vector is the
 * vertex and edge frequencies. each vertex is represented by cluster ID.
 * 
 * @author meha
 *
 */
public class CallGraphToFeatureVector {

  // Universal vertex clusterID
  Set<Integer> universalVertexBucketID;
  // Universal edge clusterID
  Set<String> universalEdgeBucketID;

  public static void main(String[] args) throws IOException {
    CommandLineArguments cmdArgs = new CommandLineArguments();
    cmdArgs.addOption(true, "-label", true); // Class label file
    cmdArgs.addOption(true, "-in", true); // Input file path
    cmdArgs.addOption(true, "-out", true); // Output file path
    cmdArgs.addOption(true, "-bucket_bits", true); // hash index modulo number of bits of the number
    cmdArgs.addOption(false, "-files_ext", true); // file extension.
    cmdArgs.addOption(false, "-max_files", true); // max number of files to process.
    cmdArgs.addOption(false, "-vertex", false); // extract vertex features only.
    cmdArgs.addOption(false, "-edge", false); // extract edge features only.
    cmdArgs.addOption(false, "-tfidf", false); // enable extracting vertex and edge tf-idf feature
                                               // instead of raw frequecy.
    cmdArgs.addOption(false, "-second_hash", true); // The second level hash function for
                                                    // clustering.
    cmdArgs.addOption(false, "-num_hash", true); // number of minhash hash values to use if using
                                                 // only subset of the values is required.
    cmdArgs.addOption(false, "-cluster_algo", true); // The LSH clustring algo to used.
    cmdArgs.addOption(false, "-cosine_sign_bits", true); // number of bits used for he input
                                                         // cosine lsh signature.
    cmdArgs.addOption(false, "-band_bits", true); // number of bits in asingle band of
                                                  // cosine lsh signature

    if (!cmdArgs.parseCommandLineArgs(args)) {
      System.out.println("Usage:\n\tjava CallGraphToFeatureVector "
          + " -label class_label_file -in input_file -out output_file "
          + " -bucket_bits culster_bucket_bits [-cluster_algo minhash(default)|cosine] "
          + " [-max_files max_number_of_files_to_process] "
          + " [-second_hash (java|simhash) default=java]  [-band_bits bits per band default=16] "
          + " [-files_ext file_extension_graph_data_file] [-vertex] [-edge] [-tfidf]\n");
      return;
    }

    String classLabelPath = cmdArgs.getOptionValue("-label");
    String inputFile = cmdArgs.getOptionValue("-in");
    String outputFile = cmdArgs.getOptionValue("-out");
    String files_ext = cmdArgs.getOptionValue("-files_ext") == null ? ".gml"
        : cmdArgs.getOptionValue("-files_ext");
    Integer max_files = cmdArgs.getOptionValue("-max_files") == null ? -1
        : Integer.parseInt(cmdArgs.getOptionValue("-max_files"));
    Integer bucket_bits = Math.min(Integer.parseInt(cmdArgs.getOptionValue("-bucket_bits")), 30);
    boolean vertexFeatures = cmdArgs.isOptionFound("-vertex");
    boolean edgeFeatures = cmdArgs.isOptionFound("-edge");
    boolean enableTfIdfFeature = cmdArgs.isOptionFound("-tfidf");
    String secondH = cmdArgs.getOptionValue("-second_hash");
    String clusterA = cmdArgs.getOptionValue("-cluster_algo") == null ? "minhash"
        : cmdArgs.getOptionValue("-cluster_algo");
    SecondLevelHash secondHash;
    if (secondH == null || "java".equals(secondH)) {
      secondHash = SecondLevelHash.JavaInBuilt;
    } else if ("simhash".equals(secondH)) {
      secondHash = SecondLevelHash.SimHash;
    } else {
      System.err.printf("Unknown Second level hash '%s'.\n", secondH);
      return;
    }
    Integer numHashFn = cmdArgs.getOptionValue("-num_hash") == null ? -1
        : Integer.parseInt(cmdArgs.getOptionValue("-num_hash"));
    int numBitsSignature = cmdArgs.getOptionValue("-cosine_sign_bits") == null ? 1024
        : Integer.parseInt(cmdArgs.getOptionValue("-cosine_sign_bits"));
    Integer bandBits = cmdArgs.getOptionValue("-band_bits") == null ? 16
        : Integer.parseInt(cmdArgs.getOptionValue("-band_bits"));


    if (!vertexFeatures && !edgeFeatures) {
      vertexFeatures = true;
      edgeFeatures = true;
    }

    File inputDir = new File(inputFile);
    if (!inputDir.isDirectory()) {
      System.err.println("Input path is not a directory.");
      return;
    }
    File classLabelFile = new File(classLabelPath);
    if (!classLabelFile.isFile() || !classLabelFile.exists()) {
      System.err.println("Class Label path is not a file or does not exist.");
      return;
    }
    File outFile = new File(outputFile);
    if (outFile.isDirectory()) {
      System.err.println("Output path is not a file.");
      return;
    }

    CallGraphToFeatureVector convertor = new CallGraphToFeatureVector();
    GraphVector[] gVectors = null;
    if (clusterA.equals("minhash")) {
      MinhashCluster clusteringAlgo = new MinhashCluster(bucket_bits, secondHash, numHashFn);
      GMLLongArrayFeatureReaderWriter featureReader = new GMLLongArrayFeatureReaderWriter();
      gVectors = convertor.extractGraphVectorFeatures(bucket_bits, inputDir, files_ext, max_files,
          featureReader, clusteringAlgo);
    } else if (clusterA.equals("cosine")) {
      CosineLSHCluster clusteringAlgo =
          new CosineLSHCluster(bucket_bits, secondHash, numBitsSignature, bandBits);
      GMLBitSetFeatureReaderWriter featureReader = new GMLBitSetFeatureReaderWriter();
      gVectors = convertor.extractGraphVectorFeatures(bucket_bits, inputDir, files_ext, max_files,
          featureReader, clusteringAlgo);
    } else {
      System.err.println("Selecte a supported clustering LSH algo.");
      return;
    }

    convertor.writerToARFF(gVectors, outFile, classLabelFile, vertexFeatures, edgeFeatures,
        enableTfIdfFeature);
  }

  /***
   * In this LSH implementation the entire minhash signature is treated as a single band.
   * Recommended way of using this is to run this multimple times.
   * 
   * @param bucketBits determins the number of clusters. number of clusters = 2 ^ (bucketBits + 1)
   * @param inDir path to input directory containing graph files.
   * @param extension file extention of the graph files.
   * @param max_files maximum number of files to process.
   * @param featureReader feature reader object.
   * @param clusterAlgo LSH clustering algorithm.
   * @return Array of graph vector represeantations.
   * @throws FileNotFoundException
   */
  public <F> GraphVector[] extractGraphVectorFeatures(Integer bucketBits, File inDir,
      String extension, Integer max_files, GMLFeatureReaderWriter<F> featureReader,
      LSHCluster<Integer, F> clusterAlgo) throws FileNotFoundException {
    File[] files = FileUtil.listFiles(extension, inDir);

    GraphVector[] gVectors =
        new GraphVector[max_files < 0 ? files.length : Math.min(files.length, max_files)];

    // Universal vertex clusterID
    universalVertexBucketID = new HashSet<Integer>();
    // Universal edge clusterID
    universalEdgeBucketID = new HashSet<String>();

    // GMLLongArrayFeatureReaderWriter featureReader = new GMLLongArrayFeatureReaderWriter();

    for (int f = 0; f < gVectors.length; f++) {
      if (f % 50 == 0) {
        System.out.println("Processed " + f);
      }
      CallGraph<F> graph = util.GMLGraphReader
          .read(new BufferedInputStream(new FileInputStream(files[f])), featureReader);
      graph.setName(files[f].getName().replaceFirst("[.][^.]+$", ""));

      gVectors[f] = new GraphVector(graph.getName());

      // For each vertex
      for (Integer v : graph.getVertices()) {
        Integer clusterID;
        if (graph.getVertex(v).getType() == FunctionType.Internal) {
          clusterID = clusterAlgo.clusterId(graph.getVertex(v).getFeatures());
        } else {
          clusterID = graph.getVertex(v).getFnName().hashCode() % (1 << bucketBits);
        }
        universalVertexBucketID.add(clusterID);
        gVectors[f].incrementVertexFrequency(clusterID);
        graph.getVertex(v).setFnName(Integer.toString(clusterID));
      }

      // For each edge.
      for (Integer v : graph.getVertices()) {
        for (Integer u : graph.getAdjacent(v)) {
          // if (graph.getVertex(u).getType() == FunctionType.Internal) {
          // continue; // Hence only consider edges from internal functions to external functions.
          // }
          // Note that the function names have been renamed to the hash bucketIndex.
          String edgeBucket = String.format("%s-%s", graph.getVertex(v).getFnName(),
              graph.getVertex(u).getFnName());
          universalEdgeBucketID.add(edgeBucket);
          gVectors[f].incrementEdgeFrequency(edgeBucket);
        }
      }
    }

    return gVectors;
  }

  public void writerToARFF(GraphVector[] gVectors, File outFile, File classLabelFile,
      boolean vertexFeatures, boolean edgeFeatures, boolean enableTfIdf) throws IOException {
    BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
    // write arff header.

    out.write(String.format("@RELATION call_graph_features\n"));
    out.write(String.format("@ATTRIBUTE ID STRING\n"));

    if (vertexFeatures) {
      for (Integer vBucket : universalVertexBucketID) {
        out.write(String.format("@ATTRIBUTE %d NUMERIC\n", vBucket));
      }
    }

    if (edgeFeatures) {
      for (String eBucket : universalEdgeBucketID) {
        out.write(String.format("@ATTRIBUTE %s NUMERIC\n", eBucket));
      }
    }

    // Writing classes 
    Map<String, String> classLabels =
        malware.data.MicrosoftDatasetReaders.readClassLables(classLabelFile);
    
    out.write("@ATTRIBUTE class  {");
    HashSet<String> classes = new HashSet<String>();
    for (String c : classLabels.values()) {
      if (classes.contains(c)) {
        continue;
      }
      classes.add(c);
      if (classes.size() > 1) {
        out.write(",");
      }
      out.write(c);
    }
    out.write("}\n");
    
    // Writing Data
    out.write(String.format("\n@DATA\n"));

    HashMap<Integer, Integer> vDocFreq = null;
    HashMap<String, Integer> eDocFreq = null;

    // If we want TF-IDF feature instead of raw frequency feature.
    if (enableTfIdf) {
      vDocFreq = new HashMap<Integer, Integer>(universalVertexBucketID.size());

      eDocFreq = new HashMap<String, Integer>(universalEdgeBucketID.size());

      for (GraphVector gv : gVectors) {
        for (Integer vBucket : universalVertexBucketID) {
          if (gv.getVertexFrequency(vBucket) > 0) {
            Integer count = vDocFreq.get(vBucket);
            if (count == null) {
              count = 0;
            }
            vDocFreq.put(vBucket, count + 1);
          }
        }

        for (String eBucket : universalEdgeBucketID) {
          if (gv.getEdgeFrequency(eBucket) > 0) {
            Integer count = eDocFreq.get(eBucket);
            if (count == null) {
              count = 0;
            }
            eDocFreq.put(eBucket, count + 1);
          }
        }
      }
    }

    // Write arff body.
    for (GraphVector gv : gVectors) {
      String featureVector;

      featureVector = toFeatureVector(gv, classLabels, vertexFeatures, edgeFeatures, vDocFreq,
          eDocFreq, gVectors.length);

      out.write(featureVector);
      out.write("\n");
    }

    out.flush();
    out.close();
  }

  /***
   * Returns to a comma separated feature vector string.
   * 
   * @param graphVector GraphVector object holding vertex and edge frequencies.
   * @param classLabels File path to class label file.
   * @param vertexFeatures If true, include vertex features.
   * @param edgeFeatures If true, include edge features.
   * @param vDocFreq Optional, the document frequency for the vertices.
   * @param eDocFreq Optional, the document frequency for the edges.
   * @param numSamples
   * @return
   */
  private String toFeatureVector(GraphVector graphVector, Map<String, String> classLabels,
      boolean vertexFeatures, boolean edgeFeatures, HashMap<Integer, Integer> vDocFreq,
      HashMap<String, Integer> eDocFreq, int numSamples) {
    StringBuilder buff = new StringBuilder();

    buff.append(graphVector.getName());

    if (vertexFeatures) {
      for (Integer vBucket : universalVertexBucketID) {
        buff.append(",");
        if (vDocFreq == null) {
          buff.append(graphVector.getVertexFrequency(vBucket));
        } else {
          double tf = graphVector.getVertexFrequency(vBucket) == 0 ? 0
              : (1 + Math.log(graphVector.getVertexFrequency(vBucket)));
          double idf = Math.log(1 + (vDocFreq.get(vBucket) == null ? 0 : vDocFreq.get(vBucket)));

          buff.append(tf * idf);
        }
      }
    }

    if (edgeFeatures) {
      for (String eBucket : universalEdgeBucketID) {
        buff.append(",");
        if (eDocFreq == null) {
          buff.append(graphVector.getEdgeFrequency(eBucket));
        } else {
          double tf = graphVector.getEdgeFrequency(eBucket) == 0 ? 0
              : (1 + Math.log(graphVector.getEdgeFrequency(eBucket)));
          double idf = Math.log(numSamples + 1)
              - Math.log(1 + (eDocFreq.get(eBucket) == null ? 0 : eDocFreq.get(eBucket)));

          buff.append(tf * idf);
        }
      }
    }

    buff.append(",");
    buff.append(classLabels.get(graphVector.getName()));

    return buff.toString();
  }

  /***
   * Graph vector representation.
   * 
   * @author meha
   *
   */
  public static class GraphVector {

    HashMap<Integer, Integer> vFrequencyMap;

    HashMap<String, Integer> eFrequencyMap;

    private String name;

    public GraphVector(String name) {
      this.name = name;
      vFrequencyMap = new HashMap<Integer, Integer>();
      eFrequencyMap = new HashMap<String, Integer>();
    }

    public int getEdgeFrequency(String edgeBucket) {
      Integer count = eFrequencyMap.get(edgeBucket);
      if (count == null) {
        return 0;
      }
      return count;
    }

    public int getVertexFrequency(Integer bucketIndex) {
      Integer count = vFrequencyMap.get(bucketIndex);
      if (count == null) {
        return 0;
      }
      return count;
    }

    public void incrementEdgeFrequency(String edgeLabel) {
      Integer count = eFrequencyMap.get(edgeLabel);
      if (count == null) {
        count = 0;
      }
      eFrequencyMap.put(edgeLabel, count + 1);
    }

    public void incrementVertexFrequency(Integer vertexLabel) {
      Integer count = vFrequencyMap.get(vertexLabel);
      if (count == null) {
        count = 0;
      }
      vFrequencyMap.put(vertexLabel, count + 1);
    }

    public Object getName() {
      return name;
    }
  }

}

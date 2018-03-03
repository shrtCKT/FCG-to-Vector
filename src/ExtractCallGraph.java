

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import malware.feature.CosineLSHLongFeatureExtractor;
import malware.feature.FeatureExtractor;
import malware.feature.HashedNgramBooleanFeatureExtractor;
import malware.feature.HashedNgramFrequencyFeatureExtractor;
import malware.feature.InstructionFeatureExtractor;
import malware.feature.MinHashFeatureExtractor;
import malware.feature.NGramFreqFeatureExtractor;
import malware.parse.AsmParser;
import malware.parse.CallGraph;
import malware.parse.Function;
import malware.parse.AsmParser.AsmMode;
import malware.parse.AsmParser.Instruction;
import util.CommandLineArguments;
import util.FileUtil;
import util.GMLBitSetFeatureReaderWriter;
import util.GMLDoubleArrayFeatureReaderWriter;
import util.GMLFeatureReaderWriter;
import util.GMLGraphReader;
import util.GMLGraphWriter;
import util.GMLInstructuinListFeatureReaderWriter;
import util.GMLLongArrayFeatureReaderWriter;
import util.hash.MurmurHashWrapper;
import util.thread.Counter;
import util.thread.FixedBlockingThreadPoolExecutor;

/**
 * Extracts Function Call Graphs from disassembled code.
 * 
 * @author meha
 *
 */
public class ExtractCallGraph {

  public static void main(String[] args) throws IOException {
    CommandLineArguments cmdArgs = new CommandLineArguments();
    cmdArgs.addOption(true, "-label", true); // Class label file
    cmdArgs.addOption(true, "-in", true); // Input file path
    cmdArgs.addOption(true, "-out", true); // Output file path
    cmdArgs.addOption(false, "-hbits", true); // Hash length in terms number of bits
    cmdArgs.addOption(false, "-num_hashfn", true); // number of hash functions
    cmdArgs.addOption(true, "-mode", true); // opcode | asm
    cmdArgs.addOption(true, "-max_files", true); // max number of files to process.
    cmdArgs.addOption(false, "-v", false); // verbous
    cmdArgs.addOption(false, "-inst", false); // Write instructions for each function.
    cmdArgs.addOption(false, "-feat", true); // Feature extractor.
    cmdArgs.addOption(false, "-ngram_len", true); // ngram length.
    cmdArgs.addOption(false, "-num_plane", true); // number of random hyper planes.
    cmdArgs.addOption(false, "-ngram_list", true); // csv file, with NO header. (ngram,index)
    cmdArgs.addOption(false, "-thread", true); // Number of threads (default = 4).
    cmdArgs.addOption(false, "-no_db", false); // if set, will not include DB,DD,DW... instructions.
    cmdArgs.addOption(false, "-ext", true); // Input file extention are (asm|gml). Default is asm.

    if (!cmdArgs.parseCommandLineArgs(args)) {
      System.out.println("Usage:\n\tjava ExtractCallGraph "
          + " -label class_label_file -in input_file -out output_file -hbits number_hash_bits "
          + " -num_hashfn number -mode (opcode|asm) " + " -max_files max_number_of_files_to_process"
          + " [-v] [-inst] "
          + " [-feat feature_extractor. minhash(default)|jaccard|cosine|inst|lshcosine|new_jaccard]"
          + " [-ngram_len nhram_length(default=1)]  "
          + " [-num_plane number_hyperplanes_cosine_lsh(default=1024)]"
          + " [-ngram_list csv_file(ngram,index)_ngram_list] "
          + " [-thread number_threads_run(default=4)]" + " [-no_db]" + " [-ext]\n");
      return;
    }

    String classLabelFile = cmdArgs.getOptionValue("-label");
    String inputFile = cmdArgs.getOptionValue("-in");
    String outputFile = cmdArgs.getOptionValue("-out");
    int numHashBits = Integer.parseInt(cmdArgs.getOptionValue("-hbits"));
    int numHashFunctions = cmdArgs.getOptionValue("-num_hashfn") == null
        ? 3
        : Integer.parseInt(cmdArgs.getOptionValue("-num_hashfn"));
    int ngramLength = cmdArgs.getOptionValue("-ngram_len") == null
        ? 1
        : Integer.parseInt(cmdArgs.getOptionValue("-ngram_len"));
    AsmMode asmMode = cmdArgs.getOptionValue("-mode") == null
        ? AsmMode.OPCODE
        : (cmdArgs.getOptionValue("-mode").equals("asm") ? AsmMode.ASM : AsmMode.OPCODE);
    int maxFiles = Integer.parseInt(cmdArgs.getOptionValue("-max_files"));
    boolean verbose = cmdArgs.isOptionFound("-v");
    boolean writeInstructions = cmdArgs.isOptionFound("-inst");
    String feat =
        cmdArgs.getOptionValue("-feat") == null ? "minhash" : cmdArgs.getOptionValue("-feat");
    int numHyperPlanes = cmdArgs.getOptionValue("-num_plane") == null
        ? 1024
        : Integer.parseInt(cmdArgs.getOptionValue("-num_plane"));
    int thread = cmdArgs.getOptionValue("-thread") == null
        ? 4
        : Integer.parseInt(cmdArgs.getOptionValue("-thread"));
    String ngramListPath = cmdArgs.getOptionValue("-ngram_list");
    boolean includeDBInstructions = !cmdArgs.isOptionFound("-no_db"); // Taking the negation.
    String ext = cmdArgs.getOptionValue("-ext") == null ? ".asm" : cmdArgs.getOptionValue("-ext");
    ext = ext.matches("^\\.\\S+") ? ext : "." + ext;

    FeatureExtractor extractor;
    GMLFeatureReaderWriter featureWriter = null;
    if (feat.equals("minhash")) {
      extractor = new MinHashFeatureExtractor(asmMode, numHashFunctions,
          new Random(Long.getLong("seed", System.currentTimeMillis())), numHashBits, ngramLength);
      featureWriter = new GMLLongArrayFeatureReaderWriter();
    } else if (feat.equals("cosine")) {
      featureWriter = new GMLDoubleArrayFeatureReaderWriter();
      extractor = new HashedNgramFrequencyFeatureExtractor(numHashBits, ngramLength, true,
          AsmMode.OPCODE, new MurmurHashWrapper((int) System.nanoTime()));
    } else if (feat.equals("jaccard")) {
      featureWriter = new GMLBitSetFeatureReaderWriter();
      extractor = new HashedNgramBooleanFeatureExtractor(numHashBits, ngramLength, AsmMode.OPCODE,
          new MurmurHashWrapper((int) System.nanoTime()));
    } else if (feat.equals("inst")) {
      featureWriter = new GMLInstructuinListFeatureReaderWriter();
      extractor = new InstructionFeatureExtractor();
    } else if (feat.equals("lshcosine")) {
      featureWriter = new GMLLongArrayFeatureReaderWriter();
      extractor = new CosineLSHLongFeatureExtractor(numHyperPlanes,
          new Random(Long.getLong("seed", System.currentTimeMillis())), numHashBits, ngramLength,
          AsmMode.OPCODE, new MurmurHashWrapper((int) System.nanoTime()));
    } else if (feat.equals("new_jaccard")) {
      featureWriter = new GMLDoubleArrayFeatureReaderWriter();
      File[] disassembledFiles;
      File input = new File(inputFile);
      if (input.isDirectory()) {
        disassembledFiles = FileUtil.listFiles(ext, input);
      } else {
        disassembledFiles = new File[1];
        disassembledFiles[0] = input;
      }

      Map<String, Integer> uniqueNgramMap;
      if (ngramListPath == null && ext.equals(".asm")) {
        System.out.println("Ngram List file not provided extracting from .asm files.");
        uniqueNgramMap =
            NGramFreqFeatureExtractor.ngramSet(ngramLength, disassembledFiles, asmMode, false);
        System.out.println("Done extracting ngram list from .asm files");
      } else if (ngramListPath == null && !ext.equals(".asm")) {
        System.err.println("Error: Ngram List file not provided and the input files are not .asm");
        return;
      } else {
        uniqueNgramMap = readNgramList(new File(ngramListPath));
      }

      extractor = new NGramFreqFeatureExtractor(ngramLength, false, uniqueNgramMap, asmMode);
    } else {
      System.err.printf("Error: the selected feature extractor \"%s\" not available.\n", feat);
      return;
    }

    if (thread > 1) {
      extractCallGraphMultiThread(new File(inputFile), new File(classLabelFile), outputFile, ext,
          maxFiles, verbose, extractor, writeInstructions, featureWriter, thread,
          includeDBInstructions);
    } else {
      extractCallGraph(new File(inputFile), new File(classLabelFile), outputFile, ext, maxFiles,
          verbose, extractor, writeInstructions, featureWriter, includeDBInstructions);
    }
  }

  /***
   * Extract class graphs from disassembled file and saves them in a GML file format. This is a
   * single threaded version.
   * 
   * @param input input file or directory path.
   * @param classLabelFile file path to class label file.
   * @param outPath path to output directory.
   * @param extension extension of the input file/s.
   * @param maxFiles maximim number of files to process.
   * @param verbose verbous log messages.
   * @param extractor the FeatureExtractor.
   * @param writeInstructions If true, write instrcution for each function in the GML file.
   * @param featureWriter GMLFeatureReaderWriter object used for writing the extracted feature
   *        values to GML file.
   * @param includeDBInstructions If true DB,DW,DD,DQ,DT instrcutions will be included.
   * @throws IOException
   */
  public static void extractCallGraph(File input, File classLabelFile, String outPath,
      String extension, int maxFiles, boolean verbose, FeatureExtractor extractor,
      boolean writeInstructions, GMLFeatureReaderWriter featureWriter,
      boolean includeDBInstructions) throws IOException {
    File[] files = null;
    if (input.isDirectory()) {
      files = FileUtil.listFiles(extension, input, true);
    } else {
      files = new File[1];
      files[0] = input;
    }

    File outDir = new File(outPath);
    if (!outDir.isDirectory()) {
      System.err.printf("ERROR: %s is not a directory.\n", outDir.getAbsolutePath());
      return;
    }

    int count = 0;
    for (File f : files) {
      String binaryName = f.getName().replaceFirst("[.][^.]+$", "");
      if (count++ > maxFiles && maxFiles > 0) {
        break;
      }

      if ((count % 100 == 0)) {
        System.err.printf("%04d files processed.\n", count);
      }

      // Extract functions
      CallGraph binCallGraph = null;
      if (extension.equals(".asm")) {
        binCallGraph = callGraphFromAsm(f, includeDBInstructions, binaryName, extractor, verbose);
      } else if (extension.equals(".gml")) {
        binCallGraph = callGraphFromGml(f, includeDBInstructions, binaryName, extractor, verbose);
      }

      if (binCallGraph == null) {
        count--;
        continue;
      }

      // List<Function> functions =
      // AsmParser.parseFunctionTransaction(new BufferedInputStream(new FileInputStream(f)),
      // AsmParser.ExtractionMode.INSTRUCTION, AsmMode.OPCODE, includeDBInstructions);
      //
      // if (functions.isEmpty()) {
      // count--;
      // continue;
      // }
      //
      // CallGraph<long[]> binCallGraph = CallGraph.build(functions, extractor, true);
      // binCallGraph.setName(binaryName);
      // if (verbose) {
      // System.err.printf("%s vertex=%d\n", binaryName, binCallGraph.vSize());
      // }

      String filename = String.format("%s.gml", binaryName);
      BufferedWriter outputStream = new BufferedWriter(new FileWriter(new File(outPath, filename)));
      GMLGraphWriter.write(binCallGraph, outputStream, writeInstructions, featureWriter);
      outputStream.close();
    }
  }

  /***
   * Builds a function call graph from as disassembled file.
   * 
   * @param f input file object.
   * @param includeDBInstructions If true DB,DW,DD,DQ,DT instrcutions will be included.
   * @param binaryName malware binary name
   * @param extractor feature extractor
   * @param verbose If true, operate in verbous mode.
   * @return
   * @throws FileNotFoundException
   */
  public static <F> CallGraph<F> callGraphFromAsm(File f, boolean includeDBInstructions,
      String binaryName, FeatureExtractor<F, List<Instruction>> extractor, boolean verbose)
      throws FileNotFoundException {
    // Extract functions
    List<Function> functions =
        AsmParser.parseFunctionTransaction(new BufferedInputStream(new FileInputStream(f)),
            AsmParser.ExtractionMode.INSTRUCTION, AsmMode.OPCODE, includeDBInstructions);

    if (functions.isEmpty()) {
      return null;
    }
    // Build call graph
    CallGraph<F> binCallGraph = CallGraph.build(functions, extractor, true);
    binCallGraph.setName(binaryName);
    if (verbose) {
      System.err.printf("%s vertex=%d\n", binaryName, binCallGraph.vSize());
    }

    return binCallGraph;
  }

  /***
   * Builds function call graph from an input .gml file.
   * 
   * @param f input file object.
   * @param includeDBInstructions If true DB,DW,DD,DQ,DT instrcutions will be included.
   * @param binaryName malware binary name
   * @param extractor feature extractor
   * @param verbose If true, operate in verbous mode.
   * @return
   * @throws FileNotFoundException
   */
  public static <F> CallGraph<F> callGraphFromGml(File f, boolean includeDBInstructions,
      String binaryName, FeatureExtractor<F, List<Instruction>> extractor, boolean verbose)
      throws FileNotFoundException {
    // Read call graph from .gml file
    CallGraph<List<Instruction>> inputGraph =
        GMLGraphReader.read(new BufferedInputStream(new FileInputStream(f)),
            new GMLInstructuinListFeatureReaderWriter());

    if (inputGraph == null) {
      return null;
    }

    // Build call graph from input graph
    CallGraph<F> binCallGraph = CallGraph.build(inputGraph, extractor);
    binCallGraph.setName(binaryName);
    if (verbose) {
      System.err.printf("%s vertex=%d\n", binaryName, binCallGraph.vSize());
    }

    return binCallGraph;
  }

  /***
   * Extract class graphs from disassembled file and saves them in a GML file format. This is a
   * single threaded version.
   * 
   * @param input input file or directory path.
   * @param classLabelFile file path to class label file.
   * @param outPath path to output directory.
   * @param extension extension of the input file/s.
   * @param maxFiles maximim number of files to process.
   * @param verbose verbous log messages.
   * @param extractor the FeatureExtractor.
   * @param writeInstructions If true, write instrcution for each function in the GML file.
   * @param featureWriter GMLFeatureReaderWriter object used for writing the extracted feature
   *        values to GML file.
   * @param maxNumActiveThreads maximum numer of threads used in multithreaded executions.
   * @param includeDBInstructions If true DB,DW,DD,DQ,DT instrcutions will be included.
   * @throws IOException
   */
  public static void extractCallGraphMultiThread(File input, File classLabelFile, String outPath,
      String extension, int maxFiles, boolean verbose, FeatureExtractor extractor,
      boolean writeInstructions, GMLFeatureReaderWriter featureWriter, int maxNumActiveThreads,
      boolean includeDBInstructions) throws IOException {
    File[] files = null;
    if (input.isDirectory()) {
      files = FileUtil.listFiles(extension, input, true);
    } else {
      files = new File[1];
      files[0] = input;
    }

    File outDir = new File(outPath);
    if (!outDir.isDirectory()) {
      System.err.printf("ERROR: No such file %s\n", outDir.getAbsolutePath());
      return;
    }

    long keepAliveTime = 240;
    TimeUnit unit = TimeUnit.SECONDS;
    FixedBlockingThreadPoolExecutor executor = FixedBlockingThreadPoolExecutor
        .threadPoolExecutorFactory(maxNumActiveThreads, keepAliveTime, unit);
    // int count = 0;
    Counter counter = new Counter(0);
    for (File f : files) {
      if (counter.getCount() > maxFiles && maxFiles > -1) {
        break;
      }
      if (counter.getCount() % 100 == 0) {
        System.err.printf("%04d files processed.\n", counter.getCount());
      }

      String binaryName = f.getName().replaceFirst("[.][^.]+$", "");

      ExtractionTask task = new ExtractionTask(f, outPath, binaryName, writeInstructions,
          featureWriter, extractor, verbose, counter, includeDBInstructions, extension);

      executor.execute(task);
    }

    executor.awaitActiveThreads();
    executor.shutdown();
    try {
      executor.awaitTermination(60, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /***
   * Reads a two column CSV file with no header where the first column is the ngram and the second
   * column is index of the ngram in a feature vector.
   * 
   * @param ngramListFile the CSV (ngram,index) file path.
   * @return A mapping from ngram to index of the ngram in a feature vector.
   * @throws FileNotFoundException
   */
  public static Map<String, Integer> readNgramList(File ngramListFile)
      throws FileNotFoundException {
    Map<String, Integer> ngramMap = new HashMap<String, Integer>();

    Scanner in = new Scanner(new BufferedInputStream(new FileInputStream(ngramListFile)));
    in.useDelimiter(",|\\s");
    while (in.hasNext()) {
      String ngram = in.next();
      int index = in.nextInt();
      ngramMap.put(ngram, index);
    }

    return ngramMap;
  }

  /***
   * A single extraction task. Extract class graphs from disassembled file and saves them in a GML
   * file format.
   * 
   * @author mehadi
   *
   */
  public static class ExtractionTask implements Runnable {
    File inputFile;
    String outPath;
    String binaryName;
    boolean writeInstructions;
    GMLFeatureReaderWriter featureWriter;
    FeatureExtractor extractor;
    boolean verbose;
    Counter counter;
    boolean includeDBInstructions;
    String extension;

    public ExtractionTask(File inputFile, String outPath, String binaryName,
        boolean writeInstructions, GMLFeatureReaderWriter featureWriter, FeatureExtractor extractor,
        boolean verbose, Counter counter, boolean includeDBInstructions, String extension) {
      super();
      this.inputFile = inputFile;
      this.outPath = outPath;
      this.binaryName = binaryName;
      this.writeInstructions = writeInstructions;
      this.featureWriter = featureWriter;
      this.extractor = extractor;
      this.verbose = verbose;
      this.counter = counter;
      this.includeDBInstructions = includeDBInstructions;
      this.extension = extension;
    }

    @Override
    public void run() {
      // Extract functions
      CallGraph binCallGraph = null;
      try {
        if (extension.equals(".asm")) {
          binCallGraph =
              callGraphFromAsm(inputFile, includeDBInstructions, binaryName, extractor, verbose);
        } else if (extension.equals(".gml")) {
          binCallGraph =
              callGraphFromGml(inputFile, includeDBInstructions, binaryName, extractor, verbose);
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        return;
      }

      if (binCallGraph == null) {
        System.err.printf("Error: Failed to build function call graph.");
        return;
      }

      // List<Function> functions;
      // try {
      // functions = AsmParser.parseFunctionTransaction(
      // new BufferedInputStream(new FileInputStream(inputFile)),
      // AsmParser.ExtractionMode.INSTRUCTION, AsmMode.OPCODE, includeDBInstructions);
      // } catch (FileNotFoundException e) {
      // e.printStackTrace();
      // return;
      // }
      //
      // if (functions.isEmpty()) {
      // return;
      // }
      //
      // CallGraph binCallGraph = CallGraph.build(functions, extractor, true);
      // binCallGraph.setName(binaryName);
      // if (verbose) {
      // System.err.printf("%s vertex=%d\n", binaryName, binCallGraph.vSize());
      // }

      String filename = String.format("%s.gml", binaryName);
      try {
        BufferedWriter outputStream =
            new BufferedWriter(new FileWriter(new File(outPath, filename)));
        GMLGraphWriter.write(binCallGraph, outputStream, writeInstructions, featureWriter);
        outputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }

      counter.increment();
    }

  }
}

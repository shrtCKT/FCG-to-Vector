package util;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Example Usage:
 *     String argModelPath = "-m";
 *     String argTestPath = "-T";
 *     
 *     CommandLineArguments cmdArgs = new CommandLineArguments();
 *     cmdArgs.addOption(true, argModelPath, true);
 *     cmdArgs.addOption(true, argTestPath, true);
 *     
 *     cmdArgs.parseCommandLineArgs(args);
 *     
 *     String modelPath = cmdArgs.getOptionValue(argModelPath);
 *     String testPath = cmdArgs.getOptionValue(argTestPath); 
 * 
 * @author mehadi
 *
 */
public class CommandLineArguments {
  private static class Option {
    private final boolean isRequired;
    private final String symbol;
    private final boolean takesValue;
    private String value;
    private boolean isFound;

    public Option(boolean isRequired, String symbol, boolean takesValue) {
      super();
      this.isRequired = isRequired;
      this.symbol = symbol;
      this.takesValue = takesValue;
      this.isFound = false;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public boolean isRequired() {
      return isRequired;
    }

    public String getSymbol() {
      return symbol;
    }

    public boolean isTakesValue() {
      return takesValue;
    }
    
    public void setFound() {
      isFound = true;
    }
    
    public boolean isFound() {
      return isFound;
    }
  }

  HashMap<String, Option> argMap;

  public CommandLineArguments() {
    argMap = new HashMap<String, Option>();
  }

  public void addOption(boolean isRequired, String symbol, boolean takesValue) {
    argMap.put(symbol, new Option(isRequired, symbol, takesValue));
  }

  public boolean hasOption(String symbol) {
    return argMap.containsKey(symbol);
  }

  public String getOptionValue(String symbol) {
    return hasOption(symbol) ? argMap.get(symbol).getValue() : null;
  }
  
  public boolean isOptionFound(String symbol) {
    return hasOption(symbol) ? argMap.get(symbol).isFound() : false;
  }

  public boolean parseCommandLineArgs(String[] args) {
    for (int i = 0; i < args.length; i++) {
      if (argMap.containsKey(args[i])) {
        Option opt = argMap.get(args[i]);
        if (opt.isTakesValue()) {
          if (i + 1 >= args.length) {
            return false;
          }
          opt.setValue(args[++i]);
        } else {
          opt.setValue(Boolean.TRUE.toString());
        }
        opt.setFound();
      }
    }
    
    return requiredOptionsFound();
  }

  private boolean requiredOptionsFound() {
    for(Map.Entry<String, Option> me : argMap.entrySet()) {
      if(me.getValue().isRequired() && !me.getValue().isFound()) {
        return false;
      }
    }
    return true;
  }

}

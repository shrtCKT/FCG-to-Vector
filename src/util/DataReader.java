package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ml.data.Attribute;
import ml.data.DataInstance;

public class DataReader {

  public static List<Attribute> readAttributes(String attributeFile)
      throws FileNotFoundException {
    List<Attribute> attributes = new ArrayList<Attribute>();
    int index = 0;
    try (Scanner in = new Scanner(new File(attributeFile))) {
      while (in.hasNextLine()) {
        String line = in.nextLine().trim();
        if (line.isEmpty()) {
          continue;
        }

        String[] separated = line.split(":");
        Attribute.Type type =
            separated[1].contains("continuous") ? Attribute.Type.CONTINUOUS
                : (separated[1].contains("ID") ? Attribute.Type.ID
                    : Attribute.Type.DISCRETE);

        Attribute attr = new Attribute(separated[0].trim(), index++, type);
        if (type == Attribute.Type.DISCRETE) {
          String[] values = separated[1].split("\\s+");
          for (int i = 0; i < values.length; i++) {
            if (values[i].trim().isEmpty()) {
              continue;
            }
            attr.addDiscreteAttributeValue(values[i].trim());
          }
        }
        attributes.add(attr);
      }
    }

    return attributes;
  }

  public static List<DataInstance> readData(String fileName,
      List<Attribute> attributes) throws FileNotFoundException {
    return readData(fileName, attributes, true);
  }

  public static List<DataInstance> readData(String fileName,
      List<Attribute> attributes, boolean hasClassLable)
      throws FileNotFoundException {
    return readData(fileName, attributes, true, false);
  }
  
  public static List<DataInstance> readData(String fileName,
      List<Attribute> attributes, boolean hasClassLable, boolean firstRowHeader)
      throws FileNotFoundException {
    List<DataInstance> data = new ArrayList<DataInstance>();
    try (Scanner in = new Scanner(new File(fileName))) {
      while (in.hasNextLine()) {
        String line = in.nextLine().trim();
        if (firstRowHeader) {
          firstRowHeader = false;
          continue;
        }
        
        if (line.isEmpty()) {
          continue;
        }
        String[] cols = line.split("[\\s|,]+");
        if (cols.length < attributes.size() + 1 && hasClassLable) {
          continue;
        } else if (cols.length < attributes.size() && !hasClassLable) {
          continue;
        }
        DataInstance di = new DataInstance();
        int i = 0;
        for (; i < cols.length - 1; i++) {
          if (attributes.get(i).getType() == Attribute.Type.CONTINUOUS) {
            try {
              di.setAttributeValueAt(i, Double.parseDouble(cols[i].trim()));
            } catch(NumberFormatException e) {
              di.setAttributeValueAt(i, Integer.parseInt(cols[i].trim()));
            }
          } else {
            di.setAttributeValueAt(i, cols[i].trim());
          }
        }
        if (hasClassLable) {
          di.setClassLabel(cols[i].trim());
        } else {
          if (attributes.get(i).getType() == Attribute.Type.CONTINUOUS) {
            di.setAttributeValueAt(i, Double.parseDouble(cols[i].trim()));
          } else {
            di.setAttributeValueAt(i, cols[i].trim());
          }
        }
        data.add(di);
      }
    }

    return data;
  }

  /**
   * Read transactions, or market basket, data. 
   * The data should be formated in such a way that there is one data point 
   * per line. The columns of the data are separated by the delimiter.
   * @param fileName    - Path to data file.
   * @param attributes  - List of attributes.
   * @param delimiter   - Column delimiters Regex.
   * @return List of DataInstance objects.
   * @throws FileNotFoundException throws an exception if file fails to open.
   */
  public static List<DataInstance> readTransactionData(String fileName,
      List<Attribute> attributes, String delimiter) throws FileNotFoundException {
    List<DataInstance> data = new ArrayList<DataInstance>();
    try (Scanner in = new Scanner(new File(fileName))) {
      while (in.hasNextLine()) {
        String line = in.nextLine().trim();
        if (line.isEmpty()) {
          continue;
        }
        String[] cols = line.split(delimiter);
        if (cols.length < attributes.size()) {
          continue;
        }
        DataInstance di = new DataInstance();

        for (int i = 0; i < cols.length; i++) {
          if (attributes.get(i).getType() == Attribute.Type.CONTINUOUS) {
            di.setAttributeValueAt(i, Double.parseDouble(cols[i].trim()));
          } else {
            di.setAttributeValueAt(i, cols[i].trim());
          }
        }

        data.add(di);
      }
    }

    return data;
  }
  
  /**
   * Read transactions, or market basket, data. 
   * The data should be formated in such a way that there is one data point 
   * per line. The columns of the data are comma separated.
   * @param fileName    - Path to data file.
   * @param attributes  - List of attributes.
   * @return List of DataInstance objects.
   * @throws FileNotFoundException throws an exception if file fails to open.
   */
  public static List<DataInstance> readTransactionData(String fileName,
      List<Attribute> attributes) throws FileNotFoundException {
    return readTransactionData(fileName, attributes, "[\\s|,]+");
  }

}

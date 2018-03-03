package util.math;

public class LinearAlgebra {
  public static double magnitude(Double[] vector) {
    double mag = 0;
    for (Double v : vector) {
      mag += v * v;
    }
    return Math.sqrt(mag);
  }
  
  public static double magnitude(double[] vector) {
    double mag = 0;
    for (Double v : vector) {
      mag += v * v;
    }
    return Math.sqrt(mag);
  }
  
  public static double dotProduct(Double[] vector1, Double[] vector2) {
    double prod = 0;
    for (int i = 0; i < vector1.length; i++) {
      prod += vector1[i] * vector2[i];
    }
    return prod;
  }
  
  public static double dotProduct(double[] vector1, double[] vector2) {
    double prod = 0;
    for (int i = 0; i < vector1.length; i++) {
      prod += vector1[i] * vector2[i];
    }
    return prod;
  }
  
  public static double cosine(Double[] vector1, Double[] vector2) {
    double dotProd = dotProduct(vector1, vector2);
    double mag1 = magnitude(vector1);
    double mag2 = magnitude(vector2);
    
    if (mag1 == 0 || mag2 == 0) {
      return 0;
    }
    
    return dotProd / (mag1 * mag2);
  }

}

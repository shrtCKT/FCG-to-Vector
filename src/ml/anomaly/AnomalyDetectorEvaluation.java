package ml.anomaly;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import ml.anomaly.DensityBasedAnomaly.DataPoint;
import ml.data.DataInstance;

public class AnomalyDetectorEvaluation {

  public static double areaUnderROC(List<DataInstance> instances,
      DensityBasedAnomaly.DataPoint[] dp, String positiveClassLabel) {
    double area = 0.0;
  
    Arrays.sort(dp, new Comparator<DensityBasedAnomaly.DataPoint>() {
      @Override
      public int compare(DataPoint p1, DataPoint p2) {
        return p1.getOutlierScore() > p2.getOutlierScore() ? 1 : (p1
            .getOutlierScore() < p2.getOutlierScore() ? -1 : 0);
      }
    });
  
    double actualP = 0.0;
    double actualN = 0.0;
    for (DataInstance d : instances) {
      if (positiveClassLabel.equals(d.getClassLabel())) {
        actualP++;
      } else {
        actualN++;
      }
    }
  
    double[] tp = new double[dp.length + 1];
    double[] fp = new double[dp.length + 1];
    tp[0] = actualP;
    fp[0] = actualN;
    
    for (int i = 1; i < dp.length + 1; i++) {
      if (positiveClassLabel.equals(instances.get(dp[i - 1].getIndex())
          .getClassLabel())) {
        tp[i] = tp[i - 1] - 1;
        fp[i] = fp[i - 1];
      } else {
        tp[i] = tp[i - 1];
        fp[i] = fp[i - 1] - 1;
      }
      
      //System.out.printf("%s\t\t%.4f\n", instances.get(dp[i - 1].getIndex()).getClassLabel(), dp[i - 1].getOutlierScore());
      area += trapizoidArea(fp[i-1] / actualN, tp[i-1] / actualP, fp[i] / actualN, tp[i] / actualP);
    }
  
    return area;
  }

  public static double trapizoidArea(double x1, double y1, double x2, double y2) {
    double h = Math.abs(x1 - x2);
    return ((y1 + y2) / 2) * h;
  }

}

package ml.random;

import java.util.Random;

public interface State {

  Change neighbourState(Random rng);

  void applyChange(Change change);

  void undoChange(Change change);
  
  double getCost();

}

package ml.random;

import java.util.Random;

public class SimulatedAnnealing {

  /***
   * Implementation of Simulated Annealing algorithm.
   * 
   * @param initialState a random initial state.
   * @param objFunction provides means to calculate the objective function(cost of a state).
   * @param beta is the annealed parameter. It is is the inverse temperature
   *        used in statistical physics. For small values of β almost any move is accepted in the
   *        process. For β → ∞ the process is essentially a downhill move in which the SA state will
   *        be replaced by the new bijective mapping only if the new state gives a lower cost.
   * @param relaxationIteration the number of times β changes.
   * @param coolingRate rate by which β changes. Takes on the value between [0,1]. 
   * @param iter is a predefined number of steps before the value of β is increased.
   * @return
   */
  public State search(State initialState, double beta,
      int relaxationIteration, double coolingRate, int iter, Random rng) {
    // calculate cost of initial state.
    double oldCost = initialState.getCost();
    State currentState = initialState;

    for (int i = 0; i < relaxationIteration; i++) {
      for (int j = 0; j < iter; j++) {
        // visit neighbouring state.
        Change change = currentState.neighbourState(rng);
        currentState.applyChange(change);
        // calculate new cost.
        double newCost = currentState.getCost();
        // calculate delta = newCost - oldCost
        double delta = newCost - oldCost;
        // if delta < 0 keep new state.
        if (delta < 0) {
          oldCost = newCost;
        } else {
          // else keep new state with some probability e^−β ∆(λφt ,λφt+1)
          double keepProb = Math.pow(Math.E, -1 * beta * delta);
          if (rng.nextDouble() < keepProb) {
            oldCost = newCost;
          } else {
            currentState.undoChange(change);
          }
        }
      }
      // update beta
      beta = beta / coolingRate;
    }
    return currentState;
  }
}

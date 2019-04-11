package algorithm.model;

import model.VGraph;

import java.util.HashMap;

/**
 * Interface for all Visualization Algorithms
 * <p>
 * Input : A set of parameters among whose a mathematical or visual graph should be given this class
 * <p>
 * for visualization or optimization
 * <p>
 * Every class uing this interface has to check the given parameters to fit the own algorithm
 *
 * @author Ronny Bergmann
 */
public interface VAlgorithmIF {

  /**
   * for the Parameters to be set
   *
   * @return An Error-Message if an error happend, an Empty String if everything is right
   */
  String setParameters(HashMap<String, Object> m);

  /**
   * Indicates, whether the given mathematical graph fits
   *
   * @return true if it fits
   */
  boolean GraphOkay();

  /**
   * Executes the algorithm and create the visual graph
   */
  void run();

  /**
   * get the actual state of the visualized Graph in the beginning this method should return an empty graph
   * after each step this method returns the actual situation
   * and in the end the finished graph
   * <p>
   * if the algorithm is started with run(), this method returns the result
   *
   * @return actual VGraph situation
   */
  VGraph getactualState();

  //
  //
  // Iterative Algorithms may use the following method for an iterative visualization of the proceeding.
  // If the algorithm is only executable at once via run() only isStebwiseRunable() should be implemented and return false
  //
  //

  /**
   * first part of run() that initializes the algorithm
   * use this method to init a stebwise execution
   */
  void start();

  /**
   * Indicates whether an algorithm is stepwise runable or not
   */
  boolean isStepwiseRunable();

  /**
   * do a step
   */
  void step();
	
	/*
	  Set the size of one step. this should be used to set the granularity of execution
	  @param i
	 */

  /**
   * indicates, whether the graph is finished
   *
   * @return true if the algorithm is finished
   */
  boolean finished();
}

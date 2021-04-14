package main.firefighters;

import main.api.City;
import main.api.CityNode;
import main.api.FireDispatch;
import main.api.Firefighter;

import java.util.*;

import static main.util.FirefightingUtilities.manhattanDistance;

public class FireDispatchImpl implements FireDispatch {

  private City city;
  private List<Firefighter> firefighters;

  public FireDispatchImpl(City city) {
    this.city = city;
  }

  @Override
  public void setFirefighters(int numFirefighters) {
    firefighters  = new ArrayList<Firefighter>(numFirefighters);
    for (int i = 0; i < numFirefighters; i++) {
      firefighters.add(new FirefighterImpl(city.getFireStation().getLocation()));
    }
  }

  @Override
  public List<Firefighter> getFirefighters() {
    return firefighters;
  }

  @Override
  public void dispatchFirefighters(CityNode... burningBuildings) {
    // Use a guided search to determine the optimal set of decisions to make
    List<DispatchDecision> solution = dispatchFirefighters_searchWithPruning(new ArrayList<CityNode>(Arrays.asList(burningBuildings)));

    // Now perform those actions with the actual firefighters
    for (DispatchDecision decision : solution) {
      // Dispatch first firefighter at the desired source node
      for (Firefighter firefighter : firefighters) {
        if (firefighter.getLocation().equals(decision.sourceFighterNode)) {
          firefighter.dispatch(city.getBuilding(decision.destinationFireNode));
          break;
        }
      }
    }
  }

  /*
    ___Complexity Analysis___
    Where:
      b = # of buildings on fire
      f = # of firefighters

    Search with Pruning:
      Consider the search space to have an upper bound of f^b possible solutions (b decisions to send any given firefighter there).
      However, when firefighters are on the same source space, it doesn't matter which firefighter you send.
      Additionally, since dispatching firefighters can only add to the distance traveled, as soon as we have a complete solution,
      all possibilities already as long as the known shortest solution can be discarded. Thus, the actual runtime
      depends on how easily pruned the possibility space is.
  */
  private List<DispatchDecision> dispatchFirefighters_searchWithPruning(List<CityNode> burningBuildings) {
    PartialDispatchSolution shortestKnownCompleteSolution = null;
    List<PartialDispatchSolution> processStack = new ArrayList<PartialDispatchSolution>();

    // Push initial state into processQueue
    processStack.add(PartialDispatchSolution.initial(city, burningBuildings, firefighters.size()));

    while (!processStack.isEmpty()) {
      // Pop off the next partial solution and add it's children to the queue
      // Favor depth-first so that we can start pruning with a shortest known solution earlier
      PartialDispatchSolution parent = processStack.remove(processStack.size() - 1);

      for (CityNode possibleDestination : parent.remainingFires) {
        for (CityNode possibleSource : parent.firefightersAvailableAtNodes.keySet()) {
          PartialDispatchSolution child = PartialDispatchSolution.fromStatePlusDecision(
                  parent, possibleDestination, possibleSource);
          // If the child is a complete solution, only keep it if it is the shortest known one
          if (child.remainingFires.isEmpty() &&
                  (null == shortestKnownCompleteSolution ||
                        child.totalDistanceTraveled < shortestKnownCompleteSolution.totalDistanceTraveled)) {
            shortestKnownCompleteSolution = child;
          // If we already have a complete solution shorter than a partial solution, discard the subtree
          } else if (!child.remainingFires.isEmpty() &&
                  (null == shortestKnownCompleteSolution ||
                          child.totalDistanceTraveled < shortestKnownCompleteSolution.totalDistanceTraveled)) {
            processStack.add(child);
          }
        }
      }
    }

    return shortestKnownCompleteSolution.decisionsSoFar;
  }

  /**
   * Decision to move a firefighter from one location to another.
   * Used to track search space rather than cloning and editing firefighters directly.
   */
  private static class DispatchDecision {
    CityNode destinationFireNode;
    CityNode sourceFighterNode;

    DispatchDecision(CityNode destinationFireNode, CityNode sourceFighterNode) {
      this.destinationFireNode = destinationFireNode;
      this.sourceFighterNode = sourceFighterNode;
    }
  }

  /**
   * Contains state of a partial solution to dispatchFirefighers, so searching through the solution tree
   * may be paused and resumed based on search priority
   */
  private static class PartialDispatchSolution {
    int totalDistanceTraveled;
    List<DispatchDecision> decisionsSoFar;
    List<CityNode> remainingFires;
    Map<CityNode,Integer> firefightersAvailableAtNodes;

    static PartialDispatchSolution initial(City city, List<CityNode> burningBuildings, int numFirefighters) {
      PartialDispatchSolution initial = new PartialDispatchSolution();
      initial.totalDistanceTraveled = 0;
      initial.decisionsSoFar = new ArrayList<DispatchDecision>(burningBuildings.size());
      initial.remainingFires = burningBuildings;
      initial.firefightersAvailableAtNodes = new HashMap<CityNode,Integer>();
      initial.firefightersAvailableAtNodes.put(city.getFireStation().getLocation(), numFirefighters);

      return initial;
    }

    static PartialDispatchSolution fromStatePlusDecision(PartialDispatchSolution lastState, CityNode destinationFireNode, CityNode sourceFighterNode) {
      PartialDispatchSolution next = new PartialDispatchSolution();
      next.totalDistanceTraveled = lastState.totalDistanceTraveled + manhattanDistance(destinationFireNode, sourceFighterNode);

      next.decisionsSoFar = new ArrayList<DispatchDecision>(lastState.decisionsSoFar);
      next.decisionsSoFar.add(new DispatchDecision(destinationFireNode, sourceFighterNode));

      next.remainingFires = new ArrayList<CityNode>(lastState.remainingFires);
      next.remainingFires.remove(destinationFireNode);

      next.firefightersAvailableAtNodes = new HashMap<CityNode,Integer>(lastState.firefightersAvailableAtNodes);
      // Subtract one from the number of fighters at sourceFighterNode, or remove if 0
      Integer fightersAtSource = next.firefightersAvailableAtNodes.getOrDefault(sourceFighterNode, 0) - 1;
      if (fightersAtSource < 1) {
        next.firefightersAvailableAtNodes.remove(sourceFighterNode);
      } else {
        next.firefightersAvailableAtNodes.replace(sourceFighterNode, fightersAtSource);
      }
      // Add one fighter to destinationFireNode, or create if the first
      if (next.firefightersAvailableAtNodes.containsKey(destinationFireNode)) {
        next.firefightersAvailableAtNodes.put(destinationFireNode, next.firefightersAvailableAtNodes.get(destinationFireNode) + 1);
      } else {
        next.firefightersAvailableAtNodes.put(destinationFireNode, 1);
      }

      return next;
    }
  }

  /*
      Brute Force Method:
      For each dispatch decision (one per building on fire), we check
      every building location against every fireman location.
      At the beginning, most firefighters are at home, making most comparisons duplicative.
      Additionally, it is greedy and therefore can make non-optimal short-term decisions.
      See the test 'fourFires_staircase', demonstrating a situation where greed fails.
  */
  @SuppressWarnings("unused")
  private void dispatchFirefighters_greedyBrute(CityNode... burningBuildings) {
    List<CityNode> remainingFires = new ArrayList<CityNode>(Arrays.asList(burningBuildings));

    // While we still have fires to extinguish...
    while (!remainingFires.isEmpty()) {
      // Get the fire closest to a current firefighter's location
      CityNode fireToExtinguish = null;
      Firefighter closestFirefighter = null;
      int shortestDistance = Integer.MAX_VALUE;

      for (CityNode fireOption : remainingFires) {
        for (Firefighter firefighter : firefighters) {
          int optionDistance = manhattanDistance(fireOption, firefighter.getLocation());
          if (optionDistance < shortestDistance) {
            shortestDistance = optionDistance;
            fireToExtinguish = fireOption;
            closestFirefighter = firefighter;
          }
        }
      }

      // Dispatch a firefighter at that location to the fire
      closestFirefighter.dispatch(city.getBuilding(fireToExtinguish));

      // Remove the fire as extinguished
      remainingFires.remove(fireToExtinguish);
    }
  }
}

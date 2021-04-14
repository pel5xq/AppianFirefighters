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
  public void dispatchFirefighers(CityNode... burningBuildings) {
  /*
    ___Complexity Analysis___
    Where:
      b = # of buildings on fire
      f = # of firefighters

    Brute Force Method: O(b^2 * f)
      For each dispatch decision (one per building on fire), we check
      every building location against every fireman location.
      At the beginning, most firefighters are at home, making most comparisons duplicative.
      Additionally, it is greedy and therefore can make non-optimal short-term decisions.
      See the test 'fourFires_staircase', demonstrating a situation where greed fails.
  */
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

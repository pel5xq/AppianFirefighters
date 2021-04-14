package main.firefighters;

import main.api.Building;
import main.api.CityNode;
import main.api.Firefighter;
import main.api.exceptions.NoFireFoundException;

import static main.util.FirefightingUtilities.manhattanDistance;

public class FirefighterImpl implements Firefighter {

  private CityNode currentLocation;
  private int distanceTravelled;

  public FirefighterImpl(CityNode startingNode) {
    currentLocation = startingNode;
    distanceTravelled = 0;
  }

  @Override
  public CityNode getLocation() {
    return currentLocation;
  }

  @Override
  public int distanceTraveled() {
    return distanceTravelled;
  }

  @Override
  public void dispatch(Building destination) {
    distanceTravelled += manhattanDistance(currentLocation, destination.getLocation());
    currentLocation = destination.getLocation();

    if (destination.isBurning()) {
      try {
        destination.extinguishFire();
      } catch (NoFireFoundException e) {
        // The if statement should prevent this, but if it were to happen
        // the solution that sent a firefighter to a non-burning location
        // would likely be sub-optimal. Let a failing test catch this.
      }
    }
  }
}

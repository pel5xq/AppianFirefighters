package main.scenarios;

import main.api.*;
import main.api.exceptions.FireproofBuildingException;
import main.impls.CityImpl;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CustomScenarios {
    @Test
    public void doubleFire_farthestFireFirst() throws FireproofBuildingException {
        executeTest(new CityImpl(2, 2, new CityNode(0, 0)), 1, 2,
                new CityNode(1, 1),
                new CityNode(0, 1));
    }

    @Test
    public void fourFires_diamond() throws FireproofBuildingException {
        executeTest(new CityImpl(3, 3, new CityNode(1, 1)), 2, 6,
                new CityNode(0, 1),
                new CityNode(1, 0),
                new CityNode(1, 2),
                new CityNode(2, 1));
    }

    @Test
    public void fourFires_square() throws FireproofBuildingException {
        executeTest(new CityImpl(3, 3, new CityNode(1, 1)), 2, 8,
                new CityNode(0, 0),
                new CityNode(2, 2),
                new CityNode(0, 2),
                new CityNode(2, 0));
    }

    @Test
    public void fourFires_staircase() throws FireproofBuildingException {
        executeTest(new CityImpl(4, 4, new CityNode(3, 0)), 4, 9,
                new CityNode(2, 2),
                new CityNode(1, 1),
                new CityNode(0, 0),
                new CityNode(3, 3));
    }

    private void executeTest(City city, int numFirefighters, int expectedDistance, CityNode... fireNodes) throws FireproofBuildingException {
        FireDispatch fireDispatch = city.getFireDispatch();
        Pyromaniac.setFires(city, fireNodes);
        fireDispatch.setFirefighters(numFirefighters);
        fireDispatch.dispatchFirefighers(fireNodes);

        List<Firefighter> firefighters = fireDispatch.getFirefighters();
        int totalDistanceTraveled = 0;
        for (Firefighter firefighter : firefighters) {
            totalDistanceTraveled += firefighter.distanceTraveled();
        }
        Assert.assertEquals(expectedDistance, totalDistanceTraveled);

        for (CityNode fire : fireNodes) {
            Assert.assertFalse(city.getBuilding(fire).isBurning());
        }
    }
}

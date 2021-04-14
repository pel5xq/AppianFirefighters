package main.util;

import main.api.CityNode;

public class FirefightingUtilities {
    public static int manhattanDistance(CityNode a, CityNode b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
}

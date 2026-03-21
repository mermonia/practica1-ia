package src.board;

import aima.search.framework.GoalTest;

/**
 * GoalTest para búsqueda local: siempre devuelve false
 * porque no hay estado objetivo (Hill Climbing / SA terminan según sus criterios).
 */
public class DesastresGoalTest implements GoalTest {
    public boolean isGoalState(Object state) {
        return false;
    }
}

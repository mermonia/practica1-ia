package src.heuristics;

import aima.search.framework.HeuristicFunction;
import src.board.RescueState;

/**
 * Heurística 1: Minimizar el tiempo total.
 * Devuelve la suma de los tiempos de todas las rutas de todos los helicópteros.
 */
public class HeuristicTotalTime implements HeuristicFunction {

    public double getHeuristicValue(Object state) {
        RescueState rescueState = (RescueState) state;
        return rescueState.getCosteTotal();
    }
}

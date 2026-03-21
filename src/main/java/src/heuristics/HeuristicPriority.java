package src.heuristics;

import aima.search.framework.HeuristicFunction;
import src.board.RescueState;

/**
 * Heurística 2: Minimizar el tiempo total con penalización por prioridad.
 * Retorna: costeTotal + W * maxTiempoP1
 * donde maxTiempoP1 es el tiempo máximo hasta rescatar un grupo de prioridad 1.
 * W es un peso que controla la importancia de rescatar rápido a los heridos.
 */
public class HeuristicPriority implements HeuristicFunction {

    private static final double W = 10.0;

    public double getHeuristicValue(Object state) {
        RescueState rescueState = (RescueState) state;
        return rescueState.getCosteTotal() + W * rescueState.getMaxTiempoP1();
    }
}

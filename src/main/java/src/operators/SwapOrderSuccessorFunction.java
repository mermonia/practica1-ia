package src.operators;

import aima.search.framework.*;
import src.board.RescueState;

import java.util.*;

/**
 * Successor Function amb únicament l'operador SwapOrder.
 * Intercanvia dos grups adjacents dins d'una mateixa ruta.
 */
public class SwapOrderSuccessorFunction implements SuccessorFunction {

    @SuppressWarnings("unchecked")
    public List getSuccessors(Object state) {
        List<Successor> successors = new ArrayList<>();
        RescueState currentState = (RescueState) state;

        int numHelis = currentState.numRutas();

        for (int h = 0; h < numHelis; h++) {
            ArrayList<Integer> ruta = currentState.getRutaHelicoptero(h);
            for (int i = 0; i < ruta.size() - 1; i++) {
                RescueState newState = new RescueState(currentState);
                ArrayList<Integer> newRuta = newState.getRutaHelicoptero(h);

                int tmp = newRuta.get(i);
                newRuta.set(i, newRuta.get(i + 1));
                newRuta.set(i + 1, tmp);

                successors.add(new Successor("SwapOrder H" + h + " pos" + i + "<->" + (i + 1), newState));
            }
        }

        return successors;
    }
}

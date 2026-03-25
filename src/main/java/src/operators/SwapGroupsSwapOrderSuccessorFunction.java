package src.operators;

import aima.search.framework.*;
import src.board.RescueState;

import java.util.*;

/**
 * Successor Function amb els operadors SwapGroups + SwapOrder.
 */
public class SwapGroupsSwapOrderSuccessorFunction implements SuccessorFunction {

    @SuppressWarnings("unchecked")
    public List getSuccessors(Object state) {
        List<Successor> successors = new ArrayList<>();
        RescueState currentState = (RescueState) state;

        int numGrupos = currentState.numGrupos();
        int numHelis = currentState.numRutas();
        int[] grupoHeli = currentState.getGrupoHeli();

        // SwapGroups
        for (int g1 = 0; g1 < numGrupos; g1++) {
            for (int g2 = g1 + 1; g2 < numGrupos; g2++) {
                int h1 = grupoHeli[g1];
                int h2 = grupoHeli[g2];
                if (h1 != h2) {
                    RescueState newState = new RescueState(currentState);
                    ArrayList<Integer> ruta1 = newState.getRutaHelicoptero(h1);
                    ArrayList<Integer> ruta2 = newState.getRutaHelicoptero(h2);

                    int pos1 = ruta1.indexOf(g1);
                    int pos2 = ruta2.indexOf(g2);

                    ruta1.set(pos1, g2);
                    ruta2.set(pos2, g1);

                    newState.getGrupoHeli()[g1] = h2;
                    newState.getGrupoHeli()[g2] = h1;

                    successors.add(new Successor("Swap G" + g1 + "<->G" + g2, newState));
                }
            }
        }

        // SwapOrder
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

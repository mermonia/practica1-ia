package src.operators;

import aima.search.framework.*;
import src.board.RescueState;

import java.util.*;

/**
 * Successor Function amb els operadors MoveGroup + SwapOrder.
 */
public class MoveGroupSwapOrderSuccessorFunction implements SuccessorFunction {

    @SuppressWarnings("unchecked")
    public List getSuccessors(Object state) {
        List<Successor> successors = new ArrayList<>();
        RescueState currentState = (RescueState) state;

        int numGrupos = currentState.numGrupos();
        int numHelis = currentState.numRutas();
        int[] grupoHeli = currentState.getGrupoHeli();

        // MoveGroup
        for (int g = 0; g < numGrupos; g++) {
            int heliOrig = grupoHeli[g];
            for (int hDest = 0; hDest < numHelis; hDest++) {
                if (hDest == heliOrig) continue;

                RescueState newState = new RescueState(currentState);
                newState.getRutaHelicoptero(heliOrig).remove(Integer.valueOf(g));
                newState.getRutaHelicoptero(hDest).add(g);
                newState.getGrupoHeli()[g] = hDest;

                successors.add(new Successor("Move G" + g + " H" + heliOrig + "->H" + hDest, newState));
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

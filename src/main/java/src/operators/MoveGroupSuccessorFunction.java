package src.operators;

import aima.search.framework.*;
import src.board.RescueState;

import java.util.*;

/**
 * Successor Function amb únicament l'operador MoveGroup.
 * Mou un grup al final de la ruta d'un altre helicòpter.
 */
public class MoveGroupSuccessorFunction implements SuccessorFunction {

    @SuppressWarnings("unchecked")
    public List getSuccessors(Object state) {
        List<Successor> successors = new ArrayList<>();
        RescueState currentState = (RescueState) state;

        int numGrupos = currentState.numGrupos();
        int numHelis = currentState.numRutas();
        int[] grupoHeli = currentState.getGrupoHeli();

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

        return successors;
    }
}

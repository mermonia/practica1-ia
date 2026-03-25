package src.operators;

import aima.search.framework.*;
import src.board.RescueState;

import java.util.*;

/**
 * Successor Function amb els operadors SwapGroups + MoveGroup.
 */
public class SwapGroupsMoveGroupSuccessorFunction implements SuccessorFunction {

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

        return successors;
    }
}

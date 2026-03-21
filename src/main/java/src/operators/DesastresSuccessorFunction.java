package src.operators;

import aima.search.framework.*;
import src.board.RescueState;

import java.util.*;

/**
 * Genera TODOS los sucesores para Hill Climbing.
 * Operadores:
 *  1) SwapGroups: intercambiar dos grupos de helicópteros distintos
 *  2) MoveGroup: mover un grupo a otra posición/helicóptero
 *  3) SwapOrder: intercambiar dos grupos adyacentes dentro de la misma ruta
 */
public class DesastresSuccessorFunction implements SuccessorFunction {

    @SuppressWarnings("unchecked")
    public List getSuccessors(Object state) {
        List<Successor> successors = new ArrayList<>();
        RescueState currentState = (RescueState) state;

        int numGrupos = currentState.numGrupos();
        int numHelis = currentState.numRutas();
        int[] grupoHeli = currentState.getGrupoHeli();

        // ============ Operador 1: SwapGroups ============
        // Intercambiar dos grupos que estén en helicópteros distintos
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

        // ============ Operador 2: MoveGroup ============
        // Mover un grupo de su helicóptero actual a otra posición en otro helicóptero
        for (int g = 0; g < numGrupos; g++) {
            int heliOrig = grupoHeli[g];
            for (int hDest = 0; hDest < numHelis; hDest++) {
                if (hDest == heliOrig) continue;

                ArrayList<Integer> rutaDest = currentState.getRutaHelicoptero(hDest);
                // Insertar al final de la ruta del helicóptero destino
                RescueState newState = new RescueState(currentState);
                newState.getRutaHelicoptero(heliOrig).remove(Integer.valueOf(g));
                newState.getRutaHelicoptero(hDest).add(g);
                newState.getGrupoHeli()[g] = hDest;

                successors.add(new Successor("Move G" + g + " H" + heliOrig + "->H" + hDest, newState));
            }
        }

        // ============ Operador 3: SwapOrder ============
        // Intercambiar dos grupos adyacentes dentro de la misma ruta
        for (int h = 0; h < numHelis; h++) {
            ArrayList<Integer> ruta = currentState.getRutaHelicoptero(h);
            for (int i = 0; i < ruta.size() - 1; i++) {
                RescueState newState = new RescueState(currentState);
                ArrayList<Integer> newRuta = newState.getRutaHelicoptero(h);
                // swap posiciones i e i+1
                int tmp = newRuta.get(i);
                newRuta.set(i, newRuta.get(i + 1));
                newRuta.set(i + 1, tmp);

                successors.add(new Successor("SwapOrder H" + h + " pos" + i + "<->" + (i + 1), newState));
            }
        }

        return successors;
    }
}

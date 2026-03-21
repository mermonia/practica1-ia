package src.operators;

import aima.search.framework.*;
import src.board.RescueState;

import java.util.*;

/**
 * Genera UN SOLO sucesor aleatorio para Simulated Annealing.
 * Elige aleatoriamente entre los 3 operadores y genera parámetros al azar.
 */
public class DesastresSuccessorFunctionSA implements SuccessorFunction {

    @SuppressWarnings("unchecked")
    public List getSuccessors(Object state) {
        List<Successor> successors = new ArrayList<>();
        RescueState currentState = (RescueState) state;

        Random rnd = new Random();
        int numGrupos = currentState.numGrupos();
        int numHelis = currentState.numRutas();
        int[] grupoHeli = currentState.getGrupoHeli();

        // Elegir operador al azar (0=swap, 1=move, 2=swapOrder)
        int operador = rnd.nextInt(3);

        switch (operador) {
            case 0: { // SwapGroups
                int g1 = rnd.nextInt(numGrupos);
                int g2 = rnd.nextInt(numGrupos);
                int intentos = 0;
                while ((g1 == g2 || grupoHeli[g1] == grupoHeli[g2]) && intentos < 50) {
                    g2 = rnd.nextInt(numGrupos);
                    intentos++;
                }
                if (g1 != g2 && grupoHeli[g1] != grupoHeli[g2]) {
                    int h1 = grupoHeli[g1];
                    int h2 = grupoHeli[g2];
                    RescueState newState = new RescueState(currentState);
                    ArrayList<Integer> ruta1 = newState.getRutaHelicoptero(h1);
                    ArrayList<Integer> ruta2 = newState.getRutaHelicoptero(h2);

                    int pos1 = ruta1.indexOf(g1);
                    int pos2 = ruta2.indexOf(g2);

                    ruta1.set(pos1, g2);
                    ruta2.set(pos2, g1);

                    newState.getGrupoHeli()[g1] = h2;
                    newState.getGrupoHeli()[g2] = h1;

                    successors.add(new Successor("SA-Swap G" + g1 + "<->G" + g2, newState));
                }
                break;
            }
            case 1: { // MoveGroup
                int g = rnd.nextInt(numGrupos);
                int heliOrig = grupoHeli[g];
                int hDest = rnd.nextInt(numHelis);
                int intentos = 0;
                while (hDest == heliOrig && intentos < 50) {
                    hDest = rnd.nextInt(numHelis);
                    intentos++;
                }
                if (hDest != heliOrig) {
                    RescueState newState = new RescueState(currentState);
                    newState.getRutaHelicoptero(heliOrig).remove(Integer.valueOf(g));
                    ArrayList<Integer> rutaDest = newState.getRutaHelicoptero(hDest);
                    int pos = rutaDest.isEmpty() ? 0 : rnd.nextInt(rutaDest.size() + 1);
                    rutaDest.add(pos, g);
                    newState.getGrupoHeli()[g] = hDest;

                    successors.add(new Successor("SA-Move G" + g + " H" + heliOrig + "->H" + hDest, newState));
                }
                break;
            }
            case 2: { // SwapOrder
                // Buscar un helicóptero con >= 2 grupos en su ruta
                List<Integer> helisConRuta = new ArrayList<>();
                for (int h = 0; h < numHelis; h++) {
                    if (currentState.getRutaHelicoptero(h).size() >= 2) {
                        helisConRuta.add(h);
                    }
                }
                if (!helisConRuta.isEmpty()) {
                    int h = helisConRuta.get(rnd.nextInt(helisConRuta.size()));
                    ArrayList<Integer> ruta = currentState.getRutaHelicoptero(h);
                    int i = rnd.nextInt(ruta.size() - 1);

                    RescueState newState = new RescueState(currentState);
                    ArrayList<Integer> newRuta = newState.getRutaHelicoptero(h);
                    int tmp = newRuta.get(i);
                    newRuta.set(i, newRuta.get(i + 1));
                    newRuta.set(i + 1, tmp);

                    successors.add(new Successor("SA-SwapOrder H" + h + " pos" + i, newState));
                }
                break;
            }
        }

        // Si no se pudo generar sucesor, devolver el mismo estado
        if (successors.isEmpty()) {
            successors.add(new Successor("SA-NoOp", new RescueState(currentState)));
        }

        return successors;
    }
}

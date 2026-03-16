package src.generators;

import IA.Desastres.*;
import src.board.RescueState;

import java.util.*;
/** Greedy
 * Criterio: elegir el helicoptero con menor distancia
 */
public class StartGreedy {
    public static RescueState generateGreedyInitialState(Grupos grupos, Centros centros){

        RescueState state = new RescueState(grupos, centros);

        int numHelisPorCentro = centros.getFirst().getNHelicopteros();
        int totalHelis = centros.size() * numHelisPorCentro;

        for(int g = 0; g < grupos.size(); g++){

            Grupo grupo = grupos.get(g);

            int mejorHeli = -1;
            double mejorDist = Double.MAX_VALUE;

            for(int c = 0; c < centros.size(); c++){

                Centro centro = centros.get(c);

                double dist = distancia(
                        grupo.getCoordX(), grupo.getCoordY(),
                        centro.getCoordX(), centro.getCoordY()
                );

                if(dist < mejorDist){
                    mejorDist = dist;
                    mejorHeli = c * numHelisPorCentro;
                }
            }

            state.getRutaHelicoptero(mejorHeli).add(g);
            state.setGrupoHeli(g, mejorHeli);
        }

        return state;
    }

    private static double distancia(int x1, int y1, int x2, int y2){
        return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }
}

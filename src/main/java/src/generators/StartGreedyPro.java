package src.generators;

import IA.Desastres.*;
import java.util.*;
import src.board.RescueState;

/** GreedyPro
 * Criterio: elegir el helicoptero con menor incremento
 */
public class StartGreedyPro {
    public static RescueState generateGreedyProInitialState(Grupos grupos, Centros centros){

        RescueState state = new RescueState(grupos, centros);

        int numHelisPorCentro = centros.getFirst().getNHelicopteros();
        int totalHelis = centros.size() * numHelisPorCentro;

        for(int g = 0; g < grupos.size(); g++){

            int mejorHeli = -1;
            int mejorPos = -1;
            double mejorIncremento = Double.MAX_VALUE;

            for(int h = 0; h < totalHelis; h++){

                ArrayList<Integer> ruta = state.getRutaHelicoptero(h);

                for(int pos = 0; pos <= ruta.size(); pos++){

                    double incremento = calcularIncremento(state,h,pos,g);

                    if(incremento < mejorIncremento){
                        mejorIncremento = incremento;
                        mejorHeli = h;
                        mejorPos = pos;
                    }
                }
            }

            state.insertarGrupo(mejorHeli,mejorPos,g);
        }

        return state;
    }

    private static double calcularIncremento(
            RescueState state,
            int heli,
            int pos,
            int grupo){

        ArrayList<Integer> ruta = state.getRutaHelicoptero(heli);

        double costeAntes = state.getCosteRuta(heli);

        ruta.add(pos,grupo);

        double costeDespues = state.getCosteRuta(heli);

        ruta.remove(pos);

        return costeDespues - costeAntes;
    }
}

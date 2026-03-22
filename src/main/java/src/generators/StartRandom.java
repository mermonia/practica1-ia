package src.generators;
import IA.Desastres.*;
import src.board.RescueState;

import java.util.Random;

public class StartRandom {

    public static RescueState generateRandomInitialState(Grupos g, Centros c){

        RescueState e = new RescueState(g,c);

        Random rnd = new Random();

        for(int i = 0; i < g.size(); i++){

            int numHelis = c.size() * c.getFirst().getNHelicopteros();
            int heli = rnd.nextInt(numHelis);

            e.getRutaHelicoptero(heli).add(i);
            e.setGrupoHeli(i, heli);
        }

        return e;
    }

}

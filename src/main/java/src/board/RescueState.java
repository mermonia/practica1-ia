package src.board;

import IA.Desastres.*;
import java.util.*;

public class RescueState {
    private Grupos grupos;
    private Centros centros;

    // Para cada grupo se le assigna un helicoptero n/NumHelicopterosGrupo = Centro
    int[] grupoHeli;
    ArrayList<ArrayList<Integer>> rutas;

    /**
     *  Constructor normal
    */
    public RescueState(Grupos g, Centros c) {

        grupos = g;
        centros = c;

        int numGrupos = g.size();
        int numHelis = c.size();

        grupoHeli = new int[numGrupos];

        rutas = new ArrayList<>();

        for(int i=0;i<numHelis;i++){
            rutas.add(new ArrayList<>());
        }
    }

    /**
     *  Constructor copia
     */
    public RescueState(RescueState r){

        grupos = r.grupos;
        centros = r.centros;

        grupoHeli = r.grupoHeli.clone();

        rutas = new ArrayList<>();

        for(ArrayList<Integer> s : r.rutas){
            rutas.add(new ArrayList<>(s));
        }
    }

    public Grupos getGrupos() {
        return grupos;
    }

    public Centros getCentros() {
        return centros;
    }

    public int[] getGrupoHeli() {
        return grupoHeli;
    }

    public ArrayList<ArrayList<Integer>> getRutas() {
        return rutas;
    }

    public ArrayList<Integer> getRutaHelicoptero(int h){
        return rutas.get(h);
    }

    public int numGrupos(){
        return grupos.size();
    }

    public int numRutas(){
        return rutas.size();
    }

}

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

    public double getCosteRuta(int h){

        double costeRuta = 0;

        ArrayList<Integer> ruta = rutas.get(h);
        if(ruta.isEmpty()) return 0;

        int helicPorCentro = centros.getFirst().getNHelicopteros();
        int centroId = h / helicPorCentro;

        Centro centro = centros.get(centroId);

        int personasActual = 0;
        int gruposSalida = 0;

        int xActual = centro.getCoordX();
        int yActual = centro.getCoordY();

        for(int g : ruta){

            Grupo grupo = grupos.get(g);
            int personasGrupo = grupo.getNPersonas();

            // si supera capacidad o grupos máximos -> cerrar salida
            if(personasActual + personasGrupo > 15 || gruposSalida == 3){

                costeRuta += distancia(xActual,yActual,
                        centro.getCoordX(),centro.getCoordY()) / 100.0 * 60.0;

                costeRuta += 10; // tiempo preparación

                personasActual = 0;
                gruposSalida = 0;

                xActual = centro.getCoordX();
                yActual = centro.getCoordY();
            }

            // volar al grupo
            double dist = distancia(xActual,yActual,
                    grupo.getCoordX(),grupo.getCoordY());

            costeRuta += dist / 100.0 * 60.0;

            // tiempo recoger personas
            int recogida = grupo.getNPersonas();
            if(grupo.getPrioridad() == 1) recogida *= 2;

            costeRuta += recogida;

            personasActual += personasGrupo;
            gruposSalida++;

            xActual = grupo.getCoordX();
            yActual = grupo.getCoordY();
        }

        // volver al centro al final
        costeRuta += distancia(xActual,yActual,
                centro.getCoordX(),centro.getCoordY()) / 100.0 * 60.0;

        return costeRuta;
    }

    public int numGrupos(){
        return grupos.size();
    }

    public int numRutas(){
        return rutas.size();
    }

    public void setGrupoHeli(int g, int heli) {
        grupoHeli[g] = heli;
    }

    public void insertarGrupo(int heli, int pos, int grupo){

        /*  check
        int heliActual = grupoHeli[grupo];
        if(heliActual != -1){
            rutas.get(heliActual).remove(Integer.valueOf(grupo));
        }*/

        rutas.get(heli).add(pos, grupo);
        grupoHeli[grupo] = heli;
    }

    private double distancia(int x1,int y1,int x2,int y2){
        return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }

}

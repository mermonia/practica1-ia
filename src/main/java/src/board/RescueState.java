package src.board;

import IA.Desastres.*;
import java.util.*;

public class RescueState {
    private Grupos grupos;
    private Centros centros;

    // Para cada grupo se le asigna un helicóptero
    int[] grupoHeli;
    ArrayList<ArrayList<Integer>> rutas;

    /**
     * Constructor normal
     */
    public RescueState(Grupos g, Centros c) {
        grupos = g;
        centros = c;

        int numGrupos = g.size();
        int totalHelis = 0;
        for (int i = 0; i < c.size(); i++) {
            totalHelis += c.get(i).getNHelicopteros();
        }

        grupoHeli = new int[numGrupos];
        Arrays.fill(grupoHeli, -1);

        rutas = new ArrayList<>();
        for (int i = 0; i < totalHelis; i++) {
            rutas.add(new ArrayList<>());
        }
    }

    /**
     * Constructor copia
     */
    public RescueState(RescueState r) {
        grupos = r.grupos;
        centros = r.centros;

        grupoHeli = r.grupoHeli.clone();

        rutas = new ArrayList<>();
        for (ArrayList<Integer> s : r.rutas) {
            rutas.add(new ArrayList<>(s));
        }
    }

    // ==================== Getters ====================

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

    public ArrayList<Integer> getRutaHelicoptero(int h) {
        return rutas.get(h);
    }

    public int numGrupos() {
        return grupos.size();
    }

    public int numRutas() {
        return rutas.size();
    }

    public int getHelicopteroCentro(int h) {
        int acum = 0;
        for (int c = 0; c < centros.size(); c++) {
            acum += centros.get(c).getNHelicopteros();
            if (h < acum) return c;
        }
        return centros.size() - 1;
    }

    // ==================== Modificadores ====================

    public void setGrupoHeli(int g, int heli) {
        grupoHeli[g] = heli;
    }

    public void insertarGrupo(int heli, int pos, int grupo) {
        rutas.get(heli).add(pos, grupo);
        grupoHeli[grupo] = heli;
    }

    /**
     * Elimina un grupo de la ruta de su helicóptero actual.
     */
    public void removeGrupo(int grupo) {
        int heli = grupoHeli[grupo];
        if (heli >= 0) {
            rutas.get(heli).remove(Integer.valueOf(grupo));
            grupoHeli[grupo] = -1;
        }
    }

    // ==================== Cálculos de coste ====================

    /**
     * Calcula el coste (tiempo en minutos) de la ruta de un helicóptero.
     */
    public double getCosteRuta(int h) {
        double costeRuta = 0;

        ArrayList<Integer> ruta = rutas.get(h);
        if (ruta.isEmpty()) return 0;

        int centroId = getHelicopteroCentro(h);
        Centro centro = centros.get(centroId);

        int personasActual = 0;
        int gruposSalida = 0;

        int xActual = centro.getCoordX();
        int yActual = centro.getCoordY();

        for (int g : ruta) {
            Grupo grupo = grupos.get(g);
            int personasGrupo = grupo.getNPersonas();

            // si supera capacidad o grupos máximos -> cerrar salida (nueva trip)
            if (personasActual + personasGrupo > 15 || gruposSalida == 3) {
                // volver al centro
                costeRuta += distancia(xActual, yActual,
                        centro.getCoordX(), centro.getCoordY()) / 100.0 * 60.0;
                costeRuta += 10; // tiempo preparación entre viajes

                personasActual = 0;
                gruposSalida = 0;
                xActual = centro.getCoordX();
                yActual = centro.getCoordY();
            }

            // volar al grupo
            costeRuta += distancia(xActual, yActual,
                    grupo.getCoordX(), grupo.getCoordY()) / 100.0 * 60.0;

            // tiempo recoger personas (prioridad 1 = heridos, tarda el doble)
            int recogida = grupo.getNPersonas();
            if (grupo.getPrioridad() == 1) recogida *= 2;
            costeRuta += recogida;

            personasActual += personasGrupo;
            gruposSalida++;

            xActual = grupo.getCoordX();
            yActual = grupo.getCoordY();
        }

        // volver al centro al final
        costeRuta += distancia(xActual, yActual,
                centro.getCoordX(), centro.getCoordY()) / 100.0 * 60.0;

        return costeRuta;
    }

    /**
     * Coste total: suma de todas las rutas de todos los helicópteros.
     */
    public double getCosteTotal() {
        double total = 0;
        for (int h = 0; h < rutas.size(); h++) {
            total += getCosteRuta(h);
        }
        return total;
    }

    /**
     * Calcula el máximo tiempo transcurrido hasta que un grupo de prioridad 1
     * es completamente recogido y llevado al centro.
     * Esto mide "lo tarde que se rescata al último grupo prioritario".
     */
    public double getMaxTiempoP1() {
        double maxTiempoP1 = 0;

        for (int h = 0; h < rutas.size(); h++) {
            ArrayList<Integer> ruta = rutas.get(h);
            if (ruta.isEmpty()) continue;

            int centroId = getHelicopteroCentro(h);
            Centro centro = centros.get(centroId);

            double tiempoAcum = 0;
            int personasActual = 0;
            int gruposSalida = 0;

            int xActual = centro.getCoordX();
            int yActual = centro.getCoordY();

            for (int g : ruta) {
                Grupo grupo = grupos.get(g);
                int personasGrupo = grupo.getNPersonas();

                if (personasActual + personasGrupo > 15 || gruposSalida == 3) {
                    tiempoAcum += distancia(xActual, yActual,
                            centro.getCoordX(), centro.getCoordY()) / 100.0 * 60.0;
                    tiempoAcum += 10;

                    personasActual = 0;
                    gruposSalida = 0;
                    xActual = centro.getCoordX();
                    yActual = centro.getCoordY();
                }

                tiempoAcum += distancia(xActual, yActual,
                        grupo.getCoordX(), grupo.getCoordY()) / 100.0 * 60.0;

                int recogida = grupo.getNPersonas();
                if (grupo.getPrioridad() == 1) recogida *= 2;
                tiempoAcum += recogida;

                personasActual += personasGrupo;
                gruposSalida++;

                xActual = grupo.getCoordX();
                yActual = grupo.getCoordY();

                // Si este grupo es prioridad 1, el tiempo de vuelta al centro
                // sería cuando queda rescatado
                if (grupo.getPrioridad() == 1) {
                    double tiempoVuelta = tiempoAcum +
                            distancia(xActual, yActual,
                                    centro.getCoordX(), centro.getCoordY()) / 100.0 * 60.0;
                    if (tiempoVuelta > maxTiempoP1) {
                        maxTiempoP1 = tiempoVuelta;
                    }
                }
            }
        }
        return maxTiempoP1;
    }

    // ==================== Utilidad ====================

    private double distancia(int x1, int y1, int x2, int y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }
}

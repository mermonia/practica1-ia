package src;

import IA.Desastres.*;
import aima.search.framework.*;
import aima.search.informed.*;

import src.board.*;
import src.generators.*;
import src.operators.*;
import src.heuristics.*;

import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {

        // ==================== Parámetros ====================
        int nGrupos = 100;
        int seed = 1234;
        int nCentros = 5;
        int nHelicopteros = 2; // por centro

        // ==================== Crear datos ====================
        Grupos grupos = new Grupos(nGrupos, seed);
        Centros centros = new Centros(nCentros, nHelicopteros, seed);

        System.out.println("=== Práctica Búsqueda Local: Desastres ===");
        System.out.println("Grupos: " + grupos.size());
        System.out.println("Centros: " + centros.size() + " (con " + nHelicopteros + " helis c/u)");
        System.out.println("Total helicópteros: " + centros.size() * nHelicopteros);
        System.out.println();

        // ==================== Estado Inicial ====================
        RescueState initialState = StartGreedyPro.generateGreedyProInitialState(grupos, centros);
        System.out.println("Coste inicial (GreedyPro): " + String.format("%.2f", initialState.getCosteTotal()) + " min");
        System.out.println("Max tiempo P1 inicial: " + String.format("%.2f", initialState.getMaxTiempoP1()) + " min");
        System.out.println();

        // ==================== Hill Climbing ====================
        System.out.println("--- Hill Climbing (Heurística: Tiempo Total) ---");
        runHillClimbing(initialState, new HeuristicTotalTime());
        System.out.println();

        System.out.println("--- Hill Climbing (Heurística: Prioridad) ---");
        runHillClimbing(initialState, new HeuristicPriority());
        System.out.println();

        // ==================== Simulated Annealing ====================
        System.out.println("--- Simulated Annealing (Heurística: Tiempo Total) ---");
        runSimulatedAnnealing(initialState, new HeuristicTotalTime());
        System.out.println();

        System.out.println("--- Simulated Annealing (Heurística: Prioridad) ---");
        runSimulatedAnnealing(initialState, new HeuristicPriority());
    }

    private static void runHillClimbing(RescueState initial, HeuristicFunction heuristic) throws Exception {
        Problem problem = new Problem(
                initial,
                new DesastresSuccessorFunction(),
                new DesastresGoalTest(),
                heuristic
        );

        long t0 = System.currentTimeMillis();
        HillClimbingSearch search = new HillClimbingSearch();
        SearchAgent agent = new SearchAgent(problem, search);
        long t1 = System.currentTimeMillis();

        RescueState result = (RescueState) search.getGoalState();
        System.out.println("  Coste total: " + String.format("%.2f", result.getCosteTotal()) + " min");
        System.out.println("  Max tiempo P1: " + String.format("%.2f", result.getMaxTiempoP1()) + " min");
        System.out.println("  Tiempo ejecución: " + (t1 - t0) + " ms");
        System.out.println("  Nodos expandidos: " + agent.getInstrumentation().getProperty("nodesExpanded"));
    }

    private static void runSimulatedAnnealing(RescueState initial, HeuristicFunction heuristic) throws Exception {
        Problem problem = new Problem(
                initial,
                new DesastresSuccessorFunctionSA(),
                new DesastresGoalTest(),
                heuristic
        );

        long t0 = System.currentTimeMillis();
        SimulatedAnnealingSearch search = new SimulatedAnnealingSearch(
                2000,   // steps
                100,    // stiter
                5,      // k
                0.001   // lambda
        );
        SearchAgent agent = new SearchAgent(problem, search);
        long t1 = System.currentTimeMillis();

        RescueState result = (RescueState) search.getGoalState();
        System.out.println("  Coste total: " + String.format("%.2f", result.getCosteTotal()) + " min");
        System.out.println("  Max tiempo P1: " + String.format("%.2f", result.getMaxTiempoP1()) + " min");
        System.out.println("  Tiempo ejecución: " + (t1 - t0) + " ms");
        System.out.println("  Nodos expandidos: " + agent.getInstrumentation().getProperty("nodesExpanded"));
    }
}

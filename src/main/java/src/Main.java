package src;

import IA.Desastres.*;
import aima.search.framework.*;
import aima.search.informed.*;


import src.board.*;
import src.generators.*;
import src.operators.*;
import src.heuristics.*;

import java.io.*;
import java.util.*;

public class Main {

    // ================================================================
    // SEEDS fixes per a reproduïbilitat (iguals per a tots els experiments)
    // ================================================================
    static final int[] SEEDS = {11, 22, 33, 44, 55, 66, 77, 88, 99, 110};

    // ================================================================
    // Paràmetres SA — actualitza'ls després de l'experiment 3
    // ================================================================
    static int    SA_STEPS  = 100000;
    static int    SA_STITER = 100;
    static int    SA_K      = 10;
    static double SA_LAMBDA = 0.001;

    // Guarda l'últim estat resultat (per llegir H1/H2 per separat a exp7)
    static RescueState lastState = null;

    // ================================================================
    // MAIN
    // ================================================================
    public static void main(String[] args) throws Exception {
        int exp = args.length > 0 ? Integer.parseInt(args[0]) : 0;
        switch (exp) {
            case 1  -> experiment1_operadors();
            case 2  -> experiment2_inicialitzacio();
            case 3  -> experiment3_SA_params();
            case 4  -> experiment4_escalabilitat_proporcional();
            case 5  -> experiment5_escalabilitat_separada();
            case 6  -> experiment6_helicopters();
            case 7  -> experiment7_heuristica2();
            default -> {
                System.out.println("Indica l'experiment: java src.Main <1-7>");
                System.out.println("  1 → Operadors");
                System.out.println("  2 → Solució inicial");
                System.out.println("  3 → Paràmetres SA");
                System.out.println("  4 → Escalabilitat proporcional (HC + SA)");
                System.out.println("  5 → Escalabilitat separada (HC)");
                System.out.println("  6 → Helicòpters per centre");
                System.out.println("  7 → Heurística H2 i ponderació");
            }
        }
    }

    // ================================================================
    // EXPERIMENT 1 — Quins operadors donen millors resultats?
    // Escenari: 5 centres, 1 heli/centre, 100 grups | HC | H1
    // Compara DesastresSuccessorFunction vs DesastresSuccessorFunctionSA
    // ================================================================
    static void experiment1_operadors() throws Exception {
        System.out.println("=== EXPERIMENT 1: Operadors ===");
        String csv = "exp,operadors,seed,h_inicial,h_final,temps_ms,nodes\n";

        String[] noms = {"OpHC", "OpSA"};
        SuccessorFunction[] ops = {
                new DesastresSuccessorFunction(),
                new DesastresSuccessorFunctionSA()
        };

        for (int oi = 0; oi < noms.length; oi++) {
            for (int seed : SEEDS) {
                Grupos  g = new Grupos(100, seed);
                Centros c = new Centros(5, 1, seed);
                RescueState ini = generaInicial("GreedyPro", g, c);
                double hIni = new HeuristicTotalTime().getHeuristicValue(ini);

                double[] res = runHC(ini, ops[oi], new HeuristicTotalTime());
                csv += String.format("1,%s,%d,%.4f,%.4f,%.0f,%.0f\n",
                        noms[oi], seed, hIni, res[0], res[1], res[2]);
                System.out.printf("  [%s] seed=%d  Hini=%.2f  Hfin=%.2f  t=%dms\n",
                        noms[oi], seed, hIni, res[0], (long)res[1]);
            }
        }
        saveCSV("exp1_operadors.csv", csv);
    }

    // ================================================================
    // EXPERIMENT 2 — Quina estratègia inicial dóna millors resultats?
    // Escenari: 5 centres, 1 heli/centre, 100 grups | HC | H1
    // Operadors fixats a l'exp. 1
    // ================================================================
    static void experiment2_inicialitzacio() throws Exception {
        System.out.println("=== EXPERIMENT 2: Solució inicial ===");
        String csv = "exp,estrategia,seed,h_inicial,h_final,temps_ms,nodes\n";

        String[] estrategies = {"Random", "Greedy", "GreedyPro"};

        for (String est : estrategies) {
            for (int seed : SEEDS) {
                Grupos  g = new Grupos(100, seed);
                Centros c = new Centros(5, 1, seed);
                RescueState ini;
                try {
                    ini = generaInicial(est, g, c);
                } catch (Exception e) {
                    System.out.println("  ERROR amb estrategia " + est + ": " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }
                double hIni = new HeuristicTotalTime().getHeuristicValue(ini);

                double[] res = runHC(ini, new DesastresSuccessorFunction(), new HeuristicTotalTime());
                csv += String.format("2,%s,%d,%.4f,%.4f,%.0f,%.0f\n",
                        est, seed, hIni, res[0], res[1], res[2]);
                System.out.printf("  [%s] seed=%d  Hini=%.2f  Hfin=%.2f  t=%dms\n",
                        est, seed, hIni, res[0], (long)res[1]);
            }
        }
        saveCSV("exp2_inicial.csv", csv);
    }

    // ================================================================
    // EXPERIMENT 3 — Ajust paràmetres Simulated Annealing
    // Graella: k × lambda × steps | Escenari base | H1
    // ================================================================
    static void experiment3_SA_params() throws Exception {
        System.out.println("=== EXPERIMENT 3: Paràmetres SA ===");
        String csv = "exp,k,lambda,steps,stiter,seed,h_final,temps_ms\n";

        int[] ks = {1, 10, 100, 1000};
        double[] lambdas = {1.0, 0.01, 0.001, 0.0001};
        int[]    stepArr = {5000, 20000, 100000};
        int      stiter  = 100;

        for (int k : ks) {
            for (double lam : lambdas) {
                for (int st : stepArr) {
                    // 5 rèpliques per la graella (amplia a 10 per als millors valors)
                    for (int i = 0; i < 5; i++) {
                        int seed = SEEDS[i];
                        Grupos  g = new Grupos(100, seed);
                        Centros c = new Centros(5, 1, seed);
                        RescueState ini = generaInicial("GreedyPro", g, c);

                        double[] res = runSA(ini, new DesastresSuccessorFunctionSA(),
                                new HeuristicTotalTime(), st, stiter, k, lam);
                        csv += String.format(java.util.Locale.US, "3,%d,%.6f,%d,%d,%d,%.4f,%.0f\n",
                                k, lam, st, stiter, seed, res[0], res[1]);
                        System.out.printf("  k=%d λ=%.5f steps=%d seed=%d → H=%.2f t=%dms\n",
                                k, lam, st, seed, res[0], (long)res[1]);
                    }
                }
            }
        }
        saveCSV("exp3_SA_params.csv", csv);
    }

    // ================================================================
    // EXPERIMENT 4 — Escalabilitat proporcional (ratio 5 centres : 100 grups)
    // Incrementa de 5 en 5 fins veure tendència | HC + SA | H1
    // ================================================================
    static void experiment4_escalabilitat_proporcional() throws Exception {
        System.out.println("=== EXPERIMENT 4: Escalabilitat proporcional ===");
        String csv = "exp,algoritme,n_centres,n_grups,seed,h_final,temps_ms\n";

        int[] centresArr = {5, 10, 15, 20, 25, 30};

        for (int nc : centresArr) {
            int ng = nc * 20; // ratio 5:100
            for (int seed : SEEDS) {
                Grupos  g = new Grupos(ng, seed);
                Centros c = new Centros(nc, 1, seed);

                // HC
                RescueState iniHC = generaInicial("GreedyPro", g, c);
                double[] hc = runHC(iniHC, new DesastresSuccessorFunction(), new HeuristicTotalTime());
                csv += String.format("4,HC,%d,%d,%d,%.4f,%.0f\n", nc, ng, seed, hc[0], hc[1]);
                System.out.printf("  HC  nc=%d ng=%d seed=%d → H=%.2f t=%dms\n",
                        nc, ng, seed, hc[0], (long)hc[1]);

                // SA
                RescueState iniSA = generaInicial("GreedyPro", g, c);
                double[] sa = runSA(iniSA, new DesastresSuccessorFunctionSA(),
                        new HeuristicTotalTime(), SA_STEPS, SA_STITER, SA_K, SA_LAMBDA);
                csv += String.format("4,SA,%d,%d,%d,%.4f,%.0f\n", nc, ng, seed, sa[0], sa[1]);
                System.out.printf("  SA  nc=%d ng=%d seed=%d → H=%.2f t=%dms\n",
                        nc, ng, seed, sa[0], (long)sa[1]);
            }
            saveCSV("exp4_escalabilitat_proporcional.csv", csv);
            System.out.println("  → Checkpoint guardat nc=" + nc);
        }
        saveCSV("exp4_escalabilitat_proporcional.csv", csv);
    }

    // ================================================================
    // EXPERIMENT 5 — Escalabilitat separada (només HC)
    // 5a: augmenta grups mantenint 5 centres
    // 5b: augmenta centres mantenint 100 grups
    // ================================================================
    static void experiment5_escalabilitat_separada() throws Exception {
        System.out.println("=== EXPERIMENT 5a: Augmenta grups (5 centres fix) ===");
        String csv = "exp,escenari,n_centres,n_grups,seed,h_final,temps_ms\n";

        int[] grupsArr = {100, 150, 200, 250, 300, 350, 400};
        for (int ng : grupsArr) {
            for (int seed : SEEDS) {
                Grupos  g = new Grupos(ng, seed);
                Centros c = new Centros(5, 1, seed);
                RescueState ini = generaInicial("GreedyPro", g, c);
                double[] res = runHC(ini, new DesastresSuccessorFunction(), new HeuristicTotalTime());
                csv += String.format(java.util.Locale.US, "5a,grups,5,%d,%d,%.4f,%.0f\n", ng, seed, res[0], res[1]);
                System.out.printf("  5a ng=%d seed=%d → H=%.2f t=%dms\n", ng, seed, res[0], (long)res[1]);
            }
        }

        System.out.println("=== EXPERIMENT 5b: Augmenta centres (100 grups fix) ===");
        int[] centresArr = {5, 10, 15, 20, 25, 30, 35};
        for (int nc : centresArr) {
            for (int seed : SEEDS) {
                Grupos  g = new Grupos(100, seed);
                Centros c = new Centros(nc, 1, seed);
                RescueState ini = generaInicial("GreedyPro", g, c);
                double[] res = runHC(ini, new DesastresSuccessorFunction(), new HeuristicTotalTime());
                csv += String.format(java.util.Locale.US,"5b,centres,%d,100,%d,%.4f,%.0f\n", nc, seed, res[0], res[1]);
                System.out.printf("  5b nc=%d seed=%d → H=%.2f t=%dms\n", nc, seed, res[0], (long)res[1]);
            }
        }
        saveCSV("exp5_escalabilitat_separada.csv", csv);
    }

    // ================================================================
    // EXPERIMENT 6 — Més helicòpters per centre
    // Compara 1, 2, 3, 5 helis/centre | 5 centres, 100 grups | HC | H1
    // ================================================================
    static void experiment6_helicopters() throws Exception {
        System.out.println("=== EXPERIMENT 6: Helicòpters per centre ===");
        System.out.println("  >> ESCRIU LES TEVES HIPÒTESIS ABANS DE MIRAR ELS RESULTATS <<");
        String csv = "exp,n_helis,seed,h_final,temps_ms\n";

        int[] helisArr = {1, 2, 3, 5};
        for (int nh : helisArr) {
            for (int seed : SEEDS) {
                Grupos  g = new Grupos(100, seed);
                Centros c = new Centros(5, nh, seed);
                RescueState ini = generaInicial("GreedyPro", g, c);
                double[] res = runHC(ini, new DesastresSuccessorFunction(), new HeuristicTotalTime());
                csv += String.format(java.util.Locale.US,"6,%d,%d,%.4f,%.0f\n", nh, seed, res[0], res[1]);
                System.out.printf("  helis=%d seed=%d → H=%.2f t=%dms\n", nh, seed, res[0], (long)res[1]);
            }
        }
        saveCSV("exp6_helicopters.csv", csv);
    }

    // ================================================================
    // EXPERIMENT 7 — H2 i ponderació
    // H_combinada = H1 + w*H2, w ∈ {0,1,2,4,8,16}  (w=0 = referència H1 pura)
    // HC i SA | escenari base
    // ================================================================
    static void experiment7_heuristica2() throws Exception {
        System.out.println("=== EXPERIMENT 7: Heurística H2 i ponderació ===");
        String csv = "exp,algoritme,w,seed,h1_final,h2_final,h_combinada,temps_ms\n";

        double[] ws = {0, 1, 2, 4, 8, 16};

        for (double w : ws) {
            HeuristicFunction hComb = buildCombined(w);

            for (int seed : SEEDS) {
                Grupos  g = new Grupos(100, seed);
                Centros c = new Centros(5, 1, seed);

                // ---- HC ----
                RescueState iniHC = generaInicial("GreedyPro", g, c);
                double[] hc = runHC(iniHC, new DesastresSuccessorFunction(), hComb);
                double h1hc = new HeuristicTotalTime().getHeuristicValue(lastState);
                double h2hc = new HeuristicPriority().getHeuristicValue(lastState);
                csv += String.format(java.util.Locale.US,"7,HC,%.0f,%d,%.4f,%.4f,%.4f,%.0f\n",
                        w, seed, h1hc, h2hc, hc[0], hc[1]);
                System.out.printf("  HC w=%.0f seed=%d  H1=%.2f  H2=%.2f  t=%dms\n",
                        w, seed, h1hc, h2hc, (long)hc[1]);

                // ---- SA ----
                RescueState iniSA = generaInicial("GreedyPro", g, c);
                double[] sa = runSA(iniSA, new DesastresSuccessorFunctionSA(),
                        hComb, SA_STEPS, SA_STITER, SA_K, SA_LAMBDA);
                double h1sa = new HeuristicTotalTime().getHeuristicValue(lastState);
                double h2sa = new HeuristicPriority().getHeuristicValue(lastState);
                csv += String.format(java.util.Locale.US,"7,SA,%.0f,%d,%.4f,%.4f,%.4f,%.0f\n",
                        w, seed, h1sa, h2sa, sa[0], sa[1]);
                System.out.printf("  SA w=%.0f seed=%d  H1=%.2f  H2=%.2f  t=%dms\n",
                        w, seed, h1sa, h2sa, (long)sa[1]);
            }
        }
        saveCSV("exp7_heuristica2.csv", csv);
    }

    // ================================================================
    // HELPERS
    // ================================================================

    /** Retorna H1 + w*H2 com a HeuristicFunction (lambda, sense classe nova). */
    static HeuristicFunction buildCombined(double w) {
        HeuristicFunction h1 = new HeuristicTotalTime();
        HeuristicFunction h2 = new HeuristicPriority();
        if (w == 0) return h1;
        return state -> h1.getHeuristicValue(state) + w * h2.getHeuristicValue(state);
    }

    /** Executa Hill Climbing. Retorna [h_final, temps_ms, nodes_expandits]. */
    static double[] runHC(RescueState initial, SuccessorFunction sf,
                          HeuristicFunction hf) throws Exception {
        Problem problem = new Problem(initial, sf, new DesastresGoalTest(), hf);
        HillClimbingSearch search = new HillClimbingSearch();
        long t0 = System.currentTimeMillis();
        new SearchAgent(problem, search);
        long t1 = System.currentTimeMillis();

        lastState = (RescueState) search.getGoalState();
        double hFinal = hf.getHeuristicValue(lastState);
        double nodes  = 0;
        nodes = search.getNodesExpanded();
        return new double[]{hFinal, (t1 - t0), nodes};
    }

    /** Executa Simulated Annealing. Retorna [h_final, temps_ms]. */
    static double[] runSA(RescueState initial, SuccessorFunction sf,
                          HeuristicFunction hf,
                          int steps, int stiter, int k, double lambda) throws Exception {
        Problem problem = new Problem(initial, sf, new DesastresGoalTest(), hf);
        SimulatedAnnealingSearch search = new SimulatedAnnealingSearch(steps, stiter, k, lambda);
        long t0 = System.currentTimeMillis();
        new SearchAgent(problem, search);
        long t1 = System.currentTimeMillis();

        lastState = (RescueState) search.getGoalState();
        double hFinal = hf.getHeuristicValue(lastState);
        return new double[]{hFinal, (t1 - t0)};
    }

    /**
     * Genera l'estat inicial segons l'estratègia.
     * Adapta els noms dels mètodes si els teus generators s'anomenen diferent.
     */
    static RescueState generaInicial(String estrategia, Grupos grupos, Centros centros) {
        return switch (estrategia) {
            case "Random"    -> StartRandom.generateRandomInitialState(grupos, centros);
            case "Greedy"    -> StartGreedy.generateGreedyInitialState(grupos, centros);
            case "GreedyPro" -> StartGreedyPro.generateGreedyProInitialState(grupos, centros);
            default -> throw new IllegalArgumentException("Estratègia desconeguda: " + estrategia);
        };
    }

    /** Escriu un String CSV a disc. */
    static void saveCSV(String filename, String content) throws Exception {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.print(content);
        }
        System.out.println("  → CSV guardat: " + filename);
    }
}

"""
plot_experiments.py
====================
Genera els gràfics de tots els experiments de la pràctica.

Ús:
    python plot_experiments.py               # tots els experiments
    python plot_experiments.py 1 3 5         # experiments concrets

Els CSVs s'han de trobar al mateix directori que aquest script,
o bé ajusta la variable CSV_DIR al principi del fitxer.

Dependències:  pip install pandas matplotlib seaborn
"""

import sys
import os
import warnings
warnings.filterwarnings("ignore")

import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.ticker as mticker
import seaborn as sns
import numpy as np

# ── Configuració ─────────────────────────────────────────────────────────────
CSV_DIR    = "."          # directori on estan els CSVs
OUTPUT_DIR = "plots"      # directori on es desaran els PNGs
DPI        = 150

# Paleta coherent entre experiments
PALETTE = ["#534AB7","#1D9E75","#D85A30","#378ADD","#D4537E","#639922","#BA7517"]

# ── Helpers ───────────────────────────────────────────────────────────────────

def read_csv(filename):
    """Llegeix un CSV amb decimal de coma o punt."""
    path = os.path.join(CSV_DIR, filename)
    if not os.path.exists(path):
        print(f"  [!] No trobat: {path}  →  saltant")
        return None
    df = pd.read_csv(path, sep=",")
    # Convertir columnes numèriques que tinguin coma decimal
    for col in df.columns:
        if df[col].dtype == object:
            try:
                df[col] = df[col].str.replace(",", ".").astype(float)
            except (ValueError, AttributeError):
                pass
    return df


def savefig(fig, name):
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    path = os.path.join(OUTPUT_DIR, name)
    fig.savefig(path, dpi=DPI, bbox_inches="tight")
    plt.close(fig)
    print(f"  → {path}")


def bar_with_err(ax, x_labels, means, stds, colors, ylabel, title, y_min=None):
    """Barres amb barra d'error de desviació estàndard."""
    x = np.arange(len(x_labels))
    bars = ax.bar(x, means, color=colors[:len(x_labels)],
                  yerr=stds, capsize=4, error_kw={"elinewidth": 1, "ecolor": "#555"})
    ax.set_xticks(x)
    ax.set_xticklabels(x_labels, rotation=25, ha="right", fontsize=9)
    ax.set_ylabel(ylabel)
    ax.set_title(title)
    if y_min is not None:
        ax.set_ylim(bottom=y_min)
    return bars


# ── Experiment 1 ──────────────────────────────────────────────────────────────

def plot_exp1(df):
    """7 operadors: h_final, reducció %, temps."""
    print("  Exp 1: operadors")

    order = ["SwapGroups","MoveGroup","SwapOrder",
             "SwapGroups+MoveGroup","SwapGroups+SwapOrder",
             "MoveGroup+SwapOrder","SwapGroups+MoveGroup+SwapOrder"]
    short = {"SwapGroups":"SG","MoveGroup":"MG","SwapOrder":"SO",
             "SwapGroups+MoveGroup":"SG+MG","SwapGroups+SwapOrder":"SG+SO",
             "MoveGroup+SwapOrder":"MG+SO","SwapGroups+MoveGroup+SwapOrder":"SG+MG+SO"}

    col_fin = "h_final" if "h_final" in df.columns else df.columns[4]
    col_ini = "h_inicial" if "h_inicial" in df.columns else df.columns[3]
    col_t   = "temps_ms" if "temps_ms" in df.columns else df.columns[5]
    col_op  = "operadors" if "operadors" in df.columns else df.columns[1]

    present = [o for o in order if o in df[col_op].unique()]
    labels  = [short[o] for o in present]

    stats = df.groupby(col_op).agg(
        mean_fin=(col_fin, "mean"),
        std_fin=(col_fin, "std"),
        mean_ini=(col_ini, "mean"),
        mean_t=(col_t, "mean"),
        std_t=(col_t, "std"),
    ).reindex(present)

    stats["red_pct"] = (stats["mean_ini"] - stats["mean_fin"]) / stats["mean_ini"] * 100

    fig, axes = plt.subplots(1, 3, figsize=(14, 4))
    fig.suptitle("Experiment 1 — Comparació d'operadors (HC)", fontweight="bold")

    bar_with_err(axes[0], labels, stats["mean_fin"], stats["std_fin"],
                 PALETTE, "h_final (mitjana ± std)", "Qualitat de solució",
                 y_min=stats["mean_fin"].min() * 0.95)

    axes[1].bar(np.arange(len(labels)), stats["red_pct"],
                color=PALETTE[:len(labels)])
    axes[1].set_xticks(np.arange(len(labels)))
    axes[1].set_xticklabels(labels, rotation=25, ha="right", fontsize=9)
    axes[1].set_ylabel("Reducció % sobre h_inicial")
    axes[1].set_title("Reducció relativa")
    axes[1].yaxis.set_major_formatter(mticker.PercentFormatter())

    bar_with_err(axes[2], labels, stats["mean_t"], stats["std_t"],
                 PALETTE, "Temps (ms)", "Temps d'execució")

    fig.tight_layout()
    savefig(fig, "exp1_operadors.png")


# ── Experiment 1.2 ────────────────────────────────────────────────────────────

def plot_exp1_2(df):
    """3 operadors × 2 centres × 3 helis."""
    print("  Exp 1.2: operadors per escenari")

    col_op  = "operador"
    col_c   = "num_centres"
    col_h   = "heli_per_centre"
    col_fin = "h_fin"
    col_t   = "temps_ms"

    df["escenari"] = df[col_c].astype(str) + "c/" + df[col_h].astype(str) + "h"
    escenaris = sorted(df["escenari"].unique())
    ops = sorted(df[col_op].unique())
    n_ops = len(ops)
    n_esc = len(escenaris)

    stats = df.groupby([col_op, "escenari"])[col_fin].agg(["mean","std"]).reset_index()

    x = np.arange(n_esc)
    width = 0.25
    fig, axes = plt.subplots(1, 2, figsize=(14, 5))
    fig.suptitle("Experiment 1.2 — Operadors per escenari (HC)", fontweight="bold")

    for ax, metric, ylabel, title in [
        (axes[0], col_fin, "h_final (mitjana)", "Qualitat"),
        (axes[1], col_t,   "Temps (ms)",        "Temps"),
    ]:
        for i, op in enumerate(ops):
            sub = df[df[col_op] == op].groupby("escenari")[metric].mean().reindex(escenaris)
            ax.bar(x + i * width, sub.values, width=width,
                   label=op, color=PALETTE[i], alpha=0.9)
        ax.set_xticks(x + width)
        ax.set_xticklabels(escenaris, rotation=20, ha="right", fontsize=9)
        ax.set_ylabel(ylabel)
        ax.set_title(title)
        ax.legend(fontsize=8)

    fig.tight_layout()
    savefig(fig, "exp1_2_operadors.png")


# ── Experiment 2 ──────────────────────────────────────────────────────────────

def plot_exp2(df):
    """3 estratègies inicials: h_inicial, h_final, temps."""
    print("  Exp 2: inicialització")

    col_est = "estrategia"
    col_ini = "h_inicial"
    col_fin = "h_final"
    col_t   = "temps_ms"

    ests   = df[col_est].unique()
    stats  = df.groupby(col_est).agg(
        mean_ini=(col_ini, "mean"), std_ini=(col_ini, "std"),
        mean_fin=(col_fin, "mean"), std_fin=(col_fin, "std"),
        mean_t=(col_t, "mean"),     std_t=(col_t, "std"),
    ).reindex(ests)

    fig, axes = plt.subplots(1, 3, figsize=(12, 4))
    fig.suptitle("Experiment 2 — Estratègia d'inicialització (HC)", fontweight="bold")

    bar_with_err(axes[0], ests, stats["mean_ini"], stats["std_ini"],
                 PALETTE, "h_inicial", "Qualitat estat inicial")
    bar_with_err(axes[1], ests, stats["mean_fin"], stats["std_fin"],
                 PALETTE, "h_final", "Qualitat solució final")
    bar_with_err(axes[2], ests, stats["mean_t"], stats["std_t"],
                 PALETTE, "Temps (ms)", "Temps d'execució")

    fig.tight_layout()
    savefig(fig, "exp2_inicialitzacio.png")


# ── Experiment 3 ──────────────────────────────────────────────────────────────

def plot_exp3(df):
    """SA: h_final per stiter (i opcionalment per k, lambda, steps)."""
    print("  Exp 3: paràmetres SA")

    col_fin  = "h_final"
    col_stit = "stiter" if "stiter" in df.columns else df.columns[2]

    vary_cols = []
    for c in ["k", "lambda", "steps"]:
        if c in df.columns and df[c].nunique() > 1:
            vary_cols.append(c)

    # Sempre plot stiter
    fig, ax = plt.subplots(figsize=(8, 4))
    stats = df.groupby(col_stit)[col_fin].agg(["mean","std"]).reset_index()
    x = np.arange(len(stats))
    ax.bar(x, stats["mean"], yerr=stats["std"], capsize=4,
           color=PALETTE[0], error_kw={"elinewidth": 1})
    ax.set_xticks(x)
    ax.set_xticklabels(stats[col_stit].astype(str), rotation=20, ha="right")
    ax.set_xlabel("stiter")
    ax.set_ylabel("h_final (mitjana ± std)")
    ax.set_title("Experiment 3 — SA: h_final per stiter")
    fig.tight_layout()
    savefig(fig, "exp3_SA_stiter.png")

    # Si hi ha més variables que varien, una figura per cadascuna
    for vc in vary_cols:
        fig, ax = plt.subplots(figsize=(8, 4))
        stats_v = df.groupby(vc)[col_fin].agg(["mean","std"]).reset_index()
        x = np.arange(len(stats_v))
        ax.bar(x, stats_v["mean"], yerr=stats_v["std"], capsize=4,
               color=PALETTE[1], error_kw={"elinewidth": 1})
        ax.set_xticks(x)
        ax.set_xticklabels(stats_v[vc].astype(str), rotation=20, ha="right")
        ax.set_xlabel(vc)
        ax.set_ylabel("h_final (mitjana ± std)")
        ax.set_title(f"Experiment 3 — SA: h_final per {vc}")
        fig.tight_layout()
        savefig(fig, f"exp3_SA_{vc}.png")

    # Heatmap k × lambda si hi ha variació
    if "k" in vary_cols and "lambda" in vary_cols:
        pivot = df.groupby(["k","lambda"])[col_fin].mean().unstack()
        fig, ax = plt.subplots(figsize=(8, 4))
        sns.heatmap(pivot, ax=ax, annot=True, fmt=".0f",
                    cmap="YlOrRd_r", cbar_kws={"label":"h_final"})
        ax.set_title("Experiment 3 — SA: heatmap k × λ")
        fig.tight_layout()
        savefig(fig, "exp3_SA_heatmap.png")


# ── Experiment 4 ──────────────────────────────────────────────────────────────

def plot_exp4(df):
    """HC vs SA escalabilitat proporcional."""
    print("  Exp 4: escalabilitat proporcional")

    col_alg = "algoritme"
    col_nc  = "n_centres"
    col_fin = "h_final"
    col_t   = "temps_ms"

    stats = df.groupby([col_alg, col_nc]).agg(
        mean_fin=(col_fin, "mean"), std_fin=(col_fin, "std"),
        mean_t=(col_t, "mean"),     std_t=(col_t, "std"),
    ).reset_index()

    algs   = stats[col_alg].unique()
    centres = sorted(stats[col_nc].unique())

    fig, axes = plt.subplots(1, 2, figsize=(12, 4))
    fig.suptitle("Experiment 4 — Escalabilitat proporcional (HC vs SA)", fontweight="bold")

    for ax, metric, std_m, ylabel, title in [
        (axes[0], "mean_fin", "std_fin", "h_final (mitjana ± std)", "Qualitat"),
        (axes[1], "mean_t",   "std_t",   "Temps (ms)",               "Temps"),
    ]:
        for i, alg in enumerate(algs):
            sub = stats[stats[col_alg] == alg].set_index(col_nc).reindex(centres)
            ax.errorbar(centres, sub[metric], yerr=sub[std_m],
                        marker="o", label=alg, color=PALETTE[i], capsize=4)
        ax.set_xlabel("Nombre de centres")
        ax.set_ylabel(ylabel)
        ax.set_title(title)
        ax.legend()
        ax.set_xticks(centres)

    fig.tight_layout()
    savefig(fig, "exp4_escalabilitat_proporcional.png")


# ── Experiment 5 ──────────────────────────────────────────────────────────────

def plot_exp5(df):
    """5a: varia grups. 5b: varia centres."""
    print("  Exp 5: escalabilitat separada")

    col_esc = "escenari"
    col_fin = "h_final"
    col_t   = "temps_ms"

    fig, axes = plt.subplots(2, 2, figsize=(12, 8))
    fig.suptitle("Experiment 5 — Escalabilitat separada (HC)", fontweight="bold")

    for row, esc_id in enumerate(["5a","5b"]):
        sub = df[df[col_esc] == esc_id].copy()
        if sub.empty:
            continue

        x_col = "n_grups" if esc_id == "5a" else "n_centres"
        xs = sorted(sub[x_col].unique())
        means_fin = sub.groupby(x_col)[col_fin].mean().reindex(xs)
        stds_fin  = sub.groupby(x_col)[col_fin].std().reindex(xs)
        means_t   = sub.groupby(x_col)[col_t].mean().reindex(xs)
        stds_t    = sub.groupby(x_col)[col_t].std().reindex(xs)

        xlabel = "Nombre de grups" if esc_id == "5a" else "Nombre de centres"

        axes[row][0].errorbar(xs, means_fin, yerr=stds_fin,
                              marker="o", color=PALETTE[row*2], capsize=4)
        axes[row][0].set_xlabel(xlabel)
        axes[row][0].set_ylabel("h_final (mitjana ± std)")
        axes[row][0].set_title(f"{'5a' if row==0 else '5b'}: qualitat")
        axes[row][0].set_xticks(xs)

        axes[row][1].errorbar(xs, means_t, yerr=stds_t,
                              marker="s", color=PALETTE[row*2+1], capsize=4)
        axes[row][1].set_xlabel(xlabel)
        axes[row][1].set_ylabel("Temps (ms)")
        axes[row][1].set_title(f"{'5a' if row==0 else '5b'}: temps")
        axes[row][1].set_xticks(xs)

    fig.tight_layout()
    savefig(fig, "exp5_escalabilitat_separada.png")


# ── Experiment 6 ──────────────────────────────────────────────────────────────

def plot_exp6(df):
    """Helicòpters per centre: h_final i temps."""
    print("  Exp 6: helicòpters per centre")

    col_nh  = "n_helis"
    col_fin = "h_final"
    col_t   = "temps_ms"

    helis  = sorted(df[col_nh].unique())
    means_f = df.groupby(col_nh)[col_fin].mean().reindex(helis)
    stds_f  = df.groupby(col_nh)[col_fin].std().reindex(helis)
    means_t = df.groupby(col_nh)[col_t].mean().reindex(helis)
    stds_t  = df.groupby(col_nh)[col_t].std().reindex(helis)

    fig, axes = plt.subplots(1, 2, figsize=(10, 4))
    fig.suptitle("Experiment 6 — Helicòpters per centre (HC)", fontweight="bold")

    axes[0].errorbar(helis, means_f, yerr=stds_f,
                     marker="o", color=PALETTE[0], capsize=4)
    axes[0].set_xlabel("Helicòpters per centre")
    axes[0].set_ylabel("h_final (mitjana ± std)")
    axes[0].set_title("Qualitat")
    axes[0].set_xticks(helis)

    axes[1].errorbar(helis, means_t, yerr=stds_t,
                     marker="s", color=PALETTE[1], capsize=4)
    axes[1].set_xlabel("Helicòpters per centre")
    axes[1].set_ylabel("Temps (ms)")
    axes[1].set_title("Temps d'execució")
    axes[1].set_xticks(helis)

    fig.tight_layout()
    savefig(fig, "exp6_helicopters.png")


# ── Experiment 7 ──────────────────────────────────────────────────────────────

def plot_exp7(df):
    """H1 + w*H2: h1_final i h2_final per w, HC vs SA."""
    print("  Exp 7: heurística H2 i ponderació")

    col_alg = "algoritme"
    col_w   = "w"
    col_h1  = "h1_final"
    col_h2  = "h2_final"
    col_t   = "temps_ms"

    ws   = sorted(df[col_w].unique())
    algs = df[col_alg].unique()

    fig, axes = plt.subplots(1, 3, figsize=(14, 4))
    fig.suptitle("Experiment 7 — Heurística combinada H1 + w·H2 (HC vs SA)", fontweight="bold")

    for metric, ax, ylabel, title in [
        (col_h1, axes[0], "h1_final (temps total)", "H1 final (qualitat temps)"),
        (col_h2, axes[1], "h2_final (prioritat)",   "H2 final (qualitat prioritat)"),
        (col_t,  axes[2], "Temps (ms)",              "Temps d'execució"),
    ]:
        for i, alg in enumerate(algs):
            sub = df[df[col_alg] == alg].groupby(col_w)[metric].mean().reindex(ws)
            axes[axes.tolist().index(ax)].plot(ws, sub.values, marker="o",
                                                label=alg, color=PALETTE[i])
        ax.set_xlabel("w (pes de H2)")
        ax.set_ylabel(ylabel)
        ax.set_title(title)
        ax.set_xticks(ws)
        ax.legend()

    fig.tight_layout()
    savefig(fig, "exp7_heuristica2.png")

    # Scatter H1 vs H2 per w i algorisme
    fig, axes = plt.subplots(1, len(algs), figsize=(6 * len(algs), 5), squeeze=False)
    for i, alg in enumerate(algs):
        sub = df[df[col_alg] == alg]
        sc = axes[0][i].scatter(sub[col_h1], sub[col_h2],
                                c=sub[col_w], cmap="plasma", alpha=0.6, s=20)
        fig.colorbar(sc, ax=axes[0][i], label="w")
        axes[0][i].set_xlabel("h1_final")
        axes[0][i].set_ylabel("h2_final")
        axes[0][i].set_title(f"{alg}: trade-off H1 vs H2")
    fig.suptitle("Experiment 7 — Trade-off H1/H2 per valor de w", fontweight="bold")
    fig.tight_layout()
    savefig(fig, "exp7_scatter_h1_h2.png")


# ── Dispatcher ────────────────────────────────────────────────────────────────

EXPERIMENTS = {
    1:  ("exp1_operadors.csv",                plot_exp1),
    12: ("exp1_2_operadors.csv",              plot_exp1_2),
    2:  ("exp2_inicial.csv",                  plot_exp2),
    3:  ("exp3_3_SA_params.csv",              plot_exp3),
    4:  ("exp4_escalabilitat_proporcional.csv", plot_exp4),
    5:  ("exp5_escalabilitat_separada.csv",   plot_exp5),
    6:  ("exp6_helicopters.csv",              plot_exp6),
    7:  ("exp7_heuristica2.csv",              plot_exp7),
}

def run(exp_id):
    if exp_id not in EXPERIMENTS:
        print(f"Experiment {exp_id} no reconegut. Opcions: {list(EXPERIMENTS)}")
        return
    filename, func = EXPERIMENTS[exp_id]
    df = read_csv(filename)
    if df is None:
        return
    func(df)


if __name__ == "__main__":
    requested = [int(x) for x in sys.argv[1:]] if len(sys.argv) > 1 else list(EXPERIMENTS)
    print(f"Generant gràfics per experiments: {requested}")
    print(f"CSVs: '{CSV_DIR}'  →  PNGs: '{OUTPUT_DIR}/'")
    for exp_id in requested:
        run(exp_id)
    print("Fet.")

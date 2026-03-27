"""
plot_experiments.py  —  versió corregida i millorada
=====================================================
Genera gràfics d'estil acadèmic per a tots els experiments.

FORMATS CSV detectats i corregits:
  exp1_operadors.csv        : decimals amb coma (h_inicial, h_final cadascun = 2 tokens)
  exp1_2_operadors.csv      : decimals amb coma (h_ini, h_fin cadascun = 2 tokens)
  exp2_inicial.csv          : decimals amb coma (h_inicial, h_final cadascun = 2 tokens)
  exp3_1_SA_params.csv      : format no estàndard "steps  h_ini -> h_final"
  exp3_2_SA_params.csv      : lambda amb coma decimal (= 2 tokens)
  exp3_3_SA_params.csv      : lambda amb coma decimal, stiter varia
  exp4_*.csv                : h_final amb coma decimal (= 2 tokens)
  exp5_*.csv                : h_final ja amb punt decimal (normal)
  exp6_*.csv                : h_final ja amb punt decimal (normal)
  exp7_*.csv                : totes les columnes numèriques amb punt decimal (normal)

Ús:
    python plot_experiments.py               # tots els experiments
    python plot_experiments.py 1 2 3 4 5 6 7

Dependències: pip install pandas matplotlib seaborn scipy
"""

import sys, os, re, warnings
warnings.filterwarnings("ignore")

import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.ticker as mticker
import matplotlib.patches as mpatches
import seaborn as sns
import numpy as np
from scipy import stats as sp_stats

# ── Configuració ──────────────────────────────────────────────────────────────
CSV_DIR    = "."
OUTPUT_DIR = "plots"
DPI        = 180

COLORS = {
    "red":    "#C0392B",
    "blue":   "#2471A3",
    "green":  "#1E8449",
    "purple": "#6C3483",
    "orange": "#CA6F1E",
    "teal":   "#148F77",
    "gray":   "#717D7E",
}
PALETTE = list(COLORS.values())

SHORT_OP = {
    "SwapGroups":                     "SG",
    "MoveGroup":                      "MG",
    "SwapOrder":                      "SO",
    "SwapGroups+MoveGroup":           "SG+MG",
    "SwapGroups+SwapOrder":           "SG+SO",
    "MoveGroup+SwapOrder":            "MG+SO",
    "SwapGroups+MoveGroup+SwapOrder": "SG+MG+SO",
}


# ── Estil global ──────────────────────────────────────────────────────────────
plt.rcParams.update({
    "font.family":        "DejaVu Sans",
    "font.size":          10,
    "axes.titlesize":     11,
    "axes.titleweight":   "bold",
    "axes.labelsize":     10,
    "xtick.labelsize":    9,
    "ytick.labelsize":    9,
    "legend.fontsize":    9,
    "figure.titlesize":   13,
    "figure.titleweight": "bold",
    "figure.facecolor":   "white",
    "axes.facecolor":     "white",
    "axes.edgecolor":     "#AAAAAA",
    "axes.linewidth":     0.8,
    "axes.grid":          True,
    "grid.color":         "#E8E8E8",
    "grid.linewidth":     0.6,
    "lines.linewidth":    1.8,
    "lines.markersize":   6,
})


# ── Parsers específics per cada CSV ───────────────────────────────────────────

def read_exp1(filename="exp1_operadors.csv"):
    path = os.path.join(CSV_DIR, filename)
    if not os.path.exists(path):
        print(f"  [!] No trobat: {path}"); return None
    rows = []
    with open(path) as f:
        next(f)
        for line in f:
            t = line.strip().split(',')
            if len(t) < 9: continue
            rows.append({
                "operadors": t[1],
                "seed":      int(t[2]),
                "h_inicial": float(f"{t[3]}.{t[4]}"),
                "h_final":   float(f"{t[5]}.{t[6]}"),
                "temps_ms":  float(t[7]),
                "nodes":     int(t[8]),
            })
    return pd.DataFrame(rows)


def read_exp1_2(filename="exp1_2_operadors.csv"):
    path = os.path.join(CSV_DIR, filename)
    if not os.path.exists(path):
        print(f"  [!] No trobat: {path}"); return None
    rows = []
    with open(path) as f:
        next(f)
        for line in f:
            t = line.strip().split(',')
            if len(t) < 11: continue
            rows.append({
                "operador":        t[1],
                "num_centres":     int(t[2]),
                "heli_per_centre": int(t[3]),
                "seed":            int(t[4]),
                "h_ini":           float(f"{t[5]}.{t[6]}"),
                "h_fin":           float(f"{t[7]}.{t[8]}"),
                "temps_ms":        float(t[9]),
                "nodes":           int(t[10]),
            })
    return pd.DataFrame(rows)


def read_exp2(filename="exp2_inicial.csv"):
    path = os.path.join(CSV_DIR, filename)
    if not os.path.exists(path):
        print(f"  [!] No trobat: {path}"); return None
    rows = []
    with open(path) as f:
        next(f)
        for line in f:
            t = line.strip().split(',')
            if len(t) < 9: continue
            rows.append({
                "estrategia": t[1],
                "seed":       int(t[2]),
                "h_inicial":  float(f"{t[3]}.{t[4]}"),
                "h_final":    float(f"{t[5]}.{t[6]}"),
                "temps_ms":   float(t[7]),
                "nodes":      int(t[8]),
            })
    return pd.DataFrame(rows)


def read_exp3_1(filename="exp3_1_SA_params.csv"):
    path = os.path.join(CSV_DIR, filename)
    if not os.path.exists(path):
        print(f"  [!] No trobat: {path}"); return None
    rows = []
    with open(path) as f:
        next(f)
        for line in f:
            m = re.match(r'\s*(\d+)\s+(\d+)\s*->\s*(\d+)', line)
            if m:
                rows.append({
                    "steps":     int(m.group(1)),
                    "h_inicial": int(m.group(2)),
                    "h_final":   int(m.group(3)),
                })
    return pd.DataFrame(rows)


def read_exp3_2(filename="exp3_2_SA_params.csv"):
    path = os.path.join(CSV_DIR, filename)
    if not os.path.exists(path):
        print(f"  [!] No trobat: {path}"); return None
    rows = []
    with open(path) as f:
        next(f)
        for line in f:
            t = line.strip().split(',')
            if len(t) < 10: continue
            rows.append({
                "k":         int(t[1]),
                "lam":       float(f"{t[2]}.{t[3]}"),
                "steps":     int(t[4]),
                "stiter":    int(t[5]),
                "seed":      int(t[6]),
                "h_inicial": float(t[7]),
                "h_final":   float(t[8]),
                "temps_ms":  float(t[9]),
            })
    return pd.DataFrame(rows)


def read_exp3_3(filename="exp3_3_SA_params.csv"):
    return read_exp3_2(filename)


def read_exp4(filename="exp4_escalabilitat_proporcional.csv"):
    path = os.path.join(CSV_DIR, filename)
    if not os.path.exists(path):
        print(f"  [!] No trobat: {path}"); return None
    rows = []
    with open(path) as f:
        next(f)
        for line in f:
            t = line.strip().split(',')
            if len(t) < 8: continue
            rows.append({
                "algoritme": t[1],
                "n_centres": int(t[2]),
                "n_grups":   int(t[3]),
                "seed":      int(t[4]),
                "h_final":   float(f"{t[5]}.{t[6]}"),
                "temps_ms":  float(t[7]),
            })
    return pd.DataFrame(rows)


def read_exp5(filename="exp5_escalabilitat_separada.csv"):
    path = os.path.join(CSV_DIR, filename)
    if not os.path.exists(path):
        print(f"  [!] No trobat: {path}"); return None
    return pd.read_csv(path)


def read_exp6(filename="exp6_helicopters.csv"):
    path = os.path.join(CSV_DIR, filename)
    if not os.path.exists(path):
        print(f"  [!] No trobat: {path}"); return None
    return pd.read_csv(path)


def read_exp7(filename="exp7_heuristica2.csv"):
    path = os.path.join(CSV_DIR, filename)
    if not os.path.exists(path):
        print(f"  [!] No trobat: {path}"); return None
    return pd.read_csv(path)


# ── Helpers de visualització ─────────────────────────────────────────────────

def savefig(fig, name):
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    path = os.path.join(OUTPUT_DIR, name)
    fig.savefig(path, dpi=DPI, bbox_inches="tight", facecolor="white")
    plt.close(fig)
    print(f"  -> {path}")


def style_ax(ax, xlabel=None, ylabel=None, title=None, ymin=None, ymax=None):
    if title:  ax.set_title(title, pad=6)
    if xlabel: ax.set_xlabel(xlabel, labelpad=4)
    if ylabel: ax.set_ylabel(ylabel, labelpad=4)
    if ymin is not None or ymax is not None:
        ax.set_ylim(bottom=ymin, top=ymax)
    ax.tick_params(axis="both", length=3, width=0.7)
    ax.spines["top"].set_visible(False)
    ax.spines["right"].set_visible(False)
    ax.spines["left"].set_color("#AAAAAA")
    ax.spines["bottom"].set_color("#AAAAAA")


def boxplot_group(ax, data_dict, colors, title="", ylabel="", xlabel="",
                  ymin=None, rotate_labels=0):
    labels = list(data_dict.keys())
    data   = [np.array(v, dtype=float) for v in data_dict.values()]
    bp = ax.boxplot(
        data, patch_artist=True, notch=False, widths=0.5,
        medianprops=dict(color="white", linewidth=2.5),
        whiskerprops=dict(linewidth=1.0, linestyle="--", color="#555"),
        capprops=dict(linewidth=1.2, color="#555"),
        flierprops=dict(marker="o", markersize=3.5, markerfacecolor="#999",
                        markeredgewidth=0.4, markeredgecolor="#777", alpha=0.7),
    )
    for patch, col in zip(bp["boxes"], colors):
        patch.set_facecolor(col); patch.set_alpha(0.88)
        patch.set_linewidth(1.2); patch.set_edgecolor("#333")
    ax.set_xticks(range(1, len(labels) + 1))
    ax.set_xticklabels(labels, rotation=rotate_labels,
                       ha="right" if rotate_labels > 0 else "center", fontsize=9)
    style_ax(ax, xlabel=xlabel, ylabel=ylabel, title=title, ymin=ymin)


def errorline(ax, xs, means, stds, color, label, marker="o"):
    ax.plot(xs, means, marker=marker, color=color, label=label,
            linewidth=1.8, markersize=6, zorder=3)
    ax.fill_between(xs, np.array(means) - np.array(stds),
                    np.array(means) + np.array(stds),
                    color=color, alpha=0.13, zorder=2)


def add_significance(ax, x1, x2, vals1, vals2):
    _, p = sp_stats.ttest_ind(vals1, vals2)
    y_top = max(np.percentile(vals1, 95), np.percentile(vals2, 95))
    yrange = ax.get_ylim()[1] - ax.get_ylim()[0]
    h = 0.04 * yrange
    ax.plot([x1, x1, x2, x2], [y_top, y_top + h, y_top + h, y_top], lw=1.0, color="#444")
    stars = "***" if p < 0.001 else ("**" if p < 0.01 else ("*" if p < 0.05 else "ns"))
    ax.text((x1 + x2) / 2, y_top + h * 1.1, stars,
            ha="center", va="bottom", fontsize=9, color="#333")


def hex_from_rgb(col):
    return f"#{int(col[0]*255):02x}{int(col[1]*255):02x}{int(col[2]*255):02x}"


# ── Experiment 1 ──────────────────────────────────────────────────────────────

def plot_exp1(df):
    print("  Exp 1: operadors")
    order   = ["SwapGroups","MoveGroup","SwapOrder","SwapGroups+MoveGroup",
                "SwapGroups+SwapOrder","MoveGroup+SwapOrder","SwapGroups+MoveGroup+SwapOrder"]
    present = [o for o in order if o in df["operadors"].unique()]
    labels  = [SHORT_OP.get(o, o) for o in present]
    colors  = PALETTE[:len(present)]
    df = df.copy()
    df["red_pct"] = (df["h_inicial"] - df["h_final"]) / df["h_inicial"] * 100

    fig, axes = plt.subplots(1, 3, figsize=(15, 5))
    fig.suptitle("Experiment 1 — Comparació d'operadors (Hill Climbing)")
    fig.subplots_adjust(top=0.88, wspace=0.38)

    for ax, col, ylabel, title, ymin_frac in [
        (axes[0], "h_final",  "h_final",       "Qualitat de solució final",      0.93),
        (axes[1], "red_pct",  "Reduccio (%)",   "Reduccio respecte h inicial",    None),
        (axes[2], "temps_ms", "Temps (ms)",     "Temps d'execucio",               None),
    ]:
        data = {labels[i]: df[df["operadors"] == op][col].dropna().values
                for i, op in enumerate(present)}
        ymin = df[col].min() * ymin_frac if ymin_frac else None
        boxplot_group(ax, data, colors, title=title, ylabel=ylabel,
                      rotate_labels=30, ymin=ymin)
    axes[1].yaxis.set_major_formatter(mticker.PercentFormatter(decimals=0))
    savefig(fig, "exp1_operadors.png")


# ── Experiment 1.2 ────────────────────────────────────────────────────────────

def plot_exp1_2(df):
    print("  Exp 1.2: operadors per escenari")
    ops_order = ["SwapGroups","SwapGroups+SwapOrder","SwapGroups+MoveGroup+SwapOrder"]
    ops = [o for o in ops_order if o in df["operador"].unique()]
    op_labels = [SHORT_OP.get(o, o) for o in ops]
    op_colors = [COLORS["blue"], COLORS["orange"], COLORS["green"]][:len(ops)]

    df = df.copy()
    df["escenari"] = df["num_centres"].astype(str) + "c / " + df["heli_per_centre"].astype(str) + "h"
    escenaris = sorted(df["escenari"].unique())
    x = np.arange(len(escenaris))
    width = 0.25

    fig, axes = plt.subplots(1, 2, figsize=(14, 5))
    fig.suptitle("Experiment 1.2 — Operadors per escenari (HC)")
    fig.subplots_adjust(top=0.88, wspace=0.35)

    for ax, col, ylabel, title in [
        (axes[0], "h_fin",    "h_final (mitjana +/- std)", "Qualitat de solucio"),
        (axes[1], "temps_ms", "Temps (ms)",                "Temps d'execucio"),
    ]:
        for i, op in enumerate(ops):
            sub   = df[df["operador"] == op].groupby("escenari")[col]
            means = sub.mean().reindex(escenaris).values
            stds  = sub.std().reindex(escenaris).values
            ax.bar(x + i * width, means, width=width, label=op_labels[i],
                   color=op_colors[i], alpha=0.88, edgecolor="#333", linewidth=0.7)
            ax.errorbar(x + i * width, means, yerr=stds,
                        fmt="none", ecolor="#444", capsize=3, elinewidth=0.9)
        ax.set_xticks(x + width * (len(ops) - 1) / 2)
        ax.set_xticklabels(escenaris, rotation=15, ha="right", fontsize=9)
        style_ax(ax, ylabel=ylabel, title=title)
        ax.legend(frameon=False, fontsize=8)
    savefig(fig, "exp1_2_operadors.png")


# ── Experiment 2 ──────────────────────────────────────────────────────────────

def plot_exp2(df):
    print("  Exp 2: inicialitzacio")
    order = ["Random","Greedy","GreedyPro"]
    ests  = [e for e in order if e in df["estrategia"].unique()]
    cols  = [COLORS["red"], COLORS["blue"], COLORS["green"]][:len(ests)]

    fig, axes = plt.subplots(1, 3, figsize=(13, 5))
    fig.suptitle("Experiment 2 — Estrategia d'inicialitzacio (Hill Climbing)")
    fig.subplots_adjust(top=0.88, wspace=0.35, bottom=0.16)

    for ax, col, ylabel, title, ymin_frac in [
        (axes[0], "h_inicial", "h_inicial",  "Qualitat estat inicial", 0.90),
        (axes[1], "h_final",   "h_final",    "Qualitat solucio final", 0.93),
        (axes[2], "temps_ms",  "Temps (ms)", "Temps d'execucio (HC)",  None),
    ]:
        data = {e: df[df["estrategia"] == e][col].dropna().values for e in ests}
        ymin = df[col].min() * ymin_frac if ymin_frac else None
        boxplot_group(ax, data, cols, title=title, ylabel=ylabel, ymin=ymin)

    if "Random" in ests and "GreedyPro" in ests:
        r  = df[df["estrategia"] == "Random"]["h_final"].dropna().values
        gp = df[df["estrategia"] == "GreedyPro"]["h_final"].dropna().values
        add_significance(axes[1], ests.index("Random")+1, ests.index("GreedyPro")+1, r, gp)

    patches = [mpatches.Patch(facecolor=cols[i], edgecolor="#333", alpha=0.88, label=e)
               for i, e in enumerate(ests)]
    fig.legend(handles=patches, loc="lower center", ncol=len(ests),
               frameon=False, fontsize=9, bbox_to_anchor=(0.5, 0.0))
    savefig(fig, "exp2_inicialitzacio.png")


# ── Experiment 3 ──────────────────────────────────────────────────────────────

def plot_exp3(df1, df2, df3):
    print("  Exp 3: parametres SA")

    # 3.1 steps
    if df1 is not None and not df1.empty:
        steps_vals = sorted(df1["steps"].unique())
        means = df1.groupby("steps")["h_final"].mean().reindex(steps_vals).values
        stds  = df1.groupby("steps")["h_final"].std().reindex(steps_vals).values
        fig, ax = plt.subplots(figsize=(9, 4.5))
        fig.suptitle("Experiment 3.1 — SA: efecte del nombre de passos (steps)")
        errorline(ax, steps_vals, means, stds, COLORS["blue"], "SA h_final")
        style_ax(ax, xlabel="steps", ylabel="h_final (mitjana +/- std)",
                 title="H final vs steps  [k=10, lambda=0.001, stiter=100]")
        ax.set_xticks(steps_vals)
        ax.set_xticklabels([f"{int(s/1000)}k" for s in steps_vals], fontsize=9)
        savefig(fig, "exp3_1_steps.png")

    # 3.2 heatmap k x lambda
    if df2 is not None and not df2.empty:
        pivot = df2.groupby(["k","lam"])["h_final"].mean().unstack()
        pivot.index   = [str(int(k)) for k in pivot.index]
        pivot.columns = [f"{v:.4f}" for v in pivot.columns]

        fig, ax = plt.subplots(figsize=(9, 5.5))
        fig.suptitle("Experiment 3.2 — SA: heatmap k x lambda (h_final mitjana)")
        sns.heatmap(pivot, ax=ax, annot=True, fmt=".0f", cmap="YlOrRd_r",
                    linewidths=0.5, linecolor="#DDD",
                    cbar_kws={"label": "h_final (mitjana)", "shrink": 0.85},
                    annot_kws={"fontsize": 9})
        ax.set_xlabel("lambda", labelpad=6)
        ax.set_ylabel("k", labelpad=6)
        ax.tick_params(axis="both", length=0)
        ax.set_title("H final per combinacio de k i lambda  [steps=150k, stiter=100]", pad=6)
        fig.tight_layout(rect=[0, 0, 1, 0.93])
        savefig(fig, "exp3_2_heatmap_k_lambda.png")

        # Linies k i lambda
        lam_vals = sorted(df2["lam"].unique())
        k_vals   = sorted(df2["k"].unique())
        fig, axes = plt.subplots(1, 2, figsize=(13, 4.5))
        fig.suptitle("Experiment 3.2 — SA: efecte de k i lambda")
        fig.subplots_adjust(top=0.88, wspace=0.35)

        for lam, col in zip(lam_vals, sns.color_palette("viridis", len(lam_vals))):
            sub = df2[df2["lam"] == lam].groupby("k")["h_final"]
            errorline(axes[0], k_vals, sub.mean().reindex(k_vals).values,
                      sub.std().reindex(k_vals).values, hex_from_rgb(col), f"lam={lam:.4f}")
        style_ax(axes[0], xlabel="k", ylabel="h_final", title="H final vs k (per lambda)")
        axes[0].set_xticks(k_vals); axes[0].legend(frameon=False, fontsize=7)

        for k, col in zip(k_vals, sns.color_palette("plasma", len(k_vals))):
            sub = df2[df2["k"] == k].groupby("lam")["h_final"]
            errorline(axes[1], range(len(lam_vals)),
                      sub.mean().reindex(lam_vals).values,
                      sub.std().reindex(lam_vals).values, hex_from_rgb(col), f"k={k}")
        axes[1].set_xticks(range(len(lam_vals)))
        axes[1].set_xticklabels([f"{v:.4f}" for v in lam_vals], rotation=20, ha="right", fontsize=8)
        style_ax(axes[1], xlabel="lambda", ylabel="h_final", title="H final vs lambda (per k)")
        axes[1].legend(frameon=False, fontsize=7)
        savefig(fig, "exp3_2_lines_k_lambda.png")

    # 3.3 stiter boxplot
    if df3 is not None and not df3.empty:
        stiter_vals = sorted(df3["stiter"].unique())
        labels_st   = [str(int(s)) for s in stiter_vals]
        cols_st = [hex_from_rgb(c) for c in sns.color_palette("Blues_d", len(stiter_vals))]
        data_st = {labels_st[i]: df3[df3["stiter"] == s]["h_final"].dropna().values
                   for i, s in enumerate(stiter_vals)}
        fig, ax = plt.subplots(figsize=(10, 4.5))
        fig.suptitle("Experiment 3.3 — SA: efecte de stiter")
        boxplot_group(ax, data_st, cols_st,
                      title="H final per stiter  [k=10, lambda=0.001, steps=150k]",
                      ylabel="h_final", xlabel="stiter (iteracions per temperatura)",
                      ymin=df3["h_final"].min() * 0.93)
        savefig(fig, "exp3_3_stiter.png")


# ── Experiment 4 ──────────────────────────────────────────────────────────────

def plot_exp4(df):
    print("  Exp 4: escalabilitat proporcional")
    algs    = df["algoritme"].unique()
    centres = sorted(df["n_centres"].unique())
    alg_colors = {a: PALETTE[i] for i, a in enumerate(algs)}

    fig, axes = plt.subplots(1, 2, figsize=(13, 5))
    fig.suptitle("Experiment 4 — Escalabilitat proporcional (HC vs SA)")
    fig.subplots_adjust(top=0.88, wspace=0.35)

    for ax, col, ylabel, title in [
        (axes[0], "h_final",  "h_final (mitjana +/- std)", "Qualitat de solucio"),
        (axes[1], "temps_ms", "Temps (ms)",                "Temps d'execucio"),
    ]:
        for alg in algs:
            sub = df[df["algoritme"] == alg].groupby("n_centres")[col]
            errorline(ax, centres, sub.mean().reindex(centres).values,
                      sub.std().reindex(centres).values, alg_colors[alg], alg)
        style_ax(ax, xlabel="Nombre de centres (grups proporcionals)", ylabel=ylabel, title=title)
        ax.set_xticks(centres); ax.legend(frameon=False)
    savefig(fig, "exp4_escalabilitat_proporcional.png")


# ── Experiment 5 ──────────────────────────────────────────────────────────────

def plot_exp5(df):
    print("  Exp 5: escalabilitat separada")
    fig, axes = plt.subplots(2, 2, figsize=(13, 9))
    fig.suptitle("Experiment 5 — Escalabilitat separada (Hill Climbing)")
    fig.subplots_adjust(top=0.93, hspace=0.45, wspace=0.35)

    configs = [
        ("grups",   "n_grups",   "Nombre de grups",   COLORS["blue"],   COLORS["teal"]),
        ("centres", "n_centres", "Nombre de centres",  COLORS["purple"], COLORS["orange"]),
    ]
    for row, (esc_id, x_col, xlabel, c_qual, c_temps) in enumerate(configs):
        sub = df[df["escenari"] == esc_id].copy()
        if sub.empty: continue
        xs = sorted(sub[x_col].unique())
        for col_idx, (col, ylabel, title, color, marker) in enumerate([
            ("h_final",  "h_final (mitjana +/- std)", f"5{'a' if row==0 else 'b'}: qualitat",        c_qual,  "o"),
            ("temps_ms", "Temps (ms)",                 f"5{'a' if row==0 else 'b'}: temps d'execucio", c_temps, "s"),
        ]):
            means = sub.groupby(x_col)[col].mean().reindex(xs).values
            stds  = sub.groupby(x_col)[col].std().reindex(xs).values
            errorline(axes[row][col_idx], xs, means, stds, color, col, marker=marker)
            style_ax(axes[row][col_idx], xlabel=xlabel, ylabel=ylabel, title=title)
            axes[row][col_idx].set_xticks(xs)
    savefig(fig, "exp5_escalabilitat_separada.png")


# ── Experiment 6 ──────────────────────────────────────────────────────────────

def plot_exp6(df):
    print("  Exp 6: helicopters per centre")
    helis  = sorted(df["n_helis"].unique())
    labels = [str(h) for h in helis]
    cols = [hex_from_rgb(c) for c in sns.color_palette("Greens_d", len(helis))]

    fig, axes = plt.subplots(1, 2, figsize=(11, 5))
    fig.suptitle("Experiment 6 — Nombre d'helicopters per centre (HC)")
    fig.subplots_adjust(top=0.88, wspace=0.35)

    for ax, col, ylabel, title, ymin_frac in [
        (axes[0], "h_final",  "h_final",    "Qualitat de solucio",  0.93),
        (axes[1], "temps_ms", "Temps (ms)", "Temps d'execucio",     None),
    ]:
        data = {labels[i]: df[df["n_helis"] == h][col].dropna().values
                for i, h in enumerate(helis)}
        ymin = df[col].min() * ymin_frac if ymin_frac else None
        boxplot_group(ax, data, cols, title=title, ylabel=ylabel,
                      xlabel="Helicopters per centre", ymin=ymin)
    savefig(fig, "exp6_helicopters.png")


# ── Experiment 7 ──────────────────────────────────────────────────────────────

def plot_exp7(df):
    print("  Exp 7: heuristica H2 i ponderacio")
    ws   = sorted(df["w"].unique())
    algs = df["algoritme"].unique()
    alg_colors = {a: PALETTE[i] for i, a in enumerate(algs)}

    fig, axes = plt.subplots(1, 3, figsize=(15, 5))
    fig.suptitle("Experiment 7 — Heuristica combinada H1 + w*H2 (HC vs SA)")
    fig.subplots_adjust(top=0.88, wspace=0.38)

    for ax, col, ylabel, title in [
        (axes[0], "h1_final",  "h1_final (temps total)", "H1 final — qualitat de temps"),
        (axes[1], "h2_final",  "h2_final (prioritat)",   "H2 final — qualitat de prioritat"),
        (axes[2], "temps_ms",  "Temps (ms)",              "Temps d'execucio"),
    ]:
        for alg in algs:
            sub = df[df["algoritme"] == alg].groupby("w")[col]
            errorline(ax, ws, sub.mean().reindex(ws).values,
                      sub.std().reindex(ws).values, alg_colors[alg], alg)
        style_ax(ax, xlabel="w (pes de H2)", ylabel=ylabel, title=title)
        ax.set_xticks(ws); ax.legend(frameon=False)
    savefig(fig, "exp7_heuristica2.png")

    # Scatter H1 vs H2
    n_algs = len(algs)
    fig, axes = plt.subplots(1, n_algs, figsize=(7*n_algs, 5), squeeze=False)
    fig.suptitle("Experiment 7 — Trade-off H1 / H2 per valor de w")
    fig.subplots_adjust(top=0.88)
    for i, alg in enumerate(algs):
        sub = df[df["algoritme"] == alg]
        sc = axes[0][i].scatter(sub["h1_final"], sub["h2_final"],
                                c=sub["w"], cmap="plasma",
                                vmin=min(ws), vmax=max(ws),
                                alpha=0.65, s=22, linewidths=0.3, edgecolors="#333")
        cb = fig.colorbar(sc, ax=axes[0][i], shrink=0.85, pad=0.02)
        cb.set_label("w", fontsize=9); cb.ax.tick_params(labelsize=8)
        style_ax(axes[0][i], xlabel="h1_final", ylabel="h2_final",
                 title=f"{alg}: trade-off H1 vs H2")
    fig.tight_layout(rect=[0, 0, 1, 0.93])
    savefig(fig, "exp7_scatter_h1_h2.png")


# ── Dispatcher ────────────────────────────────────────────────────────────────

def run(exp_id):
    if exp_id == 1:
        df = read_exp1("exp1_operadors.csv")
        if df is not None: plot_exp1(df)
    elif exp_id == 12:
        df = read_exp1_2("exp1_2_operadors.csv")
        if df is not None: plot_exp1_2(df)
    elif exp_id == 2:
        df = read_exp2("exp2_inicial.csv")
        if df is not None: plot_exp2(df)
    elif exp_id == 3:
        df1 = read_exp3_1("exp3_1_SA_params.csv")
        df2 = read_exp3_2("exp3_2_SA_params.csv")
        df3 = read_exp3_3("exp3_3_SA_params.csv")
        plot_exp3(df1, df2, df3)
    elif exp_id == 4:
        df = read_exp4("exp4_escalabilitat_proporcional.csv")
        if df is not None: plot_exp4(df)
    elif exp_id == 5:
        df = read_exp5("exp5_escalabilitat_separada.csv")
        if df is not None: plot_exp5(df)
    elif exp_id == 6:
        df = read_exp6("exp6_helicopters.csv")
        if df is not None: plot_exp6(df)
    elif exp_id == 7:
        df = read_exp7("exp7_heuristica2.csv")
        if df is not None: plot_exp7(df)
    else:
        print(f"Experiment {exp_id} no reconegut.")


ALL_IDS = [1, 12, 2, 3, 4, 5, 6, 7]

if __name__ == "__main__":
    requested = [int(x) for x in sys.argv[1:]] if len(sys.argv) > 1 else ALL_IDS
    print(f"Generant grafics per experiments: {requested}")
    print(f"CSVs: '{CSV_DIR}'  ->  PNGs: '{OUTPUT_DIR}/'")
    for exp_id in requested:
        run(exp_id)
    print("Fet.")

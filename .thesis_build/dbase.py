#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
论文配图公共库: 统一字体/配色/绘图原语(圆角框、箭头、泳道等)
风格: 简约、白底、细边框、柔和配色, 符合毕业论文插图要求。
"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch, Rectangle, Circle, Ellipse, PathPatch
from matplotlib.path import Path
import matplotlib.font_manager as fm

# ---------------- 字体 ----------------
plt.rcParams['font.sans-serif'] = ['Songti SC', 'STHeiti', 'Arial Unicode MS']
plt.rcParams['font.family'] = 'sans-serif'
plt.rcParams['axes.unicode_minus'] = False

SONG = fm.FontProperties(family='Songti SC')
HEI = fm.FontProperties(family='STHeiti')

OUT = "/Users/weibaiwang/Desktop/AAA/work/all/.thesis_build/img"
import os
os.makedirs(OUT, exist_ok=True)

# ---------------- 配色(柔和、专业) ----------------
C = {
    'blue':   '#4C7FB8', 'blue_bg':  '#EAF1F8',
    'green':  '#5B9A6B', 'green_bg': '#EAF3ED',
    'orange': '#D2793A', 'orange_bg':'#FBF0E7',
    'purple': '#7E6BA8', 'purple_bg':'#F0ECF6',
    'red':    '#C0504D', 'red_bg':   '#F7EAEA',
    'teal':   '#3E8E8E', 'teal_bg':  '#E6F2F2',
    'gray':   '#7F7F7F', 'gray_bg':  '#F2F2F2',
    'ink':    '#333333', 'line':     '#8A8A8A',
}

def new_ax(w=10, h=6.5):
    fig, ax = plt.subplots(figsize=(w, h))
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')
    fig.patch.set_facecolor('white')
    return fig, ax

def box(ax, x, y, w, h, text, fc='#EAF1F8', ec='#4C7FB8', fs=11,
        rounded=True, bold=False, tc='#333333', lw=1.3, align='center'):
    """圆角矩形节点, (x,y)为中心"""
    style = "round,pad=0.02,rounding_size=1.6" if rounded else "square,pad=0.02"
    p = FancyBboxPatch((x - w/2, y - h/2), w, h, boxstyle=style,
                       fc=fc, ec=ec, lw=lw, zorder=2)
    ax.add_patch(p)
    ax.text(x, y, text, ha=align, va='center', fontsize=fs, color=tc,
            fontproperties=SONG, zorder=3,
            fontweight='bold' if bold else 'normal', linespacing=1.4)
    return (x, y, w, h)

def cyl(ax, x, y, w, h, text, fc='#EAF3ED', ec='#5B9A6B', fs=11, tc='#333333'):
    """圆柱体(数据库)"""
    ell_h = h * 0.16
    ax.add_patch(Rectangle((x - w/2, y - h/2 + ell_h/2), w, h - ell_h,
                           fc=fc, ec='none', zorder=2))
    ax.add_patch(Ellipse((x, y - h/2 + ell_h/2), w, ell_h, fc=fc, ec=ec, lw=1.3, zorder=2))
    ax.add_patch(Ellipse((x, y + h/2 - ell_h/2), w, ell_h, fc=fc, ec=ec, lw=1.3, zorder=3))
    ax.plot([x - w/2, x - w/2], [y - h/2 + ell_h/2, y + h/2 - ell_h/2], color=ec, lw=1.3, zorder=2)
    ax.plot([x + w/2, x + w/2], [y - h/2 + ell_h/2, y + h/2 - ell_h/2], color=ec, lw=1.3, zorder=2)
    ax.text(x, y, text, ha='center', va='center', fontsize=fs, color=tc,
            fontproperties=SONG, zorder=4)

def diamond(ax, x, y, w, h, text, fc='#FBF0E7', ec='#D2793A', fs=10, tc='#333333'):
    """菱形(判断)"""
    pts = [(x, y + h/2), (x + w/2, y), (x, y - h/2), (x - w/2, y)]
    ax.add_patch(mpatches.Polygon(pts, closed=True, fc=fc, ec=ec, lw=1.3, zorder=2))
    ax.text(x, y, text, ha='center', va='center', fontsize=fs, color=tc,
            fontproperties=SONG, zorder=3, linespacing=1.3)
    return (x, y, w, h)

def ellipse(ax, x, y, w, h, text, fc='#F0ECF6', ec='#7E6BA8', fs=10, tc='#333333'):
    ax.add_patch(Ellipse((x, y), w, h, fc=fc, ec=ec, lw=1.2, zorder=2))
    ax.text(x, y, text, ha='center', va='center', fontsize=fs, color=tc,
            fontproperties=SONG, zorder=3, linespacing=1.3)
    return (x, y, w, h)

def arrow(ax, p1, p2, text='', color='#8A8A8A', fs=9, style='-|>', lw=1.3,
          rad=0.0, tc='#555555', ls='-'):
    a = FancyArrowPatch(p1, p2, arrowstyle=style, mutation_scale=13,
                        color=color, lw=lw, zorder=1,
                        connectionstyle=f"arc3,rad={rad}", linestyle=ls)
    ax.add_patch(a)
    if text:
        mx, my = (p1[0] + p2[0]) / 2, (p1[1] + p2[1]) / 2
        ax.text(mx, my + 1.5, text, ha='center', va='center', fontsize=fs,
                color=tc, fontproperties=SONG,
                bbox=dict(boxstyle='round,pad=0.15', fc='white', ec='none', alpha=0.9))

def line(ax, p1, p2, color='#8A8A8A', lw=1.2, ls='-'):
    ax.plot([p1[0], p2[0]], [p1[1], p2[1]], color=color, lw=lw, ls=ls, zorder=1)

def title(ax, text, y=97, fs=13):
    ax.text(50, y, text, ha='center', va='center', fontsize=fs,
            fontproperties=HEI, color='#222222', fontweight='bold')

def actor(ax, x, y, name, fs=10, color='#333333'):
    """火柴人(用例图角色)"""
    r = 2.2
    ax.add_patch(Circle((x, y + 6), r, fill=False, ec=color, lw=1.5, zorder=3))
    ax.plot([x, x], [y + 6 - r, y - 1], color=color, lw=1.5, zorder=3)
    ax.plot([x - 3.2, x + 3.2], [y + 2.5, y + 2.5], color=color, lw=1.5, zorder=3)
    ax.plot([x, x - 3], [y - 1, y - 5.5], color=color, lw=1.5, zorder=3)
    ax.plot([x, x + 3], [y - 1, y - 5.5], color=color, lw=1.5, zorder=3)
    ax.text(x, y - 8.5, name, ha='center', va='center', fontsize=fs,
            fontproperties=SONG, color=color)

def save(fig, name):
    path = f"{OUT}/{name}.png"
    fig.savefig(path, dpi=200, bbox_inches='tight', facecolor='white', pad_inches=0.12)
    plt.close(fig)
    print("saved", path)

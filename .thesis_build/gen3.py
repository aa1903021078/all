#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""时序图: 登录鉴权、发布探店笔记、实时聊天"""
import sys
sys.path.insert(0, "/Users/weibaiwang/Desktop/AAA/work/all/.thesis_build")
from dbase import *

def seq_diagram(fname, actors, messages, h=6.6, note=None):
    """
    actors: [(name, color)] 生命线
    messages: [(from_idx, to_idx, text, style)] style: 'solid'/'dashed'
    """
    fig, ax = new_ax(10.0, h)
    n = len(actors)
    xs = [8 + (84 * (i + 0.5) / n) for i in range(n)]
    top = 92; bot = 8
    for i, (name, color) in enumerate(actors):
        box(ax, xs[i], top, min(82/n, 20), 6.2, name, fc=C.get(color+'_bg', '#EAF1F8'),
            ec=C.get(color, '#4C7FB8'), fs=9.2, bold=True)
        # 生命线
        ax.plot([xs[i], xs[i]], [top - 3.4, bot], color='#BBBBBB', lw=1.0, ls=(0, (4, 3)), zorder=1)
    ny = len(messages)
    y0 = top - 10
    dy = (y0 - bot - 4) / max(ny, 1)
    for k, (a, b, text, style) in enumerate(messages):
        y = y0 - k * dy
        ls = '-' if style == 'solid' else (0, (5, 3))
        if a == b:
            # 自调用
            ax.annotate('', xy=(xs[a] + 0.3, y - 2), xytext=(xs[a] + 0.3, y),
                        arrowprops=dict(arrowstyle='-|>', color=C['ink'], lw=1.2))
            ax.plot([xs[a], xs[a] + 6, xs[a] + 6, xs[a]], [y, y, y - 2, y - 2],
                    color=C['ink'], lw=1.1)
            ax.text(xs[a] + 7, y - 1, text, ha='left', va='center', fontsize=8.4,
                    fontproperties=SONG, color='#333')
        else:
            col = C['blue'] if a < b else C['green']
            a1 = FancyArrowPatch((xs[a], y), (xs[b], y), arrowstyle='-|>',
                                 mutation_scale=12, color=col, lw=1.3,
                                 linestyle=ls, zorder=3)
            ax.add_patch(a1)
            mx = (xs[a] + xs[b]) / 2
            ax.text(mx, y + 1.3, text, ha='center', va='center', fontsize=8.4,
                    fontproperties=SONG, color='#333',
                    bbox=dict(boxstyle='round,pad=0.15', fc='white', ec='none', alpha=0.92))
    save(fig, fname)

def fig_seq_login():
    seq_diagram("fig5_1_seq_login",
        [("用户浏览器", 'blue'), ("AuthController", 'teal'),
         ("AuthService", 'orange'), ("SysUserMapper", 'purple'), ("MySQL", 'green')],
        [(0, 1, "POST /auth/login (账号密码)", 'solid'),
         (1, 2, "login(dto)", 'solid'),
         (2, 3, "按用户名查询用户", 'solid'),
         (3, 4, "select sys_user", 'solid'),
         (4, 3, "用户记录", 'dashed'),
         (3, 2, "SysUser", 'dashed'),
         (2, 2, "BCrypt 校验密码/状态", 'solid'),
         (2, 2, "加载角色/权限, 生成JWT", 'solid'),
         (2, 1, "token + 用户信息", 'dashed'),
         (1, 0, "R.ok(token,user)", 'dashed'),
         (0, 0, "本地存储token, 跳转首页", 'solid')],
        h=6.4)

def fig_seq_note():
    seq_diagram("fig5_2_seq_note",
        [("用户", 'blue'), ("Publish页面", 'teal'), ("文件/笔记接口", 'orange'),
         ("NoteService", 'purple'), ("MySQL", 'green')],
        [(0, 1, "填写标题/评分/正文/图片", 'solid'),
         (1, 2, "POST /files/image 上传图片", 'solid'),
         (2, 1, "返回图片URL", 'dashed'),
         (1, 2, "POST /notes 提交笔记", 'solid'),
         (2, 3, "saveNote(note)", 'solid'),
         (3, 3, "写入 UserContext 作者ID", 'solid'),
         (3, 4, "insert note", 'solid'),
         (3, 3, "回算店铺综合评分", 'solid'),
         (3, 4, "update shop rating", 'solid'),
         (3, 2, "笔记详情", 'dashed'),
         (2, 1, "R.ok 发布成功", 'dashed'),
         (1, 0, "跳转个人主页", 'dashed')],
        h=6.8)

def fig_seq_chat():
    seq_diagram("fig5_3_seq_chat",
        [("用户A", 'blue'), ("握手拦截器", 'teal'), ("WebSocket处理器", 'orange'),
         ("ChatService", 'purple'), ("用户B", 'green')],
        [(0, 1, "连接 /ws/chat?token=JWT", 'solid'),
         (1, 1, "解析JWT, 写入userId", 'solid'),
         (1, 2, "握手通过, 加入在线Map", 'dashed'),
         (0, 2, "发送JSON消息", 'solid'),
         (2, 3, "saveMessage 落库", 'solid'),
         (3, 2, "ChatMessage", 'dashed'),
         (2, 0, "回显发送者", 'dashed'),
         (2, 4, "推送在线接收者", 'solid')],
        h=5.8)

if __name__ == '__main__':
    fig_seq_login()
    fig_seq_note()
    fig_seq_chat()
    print("gen3 done")

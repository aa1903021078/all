#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""第6章 系统实现 - 界面效果图(简约 Web UI mockup)"""
import sys
sys.path.insert(0, "/Users/weibaiwang/Desktop/AAA/work/all/.thesis_build")
from dbase import *
import numpy as np

W, H = 10.0, 6.4
ARX = H / W          # 圆形/星形 x 半径修正系数

BRAND  = '#F0663F'   # 觅食记 主题橙红
BRANDL = '#FCEDE7'   # 浅橙底
INK    = '#2E2E2E'
SUB    = '#9098A0'
LINE_  = '#E6E8EB'
BG     = '#F4F5F7'
GREEN  = '#54B37A'
GOLD   = '#F5A623'
BLUE   = '#4C8DD6'

# ---------------- UI 原语 ----------------
def uax():
    fig, ax = plt.subplots(figsize=(W, H))
    ax.set_xlim(0, 100); ax.set_ylim(0, 100); ax.axis('off')
    fig.patch.set_facecolor('white')
    return fig, ax

def rrect(ax, x, y, w, h, fc='white', ec=LINE_, lw=1.0, r=1.0, z=2):
    ax.add_patch(FancyBboxPatch((x, y), w, h,
        boxstyle=f"round,pad=0,rounding_size={r}", fc=fc, ec=ec, lw=lw, zorder=z))

def flat(ax, x, y, w, h, fc='white', ec='none', lw=0, z=2):
    ax.add_patch(Rectangle((x, y), w, h, fc=fc, ec=ec, lw=lw, zorder=z))

def circ(ax, x, y, r, fc, ec='none', lw=0, z=5):
    ax.add_patch(Ellipse((x, y), 2*r*ARX, 2*r, fc=fc, ec=ec, lw=lw, zorder=z))

def txt(ax, x, y, s, fs=9, color=INK, ha='left', va='center', bold=False, z=6, font=None):
    fp = HEI if bold else (font or SONG)
    ax.text(x, y, s, fontsize=fs, color=color, ha=ha, va=va, fontproperties=fp, zorder=z)

def pill(ax, x, y, w, h, s, fc=BRANDL, tc=BRAND, fs=7.5, z=4):
    rrect(ax, x, y, w, h, fc=fc, ec='none', r=h/2, z=z)
    txt(ax, x+w/2, y+h/2, s, fs=fs, color=tc, ha='center', z=z+1)

def btn(ax, x, y, w, h, s, fc=BRAND, tc='white', fs=8.5, r=1.0, ec='none', z=4):
    rrect(ax, x, y, w, h, fc=fc, ec=ec, lw=1.0, r=r, z=z)
    txt(ax, x+w/2, y+h/2, s, fs=fs, color=tc, ha='center', bold=True, z=z+1)

def star(ax, cx, cy, s, color):
    pts = []
    for k in range(10):
        ang = -np.pi/2 + k*np.pi/5
        rr = s if k % 2 == 0 else s*0.45
        pts.append((cx + rr*ARX*np.cos(ang), cy + rr*np.sin(ang)))
    ax.add_patch(mpatches.Polygon(pts, closed=True, fc=color, ec='none', zorder=6))

def stars(ax, x, y, filled=5, s=0.95, gap=2.1):
    for i in range(5):
        star(ax, x + i*gap, y, s, GOLD if i < filled else '#DDDDDD')

def imgph(ax, x, y, w, h, fc='#EAEDF0', label=None):
    flat(ax, x, y, w, h, fc=fc, ec=LINE_, lw=0.8)
    circ(ax, x + w*0.72, y + h*0.72, min(w*ARX, h)*0.16, fc='#CBD2DA', z=3)
    ax.add_patch(mpatches.Polygon(
        [(x+w*0.12, y+h*0.28), (x+w*0.38, y+h*0.66), (x+w*0.55, y+h*0.28)],
        closed=True, fc='#C2CAD3', ec='none', zorder=3))
    ax.add_patch(mpatches.Polygon(
        [(x+w*0.42, y+h*0.28), (x+w*0.64, y+h*0.55), (x+w*0.86, y+h*0.28)],
        closed=True, fc='#D2D8DF', ec='none', zorder=3))
    if label:
        txt(ax, x+w/2, y+h*0.13, label, fs=6.6, color=SUB, ha='center', z=4)

def browser(ax, url):
    rrect(ax, 1, 1, 98, 98, fc='white', ec='#D0D0D0', lw=1.5, r=1.8, z=1)
    line(ax, (1.6, 91.3), (98.4, 91.3), color='#E4E4E4', lw=1.1)
    for i, c in enumerate(['#FF5F57', '#FEBC2E', '#28C840']):
        circ(ax, 4.6 + i*2.5, 95.3, 0.85, fc=c, z=5)
    rrect(ax, 14, 93.6, 74, 3.3, fc='#F4F5F7', ec='#E4E4E4', r=1.0, z=3)
    txt(ax, 16, 95.25, url, fs=7.0, color='#A2A2A2', z=4)

def appnav(ax, active, items=("首页", "美食地图", "菜谱广场", "发布", "消息")):
    flat(ax, 1.6, 84.6, 96.8, 6.6, fc='white', ec='none', z=2)
    line(ax, (1.6, 84.6), (98.4, 84.6), color=LINE_, lw=1.0)
    rrect(ax, 4, 86.3, 3.0, 3.0, fc=BRAND, ec='none', r=0.7, z=3)
    txt(ax, 5.5, 87.85, "觅", fs=8.5, color='white', bold=True, ha='center', z=4)
    txt(ax, 8, 87.85, "觅食记", fs=10.5, color=INK, bold=True, z=3)
    x0 = 24
    for it in items:
        col = BRAND if it == active else '#5A6068'
        txt(ax, x0, 87.85, it, fs=9, color=col, bold=(it == active), ha='center', z=3)
        if it == active:
            flat(ax, x0-4, 85.2, 8, 0.5, fc=BRAND, ec='none', z=4)
        x0 += 10
    rrect(ax, 72, 86.4, 16, 2.9, fc=BG, ec=LINE_, r=1.4, z=3)
    txt(ax, 74, 87.85, "搜索店铺 / 菜谱", fs=6.8, color=SUB, z=4)
    circ(ax, 95, 87.85, 1.7, fc=BRANDL, z=3)
    txt(ax, 95, 87.85, "我", fs=7.5, color=BRAND, ha='center', z=4)

def sectitle(ax, x, y, s, sub=None):
    flat(ax, x, y, 0.9, 3.0, fc=BRAND, ec='none', z=4)
    txt(ax, x+2.2, y+1.5, s, fs=11, color=INK, bold=True, z=4)
    if sub:
        txt(ax, x+16, y+1.5, sub, fs=7, color=SUB, z=4)

# ============ 图6-1 用户端首页 ============
def fig_home():
    fig, ax = uax()
    browser(ax, "http://localhost:5173/home")
    appnav(ax, "首页")
    # Hero
    rrect(ax, 4, 70.5, 92, 11.5, fc=BRANDL, ec='none', r=1.5, z=2)
    txt(ax, 8, 79, "发现城市里的每一味美食", fs=15, color=BRAND, bold=True, z=4)
    txt(ax, 8, 75.6, "探店打卡 · 菜谱分享 · 边吃边做", fs=9, color='#B0714F', z=4)
    btn(ax, 8, 71.3, 15, 3.0, "立即探索 →", fs=8.5, z=4)
    imgph(ax, 66, 71.5, 28, 9.5, label="首页轮播 Banner")
    # 分类
    sectitle(ax, 4, 65, "热门分类")
    cats = ["火锅", "川菜", "粤菜", "烧烤", "日料", "甜品", "小吃"]
    x = 4
    for i, c in enumerate(cats):
        pill(ax, x, 60.5, 11, 3.6, c, fc=('#FCEDE7' if i == 0 else BG),
             tc=(BRAND if i == 0 else '#606870'), fs=8.2)
        x += 12.5
    # 为你推荐
    sectitle(ax, 4, 53, "为你推荐", sub="根据评分 / 热度智能排序")
    names = [("蜀香源老火锅", "火锅", 5, "¥98/人"),
             ("粤味轩茶餐厅", "粤菜", 4, "¥76/人"),
             ("炉端烧居酒屋", "日料", 5, "¥120/人")]
    cw, gap = 29, 2.5
    for i, (nm, cat, sc, price) in enumerate(names):
        cx = 4 + i*(cw+gap)
        rrect(ax, cx, 6, cw, 44, fc='white', ec=LINE_, lw=1.1, r=1.3, z=2)
        imgph(ax, cx+1.4, 30, cw-2.8, 18)
        txt(ax, cx+2.4, 26, nm, fs=10, color=INK, bold=True, z=4)
        pill(ax, cx+2.4, 21.5, 8, 3.0, cat, fs=7.2)
        stars(ax, cx+2.9, 17.5, filled=sc, s=0.8, gap=1.9)
        txt(ax, cx+13, 17.5, f"{sc}.0", fs=8, color=GOLD, bold=True, z=4)
        txt(ax, cx+2.4, 12.5, price, fs=9, color=BRAND, bold=True, z=4)
        txt(ax, cx+cw-2.4, 12.5, "1.2km", fs=7.5, color=SUB, ha='right', z=4)
        btn(ax, cx+2.4, 7.8, cw-4.8, 3.2, "查看详情 · 在线预约", fc=BG, tc=BRAND, fs=7.6, z=4)
    save(fig, "fig6_1_home")

# ============ 图6-2 店铺详情页 ============
def fig_shop_detail():
    fig, ax = uax()
    browser(ax, "http://localhost:5173/shop/1")
    appnav(ax, "首页")
    # 左侧主图
    imgph(ax, 4, 40, 40, 40, label="店铺主图")
    imgph(ax, 4, 30, 12.3, 8)
    imgph(ax, 17.8, 30, 12.3, 8)
    imgph(ax, 31.6, 30, 12.4, 8)
    # 右侧信息
    txt(ax, 48, 78, "蜀香源老火锅(旗舰店)", fs=14, color=INK, bold=True, z=4)
    stars(ax, 48.5, 73.5, filled=5, s=0.95, gap=2.3)
    txt(ax, 61, 73.5, "4.8 分", fs=10, color=GOLD, bold=True, z=4)
    txt(ax, 70, 73.5, "· 236 条笔记", fs=8, color=SUB, z=4)
    pill(ax, 48, 68, 9, 3.4, "川菜·火锅", fs=8)
    pill(ax, 58.5, 68, 10, 3.4, "人均 ¥98", fc=BG, tc='#606870', fs=8)
    txt(ax, 48, 63, "地址：高新区软件园二路 66 号 3 栋", fs=8.5, color='#555', z=4)
    txt(ax, 48, 59, "营业：10:00 - 22:00     电话：0991-88886666", fs=8.5, color='#555', z=4)
    btn(ax, 48, 52, 15, 4.2, "点亮打卡", fc=BRAND, fs=9)
    btn(ax, 65, 52, 15, 4.2, "在线预约", fc=GREEN, fs=9)
    btn(ax, 82, 52, 12, 4.2, "咨询商家", fc=BG, tc=BRAND, fs=9, ec=BRAND, z=4)
    line(ax, (48, 47), (94, 47), color=LINE_, lw=1.0)
    # 推荐菜品
    sectitle(ax, 48, 41, "招牌推荐")
    for i, (nm, pr) in enumerate([("秘制毛肚", "¥42"), ("鸭肠拼盘", "¥38"), ("手打虾滑", "¥46")]):
        cx = 48 + i*15.7
        imgph(ax, cx, 30, 14, 8)
        txt(ax, cx+0.6, 27.5, nm, fs=8, color=INK, z=4)
        txt(ax, cx+0.6, 24.8, pr, fs=8, color=BRAND, bold=True, z=4)
    # 底部探店笔记
    sectitle(ax, 4, 21, "探店笔记", sub="最新 · 最热")
    reviews = [("美食家小林", "锅底醇厚、毛胚脆嫩，服务也很热情，强烈推荐！", "赞 128"),
               ("吃货阺May", "环境干净整洁，朋友聚餐的好去处，已经二刷~", "赞 96")]
    for i, (rn, rt, rl) in enumerate(reviews):
        cy = 13 - i*7
        rrect(ax, 4, cy, 90, 6, fc=BG, ec='none', r=0.8, z=2)
        circ(ax, 8, cy+3, 1.8, fc=BRANDL, z=3)
        txt(ax, 12, cy+4, rn, fs=8, color=INK, bold=True, z=4)
        stars(ax, 12, cy+1.6, filled=5, s=0.6, gap=1.5)
        txt(ax, 30, cy+3, rt, fs=8, color='#555', z=4)
        txt(ax, 90, cy+3, rl, fs=8, color=BRAND, ha='right', z=4)
    save(fig, "fig6_2_shop_detail")

# ============ 图6-3 发布探店笔记页 ============
def fig_publish():
    fig, ax = uax()
    browser(ax, "http://localhost:5173/publish")
    appnav(ax, "发布")
    rrect(ax, 12, 5, 76, 76, fc='white', ec=LINE_, lw=1.1, r=1.4, z=2)
    txt(ax, 18, 76, "发布探店笔记", fs=13, color=INK, bold=True, z=4)
    line(ax, (18, 72.5), (82, 72.5), color=LINE_, lw=1.0)
    # 关联店铺
    txt(ax, 18, 68, "关联店铺", fs=9, color='#555', bold=True, z=4)
    rrect(ax, 30, 66, 52, 4.2, fc=BG, ec=LINE_, r=0.8, z=3)
    txt(ax, 32, 68.1, "蜀香源老火锅（旗舰店）", fs=8.5, color=INK, z=4)
    ax.add_patch(mpatches.Polygon([(79, 68.6), (81, 68.6), (80, 67.6)], closed=True, fc=SUB, ec='none', zorder=5))
    # 评分
    txt(ax, 18, 60, "综合评分", fs=9, color='#555', bold=True, z=4)
    stars(ax, 31, 60, filled=5, s=1.1, gap=2.8)
    txt(ax, 48, 60, "5.0 力荐", fs=9, color=GOLD, bold=True, z=4)
    # 标题
    txt(ax, 18, 53.5, "笔记标题", fs=9, color='#555', bold=True, z=4)
    rrect(ax, 30, 51.5, 52, 4.2, fc='white', ec=LINE_, r=0.8, z=3)
    txt(ax, 32, 53.6, "人均百元的宝藏火锅,毛肚绝了!", fs=8.5, color=INK, z=4)
    # 正文富文本
    txt(ax, 18, 46, "图文正文", fs=9, color='#555', bold=True, z=4)
    rrect(ax, 30, 24, 52, 20, fc='white', ec=LINE_, r=0.8, z=3)
    rrect(ax, 31, 40.5, 50, 3.0, fc=BG, ec='none', r=0.4, z=4)
    for i, t in enumerate(["B", "I", "U", "H", "字", "链", "图"]):
        txt(ax, 33+i*3, 42, t, fs=7.5, color='#666', ha='center', z=5)
    for i, wd in enumerate([46, 44, 40, 30]):
        flat(ax, 32, 37-i*3, wd, 1.1, fc='#ECEEF1', ec='none', z=4)
    # 图片上传
    txt(ax, 18, 19, "上传图片", fs=9, color='#555', bold=True, z=4)
    for i in range(3):
        imgph(ax, 30+i*9, 12, 8, 8)
    rrect(ax, 57, 12, 8, 8, fc=BG, ec=LINE_, lw=1.1, r=0.6, z=3)
    txt(ax, 61, 16.5, "＋", fs=14, color=SUB, ha='center', z=4)
    txt(ax, 61, 13.5, "拖拽/点击", fs=6, color=SUB, ha='center', z=4)
    btn(ax, 66, 6.5, 16, 4.4, "发布笔记", fc=BRAND, fs=10)
    save(fig, "fig6_3_publish")

# ============ 图6-4 菜谱详情 + 采购清单 ============
def fig_recipe_detail():
    fig, ax = uax()
    browser(ax, "http://localhost:5173/recipe/1")
    appnav(ax, "菜谱广场")
    # 左:菜谱主体
    imgph(ax, 4, 62, 38, 18, label="成品图")
    txt(ax, 4, 58, "麻婆豆腐 · 家常下饭神器", fs=13, color=INK, bold=True, z=4)
    pill(ax, 4, 53.5, 9, 3.2, "难度:简单", fc='#E9F5EE', tc=GREEN, fs=7.5)
    pill(ax, 14, 53.5, 9, 3.2, "约 20 分钟", fc=BG, tc='#606870', fs=7.5)
    txt(ax, 25, 55.1, "by 家常菜达人 · 收藏 1.8k", fs=7.5, color=SUB, z=4)
    sectitle(ax, 4, 47, "所需食材")
    ings = ["嫩豆腐 400g", "牛肉末 100g", "郫县豆瓣 2勺", "花椒粉 适量",
            "蒜苗 2根", "生抽 1勺", "淀粉 少许", "食用油 适量"]
    for i, g in enumerate(ings):
        cx = 4 + (i % 2)*20
        cy = 42 - (i // 2)*4.2
        circ(ax, cx+0.8, cy, 0.7, fc=BRAND, z=4)
        txt(ax, cx+2.4, cy, g, fs=8, color='#444', z=4)
    sectitle(ax, 4, 22, "制作步骤")
    for i, s in enumerate(["豆腐切块焯水,牛肉末煸香出油。",
                           "下豆瓣酱炒出红油,加豆腐与高汤焖煮。",
                           "勾芡收汁,撒花椒粉与蒜苗即可出锅。"]):
        cy = 17 - i*5
        circ(ax, 5, cy, 1.3, fc=BRANDL, z=4)
        txt(ax, 5, cy, str(i+1), fs=8, color=BRAND, bold=True, ha='center', z=5)
        txt(ax, 8, cy, s, fs=8.2, color='#444', z=4)
    # 右:采购清单侧栏
    rrect(ax, 62, 5, 32, 75, fc=BG, ec=LINE_, lw=1.0, r=1.3, z=2)
    txt(ax, 66, 76, "我的采购清单", fs=11, color=INK, bold=True, z=4)
    btn(ax, 78, 74.5, 13, 3.6, "＋ 一键加入", fc=BRAND, fs=7.8)
    line(ax, (66, 71.5), (90, 71.5), color=LINE_, lw=1.0)
    checks = [("嫩豆腐 400g", True), ("牛肉末 100g", True), ("郫县豆瓣 2勺", False),
              ("花椒粉 适量", False), ("蒜苗 2根", True), ("生抽 1勺", False),
              ("淀粉 少许", False)]
    for i, (nm, have) in enumerate(checks):
        cy = 66 - i*6.2
        bc = GREEN if have else 'white'
        rrect(ax, 66, cy, 2.6, 2.6, fc=bc, ec=(GREEN if have else LINE_), lw=1.1, r=0.5, z=4)
        if have:
            ax.plot([66.8, 67.3, 68.1], [cy+1.3, cy+0.8, cy+2.0], color='white',
                    lw=1.4, solid_capstyle='round', zorder=5)
        col = SUB if have else INK
        txt(ax, 70, cy+1.3, nm, fs=8, color=col, z=4)
        txt(ax, 90, cy+1.3, "已有" if have else "待购", fs=7,
            color=(GREEN if have else BRAND), ha='right', z=4)
    txt(ax, 66, 9.5, "待采购 4 项 · 已备齐 3 项", fs=8, color=SUB, z=4)
    save(fig, "fig6_4_recipe_detail")

# ============ 图6-5 实时聊天 ============
def fig_chat():
    fig, ax = uax()
    browser(ax, "http://localhost:5173/chat")
    appnav(ax, "消息")
    # 会话列表
    rrect(ax, 4, 5, 26, 76, fc='white', ec=LINE_, lw=1.0, r=1.2, z=2)
    txt(ax, 7, 76.5, "消息列表", fs=10.5, color=INK, bold=True, z=4)
    line(ax, (5, 73), (29, 73), color=LINE_, lw=1.0)
    convs = [("蜀香源老火锅", "您的预约已确认~", "2", True),
             ("粤味轩茶餐厅", "亲,座位给您留好啦", "", False),
             ("客服小觅", "欢迎使用觅食记!", "", False)]
    for i, (nm, msg, badge, act) in enumerate(convs):
        cy = 64 - i*8
        if act:
            rrect(ax, 5, cy-1, 24, 7, fc=BRANDL, ec='none', r=0.8, z=2)
        circ(ax, 9, cy+2.5, 2.2, fc=('#FBD7C8' if act else BG), z=3)
        txt(ax, 13, cy+4, nm, fs=8.2, color=INK, bold=True, z=4)
        txt(ax, 13, cy+1, msg, fs=7, color=SUB, z=4)
        if badge:
            circ(ax, 27, cy+3, 1.3, fc=BRAND, z=4)
            txt(ax, 27, cy+3, badge, fs=6.5, color='white', ha='center', z=5)
    # 聊天窗口
    rrect(ax, 32, 5, 62, 76, fc='white', ec=LINE_, lw=1.0, r=1.2, z=2)
    txt(ax, 36, 76.5, "蜀香源老火锅", fs=10.5, color=INK, bold=True, z=4)
    circ(ax, 90, 76.8, 0.7, fc=GREEN, z=4)
    txt(ax, 88.5, 76.5, "在线", fs=7, color=GREEN, ha='right', z=4)
    line(ax, (33, 73), (93, 73), color=LINE_, lw=1.0)
    txt(ax, 63, 69.5, "今天 12:30", fs=6.8, color=SUB, ha='center', z=4)

    def bubble(cy, s, right=False, w=34):
        if right:
            rrect(ax, 90-w, cy, w, 6.5, fc=BRAND, ec='none', r=1.2, z=3)
            txt(ax, 90-w+2.2, cy+3.25, s, fs=8, color='white', z=4)
            circ(ax, 92, cy+3.25, 1.8, fc=BRANDL, z=3)
        else:
            circ(ax, 36, cy+3.25, 1.8, fc='#FBD7C8', z=3)
            rrect(ax, 39, cy, w, 6.5, fc=BG, ec='none', r=1.2, z=3)
            txt(ax, 41, cy+3.25, s, fs=8, color=INK, z=4)
    bubble(60, "你好,请问今晚 6 点还有 4 人桌吗?", right=False)
    bubble(51, "有的亲~已为您预留,记得准时到店哦!", right=True, w=38)
    bubble(42, "好的,那我现在就在线预约啦。", right=False, w=30)
    bubble(33, "收到！到店报手机号即可，期待您光临~", right=True, w=40)
    # 输入框
    rrect(ax, 33, 6.5, 47, 4.6, fc=BG, ec=LINE_, r=1.0, z=3)
    txt(ax, 35, 8.8, "输入消息...", fs=8, color=SUB, z=4)
    btn(ax, 81, 6.5, 12, 4.6, "发送", fc=BRAND, fs=9)
    save(fig, "fig6_5_chat")

# ============ 图6-6 运营数据大屏 ============
def fig_bigscreen():
    fig, ax = uax()
    D = '#0E1A34'; PANEL = '#152444'; CY = '#2DE0E0'; ORG = '#FF9F45'; GRN = '#4BE38B'
    flat(ax, 0, 0, 100, 100, fc=D, ec='none', z=1)
    txt(ax, 50, 95, "觅食记 · 运营数据可视化大屏", fs=15, color='white', bold=True, ha='center', z=6)
    line(ax, (20, 91.5), (80, 91.5), color=CY, lw=1.2)
    txt(ax, 6, 95, "● 实时", fs=8, color=GRN, z=6)
    txt(ax, 94, 95, "2026-07-19", fs=8, color='#8FA6C8', ha='right', z=6)
    # KPI 卡片
    kpis = [("用户总数", "12,486", CY), ("入驻店铺", "1,032", ORG),
            ("探店笔记", "8,754", GRN), ("分享菜谱", "3,269", '#B98CFF')]
    for i, (nm, val, cc) in enumerate(kpis):
        x = 4 + i*23.5
        rrect(ax, x, 78, 21, 9.5, fc=PANEL, ec='none', r=1.0, z=2)
        flat(ax, x, 78, 0.8, 9.5, fc=cc, ec='none', z=3)
        txt(ax, x+2.5, 84.5, val, fs=15, color=cc, bold=True, z=4)
        txt(ax, x+2.5, 80.5, nm, fs=8, color='#9DB2D4', z=4)
    # 折线图(近7日活跃)
    rrect(ax, 4, 42, 45, 32, fc=PANEL, ec='none', r=1.0, z=2)
    txt(ax, 7, 71, "近 7 日活跃趋势", fs=9.5, color='white', bold=True, z=4)
    xs = np.linspace(8, 45, 7); ys = np.array([48, 52, 50, 58, 55, 63, 66])
    ax.plot(xs, ys, color=CY, lw=2.0, marker='o', ms=4, zorder=4)
    ax.fill_between(xs, ys, 46, color=CY, alpha=0.12, zorder=3)
    for lb, xv in zip(["一", "二", "三", "四", "五", "六", "日"], xs):
        txt(ax, xv, 44, lb, fs=6.5, color='#8FA6C8', ha='center', z=4)
    # 柱状图(菜系分布)
    rrect(ax, 52, 42, 44, 32, fc=PANEL, ec='none', r=1.0, z=2)
    txt(ax, 55, 71, "热门菜系店铺分布", fs=9.5, color='white', bold=True, z=4)
    bx = np.linspace(57, 92, 6); bv = [26, 20, 17, 13, 9, 6]
    for xv, v, lb in zip(bx, bv, ["火锅", "川菜", "粤菜", "烧烤", "日料", "甜品"]):
        flat(ax, xv-2.4, 46, 4.8, v*0.7, fc=ORG, ec='none', z=4)
        txt(ax, xv, 44, lb, fs=6.3, color='#8FA6C8', ha='center', z=4)
    # 环形图(评分构成)
    rrect(ax, 4, 5, 30, 34, fc=PANEL, ec='none', r=1.0, z=2)
    txt(ax, 7, 35.5, "笔记评分构成", fs=9.5, color='white', bold=True, z=4)
    wedvals = [55, 25, 12, 8]; wcols = [GRN, CY, ORG, '#FF6B6B']
    start = 90
    for v, cc in zip(wedvals, wcols):
        ax.add_patch(mpatches.Wedge((17, 19), 8.5, start, start+v*3.6,
                     width=3.2, fc=cc, ec=D, lw=1.0, zorder=4))
        start += v*3.6
    txt(ax, 17, 19, "4.7", fs=13, color='white', bold=True, ha='center', z=5)
    txt(ax, 17, 15.5, "平均分", fs=6.5, color='#8FA6C8', ha='center', z=5)
    # 排行榜
    rrect(ax, 37, 5, 59, 34, fc=PANEL, ec='none', r=1.0, z=2)
    txt(ax, 40, 35.5, "人气店铺 TOP 5", fs=9.5, color='white', bold=True, z=4)
    rk = [("蜀香源老火锅", 96), ("炉端烧居酒屋", 88), ("粤味轩茶餐厅", 81),
          ("巷子口麻辣烫", 73), ("甜心烘焙工坊", 65)]
    for i, (nm, v) in enumerate(rk):
        cy = 30 - i*5
        cc = ORG if i == 0 else CY
        txt(ax, 40, cy, f"{i+1}", fs=8, color=cc, bold=True, z=4)
        txt(ax, 43, cy, nm, fs=8, color='white', z=4)
        flat(ax, 66, cy-0.8, v*0.26, 1.8, fc=cc, ec='none', z=4)
        txt(ax, 93, cy, str(v), fs=7.5, color=cc, ha='right', z=4)
    save(fig, "fig6_6_bigscreen")

# ============ 图6-7 后台内容审核 ============
def fig_review():
    fig, ax = uax()
    browser(ax, "http://localhost:5173/admin/content-review")
    # 顶栏
    flat(ax, 1.6, 84.6, 96.8, 6.6, fc='#2B3245', ec='none', z=2)
    txt(ax, 5, 87.9, "觅食记 管理后台", fs=10.5, color='white', bold=True, z=4)
    txt(ax, 88, 87.9, "审核员 · admin", fs=8, color='#B9C2D0', z=4)
    circ(ax, 94, 87.9, 1.6, fc='#4A5468', z=3)
    # 左侧菜单
    flat(ax, 1.6, 2, 18, 82.6, fc='#F7F8FA', ec='none', z=2)
    menu = [("数据仪表盘", False), ("店铺审核", False), ("内容审核", True),
            ("用户管理", False), ("运营推荐", False), ("数据大屏", False), ("系统配置", False)]
    for i, (m, act) in enumerate(menu):
        cy = 78 - i*8
        if act:
            flat(ax, 1.6, cy-2, 18, 6, fc=BRANDL, ec='none', z=3)
            flat(ax, 1.6, cy-2, 0.8, 6, fc=BRAND, ec='none', z=4)
        txt(ax, 4.5, cy+1, m, fs=8.5, color=(BRAND if act else '#555'),
            bold=act, z=4)
    # 右侧内容
    txt(ax, 22, 80, "内容审核 · 探店笔记", fs=12, color=INK, bold=True, z=4)
    for i, tab in enumerate(["待审核 12", "已通过", "已驳回"]):
        pill(ax, 22+i*13, 74.5, 12, 3.6, tab,
             fc=(BRANDL if i == 0 else BG), tc=(BRAND if i == 0 else '#606870'), fs=7.6)
    # 表头
    rrect(ax, 22, 68, 74, 3.8, fc='#F0F2F5', ec='none', r=0.5, z=3)
    for x, t in [(24, "内容预览"), (58, "作者"), (70, "关联店铺"), (84, "操作")]:
        txt(ax, x, 69.9, t, fs=8, color='#666', bold=True, z=4)
    rows = [("人均百元的宝藏火锅,毛肚绝了!", "美食家小林", "蜀香源火锅"),
            ("这家茶餐厅的丝袜奶茶太惊艳了", "吃货阿May", "粤味轩"),
            ("周末探店:居酒屋の深夜食堂", "夜猫子", "炉端烧"),
            ("避雷!服务态度差评…(疑似违规)", "匿名用户", "某快餐店")]
    for i, (c, au, sh) in enumerate(rows):
        cy = 62 - i*8
        rrect(ax, 22, cy, 74, 6.6, fc='white', ec=LINE_, lw=0.9, r=0.5, z=2)
        imgph(ax, 23.2, cy+0.9, 6, 4.8)
        txt(ax, 30, cy+3.3, c, fs=7.8, color=INK, z=4)
        txt(ax, 58, cy+3.3, au, fs=7.6, color='#555', z=4)
        txt(ax, 70, cy+3.3, sh, fs=7.6, color='#555', z=4)
        btn(ax, 80, cy+1.6, 6.5, 3.4, "通过", fc=GREEN, fs=7.3)
        btn(ax, 88, cy+1.6, 6.5, 3.4, "驳回", fc='#E0574F', fs=7.3)
    txt(ax, 22, 27, "共 12 条待审核  · 第 1 / 3 页", fs=7.6, color=SUB, z=4)
    save(fig, "fig6_7_review")

if __name__ == '__main__':
    fig_home()
    fig_shop_detail()
    fig_publish()
    fig_recipe_detail()
    fig_chat()
    fig_bigscreen()
    fig_review()
    print("gen4 done")

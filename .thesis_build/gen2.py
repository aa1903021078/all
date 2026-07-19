#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""业务流程图 + ER图"""
import sys
sys.path.insert(0, "/Users/weibaiwang/Desktop/AAA/work/all/.thesis_build")
from dbase import *

# ============ 图4-3 线下探店消费闭环流程图 ============
def fig_flow_explore():
    fig, ax = new_ax(9.6, 7.2)
    ellipse(ax, 50, 94, 20, 6, "开始", fc=C['gray_bg'], ec=C['gray'], fs=10)
    steps = [
        (50, 85, "浏览店铺列表 / 美食地图", C['blue_bg'], C['blue']),
        (50, 76, "查看店铺详情与星级评分", C['blue_bg'], C['blue']),
    ]
    for x, y, t, fc, ec in steps:
        box(ax, x, y, 44, 6.4, t, fc=fc, ec=ec, fs=10)
    d1 = diamond(ax, 50, 65, 30, 12, "是否满意?", fs=9.5)
    box(ax, 50, 52, 44, 6.4, "实时咨询商家(WebSocket)", fc=C['teal_bg'], ec=C['teal'], fs=10)
    box(ax, 50, 42, 44, 6.4, "在线预约到店(填写时间/人数)", fc=C['orange_bg'], ec=C['orange'], fs=10)
    d2 = diamond(ax, 50, 30, 32, 12, "商家确认预约?", fs=9.5)
    box(ax, 50, 17, 44, 6.4, "到店消费 → 点亮打卡店铺", fc=C['green_bg'], ec=C['green'], fs=10)
    box(ax, 50, 7.5, 44, 6.4, "发布探店笔记 → 评分互动", fc=C['green_bg'], ec=C['green'], fs=10)
    # 主线箭头
    arrow(ax, (50, 91), (50, 88.2))
    arrow(ax, (50, 81.8), (50, 79.2))
    arrow(ax, (50, 72.8), (50, 71))
    arrow(ax, (50, 59), (50, 55.2), text="是")
    arrow(ax, (50, 48.8), (50, 45.2))
    arrow(ax, (50, 24), (50, 20.2), text="是")
    arrow(ax, (50, 13.8), (50, 10.7))
    # 分支: 不满意 -> 返回浏览
    arrow(ax, (65, 65), (86, 65), text="否")
    line(ax, (86, 65), (86, 85)); arrow(ax, (86, 85), (72, 85))
    # 分支: 未确认 -> 取消
    arrow(ax, (34, 30), (16, 30), text="否")
    box(ax, 12, 20, 16, 6, "取消/改约", fc=C['red_bg'], ec=C['red'], fs=9)
    line(ax, (16, 30), (12, 30)); arrow(ax, (12, 30), (12, 23))
    save(fig, "fig4_3_flow_explore")

# ============ 图4-4 居家菜谱制作闭环流程图 ============
def fig_flow_recipe():
    fig, ax = new_ax(9.6, 7.0)
    ellipse(ax, 50, 94, 20, 6, "开始", fc=C['gray_bg'], ec=C['gray'], fs=10)
    box(ax, 50, 84, 46, 6.4, "浏览菜谱广场 / 查看菜谱详情", fc=C['blue_bg'], ec=C['blue'], fs=10)
    box(ax, 50, 74, 46, 6.4, "一键加入食材采购清单", fc=C['orange_bg'], ec=C['orange'], fs=10)
    box(ax, 50, 64, 46, 6.4, "勾选家中已有 → 过滤待采购食材", fc=C['orange_bg'], ec=C['orange'], fs=10)
    box(ax, 50, 54, 46, 6.4, "线下采购食材", fc=C['teal_bg'], ec=C['teal'], fs=10)
    box(ax, 50, 44, 46, 6.4, "按图文步骤烹饪制作", fc=C['green_bg'], ec=C['green'], fs=10)
    d = diamond(ax, 50, 32, 30, 12, "制作完成?", fs=9.5)
    box(ax, 50, 19, 46, 6.4, "核销食材(标记已用)", fc=C['green_bg'], ec=C['green'], fs=10)
    box(ax, 50, 9.5, 46, 6.4, "复刻晒图 → 分享心得互动", fc=C['purple_bg'], ec=C['purple'], fs=10)
    ys = [(91, 87.2), (80.8, 77.2), (70.8, 67.2), (60.8, 57.2), (50.8, 47.2)]
    for a, b in ys:
        arrow(ax, (50, a), (50, b))
    arrow(ax, (50, 40.8), (50, 38))
    arrow(ax, (50, 26), (50, 22.2), text="是")
    arrow(ax, (50, 15.8), (50, 12.7))
    arrow(ax, (65, 32), (86, 32), text="否")
    line(ax, (86, 32), (86, 44)); arrow(ax, (86, 44), (73, 44))
    save(fig, "fig4_4_flow_recipe")

# ============ 图5-4 图片上传处理流程图 ============
def fig_flow_upload():
    fig, ax = new_ax(9.6, 5.4)
    # 第一行: 上传发起
    ellipse(ax, 9, 78, 13, 9, "开始", fc=C['gray_bg'], ec=C['gray'], fs=9.5)
    box(ax, 31, 78, 20, 10, "前端选择/拖拽\n图片文件", fc=C['blue_bg'], ec=C['blue'], fs=9)
    box(ax, 56, 78, 22, 10, "构造FormData\nPOST /files/image", fc=C['blue_bg'], ec=C['blue'], fs=9)
    box(ax, 83, 78, 22, 10, "鉴权拦截器\n校验JWT登录", fc=C['orange_bg'], ec=C['orange'], fs=9)
    arrow(ax, (16, 78), (20.5, 78)); arrow(ax, (41.5, 78), (44.5, 78))
    arrow(ax, (67.5, 78), (71.5, 78))
    # 下到判断
    d = diamond(ax, 83, 50, 24, 13, "文件是否\n为空?", fs=9)
    arrow(ax, (83, 72.5), (83, 57))
    # 否分支: 向左处理
    box(ax, 56, 50, 22, 10, "推断扩展名\nUUID命名", fc=C['teal_bg'], ec=C['teal'], fs=9)
    box(ax, 31, 50, 22, 10, "按日期建目录\n保存至本地磁盘", fc=C['green_bg'], ec=C['green'], fs=9)
    ellipse(ax, 9, 50, 15, 9, "返回图片URL", fc=C['gray_bg'], ec=C['gray'], fs=8.5)
    arrow(ax, (71, 50), (67.5, 50), text="否")
    arrow(ax, (45, 50), (42.5, 50))
    arrow(ax, (20, 50), (16.8, 50))
    # 是分支: 向下抛异常
    box(ax, 83, 24, 30, 10, "抛出业务异常\n\"上传文件为空\"", fc=C['red_bg'], ec=C['red'], fs=9)
    arrow(ax, (83, 43.5), (83, 29), text="是")
    save(fig, "fig5_4_flow_upload")

# ============ 图4-5 数据库整体ER图 ============
def entity(ax, x, y, name, attrs, fc, ec):
    w = 22; hh = 6
    rows = len(attrs)
    box(ax, x, y, w, hh, name, fc=ec, ec=ec, fs=9.5, bold=True, tc='white')
    for i, a in enumerate(attrs):
        yy = y - hh/2 - (i + 0.5) * 4.4
        ax.add_patch(Rectangle((x - w/2, yy - 2.2), w, 4.4, fc=fc, ec=ec, lw=0.9, zorder=2))
        ax.text(x - w/2 + 1.4, yy, a, ha='left', va='center', fontsize=7.6,
                fontproperties=SONG, color='#333', zorder=3)
    return (x, y, w, hh + rows * 4.4)

def fig_er_overall():
    fig, ax = new_ax(10.4, 7.4)
    b, bg = C['blue'], C['blue_bg']
    g, gg = C['green'], C['green_bg']
    o, og = C['orange'], C['orange_bg']
    p, pg = C['purple'], C['purple_bg']
    t, tg = C['teal'], C['teal_bg']
    entity(ax, 18, 88, "sys_user 用户", ["PK id", "username", "password", "nickname", "status"], bg, b)
    entity(ax, 50, 90, "shop 店铺", ["PK id", "name", "category_id", "rating", "merchant_id"], og, o)
    entity(ax, 82, 88, "recipe 菜谱", ["PK id", "title", "author_id", "difficulty", "status"], gg, g)
    entity(ax, 50, 55, "note 探店笔记", ["PK id", "shop_id", "author_id", "rating", "like_count"], pg, p)
    entity(ax, 15, 52, "reservation 预约", ["PK id", "user_id", "shop_id", "status"], tg, t)
    entity(ax, 85, 52, "comment 评论", ["PK id", "target_id", "user_id", "parent_id"], bg, b)
    entity(ax, 14, 20, "shop_checkin 点亮", ["PK id", "user_id", "shop_id"], og, o)
    entity(ax, 40, 18, "shopping_item 采购", ["PK id", "user_id", "recipe_id", "status"], gg, g)
    entity(ax, 68, 18, "chat_message 消息", ["PK id", "session_id", "from_user", "to_user"], tg, t)
    entity(ax, 90, 20, "user_favorite 收藏", ["PK id", "user_id", "target_id"], pg, p)
    def rel(p1, p2, card=''):
        arrow(ax, p1, p2, color=C['line'], style='-', lw=1.1, text=card, fs=8)
    rel((18, 76), (44, 65)); rel((50, 78), (50, 66))
    rel((82, 76), (60, 65)); rel((28, 88), (39, 90))
    rel((26, 52), (39, 55)); rel((74, 55), (62, 55))
    rel((18, 79), (15, 62)); rel((22, 40), (34, 28))
    rel((50, 44), (66, 28)); rel((82, 76), (86, 62))
    rel((22, 79), (14, 30)); rel((82, 78), (88, 30))
    save(fig, "fig4_5_er_overall")

# ============ 图4-6 核心业务局部ER图(用户-笔记-店铺) ============
def fig_er_partial():
    fig, ax = new_ax(9.6, 6.0)
    entity(ax, 18, 90, "sys_user 用户",
           ["PK id", "username", "nickname", "avatar"], C['blue_bg'], C['blue'])
    entity(ax, 82, 90, "shop 店铺",
           ["PK id", "name", "address", "rating"], C['orange_bg'], C['orange'])
    entity(ax, 50, 40, "note 探店笔记",
           ["PK id", "FK author_id", "FK shop_id", "content", "rating"],
           C['purple_bg'], C['purple'])
    # 菱形关系
    diamond(ax, 30, 58, 20, 11, "发布", fs=10, fc=C['gray_bg'], ec=C['gray'])
    diamond(ax, 70, 58, 20, 11, "关联", fs=10, fc=C['gray_bg'], ec=C['gray'])
    # user - 发布 - note
    line(ax, (18, 69.4), (30, 63.5)); line(ax, (30, 52.5), (45, 43))
    # shop - 关联 - note
    line(ax, (82, 69.4), (70, 63.5)); line(ax, (70, 52.5), (55, 43))
    ax.text(22, 67, "1", fontsize=10.5, fontproperties=SONG, color=C['red'])
    ax.text(39, 48, "N", fontsize=10.5, fontproperties=SONG, color=C['red'])
    ax.text(78, 67, "1", fontsize=10.5, fontproperties=SONG, color=C['red'])
    ax.text(61, 48, "N", fontsize=10.5, fontproperties=SONG, color=C['red'])
    save(fig, "fig4_6_er_partial")

if __name__ == '__main__':
    fig_flow_explore()
    fig_flow_recipe()
    fig_flow_upload()
    fig_er_overall()
    fig_er_partial()
    print("gen2 done")

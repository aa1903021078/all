#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""架构类图: 论文框架、技术架构、系统总体架构、功能结构、用例图"""
import sys
sys.path.insert(0, "/Users/weibaiwang/Desktop/AAA/work/all/.thesis_build")
from dbase import *

# ============ 图1-1 论文组织结构图 ============
def fig_thesis_framework():
    fig, ax = new_ax(9.6, 6.0)
    box(ax, 50, 90, 60, 9, "基于 Spring Boot 与 Vue 的美食探店\n与菜谱分享平台设计与实现",
        fc=C['blue_bg'], ec=C['blue'], fs=12, bold=True)
    items = [
        ("第1章 绪论\n研究背景意义/现状/内容", C['gray_bg'], C['gray']),
        ("第2章 开发技术\n技术选型与开发环境", C['blue_bg'], C['blue']),
        ("第3章 系统分析\n可行性分析/需求分析", C['green_bg'], C['green']),
        ("第4章 概要设计\n架构/功能/流程/ER", C['orange_bg'], C['orange']),
        ("第5章 详细设计\n模块设计/数据库表", C['purple_bg'], C['purple']),
        ("第6章 系统实现\n核心页面与功能实现", C['teal_bg'], C['teal']),
        ("第7章 系统测试\n功能/性能测试", C['red_bg'], C['red']),
        ("第8章 结论与展望\n总结与未来改进", C['gray_bg'], C['gray']),
    ]
    xs = [20, 50, 80]
    ys = [68, 46, 24]
    coords = []
    for i, (t, fc, ec) in enumerate(items):
        x = xs[i % 3]; y = ys[i // 3]
        box(ax, x, y, 26, 12, t, fc=fc, ec=ec, fs=9.5)
        coords.append((x, y))
    arrow(ax, (50, 85.5), (coords[0][0], coords[0][1] + 6), rad=-0.1)
    for i in range(len(coords) - 1):
        arrow(ax, coords[i], coords[i + 1], rad=0.0, color=C['line'])
    save(fig, "fig1_1_framework")

# ============ 图2-1 系统技术架构图 ============
def fig_tech_arch():
    fig, ax = new_ax(9.8, 6.6)
    def layer(y, h, name, fc, ec, items):
        ax.add_patch(FancyBboxPatch((6, y - h/2), 88, h, boxstyle="round,pad=0.02,rounding_size=1.2",
                                    fc=fc, ec=ec, lw=1.4, zorder=1))
        ax.text(11, y, name, ha='center', va='center', fontsize=10.5, rotation=90,
                fontproperties=HEI, color=ec, fontweight='bold')
        n = len(items); x0 = 20; span = 72
        bw = span / n - 2
        for i, it in enumerate(items):
            cx = x0 + span * (i + 0.5) / n
            box(ax, cx, y, bw, h - 5, it, fc='white', ec=ec, fs=9)
    layer(87, 14, "表现层", C['blue_bg'], C['blue'],
          ["Vue3\n组合式API", "Element Plus\nUI组件", "ECharts\n可视化", "Vue Router\nPinia"])
    layer(68, 12, "通信层", C['teal_bg'], C['teal'],
          ["Axios\nRESTful", "JWT\n令牌鉴权", "WebSocket\n实时通信"])
    layer(49, 14, "业务层", C['orange_bg'], C['orange'],
          ["Controller\n控制器", "Service\n业务逻辑", "Interceptor\n拦截器", "RBAC\n权限"])
    layer(30, 12, "持久层", C['purple_bg'], C['purple'],
          ["MyBatis-Plus\nORM", "Mapper\n数据访问", "分页插件\n自动填充"])
    layer(12, 11, "数据层", C['green_bg'], C['green'],
          ["MySQL 8\n关系数据库", "本地文件\n图片存储"])
    for y1, y2 in [(80, 74), (62, 55), (42, 36), (24, 17.5)]:
        arrow(ax, (50, y1), (50, y2), color=C['line'], style='<|-|>', lw=1.4)
    save(fig, "fig2_1_tech_arch")

# ============ 图4-1 系统总体架构图 ============
def fig_sys_arch():
    fig, ax = new_ax(9.8, 6.4)
    box(ax, 25, 90, 26, 8, "用户端\n(浏览器)", fc=C['blue_bg'], ec=C['blue'], fs=10)
    box(ax, 50, 90, 26, 8, "商家端\n(浏览器)", fc=C['blue_bg'], ec=C['blue'], fs=10)
    box(ax, 75, 90, 26, 8, "管理后台\n(浏览器)", fc=C['blue_bg'], ec=C['blue'], fs=10)
    box(ax, 50, 74, 78, 9, "Nginx / Vite 静态资源 · 前端单页应用(SPA)",
        fc=C['teal_bg'], ec=C['teal'], fs=10.5, bold=True)
    for x in [25, 50, 75]:
        arrow(ax, (x, 86), (x, 78.6), color=C['line'], style='<|-|>')
    box(ax, 50, 60, 78, 8, "HTTP / HTTPS + WebSocket    (统一前缀 /api)",
        fc='white', ec=C['gray'], fs=10)
    arrow(ax, (50, 69.4), (50, 64), color=C['line'], style='<|-|>')
    # 后端容器
    ax.add_patch(FancyBboxPatch((10, 20), 80, 33, boxstyle="round,pad=0.02,rounding_size=1.5",
                                fc='#FafBff', ec=C['orange'], lw=1.5, zorder=1))
    ax.text(50, 50, "Spring Boot 3 应用服务", ha='center', fontsize=10.5,
            fontproperties=HEI, color=C['orange'], fontweight='bold')
    mods = ["认证/RBAC", "探店店铺", "菜谱分享", "探店笔记", "预约/点亮",
            "采购清单", "实时聊天", "评论互动", "数据统计", "内容审核", "文件上传", "系统配置"]
    for i, m in enumerate(mods):
        cx = 20 + (i % 4) * 20; cy = 43 - (i // 4) * 8
        box(ax, cx, cy, 17, 6, m, fc=C['orange_bg'], ec=C['orange'], fs=8.8)
    arrow(ax, (50, 56), (50, 53), color=C['line'], style='<|-|>')
    cyl(ax, 30, 9, 26, 12, "MySQL 8\nfoodie 库", fs=9.5)
    box(ax, 70, 9, 30, 9, "本地文件存储\n/data/upload(图片)", fc=C['green_bg'], ec=C['green'], fs=9.5)
    arrow(ax, (30, 19.5), (30, 15.5), color=C['line'], style='<|-|>')
    arrow(ax, (70, 19.5), (70, 13.6), color=C['line'], style='<|-|>')
    save(fig, "fig4_1_sys_arch")

# ============ 图4-2 系统功能结构图 ============
def fig_func_tree():
    fig, ax = new_ax(10.2, 6.6)
    box(ax, 50, 93, 40, 7, "美食探店与菜谱分享平台", fc=C['blue_bg'], ec=C['blue'], fs=11.5, bold=True)
    roots = [("用户端", 18, C['green_bg'], C['green']),
             ("商家端", 50, C['orange_bg'], C['orange']),
             ("管理后台", 82, C['purple_bg'], C['purple'])]
    subs = {
        18: ["首页推荐", "美食地图", "探店笔记", "菜谱详情", "发布创作", "采购清单", "实时聊天", "个人中心"],
        50: ["店铺管理", "预约管理", "评价申诉", "门店看板", "客户消息"],
        82: ["店铺审核", "内容审核", "用户管理", "运营推荐", "数据大屏", "系统配置"],
    }
    for name, x, fc, ec in roots:
        box(ax, x, 78, 22, 7, name, fc=fc, ec=ec, fs=10.5, bold=True)
        arrow(ax, (50, 89.5), (x, 81.5), color=C['line'], rad=0.0)
        items = subs[x]
        for i, it in enumerate(items):
            cy = 66 - i * 7.3
            box(ax, x, cy, 20, 5.6, it, fc='white', ec=ec, fs=9)
            top = 74.5 if i == 0 else 66 - (i - 1) * 7.3 - 2.8
            arrow(ax, (x, top), (x, cy + 2.8), color=C['line'], lw=1.0)
    save(fig, "fig4_2_func_tree")

# ============ 用例图通用 ============
def usecase(fname, tname, actor_name, groups, sys_title):
    fig, ax = new_ax(9.6, 7.0)
    actor(ax, 12, 55, actor_name, fs=11, color=C['blue'])
    # 系统边界
    ax.add_patch(FancyBboxPatch((28, 8), 66, 84, boxstyle="round,pad=0.02,rounding_size=1.5",
                                fc='#FCFDFE', ec=C['gray'], lw=1.5, zorder=0))
    ax.text(61, 88, sys_title, ha='center', fontsize=11, fontproperties=HEI,
            color='#444', fontweight='bold')
    colors = [(C['green_bg'], C['green']), (C['orange_bg'], C['orange']),
              (C['purple_bg'], C['purple']), (C['teal_bg'], C['teal']),
              (C['blue_bg'], C['blue']), (C['red_bg'], C['red'])]
    ncol = 2
    all_uc = []
    for gi, (gname, cases) in enumerate(groups):
        all_uc.append((gname, cases))
    # 平铺所有用例为椭圆
    flat = []
    for gi, (gname, cases) in enumerate(groups):
        for c in cases:
            flat.append((c, colors[gi % len(colors)]))
    n = len(flat)
    rows = (n + 1) // 2
    y0 = 82; dy = (82 - 15) / max(rows - 1, 1)
    for i, (c, (fc, ec)) in enumerate(flat):
        col = i % 2; row = i // 2
        cx = 47 + col * 30
        cy = y0 - row * dy
        ellipse(ax, cx, cy, 25, 7.2, c, fc=fc, ec=ec, fs=9)
        line(ax, (16, 54), (cx - 12.5, cy), color=C['line'], lw=0.9)
    save(fig, fname)

def fig_usecase_user():
    usecase("fig3_1_uc_user", "", "普通用户",
            [("", ["浏览店铺/菜谱", "全文搜索", "查看美食地图", "点亮打卡店铺",
                   "在线预约到店", "发布探店笔记", "分享菜谱", "评论点赞收藏",
                   "加入采购清单", "复刻晒图", "咨询商家", "个人中心管理"])],
            "美食探店与菜谱分享平台 · 用户端")

def fig_usecase_merchant():
    usecase("fig3_2_uc_merchant", "", "商家",
            [("", ["提交入驻申请", "编辑店铺信息", "维护推荐菜品", "处理用户预约",
                   "查看评价笔记", "提交差评申诉", "查看门店看板", "回复客户咨询"])],
            "美食探店与菜谱分享平台 · 商家端")

def fig_usecase_admin():
    usecase("fig3_3_uc_admin", "", "管理员/审核员",
            [("", ["店铺上架审核", "笔记菜谱审核", "违规内容下架", "用户账号管理",
                   "首页推荐配置", "查看数据大屏", "RBAC权限配置", "系统参数维护"])],
            "美食探店与菜谱分享平台 · 管理后台")

if __name__ == '__main__':
    fig_thesis_framework()
    fig_tech_arch()
    fig_sys_arch()
    fig_func_tree()
    fig_usecase_user()
    fig_usecase_merchant()
    fig_usecase_admin()
    print("gen1 done")

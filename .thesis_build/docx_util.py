#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""python-docx 排版助手: 严格实现模版批注要求的毕业论文格式。"""
from docx import Document
from docx.shared import Pt, Cm, RGBColor, Emu
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_LINE_SPACING, WD_BREAK
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.enum.section import WD_SECTION
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
from PIL import Image

IMG = "/Users/weibaiwang/Desktop/AAA/work/all/.thesis_build/img"

SONG = "宋体"
HEI = "黑体"
KAI = "楷体"
TNR = "Times New Roman"

# 字号(pt): 三号16 小三15 四号14 小四12 五号10.5 小五9
SZ = {"三号": 16, "小三": 15, "四号": 14, "小四": 12, "五号": 10.5, "小五": 9}


def _set_rfonts(run, cn, en):
    rpr = run._element.get_or_add_rPr()
    rfonts = rpr.find(qn('w:rFonts'))
    if rfonts is None:
        rfonts = OxmlElement('w:rFonts')
        rpr.append(rfonts)
    rfonts.set(qn('w:ascii'), en)
    rfonts.set(qn('w:hAnsi'), en)
    rfonts.set(qn('w:eastAsia'), cn)
    rfonts.set(qn('w:cs'), en)


def style_run(run, cn=SONG, en=TNR, size=12, bold=False, italic=False, color=None):
    run.font.size = Pt(size)
    run.font.bold = bold
    run.font.italic = italic
    if color is not None:
        run.font.color.rgb = color
    _set_rfonts(run, cn, en)


def _pPr(p):
    return p._p.get_or_add_pPr()


def set_first_line_indent_chars(p, chars=2):
    pPr = _pPr(p)
    ind = pPr.find(qn('w:ind'))
    if ind is None:
        ind = OxmlElement('w:ind')
        pPr.append(ind)
    ind.set(qn('w:firstLineChars'), str(int(chars * 100)))
    ind.set(qn('w:firstLine'), str(int(chars * 240)))  # 兜底: 2字*12pt≈480/2


def set_spacing(p, before=None, after=None, line=None, rule=None):
    pf = p.paragraph_format
    if before is not None:
        pf.space_before = Pt(before)
    if after is not None:
        pf.space_after = Pt(after)
    if line is not None:
        pf.line_spacing = line
        if rule is not None:
            pf.line_spacing_rule = rule


def set_char_spacing(run, pts):
    """字间距(用于标题居中加宽)"""
    rpr = run._element.get_or_add_rPr()
    spc = OxmlElement('w:spacing')
    spc.set(qn('w:val'), str(int(pts * 20)))
    rpr.append(spc)


def page_break_before(p):
    _pPr(p).append(OxmlElement('w:pageBreakBefore'))


# ---------------- 页面 / 页眉 / 页脚 ----------------
def setup_page(section):
    section.page_width = Emu(int(11907 * 635))     # twips->EMU (1 twip=635 EMU)
    section.page_height = Emu(int(16840 * 635))
    section.top_margin = Emu(int(1440 * 635))
    section.bottom_margin = Emu(int(1440 * 635))
    section.left_margin = Emu(int(1797 * 635))
    section.right_margin = Emu(int(1797 * 635))
    section.header_distance = Emu(int(850 * 635))
    section.footer_distance = Emu(int(992 * 635))


def _field(p, instr):
    """插入域(如 PAGE)"""
    r1 = p.add_run()
    fc1 = OxmlElement('w:fldChar'); fc1.set(qn('w:fldCharType'), 'begin')
    r1._element.append(fc1)
    r2 = p.add_run()
    it = OxmlElement('w:instrText'); it.set(qn('xml:space'), 'preserve'); it.text = instr
    r2._element.append(it)
    r3 = p.add_run()
    fc2 = OxmlElement('w:fldChar'); fc2.set(qn('w:fldCharType'), 'end')
    r3._element.append(fc2)
    return [r1, r2, r3]


def set_header_footer(section, header_text):
    section.different_first_page_header_footer = True
    # 页眉
    hp = section.header.paragraphs[0]
    hp.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = hp.add_run(header_text)
    style_run(r, cn=SONG, en=TNR, size=9)
    _bottom_border_paragraph(hp)
    # 页脚(页码, 小五 TNR, 居中)
    fp = section.footer.paragraphs[0]
    fp.alignment = WD_ALIGN_PARAGRAPH.CENTER
    for rr in _field(fp, "PAGE"):
        style_run(rr, cn=SONG, en=TNR, size=9)
    # 首页(封面)页眉页脚留空
    section.first_page_header.paragraphs[0].text = ""
    section.first_page_footer.paragraphs[0].text = ""


def _bottom_border_paragraph(p):
    pPr = _pPr(p)
    pbdr = OxmlElement('w:pBdr')
    bottom = OxmlElement('w:bottom')
    bottom.set(qn('w:val'), 'single')
    bottom.set(qn('w:sz'), '6')
    bottom.set(qn('w:space'), '1')
    bottom.set(qn('w:color'), '000000')
    pbdr.append(bottom)
    pPr.append(pbdr)


def enable_update_fields(doc):
    """让 Word 打开时自动更新域(目录/页码)"""
    settings = doc.settings.element
    uf = OxmlElement('w:updateFields')
    uf.set(qn('w:val'), 'true')
    settings.append(uf)


# ---------------- 段落级封装 ----------------
def add_blank(doc, size=12):
    p = doc.add_paragraph()
    set_spacing(p, before=0, after=0, line=1.0)
    r = p.add_run("")
    style_run(r, size=size)
    return p


def add_body(doc, text):
    """正文: 小四 宋体, 1.5倍行距, 首行缩进2字符, 段前段后0"""
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    set_spacing(p, before=0, after=0, line=1.5)
    set_first_line_indent_chars(p, 2)
    # 支持行内英文自动用 TNR: 简化处理, 整段用宋体+TNR混排
    r = p.add_run(text)
    style_run(r, cn=SONG, en=TNR, size=SZ["小四"])
    return p


def add_h1(doc, text, page_break=True):
    """一级标题: 三号 黑体 居中, 段前3行段后2行, 单倍行距, 章前分页"""
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_spacing(p, before=18, after=16, line=1.0)
    if page_break:
        page_break_before(p)
    r = p.add_run(text)
    style_run(r, cn=HEI, en=TNR, size=SZ["三号"], bold=True)
    return p


def add_h2(doc, text):
    """二级标题: 小三 黑体 左对齐, 段前1行段后0.5行, 1.5倍行距"""
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    set_spacing(p, before=12, after=6, line=1.5)
    r = p.add_run(text)
    style_run(r, cn=HEI, en=TNR, size=SZ["小三"], bold=True)
    return p


def add_h3(doc, text):
    """三级标题: 四号 黑体 左对齐, 段前0.5行段后0.5行, 1.5倍行距"""
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    set_spacing(p, before=8, after=4, line=1.5)
    r = p.add_run(text)
    style_run(r, cn=HEI, en=TNR, size=SZ["四号"], bold=True)
    return p


def add_figure(doc, filename, caption, width_cm=13.0):
    """插图: 居中, 图题在下方五号加粗居中, 图号按章编号"""
    path = f"{IMG}/{filename}"
    # 依据像素比例限制宽度, 避免过高
    try:
        with Image.open(path) as im:
            w_px, h_px = im.size
        ratio = h_px / w_px
        if width_cm * ratio > 18.0:      # 过高则按高度反推
            width_cm = 18.0 / ratio
    except Exception:
        pass
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_spacing(p, before=6, after=2, line=1.0)
    run = p.add_run()
    run.add_picture(path, width=Cm(width_cm))
    cap = doc.add_paragraph()
    cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_spacing(cap, before=0, after=8, line=1.0)
    rc = cap.add_run(caption)
    style_run(rc, cn=SONG, en=TNR, size=SZ["五号"], bold=True)
    return p


def _set_cell_text(cell, text, bold=False, size=SZ["五号"], align='center'):
    cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
    p = cell.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER if align == 'center' else WD_ALIGN_PARAGRAPH.LEFT
    set_spacing(p, before=2, after=2, line=1.0)
    r = p.add_run(str(text))
    style_run(r, cn=SONG, en=TNR, size=size, bold=bold)


def _cell_border(cell, edge, sz, val='single', color='000000'):
    tcPr = cell._tc.get_or_add_tcPr()
    borders = tcPr.find(qn('w:tcBorders'))
    if borders is None:
        borders = OxmlElement('w:tcBorders')
        tcPr.append(borders)
    e = borders.find(qn('w:' + edge))
    if e is None:
        e = OxmlElement('w:' + edge)
        borders.append(e)
    e.set(qn('w:val'), val)
    e.set(qn('w:sz'), str(sz))
    e.set(qn('w:space'), '0')
    e.set(qn('w:color'), color)


def add_three_line_table(doc, caption, headers, rows, widths=None):
    """三线表: 表题在上方五号加粗居中; 顶/底线1.5pt, 表头下线1pt, 无竖线"""
    cap = doc.add_paragraph()
    cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_spacing(cap, before=8, after=2, line=1.0)
    rc = cap.add_run(caption)
    style_run(rc, cn=SONG, en=TNR, size=SZ["五号"], bold=True)

    ncol = len(headers)
    table = doc.add_table(rows=1 + len(rows), cols=ncol)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = True
    # 去除默认网格线(Table Grid 自带全边框): 先清空表级边框
    tblPr = table._element.tblPr
    old = tblPr.find(qn('w:tblBorders'))
    if old is not None:
        tblPr.remove(old)
    # 表头
    for j, h in enumerate(headers):
        _set_cell_text(table.rows[0].cells[j], h, bold=True)
    # 数据
    for i, row in enumerate(rows):
        for j, val in enumerate(row):
            _set_cell_text(table.rows[i + 1].cells[j], val, bold=False,
                           align='center')
    # 三线: 首行上边=1.5pt(sz12), 首行下边=1pt(sz8), 末行下边=1.5pt(sz12)
    for c in table.rows[0].cells:
        _cell_border(c, 'top', 12)
        _cell_border(c, 'bottom', 8)
    for c in table.rows[-1].cells:
        _cell_border(c, 'bottom', 12)
    if widths:
        for row in table.rows:
            for j, w in enumerate(widths):
                row.cells[j].width = Cm(w)
    return table


def add_code_block(doc, code, caption=None):
    """代码: 五号, 无背景, 单倍行距, 外框; 用1x1表格实现边框"""
    if caption:
        cp = doc.add_paragraph()
        cp.alignment = WD_ALIGN_PARAGRAPH.CENTER
        set_spacing(cp, before=6, after=2, line=1.0)
        rc = cp.add_run(caption)
        style_run(rc, cn=SONG, en=TNR, size=SZ["五号"], bold=True)
    table = doc.add_table(rows=1, cols=1)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    cell = table.rows[0].cells[0]
    for edge in ('top', 'bottom', 'left', 'right'):
        _cell_border(cell, edge, 4)
    cell.paragraphs[0]._p.getparent().remove(cell.paragraphs[0]._p)
    for ln in code.split("\n"):
        p = cell.add_paragraph()
        set_spacing(p, before=0, after=0, line=1.0)
        p.alignment = WD_ALIGN_PARAGRAPH.LEFT
        r = p.add_run(ln if ln else " ")
        style_run(r, cn=SONG, en="Consolas", size=SZ["五号"])
    return table


def add_toc(doc):
    """目录域(TOC), 打开时由 Word 更新"""
    p = doc.add_paragraph()
    fc1 = OxmlElement('w:fldChar'); fc1.set(qn('w:fldCharType'), 'begin')
    it = OxmlElement('w:instrText'); it.set(qn('xml:space'), 'preserve')
    it.text = r'TOC \o "1-3" \h \z \u'
    fc2 = OxmlElement('w:fldChar'); fc2.set(qn('w:fldCharType'), 'separate')
    t = OxmlElement('w:t'); t.text = "右键“更新域”生成目录…"
    fc3 = OxmlElement('w:fldChar'); fc3.set(qn('w:fldCharType'), 'end')
    r1 = p.add_run(); r1._element.append(fc1)
    r2 = p.add_run(); r2._element.append(it)
    r3 = p.add_run(); r3._element.append(fc2)
    r4 = p.add_run(); r4._element.append(t); style_run(r4, size=SZ["小四"])
    r5 = p.add_run(); r5._element.append(fc3)
    return p


def new_section(doc):
    return doc.add_section(WD_SECTION.NEW_PAGE)

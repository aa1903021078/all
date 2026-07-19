#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""精确解析 styles.xml 的字体/字号/行距/段落设置"""
import zipfile
from lxml import etree

TPL = "/Users/weibaiwang/Desktop/AAA/work/all/模版.docx"
W = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
def q(t): return f'{{{W}}}{t}'

with zipfile.ZipFile(TPL) as z:
    styles = etree.fromstring(z.read('word/styles.xml'))
    doc = etree.fromstring(z.read('word/document.xml'))

TARGET = ['Normal','Heading 1','Heading 2','Heading 3','Heading 4','Heading 5',
          '图表题注','目录标题','Title','参考文献','公式','图片']

def half_pt(v):
    try: return f"{int(v)/2}pt"
    except: return v

for s in styles.findall(q('style')):
    name_el = s.find(q('name'))
    name = name_el.get(q('val')) if name_el is not None else s.get(q('styleId'))
    if name not in TARGET: continue
    print(f"\n===== 样式: {name} (id={s.get(q('styleId'))}) =====")
    rpr = s.find(q('rPr'))
    if rpr is not None:
        rfonts = rpr.find(q('rFonts'))
        if rfonts is not None:
            print(f"  字体: ascii={rfonts.get(q('ascii'))} eastAsia={rfonts.get(q('eastAsia'))} hAnsi={rfonts.get(q('hAnsi'))}")
        sz = rpr.find(q('sz'))
        if sz is not None: print(f"  字号: {half_pt(sz.get(q('val')))}")
        b = rpr.find(q('b'))
        if b is not None: print(f"  加粗: yes")
    ppr = s.find(q('pPr'))
    if ppr is not None:
        jc = ppr.find(q('jc'))
        if jc is not None: print(f"  对齐: {jc.get(q('val'))}")
        spc = ppr.find(q('spacing'))
        if spc is not None:
            print(f"  间距: before={spc.get(q('before'))} after={spc.get(q('after'))} "
                  f"beforeLines={spc.get(q('beforeLines'))} afterLines={spc.get(q('afterLines'))} "
                  f"line={spc.get(q('line'))} lineRule={spc.get(q('lineRule'))}")
        ind = ppr.find(q('ind'))
        if ind is not None:
            print(f"  缩进: firstLine={ind.get(q('firstLine'))} firstLineChars={ind.get(q('firstLineChars'))} left={ind.get(q('left'))}")
        ol = ppr.find(q('outlineLvl'))
        if ol is not None: print(f"  大纲级别: {ol.get(q('val'))}")

# 页面设置 & 页眉页脚
print("\n\n===== 页面/节设置 =====")
for sec in doc.iter(q('sectPr')):
    pgSz = sec.find(q('pgSz'))
    pgMar = sec.find(q('pgMar'))
    if pgSz is not None:
        print(f"  页面: w={pgSz.get(q('w'))} h={pgSz.get(q('h'))}")
    if pgMar is not None:
        print(f"  页边距(twips): top={pgMar.get(q('top'))} right={pgMar.get(q('right'))} "
              f"bottom={pgMar.get(q('bottom'))} left={pgMar.get(q('left'))} "
              f"header={pgMar.get(q('header'))} footer={pgMar.get(q('footer'))}")
    print("  ---")

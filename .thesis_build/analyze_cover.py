#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import zipfile
from lxml import etree
TPL = "/Users/weibaiwang/Desktop/AAA/work/all/模版.docx"
W='http://schemas.openxmlformats.org/wordprocessingml/2006/main'
def q(t): return f'{{{W}}}{t}'
def ptext(p):
    return ''.join(t.text or '' for t in p.iter(q('t')))

with zipfile.ZipFile(TPL) as z:
    doc = etree.fromstring(z.read('word/document.xml'))
    print("===== 封面/首节段落 (前30个非空) =====")
    body = doc.find(q('body'))
    cnt=0
    for p in body.iter(q('p')):
        txt = ptext(p)
        if txt.strip():
            print(repr(txt.strip()[:70]))
            cnt+=1
            if cnt>=30: break
    # 页眉
    for name in ['header1.xml','header2.xml','header3.xml','header4.xml']:
        try:
            h = etree.fromstring(z.read(f'word/{name}'))
            txt = ''.join(t.text or '' for t in h.iter(q('t')))
            print(f"\n[{name}] 页眉文本: {repr(txt.strip())}")
        except KeyError:
            pass
    # 媒体图片尺寸
    print("\n===== 模板媒体图片 =====")
    import os
    for n in z.namelist():
        if n.startswith('word/media/'):
            info = z.getinfo(n)
            print(f"  {n}: {info.file_size} bytes")

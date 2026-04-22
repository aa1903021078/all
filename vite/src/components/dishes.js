// 菜品数据库：覆盖常见肉类 / 海鲜 / 禽蛋 / 蔬菜 / 豆制品 / 主食 / 菌菇 等食材
export const dishes = [
  // 猪肉
  { name: '红烧肉', ingredients: ['五花肉', '葱', '姜', '蒜', '生抽', '老抽', '冰糖', '八角'] },
  { name: '回锅肉', ingredients: ['猪肉', '青椒', '蒜苗', '豆瓣酱', '姜', '蒜'] },
  { name: '京酱肉丝', ingredients: ['猪里脊', '甜面酱', '葱', '姜', '豆皮'] },
  { name: '糖醋里脊', ingredients: ['猪里脊', '糖', '醋', '番茄酱', '鸡蛋', '淀粉'] },
  { name: '排骨玉米汤', ingredients: ['排骨', '玉米', '胡萝卜', '姜', '葱'] },
  // 牛肉 / 羊肉
  { name: '番茄牛腩', ingredients: ['牛腩', '番茄', '土豆', '洋葱', '姜'] },
  { name: '黑椒牛柳', ingredients: ['牛柳', '洋葱', '彩椒', '黑胡椒', '蒜'] },
  { name: '孜然羊肉', ingredients: ['羊肉', '孜然', '辣椒', '葱', '蒜'] },
  // 鸡肉
  { name: '宫保鸡丁', ingredients: ['鸡胸肉', '花生', '干辣椒', '葱', '蒜'] },
  { name: '可乐鸡翅', ingredients: ['鸡翅', '可乐', '生抽', '姜'] },
  { name: '大盘鸡', ingredients: ['鸡肉', '土豆', '青椒', '洋葱', '姜', '蒜'] },
  { name: '黄焖鸡', ingredients: ['鸡腿肉', '香菇', '青椒', '土豆', '姜'] },
  // 鱼类 / 海鲜
  { name: '清蒸鲈鱼', ingredients: ['鲈鱼', '葱', '姜', '蒸鱼豉油'] },
  { name: '糖醋鱼', ingredients: ['草鱼', '糖', '醋', '番茄酱', '姜'] },
  { name: '酸菜鱼', ingredients: ['黑鱼', '酸菜', '泡椒', '蒜', '花椒'] },
  { name: '水煮鱼', ingredients: ['草鱼', '豆芽', '花椒', '辣椒', '蒜'] },
  { name: '蒜蓉粉丝蒸虾', ingredients: ['虾', '粉丝', '蒜', '葱'] },
  { name: '白灼虾', ingredients: ['虾', '姜', '葱', '料酒'] },
  { name: '辣炒蛤蜊', ingredients: ['蛤蜊', '辣椒', '蒜', '葱', '姜'] },
  { name: '葱爆八爪鱼', ingredients: ['八爪鱼', '葱', '姜', '蒜', '生抽'] },
  // 蛋 / 奶 / 豆
  { name: '番茄炒蛋', ingredients: ['番茄', '鸡蛋', '葱', '糖'] },
  { name: '韭菜炒蛋', ingredients: ['韭菜', '鸡蛋', '盐'] },
  { name: '蒸蛋羹', ingredients: ['鸡蛋', '葱', '生抽'] },
  { name: '麻婆豆腐', ingredients: ['豆腐', '牛肉末', '豆瓣酱', '花椒', '蒜'] },
  { name: '家常豆腐', ingredients: ['豆腐', '青椒', '木耳', '蒜', '生抽'] },
  // 蔬菜 / 素食
  { name: '青椒土豆丝', ingredients: ['土豆', '青椒', '蒜', '醋'] },
  { name: '酸辣土豆丝', ingredients: ['土豆', '干辣椒', '醋', '蒜'] },
  { name: '地三鲜', ingredients: ['土豆', '茄子', '青椒', '蒜'] },
  { name: '鱼香茄子', ingredients: ['茄子', '猪肉末', '泡椒', '蒜', '葱'] },
  { name: '蒜蓉西兰花', ingredients: ['西兰花', '蒜', '盐'] },
  { name: '蚝油生菜', ingredients: ['生菜', '蚝油', '蒜'] },
  { name: '醋溜白菜', ingredients: ['白菜', '醋', '干辣椒', '蒜'] },
  { name: '清炒苦瓜', ingredients: ['苦瓜', '蒜', '盐'] },
  { name: '凉拌黄瓜', ingredients: ['黄瓜', '蒜', '醋', '香油'] },
  { name: '胡萝卜炒肉', ingredients: ['胡萝卜', '猪肉', '葱', '姜'] },
  { name: '手撕包菜', ingredients: ['包菜', '干辣椒', '蒜', '醋'] },
  // 菌菇
  { name: '香菇青菜', ingredients: ['香菇', '青菜', '蒜', '蚝油'] },
  { name: '小鸡炖蘑菇', ingredients: ['鸡肉', '蘑菇', '粉条', '葱', '姜'] },
  // 主食
  { name: '蛋炒饭', ingredients: ['米饭', '鸡蛋', '葱', '火腿'] },
  { name: '西红柿鸡蛋面', ingredients: ['面条', '番茄', '鸡蛋', '葱'] },
  { name: '韭菜饺子', ingredients: ['韭菜', '鸡蛋', '虾皮', '面粉'] }
]

// 从菜品库自动抽取所有食材（用于快捷点选与"数据库里有"的判断）
export const allIngredients = Array.from(
  new Set(dishes.flatMap((d) => d.ingredients))
).sort((a, b) => a.localeCompare(b, 'zh'))

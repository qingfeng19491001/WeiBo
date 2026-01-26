package com.example.weibo.ui.login

import android.text.TextUtils


object PinyinInitialUtils {
    private val singleCharMap = mapOf(
        '阿' to 'A', '埃' to 'A', '安' to 'A', '澳' to 'A', '奥' to 'A',
        '巴' to 'B', '白' to 'B', '保' to 'B', '北' to 'B', '比' to 'B',
        '波' to 'B', '博' to 'B', '不' to 'B', '布' to 'B', '部' to 'B',
        '查' to 'C', '朝' to 'C', '成' to 'C', '城' to 'C', '赤' to 'C',
        '大' to 'D', '丹' to 'D', '德' to 'D', '东' to 'D',
        '俄' to 'E', '厄' to 'E', '二' to 'E',
        '法' to 'F', '非' to 'F', '菲' to 'F', '芬' to 'F', '佛' to 'F', '福' to 'F',
        '刚' to 'G', '哥' to 'G', '格' to 'G', '根' to 'G', '公' to 'G', '古' to 'G',
        '国' to 'G', '哈' to 'H', '海' to 'H', '韩' to 'H', '汉' to 'H', '荷' to 'H',
        '黑' to 'H', '红' to 'H', '洪' to 'H', '华' to 'H', '黄' to 'H', '回' to 'H',
        '基' to 'J', '吉' to 'J', '加' to 'J', '柬' to 'J', '津' to 'J', '京' to 'J',
        '卡' to 'K', '开' to 'K', '科' to 'K', '肯' to 'K',
        '拉' to 'L', '老' to 'L', '黎' to 'L', '利' to 'L', '立' to 'L', '列' to 'L',
        '卢' to 'L', '罗' to 'L', '马' to 'M', '毛' to 'M', '美' to 'M', '蒙' to 'M',
        '孟' to 'M', '密' to 'M', '缅' to 'M', '摩' to 'M', '墨' to 'M', '莫' to 'M',
        '南' to 'N', '尼' to 'N', '纽' to 'N', '挪' to 'N',
        '帕' to 'P', '葡' to 'P', '普' to 'P', '日' to 'R',
        '瑞' to 'R', '萨' to 'S', '塞' to 'S', '沙' to 'S', '上' to 'S', '绍' to 'S',
        '深' to 'S', '圣' to 'S', '斯' to 'S', '苏' to 'S', '所' to 'S',
        '塔' to 'T', '泰' to 'T', '坦' to 'T', '特' to 'T', '天' to 'T', '土' to 'T',
        '突' to 'T', '托' to 'T', '瓦' to 'W', '危' to 'W', '委' to 'W', '文' to 'W',
        '乌' to 'W', '西' to 'X', '新' to 'X', '匈' to 'X', '叙' to 'X', '牙' to 'Y',
        '也' to 'Y', '伊' to 'Y', '以' to 'Y', '意' to 'Y', '印' to 'Y', '英' to 'Y',
        '约' to 'Y', '越' to 'Y', '赞' to 'Z', '乍' to 'Z', '中' to 'Z', '智' to 'Z',
        '中' to 'Z'
    )

    private val multiCharMap = mapOf(
        "埃塞" to 'E', "科威特" to 'K', "肯尼亚" to 'K', "科特迪瓦" to 'K',
        "马耳他" to 'M', "马绍尔" to 'M', "摩洛哥" to 'M', "莫桑比克" to 'M',
        "马达加斯加" to 'M', "毛里求斯" to 'M', "尼日尔" to 'N', "尼日利亚" to 'N',
        "挪威" to 'N', "中国" to 'Z', "中非" to 'Z', "中国香港" to 'X',
        "中国台湾" to 'T', "中国澳门" to 'A', "哥斯达黎加" to 'G', "洪都拉斯" to 'H',
        "萨尔瓦多" to 'S', "多米尼加" to 'D', "海地" to 'H', "牙买加" to 'Y',
        "巴拿马" to 'B', "尼加拉瓜" to 'N', "巴拉圭" to 'B', "秘鲁" to 'B',
        "巴西" to 'B', "阿根廷" to 'A', "智利" to 'Z', "哥伦比亚" to 'G',
        "厄瓜多尔" to 'E', "玻利维亚" to 'B', "乌拉圭" to 'W', "古巴" to 'G',
        "几内亚" to 'J', "几内亚比绍" to 'J', "利比里亚" to 'L', "马里" to 'M',
        "布基纳法索" to 'B', "加纳" to 'J', "多哥" to 'D', "贝宁" to 'B',
        "塞内加尔" to 'S', "冈比亚" to 'G', "毛里塔尼亚" to 'M', "佛得角" to 'F',
        "吉布提" to 'J', "厄立特里亚" to 'E', "索马里" to 'S', "坦桑尼亚" to 'T',
        "乌干达" to 'W', "卢旺达" to 'L', "布隆迪" to 'B', "刚果" to 'G',
        "喀麦隆" to 'K', "加蓬" to 'J', "赤道几内亚" to 'C', "乍得" to 'Z',
        "中非" to 'Z', "突尼斯" to 'T', "利比亚" to 'L', "苏丹" to 'S',
        "南苏丹" to 'N', "埃塞俄比亚" to 'E', "埃及" to 'A', "南非" to 'N',
        "塞舌尔" to 'S', "科摩罗" to 'K', "马拉维" to 'M', "赞比亚" to 'Z',
        "津巴布韦" to 'J', "博茨瓦纳" to 'B', "纳米比亚" to 'N', "莱索托" to 'L',
        "斯威士兰" to 'S', "特立尼达和多巴哥" to 'T', "沙特" to 'S', "新加坡" to 'X',
        "新西兰" to 'X', "以色列" to 'Y', "意大利" to 'Y', "印度尼西亚" to 'Y',
        "伊朗" to 'Y', "阿尔巴尼亚" to 'A', "阿富汗" to 'A', "阿联酋" to 'A',
        "阿塞拜疆" to 'A', "安哥拉" to 'A', "爱沙尼亚" to 'A', "罗马尼亚" to 'L',
        "东帝汶" to 'D', "冰岛" to 'B', "危地马拉" to 'G', "基里巴斯" to 'J',
        "奥地利" to 'A', "委内瑞拉" to 'W', "所罗门群岛" to 'S'
    )

    fun getInitial(name: String): Char {
        if (TextUtils.isEmpty(name)) {
            return '#'
        }

        
        for ((prefix, initial) in multiCharMap) {
            if (name.startsWith(prefix)) {
                return initial
            }
        }

        
        val firstChar = name[0]
        if (firstChar in 'A'..'Z' || firstChar in 'a'..'z') {
            return firstChar.uppercaseChar()
        }

        
        return singleCharMap[firstChar] ?: '#'
    }
}
















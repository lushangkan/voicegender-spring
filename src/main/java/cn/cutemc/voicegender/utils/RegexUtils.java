package cn.cutemc.voicegender.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

    /**
     * 获得匹配正则表达式的内容
     *
     * @author zhangchao
     * @param str               字符串
     * @param reg               正则表达式
     * @param isCaseInsensitive 是否忽略大小写，true忽略大小写，false大小写敏感
     * @return 匹配正则表达式的字符串，组成的List
     */
    public static List<String> getMatchList(final String str, final String reg, final boolean isCaseInsensitive) {
        ArrayList<String> result = new ArrayList<String>();
        Pattern pattern = null;
        if (isCaseInsensitive) {
            //编译正则表达式,忽略大小写
            pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        } else {
            //编译正则表达式,大小写敏感
            pattern = Pattern.compile(reg);
        }
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        result.trimToSize();
        return result;
    }
}



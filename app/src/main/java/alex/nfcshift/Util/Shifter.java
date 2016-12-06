package alex.nfcshift.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import alex.nfcshift.MainActivity;

/**
 *
 * Created by Alex on 2016/9/26.
 *
 */

public class Shifter {

    /**
     * 替换给定字符串中匹配正则表达式的子字符串
     * @param regex：正则表达式
     * @param decStr：所要匹配的字符串
     * @param replaceStr：将符合正则表达式的子串替换为该字符串
     * @return:  decStr:  返回替换以后新的字符串
     */
    public static String replace(String regex,String decStr,String replaceStr) {
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(decStr);
        //替换
        if(m.find()){
            String newconf = m.group(0);
            newconf = newconf.substring(0,128) + replaceStr+ newconf.substring(142);
            decStr = decStr.substring(0,m.start()) + newconf +decStr.substring(m.end());
            MainActivity.result = true;
        }
        return decStr;
    }


    public static boolean isexisted(String targetStr, String decStr){
        String[] dec = decStr.split("\\|");
        for(int i = 0; i<dec.length; i++) {
            if (targetStr.equals(dec[i])) {
                return true;
            }
        }
        return false;
    }
}

package alex.nfcshift.Util;

import android.os.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static alex.nfcshift.Util.FileHelper.readSDFile;
import static alex.nfcshift.Util.FileHelper.writeSDFile;

/**
 * Created by Alex on 2016/10/11.
 */

public class DataHelper {

    public static void savedata(List<String> data, String filename){
        String temp = data.get(0);
        for(int i = 1;i<data.size();i++){
            temp = temp+"|"+data.get(i);
        }
        writeSDFile(temp, filename);
    }

    public static List<String> getdata(String filename){
        List<String> data = new ArrayList<String>();
        String[] temp = readSDFile(Environment.getExternalStorageDirectory()+"/"+filename).split("\\|");
        for(int i = 0;i<temp.length;i++){
            data.add(temp[i]);
        }
        return data;
    }

    public static String getIDfromet(String str){
        str = replaceBlank(str);
        if(str.length()<11){
            return "error";
        }
        String[] temp = new String[4];
        if(str.contains("，")){
            temp = str.split("\\，");
        }else if(str.contains(",")){
            temp = str.split("\\,");
        }else {
            for(int i = 0; i<4; i++){
                temp[i] = str.substring(3*i,3*i+1);
            }
        }
        StringBuilder sb = new StringBuilder();
        for(String s : temp){
            sb.append(s);
        }
        String testString  = sb.toString();
        if(!isDigitOrLetter(testString)){
            return "error";
        }
        if(temp.length!=4){
            return "error";
        }

        String newID_input = temp[0];
        for(int i = 1;i<temp.length;i++){
            newID_input = newID_input + ", " + temp[i];
        }
        return newID_input.toUpperCase();
    }

    public static String getNewName(String Filename){
        int i = 1;
        String newname = "新增卡片";
        while(Shifter.isexisted(newname,readSDFile(Environment.getExternalStorageDirectory()+"/"+ Filename))){
            newname = "新增卡片" + i;
            newname = newname.toString();
            i++;
        }
        return newname;
    }

    public static String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for(int i =0; i<bytes.length;i++){
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i >= 0) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public static boolean isDigitOrLetter(String str)
    {
        for (int i = str.length(); --i >= 0;)
        {
            if (!Character.isLetterOrDigit(str.charAt(i)))
            {
                return false;
            }
            if(str.charAt(i) > 'f' || (str.charAt(i) <='Z' && str.charAt(i) >'F')){
                return false;
            }
        }
        return true;
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
}

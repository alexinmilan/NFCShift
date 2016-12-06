package alex.nfcshift.Util;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import alex.nfcshift.MainActivity;


/**
 * Created by Alex on 2016/9/26.
 */

public class FileHelper {
    private Context context;
    /** SD卡是否存在**/
    private boolean hasSD = false;
    /** SD卡的路径**/
    private static String SDPATH;
    /** 当前程序包的路径**/
    private String FILESPATH;
    public FileHelper(Context context) {
        this.context = context;
        hasSD = Environment.getExternalStorageDirectory().equals(
                android.os.Environment.MEDIA_MOUNTED);
        SDPATH = Environment.getRootDirectory().getPath();
        FILESPATH = this.context.getFilesDir().getPath();
    }
    /**
     * 写入内容到SD卡中的txt文本中
     * str为内容
     */
    public static void writeSDFile(String str,String fileName)
    {
            try{
                File file = new File(Environment.getExternalStorageDirectory() , fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream FileOs = new FileOutputStream(file) ;
                FileOs.write(str.getBytes());
                FileOs.close();
                MainActivity.result = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
    /**
     * 读取SD卡中文本文件
     *
     * @param fileName
     * @return
     */
    public static String readSDFile(String fileName) {
        File file = new File("//"+fileName);
        BufferedReader reader;
        String text = "";
        try {
            // FileReader f_reader = new FileReader(file);
            // BufferedReader reader = new BufferedReader(f_reader);
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream in = new BufferedInputStream(fis);
            in.mark(4);
            byte[] first3bytes = new byte[3];
            in.read(first3bytes);//找到文档的前三个字节并自动判断文档类型。
            in.reset();

            reader = new BufferedReader(new InputStreamReader(in, "utf-8"));

            String str = reader.readLine();

            while (str != null) {
                text = text + str;
                str = reader.readLine();
                if(str != null){
                    text = text + "\n";
                }
            }
            reader.close();
            in.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }
    public String getFILESPATH() {
        return FILESPATH;
    }
    public String getSDPATH() {
        return SDPATH;
    }
    public boolean hasSD() {
        return hasSD;
    }

    public static void configreplace(){
        try{
            List<String> cmd = new ArrayList<>();
            cmd.add("su");
            cmd.add("mount -o rw,remount /system");
            cmd.add("cp /storage/emulated/0/libnfc-nxp.conf /system/etc/");
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            for (String command : cmd) {
                if (command == null) {  continue;   }
                // donnot use os.writeBytes(commmand), avoid chinese charset error
                os.write(command.getBytes());
                os.writeBytes("\n");
                os.flush();
            }
            os.writeBytes("exit\n");
            os.flush();
            MainActivity.result = true;
        }catch (IOException e){ }
    }

    public static boolean isSupported(String fileName) {
        File file = new File("//" + fileName);
        if (file.exists()) {
            return true;
        }
        return false;
    }
}


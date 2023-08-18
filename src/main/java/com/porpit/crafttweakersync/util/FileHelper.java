package com.porpit.crafttweakersync.util;

import com.porpit.crafttweakersync.CraftTweakerSync;
import com.porpit.crafttweakersync.common.scriptdata.ScriptFileInfo;
import org.apache.commons.lang3.SystemUtils;
import org.apache.tika.Tika;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public class FileHelper {
    public static String getMd5ByFile(File file) throws FileNotFoundException {
        String value = null;
        FileInputStream in = new FileInputStream(file);
        Tika tika = new Tika();
        String fileType="";
        try {
            fileType = tika.detect(in).toLowerCase();
        } catch (Throwable e){;}
        if (!fileType.matches("text/(.*)") || Charset.defaultCharset().toString().toLowerCase().matches("utf-8"))
        {
            try {
                MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(byteBuffer);
                BigInteger bi = new BigInteger(1, md5.digest());
                value = bi.toString(16);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != in) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else {

            try{
                //BufferedReader reader=new BufferedReader(new InputStreamReader(in,Charset.defaultCharset()))

                MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
                ByteBuffer byteBufferUTF8 = ByteBuffer.wrap(byteBuffer.toString().getBytes(StandardCharsets.UTF_8));
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(byteBufferUTF8);
                BigInteger bi = new BigInteger(1, md5.digest());
                value = bi.toString(16);

            } catch (Throwable e2){
                e2.printStackTrace();
            }
            finally {
                if (null != in) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        return value;
    }

    public static File getScriptDirectory() {
        File scriptFile = new File("scripts");

        if (scriptFile.exists()) {
            if (scriptFile.isDirectory()) {
                CraftTweakerSync.logger.info("scripts dir exists");
            } else {
                CraftTweakerSync.logger.info("the same name scripts file exists, can not create dir");
            }
        } else {
            CraftTweakerSync.logger.info("scripts dir not exists, create it ...");
            scriptFile.mkdir();
        }
        return scriptFile;
    }

    public static List<ScriptFileInfo> getAllScriptData(File directory) {
        List<ScriptFileInfo> listData = new ArrayList<ScriptFileInfo>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    listData.addAll(getAllScriptData(file));
                } else if (file.isFile() && file.getName().endsWith(".zs") || file.getName().endsWith(".zip")) {
                    try {
                        ScriptFileInfo scriptFileInfo = new ScriptFileInfo(file);
                        listData.add(scriptFileInfo);
                    } catch (Exception e) {
                        CraftTweakerSync.logger.warn("Failed to read" + file.getPath() + "!");
                    }
                }
            }
        }
        return listData;
    }

    public static byte[] getFileBytes(File file) throws Exception {//TODO:convet text files to utf8 from local encoding(gbk or smh) (remove this comment later)
        byte[] buffer = null;
        if (Charset.defaultCharset().toString().toLowerCase().matches("utf-8"))
        {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        }
        else{
            Tika tika=new Tika();
            String fileType="";
            FileInputStream fis = new FileInputStream(file);

            try {
                fileType = tika.detect(fis).toLowerCase();
            } catch (Throwable e){;}

            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
            if (fileType.matches("text/(.*)"))
            {
                buffer=(new String(buffer)).getBytes(StandardCharsets.UTF_8);
            }else {
                ;
            }
        }

        return buffer;
    }
    public static void getFileByBytes(byte[] bfile, String filePath,String fileName) throws Exception{
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if(!dir.exists()&&dir.isDirectory()){//判断文件目录是否存在
                dir.mkdirs();
            }
            //file = new File(filePath+File.separator+fileName);// btw the server passes path as String not Path, File.separator or '\\' (dos/windows style separator) will cause glitch if the server and client are running on different OS(or versions of this mod)
            file = new File(filePath+"/"+fileName);//since modern windows and *nix support '/' (unix style separator) as file separator, we can fix the problem mentioned on previous line
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        }finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
    public static Object getObjectFromBytes(byte[] objBytes) throws Exception {
        if (objBytes == null || objBytes.length == 0) {
            return null;
        }
        ByteArrayInputStream bi = new ByteArrayInputStream(objBytes);
        ObjectInputStream oi = new ObjectInputStream(bi);
        return oi.readObject();
    }

    /**
     * 从对象获取一个字节数组
     * @EditTime 2007-8-13 上午11:46:56
     */
    public static byte[] getBytesFromObject(Serializable obj) throws Exception {
        if (obj == null) {
            return null;
        }
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(obj);
        return bo.toByteArray();
    }

    public  static byte[] readFileToBytes(File file) throws Exception{
        InputStream in= null;
        try {
            in = new FileInputStream(file);    //真正要用到的是FileInputStream类的read()方法
            byte[] bytes= new byte[in.available()];    //in.available()是得到文件的字节数
            in.read(bytes);    //把文件的字节一个一个地填到bytes数组中
            return bytes;
        }finally {
            try {
                in.close();
            }catch (Exception e){
            }
        }
    }
    public  static void deleteFile(String path){
        File file = new File(path);
        if(file.exists()){
            boolean d = file.delete();
            if(d){
                CraftTweakerSync.logger.info("删除了文件:"+path);
            }else{
                CraftTweakerSync.logger.info("删除文件:"+path+"失败!");
            }
        }
    }
    public static void writeBytesToFile(File file, byte[] data) throws IOException{

        if (Charset.defaultCharset().toString().toLowerCase().matches("utf-8")){//if Charset.defaultCharset() is utf8
            OutputStream out = new FileOutputStream(file);
            InputStream is = new ByteArrayInputStream(data);
            byte[] buff = new byte[1024];
            int len = 0;
            while((len=is.read(buff))!=-1){
                out.write(buff, 0, len);
            }
            is.close();
            out.close();
        }else {
            //TODO:convert text files to local encoding(gbk or smh) (prob next commit)
        }

    }

}



package com.lhzq.ibms.commons.util;
import java.io.*;
import java.util.*;

import org.apache.commons.io.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;
import com.lhzq.leap.components.zip.*;

/**
 * 描述：提供文件方面的支持
 * 时间：2010-5-13
 * 作者：王志华
 * CHANGELOG：
 */
public class IOUtil{
    final private static Log log=LogFactory.getLog(IOUtil.class);
    /**
     * 将文件复制到临时目录
     *
     * @param file
     */
    synchronized public static File copyToTempDir(File file){
        // 保存至临时目录
        String _tempDir=Configuration.getTempDir();
        File _dir=new File(_tempDir);
        if(!_dir.exists()){
            _dir.mkdirs();
        }
        // 生成目标文件名
        String _prefix=_tempDir+File.separatorChar+DateUtil.formatDate(new Date(),"yyyyMMddhhssmm");
        String _suffix=".dat";
        String _fileName=_prefix+_suffix;
        int _no=2;
        File _file=new File(_fileName);
        while(_file.exists()){
            _fileName=_prefix+"_"+_no+_suffix;
            _file=new File(_fileName);
            _no++;
        }
        InputStream _fin=null;
        OutputStream _fout=null;
        try{
            _fin=new FileInputStream(file);
            _fout=new FileOutputStream(_file);
            IOUtils.copy(_fin,_fout);
        } catch(Exception ex){
            _file=null;
            log.warn("copy file failed!",ex);
        } finally{
            if(_fout!=null)
                try{
                    _fout.close();
                } catch(Exception ex){
                }
            ;
            if(_fin!=null)
                try{
                    _fin.close();
                } catch(Exception ex){
                }
            ;
        }
        return _file;
    }
    /**
     * 将文件复制到临时目录
     *
     * @param dir 临时目录路径
     * @param fin 输入流
     */
    synchronized public static File copyToDir(String dir, InputStream fin){
        // 保存至临时目录
        File _dir=new File(dir);
        if(!_dir.exists()){
            _dir.mkdirs();
        }
        // 生成目标文件名
        String _prefix=dir+File.separatorChar+DateUtil.formatDate(new Date(),"yyyyMMddhhssmm");
        String _suffix=".dat";
        String _fileName=_prefix+_suffix;
        int _no=2;
        File _file=new File(_fileName);
        while(_file.exists()){
            _fileName=_prefix+"_"+_no+_suffix;
            _file=new File(_fileName);
            _no++;
        }
        OutputStream _fout=null;
        try{
            _fout=new FileOutputStream(_file);
            IOUtils.copy(fin,_fout);
        } catch(Exception ex){
            log.warn("copy file failed!",ex);

        } finally{
            if(_fout!=null)
                try{
                    _fout.close();
                } catch(Exception ex){
                    log.warn( "close OutputStream failed!" );
                }
            if(fin!=null)
                try{
                    fin.close();
                } catch(Exception ex){
                    log.warn( "close InputStream failed!" );
                }
        }
        return _file;
    }
    /**
     * 从某个目录中读取文件
     *
     * @param dir 临时目录路径
     * @param filename 文件名
     */
    synchronized public static InputStream readFileFromDir(String dir, String filename) throws FileNotFoundException{
        boolean isok=dir.endsWith(File.separator);
        String f=isok?dir+filename:dir+File.separator+filename;
        return new FileInputStream(f);
    }
    /**
     * 删除某个目录下的文件
     *
     * @param dir
     * @param filename
     */
    public static void deleteFile(String dir, String filename){
        boolean isok=dir.endsWith(File.separator);
        String f=isok?dir+filename:dir+File.separator+filename;
        deleteFile(f);
    }
    /**
     * 移出文件
     *
     * @param fileName 删除文件
     */
    public static void deleteFile(final String fileName){
        //boolean _isok = false;
        if(StringUtils.isNotEmpty(fileName)){
            new Thread(new Runnable(){
                public void run(){
                    File _file=new File(fileName);
                    while(_file.exists()){
                        _file.delete();
                        try{
                            Thread.sleep(100);
                        } catch(Exception ex){
                            log.warn("删除文件失败!["+fileName+"]",ex);
                            break;
                        }
                    }
                }
            }).start();
        }
    }
    /** 确定目录存在 */
    public static String ensureDirExists(String dir){
        File f=new File(dir);
        if(!f.exists()){
            synchronized(IOUtil.class){
                if(!f.exists()){
                    f.mkdirs();
                }
            }
        }
        return dir;
    }

    /**
     * 将文件字节数组添加到压缩包中
     * @param files：页面上传的文件，需从临时目录中读取
     * @param byteFiles List<map(文件名=fileName,文件字节数组=fileContent)>
     */
    public static byte[] filesToZip(List files,List<Map> byteFiles){
        byte[] zipContent=null;
        try{
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            ZipOutputStream zo=new ZipOutputStream(baos);
            zo.setEncoding("GBK");
            log.info("将文件打包为zip压缩文件");
            if(byteFiles!=null){
                for(Map map:byteFiles){
                    String _fileName=(String)map.get("fileName");
                    byte[] fileContent=(byte[])map.get("fileContent");
                    zo.putNextEntry(new ZipEntry(_fileName));
                    zo.write(fileContent);
                    zo.closeEntry();
                }
            }
            if(files!=null){
                for(int i = 0, size = files.size(); i < size; i++){
                    String[] _fitem = (String[])files.get(i); // 0=附件名称;1=临时文件
                    File file = new File( _fitem[1] );
                    //判断临时文件是否存在
                    if (file.exists() ){
                        zo.putNextEntry(new ZipEntry(_fitem[0]));//附件原文件名
                        FileInputStream fin=new FileInputStream(file);
                        byte[] fileContent=new byte[(int)file.length()];
                        fin.read(fileContent);
                        zo.write(fileContent);
                        zo.closeEntry();
                    }
                }
            }
            zo.close();
            zipContent=baos.toByteArray();
            baos.close();
        } catch(Exception e){
            log.error("打包文件为zip压缩文件出错!",e);
        }
        return zipContent;
    }
}


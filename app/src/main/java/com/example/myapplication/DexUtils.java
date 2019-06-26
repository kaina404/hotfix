package com.example.myapplication;


import android.content.Context;
import android.os.Environment;
import android.widget.Toast;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

public class DexUtils {


    /**
     * 将dex包复制到/data/user/包名之下
     * @param context
     */
    public static void copyFixDex2Data(Context context){
        File odex = context.getDir("odex", Context.MODE_PRIVATE);
        String filePath = new File(odex, "fixbug.dex").getAbsolutePath();
        File fixBugDexFile = new File(filePath);
        if (fixBugDexFile.exists()) {
            fixBugDexFile.delete();
        }
        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = new FileInputStream(new File(Environment.getExternalStorageDirectory(), "fixbug.dex"));
            os = new FileOutputStream(filePath);
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            File f = new File(filePath);
            if (f.exists()) {
                Toast.makeText(context, "dex overwrite", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void loadDex(Context context) {
        File fileDir = context.getDir("odex", Context.MODE_PRIVATE);
        //优化后的缓存路径
        String optimizedDirectory = fileDir.getAbsolutePath() + File.separator + "opt_dex";
        File opFile  = new File(optimizedDirectory);
        if(!opFile.exists()){
            try {
                opFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File[] listFiles = fileDir.listFiles();
        for (File file : listFiles) {
            if (file.getName().startsWith("classes") || file.getName().endsWith(".dex")) {
                String dexPath = file.getAbsolutePath();
                //dex中的lib路径
                String librarySearchPath = null;


                //APP中真正运行的加载dex的loader，我们的目的是吧dexClassLoader中的Element"合并"到pathClassLoader的Element[]中


                //BaseDexClassLoader->pathList(DexPathList)->dexElements(Element[])

                try {
                    //-------step1-----------反射到系统的Element[]---------------------
                    //拿到系统的ClassLoader
                    Class baseDexClassLoader = Class.forName("dalvik.system.BaseDexClassLoader");
                    Field pathListField = baseDexClassLoader.getDeclaredField("pathList");
                    pathListField.setAccessible(true);
                    //APP中真正运行的加载dex的loader
                    PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();
                    Object pathListObj = pathListField.get(pathClassLoader);

                    Class<?> pathListObjClass = pathListObj.getClass();
                    Field dexElementsField = pathListObjClass.getDeclaredField("dexElements");
                    dexElementsField.setAccessible(true);
                    //private Element[] dexElements;
                    Object systemElements = dexElementsField.get(pathListObj);


                    //--------step2----------反射到自己的Element[]---------------------
                    Class myBaseDexClassLoader = Class.forName("dalvik.system.BaseDexClassLoader");
                    Field myPathListField = myBaseDexClassLoader.getDeclaredField("pathList");
                    myPathListField.setAccessible(true);
                    //我们的dex包中的
                    DexClassLoader
                            dexClassLoader = new DexClassLoader(dexPath, optimizedDirectory, librarySearchPath, context.getClassLoader());
                    Object myPathListObj = myPathListField.get(dexClassLoader);

                    Class<?> myPathListObjClass = myPathListObj.getClass();
                    Field myDexElementsField = myPathListObjClass.getDeclaredField("dexElements");
                    myDexElementsField.setAccessible(true);
                    //private Element[] dexElements;
                    Object myElements = myDexElementsField.get(myPathListObj);

                    //----------step3-------------自己的Element合并到System的Element中-------------------------------
                    //Element的类型反射拿到
                    Class elementType = systemElements.getClass().getComponentType();
                    int sysDexLength = Array.getLength(systemElements);
                    int myDexLength = Array.getLength(myElements);
                    int newElementsAryLength = myDexLength + sysDexLength;

                    Object newElements = Array.newInstance(elementType, newElementsAryLength);

                    //-----------核心----将fixDex放到最前面----------
                    for (int i = 0; i < newElementsAryLength; i++) {
                        if (i < myDexLength) {
                            Array.set(newElements, i, Array.get(myElements, i));
                        } else {
                            Array.set(newElements, i, Array.get(systemElements, i - myDexLength));
                        }
                    }

                    Field elements = pathListObj.getClass().getDeclaredField("dexElements");
                    elements.setAccessible(true);
                    elements.set(pathListObj, newElements);//合并完毕!!!!!!!

                    System.out.println("fix finish");

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
        }

    }
}

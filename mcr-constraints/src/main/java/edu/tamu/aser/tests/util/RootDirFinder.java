package edu.tamu.aser.util;

import java.net.URLClassLoader;
import java.net.URL;
import java.net.URI;

import java.io.File;
import java.io.IOException;

public class RootDirFinder extends URLClassLoader {
  private String rootDir = null;
  private String className = null;
  private boolean mainMethod = false;

  //The idea here is to produce a classpath entry
  //with every possible prefix of a path starting
  //from root until the class file made from
  //the remainder of the path is loadable.
  //This allows us to find the package name of a random
  //class file
  public RootDirFinder(File f){
    super(new URL[] {});
    String path = f.getAbsolutePath(); 
    if(!path.endsWith(".class")) return;
    path = path.replaceFirst("[.]class$","");
    String[] pathArr = path.split("\\" + File.separator);
    String prefix = "file:///";
    String pathPrefix = "";
    for(int i = 0; i < pathArr.length; ++i){
      prefix += percentEncode(pathArr[i]) + "/";
      pathPrefix += pathArr[i] + File.separator;
      try{
        addURL(new URI(prefix).toURL());
      } catch (Exception e){
        e.printStackTrace();
        System.exit(1);
      }
      String suffix = "";  
      for(int j = i + 1; j < pathArr.length - 1; ++j){
        suffix += pathArr[j] + ".";
      }
      suffix += pathArr[pathArr.length - 1];
      try {
        Class<?> c = findClass(suffix);
        c.getMethod("main", (new String[]{}).getClass());
        mainMethod = true;
        rootDir = pathPrefix;
        className = suffix;
        return;
      } catch (NoSuchMethodException e) {
        mainMethod = false;
        return;
      } catch (Throwable t){}
    }
  }

  private boolean isWindows(){
    String os = System.getProperty("os.name").toLowerCase();
    return (os.indexOf("win") > 0);
  }

  //properly encode valid path characters for URIs... mostly just space
  //is a concern
  private String percentEncode(String s){
     String ret = s.replaceAll("[!]","%21");
     ret = ret.replaceAll("[ ]","%20");
     ret = ret.replaceAll("[*]","%2A");
     ret = ret.replaceAll("[']","%27");
     ret = ret.replaceAll("[(]","%28");
     ret = ret.replaceAll("[;]","%3B");
     ret = ret.replaceAll("[:]","%3A");
     ret = ret.replaceAll("[&]","%26");
     ret = ret.replaceAll("[=]","%3D");
     ret = ret.replaceAll("[+]","%2B");
     ret = ret.replaceAll("[$]","%24");
     ret = ret.replaceAll("[,]","%2C");
     ret = ret.replaceAll("[?]","%3F");
     ret = ret.replaceAll("[#]","%23");
     ret = ret.replaceAll("[\\[]","%5B");
     ret = ret.replaceAll("[\\]]","%5D");
     return ret; 
  }

  public String getRootDir() throws IOException, NoMainMethodException {
    if(!mainMethod){
      throw new NoMainMethodException();
    }
    if(rootDir == null){
      throw new IOException("could not find root directory, is this a valid java class file?");
    }
    return rootDir;
  }

  public String getClassName() throws IOException, NoMainMethodException {
    if(!mainMethod){
      throw new NoMainMethodException();
    }
    if(rootDir == null){
      throw new IOException("could not find class name, is this a valid java class file?");
    }
    return className;
  }

  static public void main(String[] args){
    RootDirFinder f = new RootDirFinder(new File(args[0]));
    try{ 
      System.out.println(f.getRootDir());
      System.out.println(f.getClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}


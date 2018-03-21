package org.w3c.util;

public class Trace extends Throwable {

  public static void showTrace() {
    try {
      throw new Trace("");
    } catch (Trace t) {
      System.out.print("\n");
      t.printStackTrace();
    }    
  }

  public static void showTrace(String title) {
    try {
      throw new Trace("\n\""+title+"\"");
    } catch (Trace t) {
      System.out.print("\n");
      t.printStackTrace();
    }
  }

  public Trace() {
    super();
  }

  public Trace(String s) {
    super(s);
  }

}

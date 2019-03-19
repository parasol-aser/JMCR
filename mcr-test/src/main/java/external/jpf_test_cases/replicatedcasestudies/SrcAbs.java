package external.jpf_test_cases.replicatedcasestudies;

//package signs;

import external.jpf_test_cases.MyRandom;

import java.util.*;
//import check.Verify;

public class SrcAbs {
	private static Hashtable theTable = null;

private static void buildTable()
{
	// Create a brand new table
	theTable = new Hashtable();

	theTable.put("+","add");
	theTable.put("/","div");
	theTable.put("==","eq");
	theTable.put(">=","ge");
	theTable.put(">","gt");
	theTable.put("<=","le");
	theTable.put("<","lt");
	theTable.put("*","mul");
	theTable.put("!=","neq");
	theTable.put("%","rem");
	theTable.put("-","sub");
}
public static boolean choose() { 
boolean tmp;
System.out.println("choose");
//while (tmp) {
//  System.out.println(" loop choose");
//  tmp=true;
//}
tmp= MyRandom.nextRandom();
//System.out.println("after choose");
return tmp;}
public static int choose (int bits)
{
	int value;

	if (bits==0) { throw new RuntimeException("SrcAbs.choose(int) called with 0-valued bit set\n"); }
	for (value = 0; bits != 0; value ++)
	{
		bits = bits >> 1;
		if ((bits << 1) != bits)
		{
			if (bits!=0)
			{
				if (choose()) return value;
			} else return value;
		}
	}
	return value-1;
}
public static String getMethodName(String clsName, String op)
{
	String res = null;
	if (theTable == null) buildTable();
	
	return (String) theTable.get(op);
}
}

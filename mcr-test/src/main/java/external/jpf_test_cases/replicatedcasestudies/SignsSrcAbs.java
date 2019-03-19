package external.jpf_test_cases.replicatedcasestudies;

//package signs;
//import signs.SrcAbs;

public class SignsSrcAbs extends IntSrcAbs {

  public final static int NEG = 2;
  public final static int ZERO = 0;
  public final static int POS = 1;

  public static int abs(int n) {
	if (n < 0) return NEG;
	if (n == 0) return ZERO;
	if (n > 0) return POS;
	throw new RuntimeException("Cannot abstract value:  " + n);
  }  
   public static int add(int left$, int right$) {
	  if ((left$ == NEG) && (right$ == NEG)) {
		 return NEG;
	  }
	  if ((left$ == NEG) && (right$ == ZERO)) {
		 return NEG;
	  }
	  if ((left$ == NEG) && (right$ == POS)) {
		 return SrcAbs.choose(7) /* {NEG,ZERO,POS} */;
	  }
	  if ((left$ == ZERO) && (right$ == NEG)) {
		 return NEG;
	  }
	  if ((left$ == ZERO) && (right$ == ZERO)) {
		 return ZERO;
	  }
	  if ((left$ == ZERO) && (right$ == POS)) {
		 return POS;
	  }
	  if ((left$ == POS) && (right$ == NEG)) {
		 return SrcAbs.choose(7) /* {NEG,ZERO,POS} */;
	  }
	  if ((left$ == POS) && (right$ == ZERO)) {
		 return POS;
	  }
	  if ((left$ == POS) && (right$ == POS)) {
		 return POS;
	  }
	throw new RuntimeException("Can't find match for SignsSrcAbs.add(" + left$ + ", " + right$ + ")");  }
   public static int div(int left$, int right$) {
	  if ((left$ == NEG) && (right$ == NEG)) {
		 return SrcAbs.choose(7) /* {NEG,ZERO,POS} */;
	  }
	  if ((left$ == NEG) && (right$ == ZERO)) {
		 return SrcAbs.choose(7) /* {NEG,ZERO,POS} */;
	  }
	  if ((left$ == NEG) && (right$ == POS)) {
		 return SrcAbs.choose(7) /* {NEG,ZERO,POS} */;
	  }
	  if ((left$ == ZERO) && (right$ == NEG)) {
		 return ZERO;
	  }
	  if ((left$ == ZERO) && (right$ == ZERO)) {
		 return SrcAbs.choose(7) /* {NEG,ZERO,POS} */;
	  }
	  if ((left$ == ZERO) && (right$ == POS)) {
		 return ZERO;
	  }
	  if ((left$ == POS) && (right$ == NEG)) {
		 return SrcAbs.choose(7) /* {NEG,ZERO,POS} */;
	  }
	  if ((left$ == POS) && (right$ == ZERO)) {
		 return SrcAbs.choose(7) /* {NEG,ZERO,POS} */;
	  }
	  if ((left$ == POS) && (right$ == POS)) {
		 return SrcAbs.choose(7) /* {NEG,ZERO,POS} */;
	  }
	throw new RuntimeException("Can't find match for SignsSrcAbs.div(" + left$ + ", " + right$ + ")");  }
   public static boolean eq(int left$, int right$) {
	  if ((left$ == NEG) && (right$ == NEG)) {
		 return SrcAbs.choose();
	  }
	  if ((left$ == NEG) && (right$ == ZERO)) {
		 return false;
	  }
	  if ((left$ == NEG) && (right$ == POS)) {
		 return false;
	  }
	  if ((left$ == ZERO) && (right$ == NEG)) {
		 return false;
	  }
	  if ((left$ == ZERO) && (right$ == ZERO)) {
		 return true;
	  }
	  if ((left$ == ZERO) && (right$ == POS)) {
		 return false;
	  }
	  if ((left$ == POS) && (right$ == NEG)) {
		 return false;
	  }
	  if ((left$ == POS) && (right$ == ZERO)) {
		 return false;
	  }
	  if ((left$ == POS) && (right$ == POS)) {
		 return SrcAbs.choose(); 
	  }
	 throw new RuntimeException("Can't find match for SignsSrcAbs.eq(left$, right$)");   }
   public static boolean ge(int left$, int right$) {
	  if ((left$ == NEG) && (right$ == NEG)) {
		 return SrcAbs.choose();
	  }
	  if ((left$ == NEG) && (right$ == ZERO)) {
		 return false;
	  }
	  if ((left$ == NEG) && (right$ == POS)) {
		 return false;
	  }
	  if ((left$ == ZERO) && (right$ == NEG)) {
		 return true;
	  }
	  if ((left$ == ZERO) && (right$ == ZERO)) {
		 return true;
	  }
	  if ((left$ == ZERO) && (right$ == POS)) {
		 return false;
	  }
	  if ((left$ == POS) && (right$ == NEG)) {
		 return true;
	  }
	  if ((left$ == POS) && (right$ == ZERO)) {
		 return true;
	  }
	  if ((left$ == POS) && (right$ == POS)) {
		 return SrcAbs.choose();
	  }
	 throw new RuntimeException("Can't find match for SignsSrcAbs.ge(left$, right$)");   }
   public static boolean gt(int left$, int right$) {
	  if ((left$ == NEG) && (right$ == NEG)) {
		 return SrcAbs.choose();
	  }
	  if ((left$ == NEG) && (right$ == ZERO)) {
		 return false;
	  }
	  if ((left$ == NEG) && (right$ == POS)) {
		 return false;
	  }
	  if ((left$ == ZERO) && (right$ == NEG)) {
		 return true;
	  }
	  if ((left$ == ZERO) && (right$ == ZERO)) {
		 return false;
	  }
	  if ((left$ == ZERO) && (right$ == POS)) {
		 return false;
	  }
	  if ((left$ == POS) && (right$ == NEG)) {
		 return true;
	  }
	  if ((left$ == POS) && (right$ == ZERO)) {
		 return true;
	  }
	  if ((left$ == POS) && (right$ == POS)) {
		 return SrcAbs.choose();
	  }
	 throw new RuntimeException("Can't find match for SignsSrcAbs.gt(left$, right$)");   }
   public static boolean le(int left$, int right$) {
	  if ((left$ == NEG) && (right$ == NEG)) {
		 return SrcAbs.choose();
	  }
	  if ((left$ == NEG) && (right$ == ZERO)) {
		 return true;
	  }
	  if ((left$ == NEG) && (right$ == POS)) {
		 return true;
	  }
	  if ((left$ == ZERO) && (right$ == NEG)) {
		 return false;
	  }
	  if ((left$ == ZERO) && (right$ == ZERO)) {
		 return true;
	  }
	  if ((left$ == ZERO) && (right$ == POS)) {
		 return true;
	  }
	  if ((left$ == POS) && (right$ == NEG)) {
		 return false;
	  }
	  if ((left$ == POS) && (right$ == ZERO)) {
		 return false;
	  }
	  if ((left$ == POS) && (right$ == POS)) {
		 return SrcAbs.choose();
	  }
	 throw new RuntimeException("Can't find match for SignsSrcAbs.le(left$, right$)");   }
   public static boolean lt(int left$, int right$) {
	  if ((left$ == NEG) && (right$ == NEG)) {
		 return SrcAbs.choose();
	  }
	  if ((left$ == NEG) && (right$ == ZERO)) {
		 return true;
	  }
	  if ((left$ == NEG) && (right$ == POS)) {
		 return true;
	  }
	  if ((left$ == ZERO) && (right$ == NEG)) {
		 return false;
	  }
	  if ((left$ == ZERO) && (right$ == ZERO)) {
		 return false;
	  }
	  if ((left$ == ZERO) && (right$ == POS)) {
		 return true;
	  }
	  if ((left$ == POS) && (right$ == NEG)) {
		 return false;
	  }
	  if ((left$ == POS) && (right$ == ZERO)) {
		 return false;
	  }
	  if ((left$ == POS) && (right$ == POS)) {
		 return SrcAbs.choose();
	  }
	 throw new RuntimeException("Can't find match for SignsSrcAbs.lt(left$, right$)");   }
   public static int mul(int left$, int right$) {
	  if ((left$ == NEG) && (right$ == NEG)) {
		 return POS;
	  }
	  if ((left$ == NEG) && (right$ == ZERO)) {
		 return ZERO;
	  }
	  if ((left$ == NEG) && (right$ == POS)) {
		 return NEG;
	  }
	  if ((left$ == ZERO) && (right$ == NEG)) {
		 return ZERO;
	  }
	  if ((left$ == ZERO) && (right$ == ZERO)) {
		 return ZERO;
	  }
	  if ((left$ == ZERO) && (right$ == POS)) {
		 return ZERO;
	  }
	  if ((left$ == POS) && (right$ == NEG)) {
		 return NEG;
	  }
	  if ((left$ == POS) && (right$ == ZERO)) {
		 return ZERO;
	  }
	  if ((left$ == POS) && (right$ == POS)) {
		 return POS;
	  }
	throw new RuntimeException("Can't find match for SignsSrcAbs.mul(" + left$ + ", " + right$ + ")");  }
   public static boolean neq(int left$, int right$) {
	  if ((left$ == NEG) && (right$ == NEG)) {
		 return SrcAbs.choose();
	  }
	  if ((left$ == NEG) && (right$ == ZERO)) {
		 return true;
	  }
	  if ((left$ == NEG) && (right$ == POS)) {
		 return true;
	  }
	  if ((left$ == ZERO) && (right$ == NEG)) {
		 return true;
	  }
	  if ((left$ == ZERO) && (right$ == ZERO)) {
		 return false;
	  }
	  if ((left$ == ZERO) && (right$ == POS)) {
		 return true;
	  }
	  if ((left$ == POS) && (right$ == NEG)) {
		 return true;
	  }
	  if ((left$ == POS) && (right$ == ZERO)) {
		 return true;
	  }
	  if ((left$ == POS) && (right$ == POS)) {
		 return SrcAbs.choose();
	  }
	 throw new RuntimeException("Can't find match for SignsSrcAbs.neq(left$, right$)");   }
   public static int rem(int left$, int right$) {
	  if ((left$ == NEG) && (right$ == NEG)) {
		 return SrcAbs.choose(7) /* {NEG,ZERO,POS} */;
	  }
	  if ((left$ == NEG) && (right$ == ZERO)) {
		 return SrcAbs.choose(7) /* {NEG,ZERO,POS} */;
	  }
	  if ((left$ == NEG) && (right$ == POS)) {
		 return SrcAbs.choose(7) /* {NEG,ZERO,POS} */;
	  }
	  if ((left$ == ZERO) && (right$ == NEG)) {
		 return ZERO;
	  }
	  if ((left$ == ZERO) && (right$ == ZERO)) {
		 return SrcAbs.choose(7) /* {NEG,ZERO,POS} */;
	  }
	  if ((left$ == ZERO) && (right$ == POS)) {
		 return ZERO;
	  }
	  if ((left$ == POS) && (right$ == NEG)) {
		 return SrcAbs.choose(7) /* {NEG,ZERO,POS} */;
	  }
	  if ((left$ == POS) && (right$ == ZERO)) {
		 return SrcAbs.choose(7) /* {NEG,ZERO,POS} */;
	  }
	  if ((left$ == POS) && (right$ == POS)) {
		 return SrcAbs.choose(7) /* {NEG,ZERO,POS} */;
	  }
	throw new RuntimeException("Can't find match for SignsSrcAbs.rem(" + left$ + ", " + right$ + ")");  }
   public static int sub(int left$, int right$) {
	  if ((left$ == NEG) && (right$ == NEG)) {
		 return SrcAbs.choose(7) /* {NEG,ZERO,POS} */;
	  }
	  if ((left$ == NEG) && (right$ == ZERO)) {
		 return NEG;
	  }
	  if ((left$ == NEG) && (right$ == POS)) {
		 return NEG;
	  }
	  if ((left$ == ZERO) && (right$ == NEG)) {
		 return POS;
	  }
	  if ((left$ == ZERO) && (right$ == ZERO)) {
		 return ZERO;
	  }
	  if ((left$ == ZERO) && (right$ == POS)) {
		 return NEG;
	  }
	  if ((left$ == POS) && (right$ == NEG)) {
		 return POS;
	  }
	  if ((left$ == POS) && (right$ == ZERO)) {
		 return POS;
	  }
	  if ((left$ == POS) && (right$ == POS)) {
		 return SrcAbs.choose(7) /* {NEG,ZERO,POS} */;
	  }
	throw new RuntimeException("Can't find match for SignsSrcAbs.sub(" + left$ + ", " + right$ + ")");  }
}

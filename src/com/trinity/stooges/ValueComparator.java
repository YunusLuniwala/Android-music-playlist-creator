package com.trinity.stooges;

import java.util.Comparator;
import java.util.Map;

public class ValueComparator implements Comparator<Object> {

	  Map<Integer, Float> base;
	  
	  public ValueComparator(Map<Integer, Float> base) {
	      this.base = base;
	  }

	  public int compare(Object a, Object b) {
		  if((Float)base.get(a) < (Float)base.get(b)) {
			  return 1;
		  } 
		  else if((Float)base.get(a) == (Float)base.get(b)) {
			  return 0;
		  } 
		  else {
			  return -1;
		  }
	  }
}




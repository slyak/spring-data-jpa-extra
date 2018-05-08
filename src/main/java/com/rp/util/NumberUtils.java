package com.rp.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

/**
 * @author <a href="mailto:rplees.i.ly@gmail.com">rplees</a>
 * date 2010-04-07
 * {@code} 数字类型的常用方法
 */
public class NumberUtils {
	public static final int DEFAULT_VALUE = 0 ;
	public static final DecimalFormat df1 = new DecimalFormat("#0.0"); 
	public static final DecimalFormat df = new DecimalFormat("#0"); 
	
	public static Number sum(Number... ns) {
		if(ns == null || ns.length == 0)
			return 0;
		
		double d = 0;
		for (Number number : ns) {
			if(ns != null) {
				d += number.doubleValue();
			}
		}
		
		return d;
	}
	
	public static int sum(int... ns) {
		if(ns == null || ns.length == 0)
			return 0;
		
		int d = 0;
		for (int number : ns) {
			if(ns != null) {
				d += number;
			}
		}
		
		return d;
	}
	/**
	 * 判断该数字是否为有效数字
	 * 主要判断 是否为空和是否大于DEFAULT_VALUE
	 * @param n
	 * @return true 为有效数字
	 */
	public static boolean isValid(Number n) {
		if(n != null) {
			if(n instanceof Double) {
				return n.doubleValue() > 0D;
			} else if (n instanceof Float){
				return n.floatValue() > 0L;
			} else if (n instanceof Long){
				return n.longValue() > 0L;
			}
			
			return n.intValue() > DEFAULT_VALUE;
		} else {
			return false;
		}
	}
	
	/**
	 * 判断该数字是否为有效数字
	 * 主要判断 是否为空和是否大于等于DEFAULT_VALUE
	 * @param n
	 * @return
	 * @created 2016年9月21日 上午11:31:03
	 */
	public static boolean isValidWithZero(Number n) {
		if(n != null) {
			if(n instanceof Double) {
				return n.doubleValue() >= 0D;
			} else if (n instanceof Float){
				return n.floatValue() >= 0L;
			} else if (n instanceof Long){
				return n.longValue() >= 0L;
			}
			
			return n.intValue() >= DEFAULT_VALUE;
		} else {
			return false;
		}
	}
	/**
	 * 判断该数字是否为无效数字
	 * @param n
	 * @return true为无效数字
	 */
	public static boolean isNotValidWithoutZero(Number n) {
		return !isValidWithZero(n);
	}

	/**
	 * 判断该数字是否为无效数字
	 * @param n
	 * @return true为无效数字
	 */
	public static boolean isNotValid(Number n) {
		return !isValid(n);
	}
	
	
	/**
	 * 判断该数字是否为无效数字
	 * @param n
	 * @return true为无效数字
	 */
	public static boolean isGt0Valid(Number n) {
		return (n != null && n.intValue() >= DEFAULT_VALUE);
	}
	
	/**
	 * 去高位数保留指定长度的整数
	 * cutTopNumber(7123456, 3) -->456
	 * @param n 目标数
	 * @param len 保留的长度
	 * @return
	 */
	public static Integer cutTopNumber(Integer n, int len){
		if (n == null)	return -1;
		String ns = String.valueOf(n) ;
		
		if(ns.length() <= len)
			return n ;
		
		return Integer.parseInt(ns.substring(ns.length()-len, ns.length())) ;	
	}
	/**
	 * 对集合里面的数做 逻辑（|）的操作并返回
	 * @param ns Number...集合
	 * @return Number
	 */
	public static Number logic(Number... ns){
		Number n = 0 ;
		for (Number number : ns) {
			n = n.intValue() | number.intValue() ;
		}
		return n ;
	}
	/**
	 * 对集合里面的数做 逻辑（|）的操作并返回
	 * @param nl List<Number>集合
	 * @return Number
	 */
	public static Number logic(List<? extends Number> nl){
		Number n = 0 ;
		for (Number number : nl) {
			n = n.intValue() | number.intValue() ;
		}
		return n ;
	}
	
	/**
	 * 
	 * t （逻辑 |）是否在 t2 里面
	 * @param t
	 * @param t2
	 * @return
	 */
	public static boolean logicIsContain(Integer t, Integer t2) {
		if(t == null) return false;
		if(t < 0 || t2 < 0) return false;
		return (t & t2) > 0;
	}
	
	public static Number random(){
		return random(6);
	}
	
	public static Number random(int c){
		if (c < 1 || c > 18) {
			throw new IllegalArgumentException(c + "的大小不在1-18之间.");
		}
		
		Random r = new Random(); 
		double nextDouble = r.nextDouble();
		while(nextDouble < 0.1) {
			nextDouble = r.nextDouble();
		}
		
		double pow = Math.pow(10, c);
		long rannum = (long) (nextDouble * pow); //获取随机数
		
		return rannum ;
	}
	
	public static Number random(int c, Number min, Number max) {

		numberIsValid(c, min, max);

		Number number = random(c);

		if (number.doubleValue() >= min.doubleValue() && number.doubleValue() <= max.doubleValue()) {
			return number;
		}

		return random(c, min, max);
	}
	
	/**
	 * 随机生成一个正整数,大小区间在（min - max）
	 * @param min
	 * @param max
	 * @return
	 * @author Andy
	 * @created 2017年8月28日 下午1:19:10
	 */
	public static int random(int min, int max) {

		if (min < 0 || max < 0) {
			throw new IllegalArgumentException("不能生成无效的数字.");
		}

		if (min > max) {
			throw new IllegalArgumentException("最小值不能大于最大值.");
		}

		Random r = new Random();
		int nextInt = r.nextInt(max);

		if (nextInt < min) {
			return random(min, max);
		}

		return nextInt;
	}
	
	public static void numberIsValid(int c, Number min, Number max) {

		if (min == null || max == null) {
			throw new IllegalArgumentException("max,min不能为空.");
		}

		int minLen = min.toString().length();
		int maxLen = max.toString().length();

		if (c >= minLen && c <= maxLen) {
			return;
		}

		throw new IllegalArgumentException("当前长度为" + c + "最小值与最大值长度分别为" + minLen + "," + maxLen);
	}
	
	
	public static double setDecimal(double d, int len) {
		double rate = Math.pow(10, len);
		return ((long) (d * rate)) / rate;
	}
	
	public static long parseLong(String str, long defaultValue){
		try {
			return Long.parseLong(str);
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	public static long parseLong(Object obj, long defaultValue) {

		if (obj == null) {
			return defaultValue;
		}

		if (obj instanceof Number) {
			return ((Number) obj).longValue();
		}

		return parseLong(obj.toString(), defaultValue);
	}
	
	public static long parseLong(String str){
		return parseLong(str, -1);
	}
	
	public static int parseInt(String str, int defaultInt){
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			return defaultInt;
		}
	}
	
	public static int parseInt(String str){
		return parseInt(str, -1);
	}
	
	public static double parseDouble(String str, double defaultDouble){
		try {
			return Double.parseDouble(str);
		} catch (Exception e) {
			return defaultDouble;
		}
	}
	
	public static double parseDouble(String str){
		return parseDouble(str, 0d);
	}
	
	public static float parseFloat(String str, float defaultFloat){
		try {
			return Float.parseFloat(str);
		} catch (Exception e) {
			return defaultFloat;
		}
	}
	
	public static float parseFloat(String str){
		return parseFloat(str, 0F);
	}
	
	
	/**
	 * 验证数字是否在有效区间范围
	 * @param intVal
	 * @param min
	 * @param max
	 * @return
	 * @created 2017年4月17日 下午6:02:15
	 */
	public static boolean sizeIsValid(Number intVal, Number min, Number max) {

		if (intVal == null) {
			return false;
		}

		if (intVal.doubleValue() < min.doubleValue() || intVal.doubleValue() > max.doubleValue()) {
			return false;
		}

		return true;
	}
	
	public static boolean sizeIsNotValid(Number intVal, Number min, Number max) {

		return !sizeIsValid(intVal, min, max);
	}
	
	/**
	 * 该方法不支持float与其他数据类型比较
	 * @param n1
	 * @param n2
	 * @return
	 * @author Andy
	 * @created 2017年7月14日 下午9:57:58
	 */
	public static boolean equals(Number n1, Number n2) {
		if (n1 == null || n2 == null) {
			return false;
		}

		if (n1.getClass().equals(n2.getClass())) {
			return n1.equals(n2);
		}

		if (n1 instanceof Float || n2 instanceof Float) {
			throw new IllegalArgumentException("该方法不支持float类型比较.");
		}

		BigDecimal b1 = new BigDecimal(String.valueOf(n1));
		BigDecimal b2 = new BigDecimal(String.valueOf(n2));

		return b1.compareTo(b2) == 0;
	}
	
	/**
	 * n1=null,n2=null => true
	 * @param n1
	 * @param n2
	 * @return
	 */
	public static boolean notEquals(Number n1, Number n2) {
		return !equals(n1, n2);
	}
}
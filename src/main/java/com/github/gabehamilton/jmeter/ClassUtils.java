package com.github.gabehamilton.jmeter;

import org.spockframework.runtime.model.FeatureMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gabe on 1/27/15.
 */
public class ClassUtils {
	public static List<Method> getTestMethods(Class clazz) {
		return getMethodsWithAnnotation(clazz, FeatureMetadata.class);
	}
	public static List<Method> getMethodsWithAnnotation(Class clazz, Class<? extends Annotation> annotation) {
		List<Method> result = new ArrayList<Method>();
		if(null != clazz && null != annotation) {
			for(Method m : clazz.getMethods()) {
				if(m.isAnnotationPresent(annotation)) {
					result.add(m);
				}
			}
		}
		return result;
	}

	public static Method getSpockTestMethod(Class clazz, String spockNameOfTest) {
		Class<? extends Annotation> annotation = FeatureMetadata.class;
		if(null != clazz && null != annotation) {
			for(Method m : clazz.getMethods()) {
				if(m.isAnnotationPresent(annotation)) {
					if(((FeatureMetadata)m.getAnnotation(annotation)).name().equals(spockNameOfTest)) {
						return m;
					}
				}
			}
		}
		return null;
	}

}

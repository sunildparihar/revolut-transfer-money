package com.revolut.core.fundstransfer.gateway;

import com.revolut.sdk.fundstransfer.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This Class usues reflection to find the actual service implementation method based on following
 *
 *  1. The Class Type of the Service Interface
 *  2. The invoking Method name of the Service Interface
 *  3. The actual parameters to be passed to the method, in the same order as declared in the method
 *
 *  This class caches the found methods in order to avoid calling redundant refection logic every time and for increasing performance.
 *
 */
public class ReflectionUtils {

    private static ConcurrentHashMap<Class, ConcurrentHashMap<String, List<Method>>> classType2MethodName2MethodsMap
            = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Class, ConcurrentHashMap<String, ConcurrentHashMap<String, Method>>> classType2MethodName2ParamTypes2MethodMap
            = new ConcurrentHashMap<>();

    public static Method findMatchingMethodUsingReflection(Class serviceClassType, String methodName, Object... arguments) throws ServiceException {

        ConcurrentHashMap<String, List<Method>> methodNameToMethodsMap
                = classType2MethodName2MethodsMap.get(serviceClassType);
        ConcurrentHashMap<String, ConcurrentHashMap<String, Method>> methodName2ParamTypes2MethodMap
                = classType2MethodName2ParamTypes2MethodMap.get(serviceClassType);

        if (methodNameToMethodsMap == null) {
            methodNameToMethodsMap = new ConcurrentHashMap<>();
            classType2MethodName2MethodsMap.putIfAbsent(serviceClassType, methodNameToMethodsMap);

            methodName2ParamTypes2MethodMap = new ConcurrentHashMap<>();
            classType2MethodName2ParamTypes2MethodMap.putIfAbsent(serviceClassType, methodName2ParamTypes2MethodMap);

        }

        List<Method> matchingMethods = methodNameToMethodsMap.get(methodName);
        ConcurrentHashMap<String, Method> paramTypes2MethodMap = methodName2ParamTypes2MethodMap.get(methodName);

        if (CollectionUtils.isEmpty(matchingMethods)) {
            Method[] allMethods = serviceClassType.getDeclaredMethods();
            matchingMethods = new ArrayList<>();
            for (Method method : allMethods) {
                if (method.getName().equals(methodName)) {
                    matchingMethods.add(method);
                }
            }
            methodNameToMethodsMap.putIfAbsent(methodName, matchingMethods);
            if (matchingMethods.size() > 1) {
                paramTypes2MethodMap = new ConcurrentHashMap<>();
                for (Method method : matchingMethods) {
                    String methodParamTypeNames = constructClassTypeString(method.getParameterTypes());
                    paramTypes2MethodMap.putIfAbsent(methodParamTypeNames, method);
                }
                methodName2ParamTypes2MethodMap.putIfAbsent(methodName, paramTypes2MethodMap);
            }
        }

        Method matchingMethod = null;
        if (matchingMethods.size() == 1) {
            matchingMethod = matchingMethods.get(0);
        } else if (matchingMethods.size() > 1){
            String inputParamTypeNames = constructClassTypeString(arguments);
            matchingMethod = paramTypes2MethodMap.get(inputParamTypeNames);
        }

        if (matchingMethod == null) {
            throw new ServiceException("No Matching service implementation found");
        }

        return matchingMethod;
    }

    private static String constructClassTypeString(Object... parameters) {
        StringBuilder classNamesString = new StringBuilder();
        for (Object obj : parameters) {
            classNamesString.append(obj.getClass().getCanonicalName()).append(",");
        }

        return classNamesString.toString();
    }

    private static String constructClassTypeString(Class... parameters) {
        StringBuilder classNamesString = new StringBuilder();
        for (Class classType : parameters) {
            if (classType.isPrimitive()) {
                classType = ClassUtils.primitiveToWrapper(classType);
            }
            classNamesString.append(classType.getCanonicalName()).append(",");
        }

        return classNamesString.toString();
    }
}

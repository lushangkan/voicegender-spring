package cn.cutemc.voicegender.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

public class AspectUtils {
    /**
     * 从 JoinPoint 中获取指定参数名的参数值
     *
     * @param joinPoint JoinPoint
     * @param paramName 参数名
     * @param clazz 参数类型
     * @return 参数对象
     * @author Lishuzhen (from <a href="https://blog.csdn.net/lishuzhen5678/article/details/109719660">...</a>)
     */
    public static <T> T getParamByName(JoinPoint joinPoint, String paramName, Class<T> clazz) {
        // 获取所有参数的值
        Object[] args = joinPoint.getArgs();
        // 获取方法签名
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        // 在方法签名中获取所有参数的名称
        String[] parameterNames = methodSignature.getParameterNames();
        // 根据参数名称拿到下标， 参数值的数组和参数名称的数组下标是一一对应的
        int index = ArrayUtils.indexOf(parameterNames, paramName);
        // 在参数数组中取出下标对应参数值
        Object obj = args[index];

        if (obj == null) {
            return null;
        }

        // 将object对象转为Class返回
        if (clazz.isInstance(obj)) {
            return clazz.cast(obj);
        }

        throw new RuntimeException("Parameter type mismatch");
    }
}

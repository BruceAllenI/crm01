package com.xxxx.crm.aspect;

import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageRowBounds;
import com.xxxx.crm.annoation.RequiredPermission;
import com.xxxx.crm.exceptions.NoLoginException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

@Component
@Aspect
public class PermissionProxy {

    @Resource
    private HttpSession session;

    @Around(value = "@annotation(com.xxxx.crm.annoation.RequiredPermission)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        List<String> permissions = (List<String>) session.getAttribute("permissions");
        if (null == permissions || permissions.size() == 0) {
            throw new NoLoginException();
        }
        Object result = null;
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        RequiredPermission requirePermission = methodSignature.getMethod().getDeclaredAnnotation(RequiredPermission.class);
        if (!(permissions.contains(requirePermission.code()))) {
            throw new NoLoginException();
        }
        result = pjp.proceed();
        return result;

    }
}
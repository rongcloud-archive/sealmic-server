package cn.rongcloud.permission;

import cn.rongcloud.pojo.RoleEnum;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DeclarePermissions {
	public RoleEnum[] value() default {};
}

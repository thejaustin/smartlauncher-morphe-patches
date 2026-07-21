package app.revanced.patcher.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CompatiblePackage {
    String name() default "";
    String[] versions() default {};
}

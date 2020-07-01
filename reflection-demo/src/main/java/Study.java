import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 于露
 */
@Target({ElementType.FIELD,ElementType.TYPE}) //元注解，定义注解的修饰范围，可以设置多个
@Retention(RetentionPolicy.RUNTIME) //元注解，定义注解的生命周期
public @interface Study { //注解内部可以设置值，也可以不设置

    String name() default "yulu";

    String[] mores();
}

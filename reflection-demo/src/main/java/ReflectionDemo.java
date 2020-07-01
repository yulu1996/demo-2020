import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * 反射：在运行状态中，对于任意一个类，都能够知道这个类的所有属性和方法；
 * 对于任意一个对象，都能够调用它的任意一个方法和属性；
 * 这种动态获取的信息以及动态调用对象的方法的功能成为Java语言的反射。
 *
 * 反射提高了Java程序的灵活性和扩展性，降低耦合性，提高自适应能力
 * 反射会对性能造成一定的影响（ms级），同时让代码的可读性变低。
 *
 * @author 于露
 */
public class ReflectionDemo {
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        //实例1：通过反射获取类元信息 1
        Person person = new Person();
        Class<? extends Person> aClass = person.getClass();
        System.out.println(aClass);
        //实例1：通过反射获取类元信息 2
        Class<?> aClass1 = Class.forName("Person");
        System.out.println(aClass1);

        //实例2：通过反射获取类名、包名
        String name = aClass1.getName();
        String simpleName = aClass1.getSimpleName();
        //包名+类名
        System.out.println(name);
        //类名
        System.out.println(simpleName);

        //实例3：类属性
        Field[] declaredFields = aClass1.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            System.out.println(declaredField);
        }

        //实例4：获取类属性具体的值
        person.setName("yulu");
        person.setAge(18);
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            System.out.println(declaredField.get(person));
        }

        //实例4的另一种写法
        Object p = aClass.newInstance();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            if ("name".equals(declaredField.getName())){
                declaredField.set(p,"yulu");
            }else {
                declaredField.set(p,18);
            }
            System.out.println(declaredField.get(p));
        }

        //实例5：反射获取当前类的方法
        Method[] methods = aClass.getMethods();
        for (Method method : methods) {
            System.out.println(method.getName());
        }

        Method method = aClass.getMethod("getAge");
        //反射执行方法
        Object invoke = method.invoke(p);
        System.out.println(invoke);

        //实例6：获得注解
        Study study = aClass.getAnnotation(Study.class);
        System.out.println(study);
        String[] mores = study.mores();
        for (String more : mores) {
            System.out.println(more);
        }
        String name1 = study.name();
        System.out.println(name1);

    }
}

package quoters;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class ProfilingHandlerBeanPostProcessor implements BeanPostProcessor {

    private Map<String, Class> map = new HashMap<String, Class>();
    private ProfilingController controller = new ProfilingController();

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        if(beanClass.isAnnotationPresent(Profiling.class)){
            map.put(beanName,beanClass);
        }
        return bean;
    }


    public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
        Class beanClass = map.get(beanName);
        if(beanClass != null){
            return Proxy.newProxyInstance(beanClass.getClassLoader(), beanClass.getInterfaces(), new InvocationHandler() {

                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if(controller.isEnabled()){
                    System.out.println("Profiling...");
                    long before = System.nanoTime();
                    Object retVal = method.invoke(bean, args);
                    long after = System.nanoTime();
                    System.out.println(after - before);
                    System.out.println("That's all");
                    return retVal;
                    }else {
                        return method.invoke(bean,args);
                    }
                }
            });

        }

        return bean;
    }
}

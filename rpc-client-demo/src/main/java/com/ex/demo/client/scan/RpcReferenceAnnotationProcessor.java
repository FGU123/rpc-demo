package com.ex.demo.client.scan;

import java.beans.PropertyDescriptor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.ex.demo.annotation.RpcReference;
import com.ex.demo.client.proxy.SeviceConsumerProxy;

/**
 * 
 * to find classes with RpcReference Annotation 
 * and then inject into a field where this rpcReferenced class is being dependent on
 *
 */
@Component
public class RpcReferenceAnnotationProcessor extends InstantiationAwareBeanPostProcessorAdapter {

	@Override
	public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean,
			String beanName) throws BeanCreationException {

		InjectionMetadata metadata = findInjectionMetadata(beanName, bean.getClass(), pvs);
		try {
			metadata.inject(bean, beanName, pvs);
		} catch (BeanCreationException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new BeanCreationException(beanName, "Injection of @RpcReference dependencies is failed", ex);
		}
		return pvs;
	}

	private InjectionMetadata findInjectionMetadata(String beanName, Class<? extends Object> clazz,
			PropertyValues pvs) {
		List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
		Class<?> targetClass = clazz;

		do {
			final List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();

			ReflectionUtils.doWithLocalFields(targetClass, field -> {
				AnnotationAttributes ann = findRpcReferenceAnnotation(field);
				if (ann != null) {
					if (Modifier.isStatic(field.getModifiers())) {
						return;
					}
					currElements.add(new AnnotatedFieldElement(field));
				}
			});

			elements.addAll(0, currElements);
			targetClass = targetClass.getSuperclass();
		}
		while (targetClass != null && targetClass != Object.class);

		return new InjectionMetadata(clazz, elements);
	}
	
	private AnnotationAttributes findRpcReferenceAnnotation(AccessibleObject ao) {
		AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(ao, RpcReference.class);
		if (attributes != null) {
			return attributes;
		}
		return null;
	}

	public class AnnotatedFieldElement extends InjectionMetadata.InjectedElement {

        private final Field field;

        protected AnnotatedFieldElement(Field field) {
            super(field, null);
            this.field = field;
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {

            Class<?> injectedType = field.getType();

            Object injectedObject = createProxy(injectedType);

            ReflectionUtils.makeAccessible(field);

            field.set(bean, injectedObject);

        }

    }
	
	public Object createProxy(Class<?> serviceClass) {
		return SeviceConsumerProxy.getInstance().createProxy(serviceClass);
	}
}

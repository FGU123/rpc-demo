package com.ex.demo.server.scan;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import com.ex.demo.annotation.RpcService;
import com.ex.demo.server.global.Environment;

import cn.hutool.core.util.ArrayUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Actual Scanner Registrar implemention to scan base-packages to find out all
 * the classes with annotations specified
 */
@Slf4j
@Configuration
public class RpcComponentScanRegistrar implements ImportBeanDefinitionRegistrar {

	private static final String RESOURCE_PATTERN = "/**/*.class";

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		Set<String> packagesToScan = getPackagesToScan(importingClassMetadata);
		for (String basePackage : packagesToScan) {
			scanAnnotation(basePackage);
		}
	}

	private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
		AnnotationAttributes attributes = AnnotationAttributes
				.fromMap(metadata.getAnnotationAttributes(RpcComponentScan.class.getName()));
		String[] basePackages = attributes.getStringArray("basePackages");
		Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");
		String[] value = attributes.getStringArray("value");

		Set<String> packagesToScan = new LinkedHashSet<String>(Arrays.asList(value));
		packagesToScan.addAll(Arrays.asList(basePackages));
		for (Class<?> basePackageClass : basePackageClasses) {
			packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
		}
		if (packagesToScan.isEmpty()) {
			return Collections.singleton(ClassUtils.getPackageName(metadata.getClassName()));
		}
		return packagesToScan;
	}

	private void scanAnnotation(String basePackage) {
		Map<String, Object> serviceBeans = new HashMap<>();
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		// 扫描的包名
		try {
			String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
					+ ClassUtils.convertClassNameToResourcePath(basePackage) + RESOURCE_PATTERN;
			Resource[] resources = resourcePatternResolver.getResources(pattern);
			MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
			for (Resource resource : resources) {
				if (resource.isReadable()) {
					MetadataReader reader = readerFactory.getMetadataReader(resource);
					// 扫描到的class
					String className = reader.getClassMetadata().getClassName();
					Class<?> clazz = Class.forName(className);
					// 判断是否有指定注解
					RpcService annotation = clazz.getAnnotation(RpcService.class);
					if (annotation != null) {
						// 这里只使用该类implement的第一个interface作为key，多重接口实现的话要注意
						Class<?>[] interfaces = clazz.getInterfaces();
						if (ArrayUtil.isNotEmpty(interfaces)) {
							serviceBeans.put(interfaces[0].getName(), clazz.newInstance());
						}
					}
				}
			}
			Environment.getServiceBeans().putAll(serviceBeans);
			log.info("scanned rpc services: " + serviceBeans.toString());
		} catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			log.error("rpc component scan error", e);
		}
	}
}

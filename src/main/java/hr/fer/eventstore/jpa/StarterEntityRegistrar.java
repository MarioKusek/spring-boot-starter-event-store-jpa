package hr.fer.eventstore.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class StarterEntityRegistrar implements ImportBeanDefinitionRegistrar {
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    logger.debug("Registring EventJpaRepository");
    AutoConfigurationPackages.register(registry, EventJpaRepository.class.getPackageName());
  }
}
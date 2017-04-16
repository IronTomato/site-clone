package com.irontomato.siteclone.retriable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

@Component
public class WebResourceDowloadRetriableFactory implements BeanFactoryAware{
    private BeanFactory beanFactory;

    public WebResourceDownloadRetriable create(String url) {
        WebResourceDownloadRetriable retriable = beanFactory.getBean(WebResourceDownloadRetriable.class);
        retriable.setUrl(url);
        return retriable;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}

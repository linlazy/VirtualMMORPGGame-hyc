package com.mmorpg.mbdl.framework.resource.config;

import com.mmorpg.mbdl.framework.resource.core.StaticResHandler;
import com.mmorpg.mbdl.framework.resource.facade.IStaticRes;
import com.mmorpg.mbdl.framework.resource.impl.StaticRes;
import org.springframework.context.annotation.Bean;

/**
 * 静态资源配置类
 *
 * @author Sando Geek
 * @since v1.0
 **/
public class StaticResConfiguration {
    private String packageToScan;
    private String suffix = ".xlsx";
    private Class<? extends IStaticRes> baseClass = StaticRes.class;

    /**
     * 定义扫包路径
     * @param packageToScan ResDef注解的类所在包路径
     */
    public void setPackageToScan(String packageToScan) {
        this.packageToScan = packageToScan;
    }

    /**
     * 默认的静态资源格式后缀名
     * @param suffix 后缀名，如07版excel，则填写xlsx
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    // /**
    //  * IStaticRes基础实现类
    //  * @param baseClass
    //  */
    // public void setBaseClass(Class<IStaticRes> baseClass) {
    //     this.baseClass = baseClass;
    // }

    @Bean
    StaticResHandler iStaticResRegistrer(){
        StaticResHandler iStaticResRegistrer = new StaticResHandler();
        iStaticResRegistrer.setBaseClass(baseClass);
        iStaticResRegistrer.setPackageToScan(packageToScan);
        iStaticResRegistrer.setSuffix(suffix);
        return iStaticResRegistrer;
    }
}

package com.boco.power.builder;

import com.boco.power.constant.PackageConfig;
import com.boco.power.constant.ProjectConfig;
import com.boco.power.database.DbProvider;
import com.boco.power.factory.DbProviderFactory;

/**
 * Created by yu on 2016/12/10.
 */
public abstract class AbstractCodeWriter {

    /**
     * 数据库信息
     */
    private DbProvider dataBaseInfo;

    /**
     * 包配置
     */
    private PackageConfig packageConfig;

    /**
     * 工程配置
     */
    private ProjectConfig projectConfig;


    /**
     *
     */
    protected ConfigBuilder config;

    /**
     * 初始化配置
     */
    protected void initConfig() {
        if (null == config) {
            dataBaseInfo = new  DbProviderFactory().getInstance();
            config = new ConfigBuilder(dataBaseInfo,packageConfig,projectConfig);

        }
    }

    public DbProvider getDataBaseInfo() {
        return dataBaseInfo;
    }

    public void setDataBaseInfo(DbProvider dataBaseInfo) {
        this.dataBaseInfo = dataBaseInfo;
    }

    public PackageConfig getPackageConfig() {
        return packageConfig;
    }

    public void setPackageConfig(PackageConfig packageConfig) {
        this.packageConfig = packageConfig;
    }

    public ProjectConfig getProjectConfig() {
        return projectConfig;
    }

    public void setProjectConfig(ProjectConfig projectConfig) {
        this.projectConfig = projectConfig;
    }

    public ConfigBuilder getConfig() {
        return config;
    }

    public void setConfig(ConfigBuilder config) {
        this.config = config;
    }
}
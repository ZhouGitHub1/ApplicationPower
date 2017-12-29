package com.boco.power.builder;

import com.boco.common.util.FileUtil;
import com.boco.common.util.StringUtil;
import com.boco.power.constant.ConstVal;
import com.boco.power.constant.GeneratorConstant;
import com.boco.power.constant.SpringBootProjectConfig;
import com.boco.power.database.Column;
import com.boco.power.database.DbProvider;
import com.boco.power.database.TableInfo;
import com.boco.power.factory.DbProviderFactory;
import com.boco.power.utils.BeetlTemplateUtil;
import com.boco.power.utils.GeneratorProperties;
import com.boco.power.utils.PathUtil;
import com.boco.power.utils.PropertiesUtils;
import org.beetl.core.Template;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author on 2016/12/7.
 */
public class CodeWriter extends AbstractCodeWriter {


    public void execute() {
        //初始化配置
        initConfig();
        //java代码目录
        mkdirs(config.getPathInfo());
        //创建配置文件路径
        mkdirs(config.getBaseConfigPathInfo());
        //创建配置文件
        writeBaseConfig(config.getBaseConfigFilesPath());
        //创建代码
        writeCode(config);
        //创建项目所需基础类
        writeBaseCode(config,false);
    }

    public void executeSpringBoot() {
        //初始化配置
        initSpringBootConfig();
        //java代码目录
        mkdirs(config.getPathInfo());
        //创建配置文件路径
        mkdirs(config.getBaseConfigPathInfo());
        //创建配置文件
        writeBaseConfig(config.getBaseConfigFilesPath());
        //创建代码
        writeCode(config);
        //创建项目所需基础类
        writeSpringBootBaseCode(config);
        //创建assembly配置
        writeAssemblyConfig(config,new SpringBootProjectConfig());

        writeDbSourceAndJTACode(config,new SpringBootProjectConfig());

    }

    /**
     * 处理输出目录
     *
     * @param pathInfo 路径信息
     */
    private void mkdirs(Map<String, String> pathInfo) {
        for (Map.Entry<String, String> entry : pathInfo.entrySet()) {
            File dir = new File(entry.getValue());
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }

    /**
     * 创建工程所需配置文件
     *
     * @param baseConfigFiles web工程所需的配置文件
     */
    private void writeBaseConfig(Map<String, String> baseConfigFiles) {
        String basePackage = GeneratorProperties.basePackage();
        PropertiesUtils dbProp = new PropertiesUtils("jdbc.properties");
        Template template;
        String key;
        for (Map.Entry<String, String> entry : baseConfigFiles.entrySet()) {
            key = entry.getKey();
            if (ConstVal.TPL_JDBC.equals(key)) {
                String currentPath = Thread.currentThread().getContextClassLoader().getResource(ConstVal.JDBC).getPath();
                FileUtil.nioTransferCopy(new File(currentPath), new File(entry.getValue()));
            } else {
                template = BeetlTemplateUtil.getByName(key);
                template.binding(GeneratorConstant.BASE_PACKAGE, basePackage);
                template.binding(GeneratorConstant.APPLICATION_NAME, GeneratorProperties.applicationName());
                //spring config
                template.binding("mappingDir", basePackage.replaceAll("[.]", "/"));
                template.binding("jdbcUrl", "${jdbc.url}");
                template.binding("jdbcUserName", "${jdbc.username}");
                template.binding("jdbcPassword", "${jdbc.password}");
                //pom
                template.binding("projectVersion", "${project.version}");
                template.binding("springVersion", "${spring.version}");
                template.binding("mybatisVersion", "${mybatis.version}");
                template.binding("jacksonVersion", "${jackson.version}");
                template.binding("slf4jVersion", "${slf4j.version}");
                template.binding("log4j2Version", "${log4j2.version}");
                template.binding("useAssembly",GeneratorProperties.getAssembly());
                template.binding("useJTA",GeneratorProperties.isJTA());
                template.binding("isMultipleDataSource",GeneratorProperties.isMultipleDataSource());
                //log4j2
                template.binding("LOG_HOME", "${LOG_HOME}");
                template.binding("LOG_PATH","${sys:logging.path}");
                template.binding("CATALINA_HOME", "${CATALINA_HOME}");
                //mybatis config
                template.binding("cacheEnabled", GeneratorProperties.enableCache());

                //SpringBoot yml
                template.binding("dbUrl", dbProp.getProperty("jdbc.url"));
                template.binding("dbUserName", dbProp.getProperty("jdbc.username"));
                template.binding("dbPassword", dbProp.getProperty("jdbc.password"));
                template.binding("dbDriver", dbProp.getProperty("jdbc.driver"));
                template.binding("list",GeneratorProperties.getMultipleDataSource());
                template.binding("isJTA",GeneratorProperties.isJTA());
                FileUtil.writeFileNotAppend(template.render(), entry.getValue());
            }
        }
    }

    /**
     * base code是项目的一些基本类，包括单元测基类
     * 时间转换类
     *
     * @param config
     */
    private void writeBaseCode(ConfigBuilder config,boolean isSprinboot) {
        String basePackage = GeneratorProperties.basePackage();
        Map<String, String> dirMap = config.getPathInfo();
        for (Map.Entry<String, String> entry : dirMap.entrySet()) {
            String value = entry.getValue();
            String key = entry.getKey();
            if (ConstVal.SERVICE_TEST_PATH.equals(key)) {
                String templateName = ConstVal.TPL_SERVICE_BASE_TEST;
                if(isSprinboot){
                    templateName = ConstVal.TPL_SPRING_BOOT_SERVICE_BASE_TEST;
                }
                Template template = BeetlTemplateUtil.getByName(templateName);
                template.binding(GeneratorConstant.COMMON_VARIABLE);
                FileUtil.writeFileNotAppend(template.render(), value + "\\ServiceBaseTest.java");
            }
            if (ConstVal.CONTROLLER_TEST_PATH.equals(key)) {
                String templateName = ConstVal.TPL_CONTROLLER_BASE_TEST;
                if(isSprinboot){
                    templateName = ConstVal.TPL_SPRING_BOOT_CONTROLLER_BASE_TEST;
                }
                Template template = BeetlTemplateUtil.getByName(templateName);
                template.binding(GeneratorConstant.COMMON_VARIABLE);
                FileUtil.writeFileNotAppend(template.render(), value + "\\ControllerBaseTest.java");
            }
            if (ConstVal.DATE_CONVERTER_PATH.equals(key)) {
                Template template = BeetlTemplateUtil.getByName(ConstVal.TPL_DATE_CONVERTER);
                template.binding(GeneratorConstant.COMMON_VARIABLE);
                FileUtil.writeFileNotAppend(template.render(), value + "\\DateConverter.java");
            }
            if(ConstVal.REST_ERROR_PATH.contains(key)){
                Template template = BeetlTemplateUtil.getByName(ConstVal.TPL_REST_ERROR);
                template.binding(GeneratorConstant.COMMON_VARIABLE);
                FileUtil.writeFileNotAppend(template.render(), value + "\\RestExceptionHandler.java");
            }
        }
    }

    /**
     * 创建SpringBoot的基础类代码
     *
     * @param config
     */
    private void writeSpringBootBaseCode(ConfigBuilder config) {
        String basePackage = GeneratorProperties.basePackage();
        writeBaseCode(config,true);
        //创建启动的主类
        Template template = BeetlTemplateUtil.getByName(ConstVal.TPL_SPRING_BOOT_MAIN);
        template.binding(GeneratorConstant.COMMON_VARIABLE);
        String basePackagePath = PathUtil.joinPath(config.getProjectPath().getJavaSrcPath(), basePackage);
        FileUtil.writeFileNotAppend(template.render(), basePackagePath + "\\SpringBootMainApplication.java");
    }

    private void writeDbSourceAndJTACode(ConfigBuilder config, SpringBootProjectConfig projectConfig){
        String basePackage = GeneratorProperties.basePackage();
        Map<String, String> dirMap = config.getPathInfo();
        Set<String> dataSources = GeneratorProperties.getMultipleDataSource();
        if(GeneratorProperties.isJTA()||dataSources.size()>0){
            Template jtaTpl = BeetlTemplateUtil.getByName(ConstVal.TPL_JTA);
            jtaTpl.binding(GeneratorConstant.COMMON_VARIABLE);
            FileUtil.writeFileNotAppend(jtaTpl.render(),dirMap.get(ConstVal.DATA_SOURCE_FIG)+"\\TransactionManagerConfig.java");
        }
        if(dataSources.size()>0){
            String configPath = dirMap.get(ConstVal.DATA_SOURCE_FIG);
            Template aspectTpl = BeetlTemplateUtil.getByName(ConstVal.TPL_DATASOURCE_ASPECT);
            aspectTpl.binding(GeneratorConstant.COMMON_VARIABLE);
            FileUtil.writeFileNotAppend(aspectTpl.render(),dirMap.get(ConstVal.ASPECT)+"\\DbAspect.java");

            DataSourceKeyBuilder sourceKeyBuilder = new DataSourceKeyBuilder();
            String  dataSourceTpl = sourceKeyBuilder.builderDataSourceKey(dataSources);
            FileUtil.writeFileNotAppend(dataSourceTpl,dirMap.get(ConstVal.CONSTANTS)+"\\DataSourceKey.java");

            Template abstractCfg = BeetlTemplateUtil.getByName(ConstVal.TPL_DATASOURCE_CFG);
            abstractCfg.binding(GeneratorConstant.COMMON_VARIABLE);
            FileUtil.writeFileNotAppend(abstractCfg.render(),configPath+"\\AbstractDataSourceConfig.java");

            SpringBootMybatisCfgBuilder builder = new SpringBootMybatisCfgBuilder();
            String mybatisCfgTpl = builder.createMybatisCfg(dataSources);
            FileUtil.writeFileNotAppend(mybatisCfgTpl,configPath+"\\MyBatisConfig.java");
        }
    }
    private void writeAssemblyConfig(ConfigBuilder config, SpringBootProjectConfig projectConfig){
        if(GeneratorProperties.getAssembly()){
            Map<String,String> configPath = config.getBaseConfigPathInfo();
            //创建脚本
            String binPath = configPath.get(ConstVal.ASSEMBLY_BIN);
            Map<String,String> scripts = new ScriptBuilder().generateScripts();
            for(Map.Entry<String,String> entry:scripts.entrySet()){
                FileUtil.writeFileNotAppend(entry.getValue(),binPath+"\\"+entry.getKey());
            }
            //复制assembly.xml
            String assemblyRoot = configPath.get(ConstVal.ASSEMBLY_DIR);
            String assemblyXml = Thread.currentThread().getContextClassLoader().getResource(ConstVal.TPL_ASSEMBLY_XML).getPath();
            FileUtil.nioTransferCopy(new File(assemblyXml), new File(assemblyRoot+"\\assembly.xml"));
            //拷贝配置文件
            String basePath = config.getProjectPath().getBasePath();

            String ymlPath = PathUtil.connectPath(basePath, projectConfig.getApplicationYmlAssembly());
            FileUtil.nioTransferCopy(new File(PathUtil.connectPath(basePath,projectConfig.getApplicationYml())),new File(ymlPath));
            String log4j2Path = PathUtil.connectPath(basePath,projectConfig.getLog4j2Assembly());
            FileUtil.nioTransferCopy(new File(PathUtil.connectPath(basePath,projectConfig.getLog4j2())),new File(log4j2Path));
        }
    }

    /**
     * 生成model,dao,service,controller,controllerTest,serviceTest代码
     *
     * @param config
     */
    private void writeCode(ConfigBuilder config) {
        Map<String, String> dirMap = config.getMybatisGenPath();
        List<TableInfo> tables = config.getTableInfo();
        DbProvider dbProvider = new DbProviderFactory().getInstance();
        for (TableInfo tableInfo : tables) {
            String table = tableInfo.getName();
            Map<String, Column> columnMap = dbProvider.getColumnsInfo(table);
            //实体名需要移除表前缀
            String tableTemp = StringUtil.removePrefix(table, GeneratorProperties.tablePrefix());
            String entityName = StringUtil.toCapitalizeCamelCase(tableTemp);
            for (Map.Entry<String, String> entry : dirMap.entrySet()) {
                String value = entry.getValue();
                String key = entry.getKey();
                IBuilder builder = null;
                try {
                    builder = (IBuilder)Class.forName(key).newInstance();
                }catch (Exception e){
                    e.printStackTrace();
                }
                String template = builder.generateTemplate(tableInfo,columnMap);
                FileUtil.writeFileNotAppend(template, String.format(value,entityName));
            }
        }
    }
}

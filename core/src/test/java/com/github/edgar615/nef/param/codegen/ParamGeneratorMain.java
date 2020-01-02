package com.github.edgar615.nef.param.codegen;

import com.github.edgar615.jdbc.codegen.gen.CodegenOptions;
import com.github.edgar615.jdbc.codegen.gen.DaoGenerator;
import com.github.edgar615.jdbc.codegen.gen.DaoImplGenerator;
import com.github.edgar615.jdbc.codegen.gen.DaoOptions;
import com.github.edgar615.jdbc.codegen.gen.DomainGenerator;
import com.github.edgar615.jdbc.codegen.gen.DomainKitGenerator;
import com.github.edgar615.jdbc.codegen.gen.DomainOptions;
import com.github.edgar615.jdbc.codegen.gen.DomainRuleGenerator;
import com.github.edgar615.jdbc.codegen.gen.Generators;

public class ParamGeneratorMain {

  public static void main(String[] args) {
    CodegenOptions options = new CodegenOptions()
        .setUsername("course")//用户名
        .setPassword("Course@)19") //密码
        .setHost("rm-bp1wa03k4d2nro53neo.mysql.rds.aliyuncs.com") //数据库地址
        .setPort(3306) //端口，默认值3306
        .setDatabase("course") //数据库
        .setSrcFolderPath("core/src/main/java")//生成JAVA文件的存放目录
        .setDomainPackage("com.github.edgar615.nef.param.entity")//domain类的包名
        .setIgnoreColumnsStr("db_created_on,db_updated_on")
        .addGenTable("param_group")
        .addGenTable("param_def")
        .addGenTable("param_value")
        .addGenTable("param_option");
    Generators.create(options)
        .addGenerator(DomainGenerator.create(options, new DomainOptions()))
        .addGenerator(DomainKitGenerator.create(options))
        .addGenerator(DomainRuleGenerator.create(options))
        .addGenerator(DaoGenerator.create(options, new DaoOptions().setDaoPackage("com.github.edgar615.nef.param.dao")))
        .addGenerator(DaoImplGenerator.create(options, new DaoOptions().setDaoPackage("com.github.edgar615.nef.param.dao")))
        .generate();
  }
}

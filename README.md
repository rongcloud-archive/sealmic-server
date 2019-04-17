# SealMic-Server

## 项目介绍
* SealMic-Server 是基于 SpringBoot 框架实现
* 依赖于 MySql 的数据存储，Redis 的数据缓存
* 依赖融云 IM 服务，收发信令

## 使用方法
* 去融云官网注册、申请 AppKey 和 Secret
* 使用申请后的 AppKey 和 Secret 替换 [application.properties](https://github.com/rongcloud/sealmic-server/blob/master/src/main/resources/application.properties)下的 IM config
* 使用 mysql 执行项目目录下的 [tools/sealmic.sql](https://github.com/rongcloud/sealmic-server/blob/master/tools/sealmic.sql)，创建数据库
* 通过 mvn package 编译出 jar 或者 IntelliJ IDE 运行工程
* 通过 java -jar SealMic-1.0.0-SNAPSHOT.jar 启动服务，默认启用 9991 端口，默认是 HTTP 请求

## 设计文档
* [详细设计文档](https://github.com/rongcloud/sealmic-server/blob/master/tools/%E8%AE%BE%E8%AE%A1%E6%96%87%E6%A1%A3.md)

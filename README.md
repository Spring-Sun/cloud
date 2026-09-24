# cloud · 多模块 Spring Cloud 微服务脚手架

一个基于 **Spring Boot 4.1.0 + Spring Cloud 2025.1.3 + Spring Cloud Alibaba 2025.1.0.0** 的多模块 Maven 工程，
内置 Nacos 注册/配置中心、Gateway 统一网关、MyBatis-Plus、RabbitMQ、MySQL、Redis 以及基于 S3 协议的文件服务，
并已开启 **Spring AOT** 处理，可直接完成打包（并支持 GraalVM Native Image）。

## 技术栈与版本

| 组件 | 版本 / 说明 |
| --- | --- |
| spring-boot-dependencies | 4.1.0 |
| spring-cloud-dependencies | 2025.1.3 |
| spring-cloud-alibaba-dependencies | 2025.1.0.0 |
| JDK | 25（`java.version=25`、`maven.compiler.release=25`，本机 GraalVM JDK 25） |
| 持久层 | MyBatis-Plus 3.5.9（含 `mybatis-plus-jsqlparser`）+ MySQL |
| 缓存 | Redis（Lettuce） |
| 消息队列 | RabbitMQ（`spring-boot-starter-amqp`，JSON 消息 + 死信队列） |
| 注册/配置中心 | Nacos |
| 网关 | Spring Cloud Gateway（WebFlux，`spring-cloud-starter-gateway-server-webflux`） |
| 对象存储 | AWS SDK v2 S3（兼容 MinIO / Ceph / 阿里云 OSS S3 协议） |
| 鉴权 | JWT（jjwt 0.12.x），网关统一校验并向下游透传用户身份 |

## 模块结构

```
cloud (parent, pom)
├── cloud-common (pom 聚合)
│   ├── cloud-common-core        统一响应 R / PageResult、异常、全局异常处理、常量
│   ├── cloud-common-redis       RedisTemplate + RedisService 自动配置
│   ├── cloud-common-mybatis     MyBatis-Plus 分页/防全表更新、审计字段填充
│   ├── cloud-common-rabbitmq    JSON 消息转换器、业务交换机/队列/死信拓扑
│   └── cloud-common-security    JWT 工具、LoginUser、基于请求头的用户上下文（Servlet）
├── cloud-gateway                响应式网关：统一入口、路由、Token 校验（端口 8080）
├── cloud-auth                   认证服务：签发/注销 Token（端口 9200）
└── cloud-modules (pom 聚合)
    ├── cloud-system             业务示例：MyBatis-Plus CRUD + RabbitMQ 生产/消费（端口 9201）
    ├── cloud-file               文件服务：S3 协议上传/下载/删除，图片上传自动转存 WebP（端口 9300）
    └── cloud-job                定时任务：Spring Scheduling + Redis 分布式锁（端口 9400）
```

公共模块均通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
以**自动配置**方式装配，业务服务无需额外 `@ComponentScan` 即可生效；网关为响应式，
`cloud-common-security`（Servlet 专用）不会在网关中激活。

## 请求链路

```
Client → cloud-gateway(8080)
          │  AuthGlobalFilter 校验 Bearer Token
          │  校验通过后注入内部头 X-User-Id / X-Username（并剔除客户端伪造头）
          ├─ /auth/**    → lb://cloud-auth   (StripPrefix=1)
          ├─ /system/**  → lb://cloud-system (StripPrefix=1)
          ├─ /file/**    → lb://cloud-file   (StripPrefix=1)
          └─ /job/**     → lb://cloud-job    (StripPrefix=1)
```

下游服务通过 `UserContextInterceptor` 读取上述内部头，填充 `UserContext`。
网关鉴权白名单在 `cloud-gateway/src/main/resources/application.yml` 的
`cloud.gateway.auth.white-list` 中配置（如 `/auth/login`）。

## 构建与 AOT 打包

每个可运行服务都绑定了 `spring-boot-maven-plugin` 的两个目标：
`repackage`（生成可执行 Jar）与 `process-aot`（Spring AOT 处理）。

```bash
# 全量构建（含 AOT 处理 + 打包），产物位于各模块 target/*.jar
mvn clean package -DskipTests

# 仅构建某个服务及其依赖
mvn clean package -pl cloud-gateway -am -DskipTests

# 运行
java -jar cloud-gateway/target/cloud-gateway.jar
```

AOT 处理成功后会在各服务 `target/spring-aot/main/{classes,sources,resources}` 生成
AOT 代码与反射/资源提示（如 Nacos、MyBatis-Plus 相关的 hint）。

> **关于 AOT 与外部依赖**：`process-aot` 会启动一次应用上下文。为避免构建期强依赖基础设施，
> 所有服务的 Nacos 配置导入均使用 `optional:nacos:xxx.yaml` 前缀；数据源/Redis/RabbitMQ 连接
> 均为惰性建立。因此在 Nacos/MySQL/Redis/RabbitMQ **不可达时构建仍能成功**（日志中会出现
> Nacos 连接重试信息，属正常现象）。

### GraalVM Native Image（可选）

本机为 GraalVM，可直接编译原生镜像（需已安装 native-image 组件）：

```bash
mvn -Pnative native:compile -pl cloud-gateway -am -DskipTests
```

## 运行前置依赖

需自行准备并可通过环境变量覆盖地址（默认均为 `127.0.0.1`）：

| 依赖 | 环境变量 | 默认值 |
| --- | --- | --- |
| Nacos | `NACOS_SERVER_ADDR` / `NACOS_NAMESPACE` | `127.0.0.1:8848` / 空 |
| MySQL | `MYSQL_HOST` `MYSQL_PORT` `MYSQL_DB` `MYSQL_USER` `MYSQL_PASSWORD` | `127.0.0.1:3306/cloud_system` `root/root` |
| Redis | `REDIS_HOST` `REDIS_PORT` `REDIS_PASSWORD` | `127.0.0.1:6379` 无密码 |
| RabbitMQ | `RABBITMQ_HOST` `RABBITMQ_PORT` `RABBITMQ_USER` `RABBITMQ_PASSWORD` | `127.0.0.1:5672` `guest/guest` |
| S3/MinIO | `S3_ENDPOINT` `S3_REGION` `S3_ACCESS_KEY` `S3_SECRET_KEY` `S3_BUCKET` | `http://127.0.0.1:9000` `us-east-1` `minioadmin/minioadmin` `cloud` |
| JWT 密钥 | `JWT_SECRET` | 见各 `application.yml`（网关与 auth 必须一致） |

- 建库脚本：`cloud-modules/cloud-system/src/main/resources/db/schema.sql`（不会被自动执行）。
- 生产环境请务必修改默认 `JWT_SECRET` 及各中间件口令。

## 快速验证接口

```bash
# 1. 登录获取 token（演示账号 admin / admin123）
curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}'

# 2. 携带 token 访问 system 服务（分页）
curl "http://localhost:8080/system/user/page?pageNum=1&pageSize=10" \
     -H "Authorization: Bearer <access_token>"

# 3. 文件上传
curl -X POST http://localhost:8080/file/oss/upload \
     -H "Authorization: Bearer <access_token>" \
     -F "file=@/path/to/local/file.png"
```

## 备注

- Lombok（1.18.46，由 Spring Boot BOM 管理）已在 `cloud-common-core` 引入；因 JDK 23+ 默认不再从 classpath 自动运行注解处理器，已在父 POM 的 `maven-compiler-plugin` 中通过 `annotationProcessorPaths` 显式声明 Lombok 处理器。其他模块如需使用，只需添加 `org.projectlombok:lombok` 依赖即可。
- 图片 WebP 转码：`cloud-file` 上传图片时，若为可转换的光栅图片（PNG/JPEG/GIF/BMP）且 `cloud.file.webp.enabled=true`，会同时存储**原件**与 **WebP** 两份，上传接口返回 `originalKey` 与 `webpKey`。WebP 编码由 `org.sejda.imageio:webp-imageio`（内置跨平台原生库）提供；转换失败时自动降级为仅保留原件，不影响上传。
  - 在 JDK 24+ 运行 `cloud-file` 时，建议加上 `--enable-native-access=ALL-UNNAMED` 启动参数以消除原生库加载告警：`java --enable-native-access=ALL-UNNAMED -jar cloud-file.jar`。
- 各服务端口：gateway 8080、auth 9200、system 9201、file 9300、job 9400。

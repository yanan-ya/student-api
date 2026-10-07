# 项目检查与测试总结

## 结果

已检查全部业务类、包结构、Maven 配置、数据库配置示例和原有测试；本机数据库配置只核对配置项，不记录账号密码。保留 Controller → Service → DAO → JdbcTemplate 的结构。

2026-10-03 执行 `./mvnw.cmd -B test`，结果为 **51 个测试，0 失败，0 错误，0 跳过**。原来有 1 个启动测试，本次增加了 50 个测试用例（参数化测试按实际执行次数计算）。执行环境为本机 JDK 25，项目仍按原有 Java 21 配置编译，没有升级框架或 Java 目标版本。

先在原业务代码上运行第一批回归测试：41 个测试中 11 个失败，分别对应更新参数未校验、错误请求被归类为系统异常、更新和删除不存在学生仍成功。修复后所有用例通过。首次 Maven 启动失败是受限执行环境无法读取本机 JDK 的安全配置；正常权限下原 Wrapper 能正常运行，因此未修改 Wrapper。

## 发现并修复的问题

| 问题 | 处理方式 |
| --- | --- |
| PUT 未校验邮箱、成绩，负数和超过 100 的成绩也会进入 Service | 增加 `UpdateStudentRequest` 和 `@Valid`，保持更新不要求姓名 |
| DELETE 的空白或非法邮箱未校验 | 在请求参数上增加约束，统一返回参数错误 |
| 更新、删除 SQL 影响 0 行时仍报告成功 | Service 根据影响行数抛出 `StudentNotFoundException`，返回业务码 `10003` |
| JSON 损坏、字段类型错误、缺少必需查询参数进入系统异常兜底 | 分别捕获请求解析、参数缺失、方法参数校验异常，返回 `10002` |
| 系统异常没有日志，客户端只看到“系统繁忙”但服务端无法定位 | 服务端记录异常堆栈，响应仍保持通用提示 |
| DAO、Service、Controller 声明宽泛的 `throws Exception` | 移除无必要的声明；JdbcTemplate 的数据访问异常本来就是运行时异常 |
| 查询使用 `SELECT *`；注释误称 SQL 参数顺序可能不重要 | 明确列名，纠正占位符顺序注释 |
| 唯一的启动测试不检查接口和 SQL，测试配置依赖本机环境 | 补充分层测试、完整链路测试和独立 H2 配置 |
| 仓库缺少建表定义，无法仅凭仓库确认邮箱唯一约束 | 增加手工参考 DDL，测试表明确包含邮箱唯一约束 |
| 启动类手动请求示例端口与配置示例不一致 | 将注释中的端口改为配置示例使用的 8081 |

没有把代码风格当成缺陷：构造器注入、参数化 SQL、现有分层和 `Result<T>` 都适合当前项目。`student_api` 包名、`getAllStudent` 等命名有改进空间，但整体重命名没有实际收益，本次保留。

## 接口兼容性

路径、HTTP 方法和成功响应保持原样：

| 请求 | 行为 |
| --- | --- |
| `GET /student` | 返回所有学生；没有数据时返回空数组，未额外承诺排序 |
| `POST /student` | 用 `name/email/score` 添加学生 |
| `PUT /student` | 按 `email` 更新 `score`，不要求 `name`；原先带 `id/name` 的请求仍能使用 |
| `DELETE /student?email=...` | 按邮箱删除 |

继续使用 HTTP 200 搭配 `code/message/data` 的已有风格，未迁移为 HTTP 201/204/400/404/409。业务码仍为成功 `10000`、邮箱重复 `10001`、参数错误 `10002`、系统异常 `99999`；新增 `10003` 表示学生不存在。

有意修正的边界行为：非法更新和删除参数不再被接受；不存在学生的更新、删除不再报告成功；无法解析的请求不再报告系统繁忙。

`score` 仍为原来的 `double`，未将字段改为必填包装类型。是否要求请求显式提供成绩、邮箱是否忽略大小写等业务规则，需要单独确定，当前没有自行改变。PUT 仍只改成绩，也没有改成 PATCH 或按 ID 寻址。

## 测试说明

| 测试类 | 数量 | 验证内容 |
| --- | ---: | --- |
| `StudentControllerTest` | 29 | 列表、空列表、新增、重复邮箱、姓名/邮箱/成绩校验、0 和 100 边界、更新不要求姓名、旧请求格式兼容、删除、学生不存在、缺失参数、损坏 JSON、系统异常不泄漏内部细节 |
| `StudentServiceTest` | 8 | 列表、空列表、增改删参数传递及结果、重复邮箱异常不被吞掉、更新/删除影响 0 行时抛业务异常 |
| `StudentDaoTest` | 9 | 真实 JdbcTemplate SQL、所有列映射、自动生成 ID、唯一约束、只改目标学生成绩、同成绩更新、只删除目标学生、不存在返回 0、带引号输入作为数据处理 |
| `StudentApiApplicationTests` | 5 | 保留启动测试，增加完整增查改删链路、数据库重复邮箱到 API 响应、两种不存在场景、非法更新不改变已存成绩 |

- Controller 使用 `@WebMvcTest` 和 MockMvc，模拟 Service，真实执行 JSON 绑定、校验和异常处理。
- Service 使用 Mockito，不启动 Spring。影响行数和业务异常属于这一层的职责。
- DAO 使用 `@JdbcTest`，真实运行 SQL，不模拟 JdbcTemplate；每个用例自动回滚。
- 集成测试使用 `@SpringBootTest`、MockMvc 和事务回滚，验证真实三层协作。这里是进程内 MVC 测试，没有启动独立 HTTP 服务器。
- 测试资源中的 `application.properties` 优先于主资源的同名本地配置。H2 仅为 test 依赖，不替换运行时 MySQL；测试建表 SQL 也只在测试资源中。

复现命令（项目根目录，PowerShell）：

```powershell
.\mvnw.cmd -B test
```

测试报告位于 `target/surefire-reports/`。本次修复前、修复后的日志分别在 `target/review-before-fixes.log` 和 `target/review-after-fixes.log`，这些构建产物不提交到 Git。系统异常测试故意制造一次数据访问错误，因此日志里出现对应 ERROR 堆栈是预期行为，应以测试汇总判断是否通过。

## 修改和新增文件

修改：

- `pom.xml`：增加 test 范围的 JDBC 测试支持和 H2。
- `src/main/java/com/example/student_api/controller/StudentController.java`：更新、删除校验，移除无用局部变量和异常声明。
- `src/main/java/com/example/student_api/service/StudentService.java`：不存在学生的业务判断。
- `src/main/java/com/example/student_api/dao/StudentDao.java`：明确列名和参数顺序，移除异常声明。
- `src/main/java/com/example/student_api/handler/GlobalExceptionHandler.java`：区分业务、输入和系统异常，记录日志，安全提取校验提示。
- `src/main/java/com/example/student_api/StudentApiApplication.java`：仅修正注释里的示例端口。
- `src/main/resources/application.properties.example`：补充建表参考和测试配置说明。
- `src/test/java/com/example/student_api/StudentApiApplicationTests.java`：增加完整链路测试。

新增：

- `src/main/java/com/example/student_api/dto/UpdateStudentRequest.java`。
- `src/main/java/com/example/student_api/exception/StudentNotFoundException.java`。
- `src/test/java/com/example/student_api/controller/StudentControllerTest.java`。
- `src/test/java/com/example/student_api/service/StudentServiceTest.java`。
- `src/test/java/com/example/student_api/dao/StudentDaoTest.java`。
- `src/test/resources/application.properties` 和 `schema-test.sql`。
- `docs/student-schema.sql` 和本报告。

工作区开始时已有 README.md 修改，本次未改动它。本机 `src/main/resources/application.properties` 未修改。

## 建议重点理解的内容

1. **分层不是简单地把方法搬到三个类。** Controller 处理输入和响应，Service 决定“0 行是否属于业务失败”，DAO 只执行 SQL 并返回影响行数。异常处理器负责将异常翻译成统一响应。
2. **`@Valid` 必须对应请求的真实要求。** 给更新接口直接套用新增学生实体的规则，会错误要求姓名。这里一个两字段 record 已足够，不需要建立复杂 DTO 继承体系。
3. **唯一性应由数据库约束保证。** 仅在插入前查询邮箱，无法防止并发重复插入。测试让真实唯一约束产生 `DuplicateKeyException`，再验证全局处理器返回 `10001`。
4. **JdbcTemplate 的参数按位置绑定。** `UPDATE student SET score=? WHERE email=?` 必须先传成绩再传邮箱。影响行数可用于判断目标是否存在，没必要先 SELECT 再 UPDATE。
5. **隔离测试和集成测试解决不同问题。** mock 能定位一层的逻辑，但不能证明 SQL 正确；真实数据库测试验证约束和数据变化；完整链路测试验证几层是否接得起来。
6. **异常信息分两种受众。** 客户端收到稳定业务码，开发者在服务端看到堆栈；不能把 SQL 或连接细节直接返回给调用者。

## 验证边界和目前不应复杂化的部分

H2 可以检查本项目使用的基础 SQL、字段映射和唯一约束，但不能完全代替 MySQL：排序规则、邮箱大小写、驱动影响行数等行为仍受真实数据库配置影响。本次没有连接或修改本机 MySQL，不能声称现有数据库的邮箱索引已经核实。

`docs/student-schema.sql` 是供新数据库手工建表的参考，没有启动时自动执行，也不是对现有表的迁移。已有库可手工查看 `SHOW CREATE TABLE student`，确认 email 的唯一约束。示例字段长度不是从本机数据库读到的，也没有据此给接口增加长度限制。MySQL 连接不要随意启用 `useAffectedRows=true`，否则“更新为同一成绩”的影响行数语义可能与当前不存在判断不一致。

配置文件已忽略真实数据库账号密码；示例中的关闭 SSL 等参数用于本地学习，生产部署时再结合环境配置 TLS 和凭据。当前项目不需要为此引入配置中心。

现在没有必要引入 Service/DAO 的接口与 Impl 配对、通用 BaseService/BaseDao、JPA/MyBatis 迁移、缓存、微服务、复杂异常继承树、映射框架或给每个单条 SQL 操作添加事务。分页、邮箱大小写规范、缺失成绩的规则、HTTP 状态码调整和真实 MySQL 容器测试可以在明确需求后逐项决定。没有做重大架构改造。

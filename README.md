# Video-Detect: 基于 SpringBoot 和 YOLO 的视频流数据处理和目标检测系统

![演示视频](https://gitee.com/zhuanghongdong/video-detect/raw/master/%E6%BC%94%E7%A4%BA%E8%A7%86%E9%A2%91.gif)

## 系统架构说明
本系统采用前后端分离架构，包含三大部分：

- 前端：基于Vue 3，负责用户交互与数据可视化。
- 后端：基于Spring Boot，负责业务逻辑、数据管理、权限认证等。
- 目标检测服务：基于YOLOv8与Flask，负责视频/图片的目标检测推理。

## 主要技术栈
- 前端：Vue 3、Element Plus、ECharts、Axios

- 后端：Spring Boot、MyBatis-Plus、MySQL、Redis、JWT、FFmpeg
- AI推理：YOLOv8（ultralytics）、Flask

## 主要功能
- 实时视频流目标检测与结果展示

- 设备、检查、火灾等业务数据管理
- 检测结果的可视化与统计分析
- 用户权限与安全认证
- 文件上传与管理
- 各模块启动方式

前端

```bash
cd video_detect_front
npm install
npm run serve
```

后端

```bash
cd video_detect_backend/vd_server
mvn spring-boot:run
```

目标检测服务

```bash
cd video_detect_yolo/flask
pip install -r requirements.txt
python run.py
```

数据库

```bash
使用MySQL，表结构见video_detect.sql
```

## 5.1 运行环境

### (1) 硬件环境
- CPU：4核及以上（建议 8 核）。
- 内存：至少 8GB，建议 16GB 及以上（同时运行前端/后端/检测服务时更稳定）。
- 显存：
  - CPU推理可不依赖独立显卡；
  - 若启用GPU推理，建议 NVIDIA 显卡，显存 >= 6GB。
- 磁盘：至少预留 10GB 可用空间（含模型、日志、依赖包）。

### (2) 软件环境
- 操作系统：Windows 10/11、Ubuntu 20.04+ 或其他可运行 Java/Python/Node.js 的系统。
- JDK：Java 17（`video_detect_backend/pom.xml` 中 `java.version=17`）。
- Maven：3.8+（用于构建 Spring Boot 2.7.3 多模块工程）。
- Python：3.9+（`flask==2.3.2`、`numpy>=1.26.0`、`pillow>=9.5.0` 依赖环境）。
- Node.js：14.17+（前端使用 `@vue/cli-service ~5.0.0`），npm 建议 6+。
- 数据库与中间件：MySQL 8.0+、Redis 6.0+。
- 主要框架/SDK：Spring Boot、Flask、ultralytics（YOLOv8）。
- 开发工具（可选）：IDEA（后端）、PyCharm/VS Code（Python）、VS Code（前端）。

## 5.2 加载及运行方法

### (1) 在系统环境变量中增加如下配置（按本项目实际）
1）`JAVA_HOME`：必须配置为 JDK 17 安装目录（后端 `video_detect_backend/pom.xml` 使用 `java.version=17`），并将 `%JAVA_HOME%\bin` 加入 `PATH`。  
2）`MAVEN_HOME`：建议配置 Maven 3.8+ 安装目录，并将 `%MAVEN_HOME%\bin` 加入 `PATH`（用于 `video_detect_backend` 多模块构建）。  
3）`PYTHONPATH`：**本项目默认无需手工配置**。`video_detect_yolo/flask/app/api.py` 在运行时会自动将 `video_detect_yolo/ultralytics-main` 加入 `sys.path`；仅在你使用外部脚本直接调用 `ultralytics-main` 时，才需要手动追加该路径。

### (2) 在开发工具中打开项目并加载程序集
- 后端：在 IDEA 中打开 `video_detect_backend/pom.xml`（Maven 根工程），等待 Maven 依赖加载完成。  
- 前端：在 VS Code 中打开 `video_detect_front` 目录并执行 `npm install`。  
- 检测服务：在 PyCharm/VS Code 中打开 `video_detect_yolo/flask` 目录并安装 `requirements.txt` 依赖。

### (3) 在控制台执行指令，启动各模块服务
按顺序启动，推荐先启动基础服务（MySQL、Redis），再启动后端、检测服务、前端：

```bash
# 1) 启动后端
cd video_detect_backend/vd_server
mvn spring-boot:run

# 2) 启动检测服务
cd ../../video_detect_yolo/flask
pip install -r requirements.txt
python run.py --host 0.0.0.0 --port 5000

# 3) 启动前端
cd ../../video_detect_front
npm install
npm run serve
```

### (4) 将配置文件中的配置项修改为本机地址
1）后端数据库与Redis配置：`video_detect_backend/vd_server/src/main/resources/application-dev.yml`  
- 将 `spring.datasource.druid.url`、`username`、`password` 改为本机 MySQL 实际参数。  
- 将 `spring.redis.host`、`spring.redis.port` 改为本机 Redis 实际参数。  

2）检测模型配置：`video_detect_yolo/flask/app/model_config.json`  
- 将 `current_model` 和 `models` 中对应路径配置为本机实际模型路径。  

3）如需局域网访问：  
- 启动 Flask 时使用本机IP（如 `python run.py --host 192.168.1.100 --port 5000`）。  
- 前端/后端涉及固定主机地址时，同步改为该本机IP地址。

## 5.3 数据资源与实验素材说明（按本项目实际）

为避免“模板化数据集描述”与项目实际不一致，以下仅列出仓库中可核验的数据资源与实验素材，并给出支撑材料路径。

| 序号 | 数据资源/实验素材 | 用途 | 规模/时间范围 | 支撑材料（文件路径） |
|---|---|---|---|---|
| 1 | 业务数据库样例数据（MySQL 导出） | 用于后端业务联调、统计分析页面与历史检测记录回放 | 1 份 SQL 导出文件；包含多张业务表及历史记录，文件名带导出时间 `202604301049` | `dump-video_detect-202604301049.sql` |
| 2 | YOLO 检测模型权重（本地推理模型） | 用于 Flask 检测服务加载模型并执行火焰/烟雾检测 | 4 个 `.pt` 权重文件；具体启用模型由配置切换 | `video_detect_yolo/flask/models/best12.pt`、`video_detect_yolo/flask/models/best8.pt`、`video_detect_yolo/flask/models/v8best12.pt`、`video_detect_yolo/flask/models/train4v12best.pt` |
| 3 | 模型配置数据 | 指定当前启用模型及候选模型路径映射 | 1 份 JSON 配置 | `video_detect_yolo/flask/app/model_config.json` |
| 4 | 检测接口测试素材（示例图片） | 用于本地验证 `/api/detect` 接口可用性 | 1 张测试图 | `video_detect_yolo/flask/test.jpg` |
| 5 | YOLO 框架自带示例素材 | 用于快速验证推理链路或调试 | 2 张示例图 | `video_detect_yolo/ultralytics-main/assets/bus.jpg`、`video_detect_yolo/ultralytics-main/assets/zidane.jpg` |

### 说明
- 本项目仓库内**未包含**你提到的“商品评论自建数据集”与“xx 电商江浙沪物流公开数据集”相关文件或引用配置；若后续要纳入本项目，请补充数据来源说明、采集/授权依据、脱敏策略与数据文件路径。  
- 若用于论文/验收材料，建议在附件中补充：数据字典、样本截图、标注规范、采样时间区间、清洗规则与统计口径说明。

## 5.4 YOLO模型资源清单（本项目）

当前项目中的 YOLO 模型资源如下：

| 模型名称（配置键） | 权重文件路径 | 说明 |
|---|---|---|
| `YOLOv8` | `video_detect_yolo/flask/models/v8best12.pt` | 在 `video_detect_yolo/flask/app/model_config.json` 的 `models` 中已配置，可切换使用。 |
| `YOLOv12` | `video_detect_yolo/flask/models/train4v12best.pt` | 在 `video_detect_yolo/flask/app/model_config.json` 的 `models` 中已配置，可切换使用。 |
| （备用） | `video_detect_yolo/flask/models/best12.pt` | 仓库内存在，可作为本地备用权重。 |
| （备用） | `video_detect_yolo/flask/models/best8.pt` | 仓库内存在，可作为本地备用权重。 |

### 说明
- 模型切换配置文件：`video_detect_yolo/flask/app/model_config.json`。  
- 检测服务启动后通过该配置中的 `models` 映射加载对应权重。

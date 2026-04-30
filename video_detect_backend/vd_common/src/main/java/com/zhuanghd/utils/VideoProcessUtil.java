package com.zhuanghd.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class VideoProcessUtil {

    /**
     * 使用FFmpeg处理视频：消音、压缩到640分辨率、转换为AVI格式，覆盖原始视频
     *
     * @param videoPath 视频路径
     * @return 处理是否成功
     */
    public boolean processVideo(String videoPath) {
        try {
            // 创建临时输出路径
            String tempOutputPath = videoPath + ".temp.avi";
            
            // 确保输出目录存在
            File outputDir = new File(videoPath.substring(0, videoPath.lastIndexOf("/")));
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // 构建FFmpeg命令：消音、压缩到640分辨率、转换为AVI格式
            List<String> command = new ArrayList<>();
            command.add("ffmpeg");
            command.add("-i");
            command.add(videoPath);
            command.add("-an");                 // 去除音频
            command.add("-vf");
            command.add("scale=640:-1");        // 缩放到640宽度，高度自适应
            command.add("-c:v");
            command.add("mpeg4");               // 使用mpeg4编码器
            command.add("-q:v");
            command.add("5");                   // 设置视频质量（1-31，1最高）
            command.add("-y");                  // 覆盖已存在的文件
            command.add(tempOutputPath);

            // 执行命令
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 读取命令输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug(line);
            }

            // 等待命令执行完成
            boolean completed = process.waitFor(5, TimeUnit.MINUTES);
            if (!completed) {
                log.error("FFmpeg处理超时");
                process.destroy();
                return false;
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("FFmpeg处理失败，退出码: " + exitCode);
                return false;
            }
            
            // 删除原始文件并重命名临时文件为原文件名
            File originalFile = new File(videoPath);
            if (originalFile.exists() && originalFile.delete()) {
                File tempFile = new File(tempOutputPath);
                tempFile.renameTo(originalFile);
                log.info("视频处理完成并覆盖原文件: {}", videoPath);
            } else {
                log.error("无法删除原始文件或重命名临时文件");
                return false;
            }

            return true;
        } catch (IOException | InterruptedException e) {
            log.error("FFmpeg处理异常", e);
            return false;
        }
    }

    /**
     * 生成视频缩略图
     *
     * @param videoPath      视频路径
     * @param thumbnailPath  缩略图保存路径
     * @return 处理是否成功
     */
    public boolean generateThumbnail(String videoPath, String thumbnailPath) {
        try {
            // 获取处理后的视频路径
            String processedVideoPath = videoPath.substring(0, videoPath.lastIndexOf("/")) + "/results/detection/" + new File(videoPath).getName();
            
            // 检查处理后的视频是否存在，如果不存在则使用原始视频
            File processedVideo = new File(processedVideoPath);
            String actualVideoPath = processedVideo.exists() ? processedVideoPath : videoPath;
            
            log.info("生成缩略图使用的视频: {}", actualVideoPath);
            
            // 确保输出目录存在
            File outputDir = new File(thumbnailPath.substring(0, thumbnailPath.lastIndexOf("/")));
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // 构建FFmpeg命令：截取视频第1秒的帧作为缩略图
            List<String> command = new ArrayList<>();
            command.add("ffmpeg");
            command.add("-i");
            command.add(actualVideoPath);
            command.add("-ss");
            command.add("00:00:01");         // 从视频1秒处开始
            command.add("-frames:v");
            command.add("1");                // 只截取一帧
            command.add("-q:v");
            command.add("2");                // 高质量
            command.add("-y");               // 覆盖已存在的文件
            command.add(thumbnailPath);

            // 执行命令
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 读取命令输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug(line);
            }

            // 等待命令执行完成
            boolean completed = process.waitFor(1, TimeUnit.MINUTES);
            if (!completed) {
                log.error("缩略图生成超时");
                process.destroy();
                return false;
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("缩略图生成失败，退出码: " + exitCode);
                return false;
            }

            return true;
        } catch (IOException | InterruptedException e) {
            log.error("缩略图生成异常", e);
            return false;
        }
    }

    /**
     * 使用YOLOv8模型检测视频中的烟雾和火焰，覆盖原始视频
     *
     * @param videoPath 视频路径
     * @return 检测结果，包含FireDO和Smoke的概率和视频持续时间（秒）
     */
    public Map<String, Object> detectFireAndSmoke(String videoPath) {
        Map<String, Object> results = new HashMap<>();
        results.put("FireDO", 0.0);
        results.put("Smoke", 0.0);
        results.put("Duration", 0);

        try {
            String modelPath = LocalFileUtil.MODEL_PATH;
            String outputDir = videoPath.substring(0, videoPath.lastIndexOf("/")) + "/results";
            
            // 确保输出目录存在
            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 创建临时Python脚本文件
            File tempScript = File.createTempFile("detect_fire_smoke_", ".py");
            
            // Python脚本内容
            String pythonScript = "import sys\n" +
                    "import json\n" +
                    "import os\n" +
                    "import cv2\n" +
                    "from ultralytics import YOLO\n"+
                    "\n" +
//                    "# 添加ultralytics包的路径\n" +
//                    "sys.path.append('E:/Graduation_project/7.program/video-detect/video_detect_yolo/ultralytics-main')\n" +
//                    "from ultralytics import YOLO\n" +
                    "\n" +
                    "# 打印路径确认\n" +
                    "print('Python Path:', sys.path)\n" +
                    "print('Current Directory:', os.getcwd())\n" +
                    "\n" +
                    "# 获取视频时长\n" +
                    "def get_video_duration(video_path):\n" +
                    "    cap = cv2.VideoCapture(video_path)\n" +
                    "    if not cap.isOpened():\n" +
                    "        return 0\n" +
                    "    fps = cap.get(cv2.CAP_PROP_FPS)\n" +
                    "    frame_count = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))\n" +
                    "    duration = int(frame_count / fps) if fps > 0 else 0\n" +
                    "    cap.release()\n" +
                    "    return duration\n" +
                    "\n" +
                    "# 加载模型\n" +
                    "model_path = r'" + modelPath + "'\n" +
                    "print('Loading model from:', model_path)\n" +
                    "model = YOLO(model_path)\n" +
                    "\n" +
                    "# 获取视频时长\n" +
                    "video_path = r'" + videoPath + "'\n" +
                    "video_duration = get_video_duration(video_path)\n" +
                    "print('Video duration:', video_duration, 'seconds')\n" +
                    "\n" +
                    "# 执行视频预测并保存带有检测结果的视频\n" +
                    "output_dir = r'" + outputDir + "'\n" +
                    "print('Processing video:', video_path)\n" +
                    "results = model.predict(source=video_path, conf=0.60, imgsz=640, iou=0.5, save=True, project=output_dir, name='detection')\n" +
                    "\n" +
                    "# 分析结果\n" +
                    "fire_conf = 0.0\n" +
                    "smoke_conf = 0.0\n" +
                    "fire_count = 0\n" +
                    "smoke_count = 0\n" +
                    "\n" +
                    "for r in results:\n" +
                    "    for c, conf in zip(r.boxes.cls.tolist(), r.boxes.conf.tolist()):\n" +
                    "        if int(c) == 0:  # FireDO\n" +
                    "            fire_conf += conf\n" +
                    "            fire_count += 1\n" +
                    "        elif int(c) == 1:  # Smoke\n" +
                    "            smoke_conf += conf\n" +
                    "            smoke_count += 1\n" +
                    "\n" +
                    "# 计算平均置信度\n" +
                    "fire_avg = fire_conf / fire_count if fire_count > 0 else 0.0\n" +
                    "smoke_avg = smoke_conf / smoke_count if smoke_count > 0 else 0.0\n" +
                    "\n" +
                    "# 获取生成的输出视频路径\n" +
                    "output_video_path = os.path.join(output_dir, 'detection', os.path.basename(video_path))\n" +
                    "print('Output video path:', output_video_path)\n" +
                    "\n" +
                    "# 如果输出视频存在，覆盖原始视频\n" +
                    "if os.path.exists(output_video_path):\n" +
                    "    try:\n" +
                    "        if os.path.exists(video_path):\n" +
                    "            os.remove(video_path)\n" +
                    "        os.rename(output_video_path, video_path)\n" +
                    "        print('Successfully replaced original video with detection results')\n" +
                    "    except Exception as e:\n" +
                    "        print('Error replacing video file:', str(e))\n" +
                    "\n" +
                    "# 输出结果为JSON格式\n" +
                    "result = {\n" +
                    "    'FireDO': fire_avg,\n" +
                    "    'Smoke': smoke_avg,\n" +
                    "    'Duration': video_duration\n" +
                    "}\n" +
                    "print('boxes:1111111', r.boxes)\n" +
                    "print('boxes attrs:2222222', dir(r.boxes))\n" +
                    "print('RESULT_JSON:', json.dumps(result))";


            // 写入临时脚本文件
            try (java.io.FileWriter writer = new java.io.FileWriter(tempScript)) {
                writer.write(pythonScript);
            }
            
            log.info("临时Python脚本创建于: {}", tempScript.getAbsolutePath());
            
            // 构建命令来激活conda环境并执行Python脚本文件
            List<String> command = new ArrayList<>();
            command.add("cmd");
            command.add("/c");
            command.add("conda activate D:\\work\\Anaconda3-2025.06-1\\envs\\yolov12 && python \"" + tempScript.getAbsolutePath() + "\"");

//            command.add("conda activate D:\\Env\\Conda_YOLO && python \"" + tempScript.getAbsolutePath() + "\"");
//            command.add("conda activate yolov8 && python \"" + tempScript.getAbsolutePath() + "\"");
            
            log.info("执行命令: {}", command);

            // 执行命令
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 读取输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log.info("Python输出: {}", line);
                
                // 特别检查包含结果的行
                if (line.contains("RESULT_JSON:")) {
                    String jsonResult = line.substring(line.indexOf("RESULT_JSON:") + "RESULT_JSON:".length()).trim();
                    log.info("捕获到JSON结果: {}", jsonResult);

                    try {
                        // 解析JSON字符串
                        if (jsonResult.contains("{") && jsonResult.contains("}")) {
                            jsonResult = jsonResult.replace("'", "\""); // 替换单引号为双引号

                            // 简单解析JSON
                            jsonResult = jsonResult.replace("{", "").replace("}", "").replace("\"", "");
                            String[] pairs = jsonResult.split(",");
                            for (String pair : pairs) {
                                String[] keyValue = pair.split(":");
                                if (keyValue.length == 2) {
                                    String key = keyValue[0].trim();
                                    if (key.equals("Duration")) {
                                        results.put(key, Integer.parseInt(keyValue[1].trim()));
                                    } else {
                                        results.put(key, Double.parseDouble(keyValue[1].trim()));
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("解析JSON结果异常", e);
                    }
                }
            }

            // 等待命令执行完成
            boolean completed = process.waitFor(10, TimeUnit.MINUTES);
            if (!completed) {
                log.error("YOLO模型检测超时");
                process.destroy();
                return results;
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("YOLO模型检测失败，退出码: {}", exitCode);
                log.error("输出日志: {}", output.toString());
                return results;
            }

            // 如果没有通过RESULT_JSON标记找到结果，尝试从完整输出中提取JSON
            if (!results.containsKey("Duration") || !results.containsKey("FireDO") || !results.containsKey("Smoke")) {
                log.info("未通过标记找到结果，尝试从完整输出中提取");
                
                String jsonOutput = output.toString();
                if (jsonOutput.contains("{") && jsonOutput.contains("}")) {
                    int start = jsonOutput.indexOf("{");
                    int end = jsonOutput.lastIndexOf("}") + 1;
                    String jsonStr = jsonOutput.substring(start, end);
                    
                    log.info("提取的JSON字符串: {}", jsonStr);
                    
                    // 简单解析JSON
                    jsonStr = jsonStr.replace("{", "").replace("}", "").replace("\"", "");
                    String[] pairs = jsonStr.split(",");
                    for (String pair : pairs) {
                        String[] keyValue = pair.split(":");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim();
                            if (key.equals("Duration")) {
                                results.put(key, Integer.parseInt(keyValue[1].trim()));
                            } else {
                                results.put(key, Double.parseDouble(keyValue[1].trim()));
                            }
                        }
                    }
                }
            }
            
            // 删除临时文件
            try {
                boolean deleted = tempScript.delete();
                if (!deleted) {
                    log.warn("临时Python脚本未被删除: {}", tempScript.getAbsolutePath());
                    tempScript.deleteOnExit(); // 尝试在JVM退出时删除
                }
            } catch (Exception e) {
                log.warn("删除临时Python脚本异常", e);
            }
            
            log.info("检测结果: {}", results);
            return results;
        } catch (Exception e) {
            log.error("YOLO模型检测异常", e);
            return results;
        }
    }
} 
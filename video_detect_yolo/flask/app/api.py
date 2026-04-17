import os
import sys
import base64
import io
import json
import time
from flask import Blueprint, request, jsonify
from PIL import Image

# 创建API蓝图
api_bp = Blueprint('api', __name__)

current_dir = os.path.dirname(os.path.abspath(__file__))
config_path = os.path.join(current_dir, 'model_config.json')
default_model_path = os.path.join(current_dir, '..', 'models', 'best12.pt')

# 全局变量，用于存储模型实例
yolo_model = None
loaded_model_path = None
loaded_model_name = None


def _default_model_config():
    """默认模型配置"""
    return {
        'current_model': 'yolov12',
        'models': {
            'yolov12': '../models/best12.pt'
        }
    }


def read_model_config():
    """读取模型配置文件，不存在则自动创建"""
    if not os.path.exists(config_path):
        default_config = _default_model_config()
        with open(config_path, 'w', encoding='utf-8') as f:
            json.dump(default_config, f, ensure_ascii=False, indent=2)
        return default_config

    with open(config_path, 'r', encoding='utf-8') as f:
        config = json.load(f)

    if 'current_model' not in config:
        config['current_model'] = 'yolov12'
    if 'models' not in config or not isinstance(config['models'], dict):
        config['models'] = {'yolov12': '../models/best12.pt'}

    return config


def write_model_config(config):
    """写入模型配置文件"""
    with open(config_path, 'w', encoding='utf-8') as f:
        json.dump(config, f, ensure_ascii=False, indent=2)


def resolve_model_path(path_value):
    """解析配置中的模型路径（支持相对路径）"""
    if not path_value:
        return default_model_path
    if os.path.isabs(path_value):
        return path_value
    return os.path.abspath(os.path.join(current_dir, path_value))


def get_active_model_info():
    """获取当前激活模型名称与真实路径"""
    config = read_model_config()
    current_model = config.get('current_model')
    models = config.get('models', {})

    configured_path = models.get(current_model)
    if configured_path is None:
        # 如果当前模型名找不到，回退到第一个模型
        if models:
            current_model = next(iter(models.keys()))
            configured_path = models[current_model]
            config['current_model'] = current_model
            write_model_config(config)
        else:
            current_model = 'yolov12'
            configured_path = '../models/best12.pt'
            config['current_model'] = current_model
            config['models'] = {current_model: configured_path}
            write_model_config(config)

    return current_model, resolve_model_path(configured_path), config


def load_model(force_reload=False):
    """按配置延迟加载YOLO模型，支持切换模型时重载"""
    global yolo_model, loaded_model_path, loaded_model_name

    model_name, model_path, _ = get_active_model_info()

    if (
        not force_reload
        and yolo_model is not None
        and loaded_model_path == model_path
    ):
        return True

    try:
        # 将上级目录加入路径，以便导入ultralytics模块
        yolo_path = os.path.join(current_dir, '..', '..', 'ultralytics-main')
        if yolo_path not in sys.path:
            sys.path.append(yolo_path)

        # 导入YOLO
        from ultralytics import YOLO

        # 加载模型
        yolo_model = YOLO(model_path)
        loaded_model_path = model_path
        loaded_model_name = model_name

        print('🔥 YOLO actually loaded from:', yolo_model.ckpt_path)
        print(f'成功加载模型: {model_name} -> {model_path}')
        return True
    except Exception as e:
        print(f'加载模型失败: {str(e)}')
        return False


@api_bp.route('/health', methods=['GET'])
def health_check():
    """健康检查接口"""
    model_name, model_path, _ = get_active_model_info()
    return jsonify({
        'status': 'ok',
        'timestamp': time.time(),
        'service': 'video-detect-flask',
        'model_name': model_name,
        'model_path': model_path,
        'model_loaded': yolo_model is not None,
        'loaded_model_path': loaded_model_path
    })


@api_bp.route('/model/config', methods=['GET'])
def get_model_config():
    """获取模型配置"""
    model_name, model_path, config = get_active_model_info()
    return jsonify({
        'current_model': model_name,
        'current_model_path': model_path,
        'models': config.get('models', {})
    })


@api_bp.route('/model/switch', methods=['POST'])
def switch_model():
    """切换当前检测模型（通过更新配置中的模型名）"""
    if not request.is_json:
        return jsonify({'error': '仅接受JSON格式请求'}), 400

    data = request.get_json() or {}
    target_model = data.get('model_name')
    if not target_model:
        return jsonify({'error': '缺少model_name参数'}), 400

    config = read_model_config()
    models = config.get('models', {})
    if target_model not in models:
        return jsonify({'error': f'未找到模型: {target_model}'}), 400

    config['current_model'] = target_model
    write_model_config(config)

    if not load_model(force_reload=True):
        return jsonify({'error': '模型切换后加载失败，请检查模型路径'}), 500

    return jsonify({
        'success': True,
        'current_model': loaded_model_name,
        'model_path': loaded_model_path
    })


@api_bp.route('/detect', methods=['POST'])
def detect():
    """图像检测接口"""
    # 检查请求
    if not request.is_json:
        return jsonify({'error': '仅接受JSON格式请求'}), 400

    # 获取图像数据
    data = request.get_json()
    if 'image' not in data:
        return jsonify({'error': '请求中未找到图像数据'}), 400

    # 解码Base64图像
    try:
        image_data = base64.b64decode(data['image'])
        image = Image.open(io.BytesIO(image_data))
    except Exception as e:
        return jsonify({'error': f'图像解码失败: {str(e)}'}), 400

    # 加载模型（如果尚未加载）
    if not load_model():
        return jsonify({'error': '模型加载失败'}), 500

    # 执行检测
    try:
        results = yolo_model(image)

        # 处理检测结果
        detections = []
        for result in results:
            # 获取边界框
            for box, cls, conf in zip(result.boxes.xyxy.cpu().numpy(),
                                      result.boxes.cls.cpu().numpy(),
                                      result.boxes.conf.cpu().numpy()):
                x1, y1, x2, y2 = map(int, box)
                class_id = int(cls)
                confidence = float(conf)

                # 获取类别名称
                class_name = result.names[class_id]

                # 转换为[x, y, width, height]格式，前端更容易处理
                det_box = [x1, y1, x2 - x1, y2 - y1]

                detections.append({
                    'box': det_box,
                    'label': class_name,
                    'confidence': confidence
                })

        # 返回结果
        return jsonify({
            'timestamp': time.time(),
            'model_name': loaded_model_name,
            'model_path': loaded_model_path,
            'detections': detections
        })

    except Exception as e:
        print(f'检测过程中出错: {str(e)}')
        return jsonify({'error': f'检测过程中出错: {str(e)}'}), 500

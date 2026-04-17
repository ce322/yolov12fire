import os
import sys
import argparse
import json
from app import create_app

def parse_args():
    parser = argparse.ArgumentParser(description='启动视频目标检测服务')
    parser.add_argument('--host', type=str, default='0.0.0.0', help='监听地址')
    parser.add_argument('--port', type=int, default=5000, help='监听端口')
    parser.add_argument('--debug', action='store_true', help='是否启用调试模式')
    return parser.parse_args()

def main():
    # 解析命令行参数
    args = parse_args()
    
    # 检查是否存在ultralytics模块路径
    yolo_path = os.path.join(os.path.dirname(__file__), '..', 'ultralytics-main')
    if not os.path.exists(yolo_path):
        print(f"警告: 未找到YOLOv8模块路径: {yolo_path}")
        print("请确保ultralytics-main目录存在于video_detect_yolo目录中")
    
    # 检查模型目录是否存在，不存在则创建
    model_dir = os.path.join(os.path.dirname(__file__), 'models')
    if not os.path.exists(model_dir):
        os.makedirs(model_dir)
        print(f"已创建模型目录: {model_dir}")
    
    # 读取模型配置文件
    config_path = os.path.join(os.path.dirname(__file__), 'app', 'model_config.json')
    model_path = os.path.join(model_dir, 'best12.pt')
    if os.path.exists(config_path):
        try:
            with open(config_path, 'r', encoding='utf-8') as f:
                model_config = json.load(f)
            current_model = model_config.get('current_model')
            model_path_value = model_config.get('models', {}).get(current_model)
            if model_path_value:
                if os.path.isabs(model_path_value):
                    model_path = model_path_value
                else:
                    model_path = os.path.abspath(os.path.join(os.path.dirname(config_path), model_path_value))
        except Exception as e:
            print(f"警告: 读取模型配置失败，将使用默认模型路径。错误: {str(e)}")

    # 检查是否存在模型文件
    if not os.path.exists(model_path):
        print(f"警告: 未找到模型文件: {model_path}")
        print("请在 app/model_config.json 中配置正确的模型路径")
    
    try:
        # 创建应用
        app = create_app()
        
        # 打印服务信息
        print(f"启动服务于 http://{args.host}:{args.port}")
        if os.path.exists(model_path):
            print(f"使用YOLOv8模型: {model_path}")
        print("健康检查接口: /api/health")
        print("检测接口: /api/detect (POST)")
        
        # 启动服务
        app.run(host=args.host, port=args.port, debug=args.debug)
        return 0
        
    except Exception as e:
        print(f"服务启动失败: {str(e)}")
        return 1

if __name__ == '__main__':
    sys.exit(main()) 

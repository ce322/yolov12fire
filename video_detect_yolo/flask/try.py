import os
import ultralytics              # ✅ 必须有
from ultralytics import YOLO
from PIL import Image


print("🔥 ultralytics path =", ultralytics.__file__)
current_dir = os.path.dirname(os.path.abspath(__file__))
model_path = os.path.join(current_dir, "models", "best12.pt")

print("model_path =", model_path)

model = YOLO(model_path)
img = Image.open("test.jpg")
model(img)

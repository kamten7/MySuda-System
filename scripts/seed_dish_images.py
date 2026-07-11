"""
速达外卖 — 菜品/套餐图片生成 + MinIO 上传 + 数据库更新

功能：
1. 为 41 道菜品 + 8 个套餐生成带渐变色 + 食物 emoji + 菜名的 PNG 图（400x300）
2. 上传到 MinIO bucket sky-take-out
3. 更新 MySQL dish/setmeal 表的 image 字段为 MinIO 的访问 URL
"""

import io, json, os
import pymysql
from minio import Minio
from PIL import Image, ImageDraw, ImageFont

# ========================== 配置 ==========================

MYSQL = {
    "host": "127.0.0.1", "port": 3308,
    "user": "root", "password": "20050904@Aa",
    "database": "sky_take_out", "charset": "utf8mb4",
}

MINIO = {
    "endpoint": "127.0.0.1:9010",
    "access_key": "minioadmin",
    "secret_key": "minioadmin",
    "bucket": "sky-take-out",
    "secure": False,
}

# 每道菜的配色与图标 (name -> (emoji, gradient_start, gradient_end, text_color))
DISH_STYLES = {
    "黄焖鸡米饭":   ("🍗", "#FF6B35", "#F7931E", "#ffffff"),
    "麻辣香锅":     ("🌶️", "#DC2626", "#F97316", "#ffffff"),
    "牛肉拉面":     ("🍜", "#D97706", "#F59E0B", "#ffffff"),
    "烤鸭饭":       ("🦆", "#B45309", "#D97706", "#ffffff"),
    "鱼香肉丝盖饭": ("🐟", "#E11D48", "#F43F5E", "#ffffff"),
    "番茄鸡蛋面":   ("🍅", "#EF4444", "#F87171", "#ffffff"),
    "宫保鸡丁盖饭": ("🥜", "#B91C1C", "#EF4444", "#ffffff"),
    "红烧肉盖饭":   ("🥩", "#C2410C", "#EA580C", "#ffffff"),
    "糖醋里脊盖饭": ("🍖", "#9A3412", "#C2410C", "#ffffff"),
    "麻婆豆腐盖饭": ("🧈", "#DC2626", "#EA580C", "#ffffff"),
    "回锅肉盖饭":   ("🥓", "#B45309", "#D97706", "#ffffff"),
    "咖喱鸡块饭":   ("🍛", "#CA8A04", "#EAB308", "#ffffff"),
    "香菇滑鸡盖饭": ("🍄", "#65A30D", "#84CC16", "#ffffff"),
    "红烧牛肉面":   ("🐮", "#DC2626", "#B91C1C", "#ffffff"),
    "重庆小面":     ("🌶️", "#E53935", "#F4511E", "#ffffff"),
    "炸酱面":       ("🥒", "#7C3AED", "#8B5CF6", "#ffffff"),
    "螺蛳粉":       ("🐌", "#059669", "#10B981", "#ffffff"),
    "酸辣粉":       ("🍲", "#E11D48", "#FB7185", "#ffffff"),
    "过桥米线":     ("🍜", "#0EA5E9", "#38BDF8", "#ffffff"),
    "农家小炒肉":   ("🌿", "#16A34A", "#22C55E", "#ffffff"),
    "蒜蓉西兰花":   ("🥦", "#15803D", "#22C55E", "#ffffff"),
    "干锅花菜":     ("🥬", "#CA8A04", "#EAB308", "#ffffff"),
    "水煮肉片":     ("🥘", "#DC2626", "#F97316", "#ffffff"),
    "鱼香茄子煲":   ("🍆", "#7C2D12", "#9A3412", "#ffffff"),
    "酸菜鱼":       ("🐟", "#0284C7", "#38BDF8", "#ffffff"),
    "拍黄瓜":       ("🥒", "#166534", "#22C55E", "#ffffff"),
    "凉拌木耳":     ("🫘", "#1E293B", "#475569", "#ffffff"),
    "皮蛋豆腐":     ("🥚", "#F59E0B", "#FBBF24", "#1f2937"),
    "口水鸡":       ("🐔", "#E11D48", "#F97316", "#ffffff"),
    "炸鸡排":       ("🍗", "#D97706", "#F59E0B", "#ffffff"),
    "春卷":         ("🥟", "#B45309", "#F59E0B", "#ffffff"),
    "珍珠奶茶":     ("🧋", "#A16207", "#D4A574", "#ffffff"),
    "芒果冰沙":     ("🥭", "#EAB308", "#FDE047", "#1f2937"),
    "杨枝甘露":     ("🥥", "#F59E0B", "#FDE68A", "#1f2937"),
    "冰镇柠檬水":   ("🍋", "#22D3EE", "#A5F3FC", "#1f2937"),
    "红豆双皮奶":   ("🍮", "#E11D48", "#FB7185", "#ffffff"),
    "豆浆油条":     ("🥖", "#B45309", "#D97706", "#ffffff"),
    "小笼包":       ("🥟", "#F97316", "#FBBF24", "#1f2937"),
    "煎饼果子":     ("🥞", "#D97706", "#F59E0B", "#ffffff"),
    "皮蛋瘦肉粥":   ("🥣", "#78716C", "#A8A29E", "#ffffff"),
    "鸡蛋灌饼":     ("🍳", "#EAB308", "#FDE047", "#1f2937"),
}

SETMEAL_STYLES = {
    "一荤一素套餐":   ("🍱", "#F97316", "#22C55E", "#ffffff"),
    "两荤一素套餐":   ("🥩", "#DC2626", "#16A34A", "#ffffff"),
    "麻辣香锅双人餐": ("🌶️", "#B91C1C", "#EA580C", "#ffffff"),
    "单人豪华套餐":   ("🍽️", "#7C3AED", "#A78BFA", "#ffffff"),
    "双人商务套餐":   ("🥂", "#1E3A8A", "#3B82F6", "#ffffff"),
    "宿舍聚餐套餐":   ("🎉", "#E11D48", "#F97316", "#ffffff"),
    "轻食沙拉套餐":   ("🥗", "#16A34A", "#86EFAC", "#1f2937"),
    "牛肉面+饮品套餐":("🍜", "#D97706", "#38BDF8", "#ffffff"),
}


def hex_to_rgb(h: str):
    hx = h.lstrip("#")
    return tuple(int(hx[i:i+2], 16) for i in (0, 2, 4))


def create_food_image(name: str, style: tuple) -> bytes:
    """生成 400x300 PNG：渐变色背景 + 半透明装饰圆 + 食物 emoji + 菜名"""
    emoji, start, end, text_color = style
    W, H = 400, 300
    sr, sg, sb = hex_to_rgb(start)
    er, eg, eb = hex_to_rgb(end)
    tc = hex_to_rgb(text_color)

    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # 渐变背景
    for y in range(H):
        t = y / H
        r = int(sr + (er - sr) * t)
        g = int(sg + (eg - sg) * t)
        b = int(sb + (eb - sb) * t)
        draw.line([(0, y), (W, y)], fill=(r, g, b))

    # 装饰：右上 + 左下白色半透明圆
    draw.ellipse([(W - 120, -40), (W + 40, 120)], fill=(255, 255, 255, 30))
    draw.ellipse([(-60, H - 60), (100, H + 100)], fill=(255, 255, 255, 20))

    # emoji 背景半透明圆
    icon_bg = tc + (40,)
    draw.ellipse([(W // 2 - 55, 40), (W // 2 + 55, 150)], fill=icon_bg)

    # 加载中文字体
    font_emoji = None
    font_name = None
    font_tag = None
    for fp in ["C:/Windows/Fonts/msyh.ttc", "C:/Windows/Fonts/simhei.ttf"]:
        if os.path.exists(fp):
            try:
                font_emoji = ImageFont.truetype(fp, 64)
                font_name = ImageFont.truetype(fp, 30)
                font_tag = ImageFont.truetype(fp, 22)
                break
            except Exception:
                continue
    if font_emoji is None:
        font_emoji = ImageFont.load_default()
        font_name = ImageFont.load_default()
        font_tag = ImageFont.load_default()

    # emoji 居中
    bb = draw.textbbox((0, 0), emoji, font=font_emoji)
    ew, eh = bb[2] - bb[0], bb[3] - bb[1]
    draw.text(((W - ew) // 2, (190 - eh) // 2 + 10), emoji, font=font_emoji, fill=(255, 255, 255, 230))

    # 菜名
    bb2 = draw.textbbox((0, 0), name, font=font_name)
    nw = bb2[2] - bb2[0]
    draw.text(((W - nw) // 2, 195), name, font=font_name, fill=tc)

    # 品牌标签
    tag = "速达外卖"
    bb3 = draw.textbbox((0, 0), tag, font=font_tag)
    tw = bb3[2] - bb3[0]
    draw.text(((W - tw) // 2, 240), tag, font=font_tag, fill=tc + (100,))

    # 合成 RGB PNG
    result = Image.new("RGB", (W, H), (255, 255, 255))
    result.paste(img, mask=img.split()[3])
    buf = io.BytesIO()
    result.save(buf, format="PNG", optimize=True)
    return buf.getvalue()


def main():
    # 1. MySQL
    conn = pymysql.connect(**MYSQL)
    cur = conn.cursor()

    # 2. MinIO
    client = Minio(
        MINIO["endpoint"],
        access_key=MINIO["access_key"],
        secret_key=MINIO["secret_key"],
        secure=MINIO["secure"],
    )
    bucket = MINIO["bucket"]
    if not client.bucket_exists(bucket):
        client.make_bucket(bucket)
        print(f"[MinIO] Bucket created: {bucket}")

    # Public-read policy
    policy = {
        "Version": "2012-10-17",
        "Statement": [{
            "Effect": "Allow",
            "Principal": {"AWS": ["*"]},
            "Action": ["s3:GetObject"],
            "Resource": [f"arn:aws:s3:::{bucket}/*"],
        }],
    }
    client.set_bucket_policy(bucket, json.dumps(policy))
    print(f"[MinIO] Bucket policy set to public-read\n")

    minio_base = f"http://{MINIO['endpoint']}/{bucket}"

    # 3. 菜品
    cur.execute("SELECT id, name FROM dish ORDER BY id")
    dishes = cur.fetchall()
    for dish_id, dish_name in dishes:
        style = DISH_STYLES.get(dish_name)
        if not style:
            print(f"  [SKIP] {dish_name}")
            continue
        img = create_food_image(dish_name, style)
        obj = f"dish/{dish_id}.png"
        client.put_object(bucket, obj, io.BytesIO(img), len(img), content_type="image/png")
        url = f"{minio_base}/{obj}"
        cur.execute("UPDATE dish SET image = %s WHERE id = %s", (url, dish_id))
        print(f"  [dish] {dish_name} → {url}")
    conn.commit()
    print(f"\n=== 菜品: {len(dishes)} 完成 ===\n")

    # 4. 套餐
    cur.execute("SELECT id, name FROM setmeal ORDER BY id")
    setmeals = cur.fetchall()
    for sm_id, sm_name in setmeals:
        style = SETMEAL_STYLES.get(sm_name)
        if not style:
            print(f"  [SKIP] {sm_name}")
            continue
        img = create_food_image(sm_name, style)
        obj = f"setmeal/{sm_id}.png"
        client.put_object(bucket, obj, io.BytesIO(img), len(img), content_type="image/png")
        url = f"{minio_base}/{obj}"
        cur.execute("UPDATE setmeal SET image = %s WHERE id = %s", (url, sm_id))
        print(f"  [setmeal] {sm_name} → {url}")
    conn.commit()

    cur.close()
    conn.close()
    print(f"\n=== 套餐: {len(setmeals)} 完成 ===")
    print(f"✅ 全部完成！{len(dishes)} 道菜品 + {len(setmeals)} 个套餐")


if __name__ == "__main__":
    main()

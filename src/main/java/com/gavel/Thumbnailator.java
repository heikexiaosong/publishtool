package com.gavel;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Thumbnailator {

    public static void main(String[] args) throws Exception {
        // 原图片地址
        String imageUrl = "D:\\pics\\src.jpg";


        System.out.println(new File(imageUrl).getName());

        // 水印图片 相对于resource目录
        String watermark = "D:\\pics\\jingsu_1.png";
        // 输出到文件
        String outputFile = "D:\\pics\\src_res.jpg";
        // 不透明度
        float opacity = 0.75f;

        try {
            // 获取原图文件
            File file = new File(imageUrl);
            // ImageIO读取图片
            BufferedImage image = ImageIO.read(file);

            Thumbnails.of(image)
                    // 设置图片大小
                    .size(image.getWidth(), image.getHeight())
                    // 加水印 参数：1.水印位置 2.水印图片 3.不透明度0.0-1.0
                    .watermark(Positions.TOP_LEFT, ImageIO.read(new File(watermark)), opacity)
                    // 输出到文件
                    .toFile(outputFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

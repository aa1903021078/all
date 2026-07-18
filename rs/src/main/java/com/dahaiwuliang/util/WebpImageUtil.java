package com.dahaiwuliang.util;

import com.dahaiwuliang.common.BusinessException;
import com.dahaiwuliang.config.FoodieProperties;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.UUID;

/**
 * 图片处理工具: 上传自动等比压缩, 优先转 WebP 格式(WebP 不可用时回退 JPEG)
 */
@Slf4j
@Component
public class WebpImageUtil {

    private final FoodieProperties properties;

    public WebpImageUtil(FoodieProperties properties) {
        this.properties = properties;
    }

    /**
     * 保存并压缩图片, 返回可访问的相对 URL (如 /upload/20240718/xxx.webp)
     */
    public String saveAsWebp(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件为空");
        }
        BufferedImage src;
        try {
            src = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException("图片读取失败");
        }
        if (src == null) {
            throw new BusinessException("不支持的图片格式");
        }

        // 等比压缩到最长边
        BufferedImage scaled = scale(src, properties.getUpload().getMaxSide());

        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        File dir = new File(properties.getUpload().getDir(), dateDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("创建上传目录失败");
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // 优先 WebP
        File webpFile = new File(dir, uuid + ".webp");
        if (writeWebp(scaled, webpFile)) {
            return buildUrl(dateDir, webpFile.getName());
        }
        // 回退 JPEG
        File jpgFile = new File(dir, uuid + ".jpg");
        try {
            Thumbnails.of(toRgb(scaled)).scale(1.0)
                    .outputFormat("jpg")
                    .outputQuality(properties.getUpload().getWebpQuality())
                    .toFile(jpgFile);
            log.warn("WebP 编码不可用, 已回退为 JPEG: {}", jpgFile.getName());
            return buildUrl(dateDir, jpgFile.getName());
        } catch (IOException e) {
            throw new BusinessException("图片保存失败");
        }
    }

    private String buildUrl(String dateDir, String fileName) {
        return properties.getUpload().getUrlPrefix() + "/" + dateDir + "/" + fileName;
    }

    private BufferedImage scale(BufferedImage src, int maxSide) {
        int w = src.getWidth();
        int h = src.getHeight();
        int max = Math.max(w, h);
        if (max <= maxSide) {
            return src;
        }
        double ratio = (double) maxSide / max;
        try {
            return Thumbnails.of(src)
                    .size((int) (w * ratio), (int) (h * ratio))
                    .asBufferedImage();
        } catch (IOException e) {
            return src;
        }
    }

    /**
     * 使用 ImageIO 的 webp writer 写文件, 成功返回 true
     */
    private boolean writeWebp(BufferedImage image, File target) {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
        if (!writers.hasNext()) {
            writers = ImageIO.getImageWritersByFormatName("webp");
        }
        if (!writers.hasNext()) {
            return false;
        }
        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(target)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                String[] types = param.getCompressionTypes();
                if (types != null && types.length > 0 && param.getCompressionType() == null) {
                    param.setCompressionType(types[0]);
                }
                param.setCompressionQuality((float) properties.getUpload().getWebpQuality());
            }
            writer.write(null, new IIOImage(image, null, null), param);
            return true;
        } catch (Exception e) {
            log.warn("WebP 编码失败: {}", e.getMessage());
            if (target.exists()) {
                target.delete();
            }
            return false;
        } finally {
            writer.dispose();
        }
    }

    /**
     * 转换为无 alpha 通道的 RGB 图像 (JPEG 不支持透明)
     */
    private BufferedImage toRgb(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_RGB) {
            return src;
        }
        BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.drawImage(src, 0, 0, Color.WHITE, null);
        g.dispose();
        return rgb;
    }
}

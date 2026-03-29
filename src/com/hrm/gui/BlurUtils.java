package com.hrm.gui;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 * Tiện ích xử lý ảnh raster cho giao diện (làm mờ Gaussian bằng {@link ConvolveOp}).
 * <p>
 * Có thể dùng cho nền đăng nhập; kernel được chuẩn hóa để tránh sáng tối lệch.
 * </p>
 */
public class BlurUtils {

    /**
     * Áp dụng Gaussian Blur lên BufferedImage.
     * @param src   Ảnh gốc
     * @param radius Bán kính làm mờ (càng lớn càng mờ)
     * @return Ảnh đã được làm mờ
     */
    public static BufferedImage applyGaussianBlur(BufferedImage src, int radius) {
        if (radius < 1) return src;

        int size = radius * 2 + 1;
        float[] data = new float[size * size];

        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;

        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                float distance = x * x + y * y;
                int index = (y + radius) * size + (x + radius);
                data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
                total += data[index];
            }
        }

        // Normalize
        for (int i = 0; i < data.length; i++) {
            data[i] /= total;
        }

        Kernel kernel = new Kernel(size, size, data);

        // Tạo ảnh đích có cùng kích thước nhưng không có alpha channel
        // để ConvolveOp hoạt động ổn định
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        // Vẽ src lên dest trước để đảm bảo nền không bị đen
        dest.getGraphics().drawImage(src, 0, 0, null);

        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(dest, null);
    }
}

package com.yamashiroya.payment_api.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    /**
     * CLOUDINARY_URL 環境変数（例: cloudinary://api_key:api_secret@cloud_name）から
     * Cloudinary インスタンスを生成して Bean として登録します。
     *
     * Render の環境変数に CLOUDINARY_URL を設定するだけで自動的に接続されます。
     */
    @Bean
    public Cloudinary cloudinary() {
        String cloudinaryUrl = System.getenv("CLOUDINARY_URL");
        if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) {
            throw new IllegalStateException(
                "環境変数 CLOUDINARY_URL が設定されていません。" +
                "例: cloudinary://api_key:api_secret@cloud_name"
            );
        }
        return new Cloudinary(cloudinaryUrl);
    }
}

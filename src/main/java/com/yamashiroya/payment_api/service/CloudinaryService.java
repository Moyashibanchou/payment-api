package com.yamashiroya.payment_api.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * MultipartFile を受け取り、Cloudinary にアップロードして
     * 完全な画像 URL（https://res.cloudinary.com/...）を返します。
     *
     * @param file アップロードする画像ファイル
     * @return Cloudinary から返された secure_url
     * @throws IOException アップロード失敗時
     */
    public String upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("アップロードするファイルが空です。");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> result = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "folder", "yamashiroya/products",
                "resource_type", "image"
            )
        );

        String secureUrl = (String) result.get("secure_url");
        if (secureUrl == null || secureUrl.isBlank()) {
            throw new IOException("Cloudinary からの URL 取得に失敗しました。");
        }

        return secureUrl;
    }
}

package com.hireflow.hireflow.infra.s3;

import com.hireflow.hireflow.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file) {
        // PDF 파일인지 검증
        if (!file.getContentType().equals("application/pdf")) {
            throw new BadRequestException("PDF 파일만 업로드 가능합니다.");
        }

        // 파일명 중복 방지: UUID + 원본 파일명
        String fileName = "resumes/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

        } catch (IOException e) {
            throw new BadRequestException("파일 업로드 중 오류가 발생했습니다.");
        }

        // 업로드된 파일의 S3 URL 반환
        return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;
    }

    public byte[] download(String resumeUrl) {
        String key = extractKeyFromUrl(resumeUrl);

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        return s3Client.getObjectAsBytes(request).asByteArray();
    }

    private String extractKeyFromUrl(String resumeUrl) {
        //  URL에서 key 추출 - 시작점으로부터 처음 만나는 resumes/의 위치
        return resumeUrl.substring(resumeUrl.indexOf("resumes/"));
    }
}

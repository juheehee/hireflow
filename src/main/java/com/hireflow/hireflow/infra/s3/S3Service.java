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

    private static final String PDF_MAGIC_BYTES = "%PDF";

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file) {
        byte[] fileBytes;

        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("파일을 읽는 중 오류가 발생했습니다.");
        }

        // 1차 검증 : Content-Type 헤더 (null-safe)
        if (!"application/pdf".equals(file.getContentType())) {
            throw new BadRequestException("PDF 파일만 업로드 가능합니다.");
        }

        // 2차 검증 : 실제 파일 내용 기준 magic bytes 확인 (Content-Type 위조 대응)
        if (!isPdf(fileBytes)) {
            throw new BadRequestException("유효한 PDF 파일이 아닙니다.");
        }

        // 파일명 중복 방지: UUID + 원본 파일명
        String fileName = "resumes/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(fileBytes));

        // 업로드된 파일의 S3 URL 반환
        return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;
    }

    private boolean isPdf(byte[] fileBytes) {
        int magicLength = PDF_MAGIC_BYTES.length(); // "%PDF" 길이인 4
        if (fileBytes.length < magicLength) {
            return false; // 파일이 너무 작으면 PDF일 수 없음
        }
        String header = new String(fileBytes, 0, magicLength); // 파일 앞 4바이트를 문자로 변환
        return PDF_MAGIC_BYTES.equals(header); // "%PDF"랑 일치하는지 비교
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

package kr.ssok.userservice.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import kr.ssok.userservice.exception.UserException;
import kr.ssok.userservice.exception.UserResponseStatus;
import kr.ssok.userservice.service.S3FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileServiceImpl implements S3FileService {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.profile-image-path}")
    private String profileImagePath;

    @Override
    public String uploadFile(MultipartFile file, String userId) {
        validateFile(file);
        
        String fileName = generateFileName(file.getOriginalFilename(), userId);
        String key = profileImagePath + "/" + fileName;
        
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName, key, file.getInputStream(), metadata);
            
            amazonS3.putObject(putObjectRequest);
            
            log.info("S3 파일 업로드 성공: {}", key);
            return fileName;
            
        } catch (IOException e) {
            log.error("S3 파일 업로드 실패: {}", e.getMessage());
            throw new UserException(UserResponseStatus.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            String key = profileImagePath + "/" + fileName;
            amazonS3.deleteObject(bucketName, key);
            log.info("S3 파일 삭제 성공: {}", key);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", e.getMessage());
            throw new UserException(UserResponseStatus.FILE_DELETE_ERROR);
        }
    }

    @Override
    public String getFileUrl(String fileName) {
        String key = profileImagePath + "/" + fileName;
        return amazonS3.getUrl(bucketName, key).toString();
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new UserException(UserResponseStatus.FILE_EMPTY);
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new UserException(UserResponseStatus.INVALID_FILE_TYPE);
        }
        
        // 5MB 제한
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new UserException(UserResponseStatus.FILE_SIZE_EXCEEDED);
        }
    }

    private String generateFileName(String originalFilename, String userId) {
        String extension = getFileExtension(originalFilename);
        return userId + "_" + UUID.randomUUID().toString() + "." + extension;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            throw new UserException(UserResponseStatus.INVALID_FILE_TYPE);
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}

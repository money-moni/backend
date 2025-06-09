package kr.ssok.userservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3FileService {
    String uploadFile(MultipartFile file, String userId);
    void deleteFile(String fileName);
    String getFileUrl(String fileName);
}

package kr.ssok.userservice.service;

import kr.ssok.userservice.dto.ProfileResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    void uploadProfileImage(String userId, MultipartFile file);
    ProfileResponseDto getProfileImage(String userId);
    void updateProfileImage(String userId, MultipartFile file);
    void deleteProfileImage(String userId);
}

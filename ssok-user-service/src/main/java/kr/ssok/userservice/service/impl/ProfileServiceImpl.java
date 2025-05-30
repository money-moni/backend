package kr.ssok.userservice.service.impl;

import kr.ssok.userservice.dto.ProfileResponseDto;
import kr.ssok.userservice.entity.ProfileImage;
import kr.ssok.userservice.entity.User;
import kr.ssok.userservice.exception.UserException;
import kr.ssok.userservice.exception.UserResponseStatus;
import kr.ssok.userservice.repository.ProfileImageRepository;
import kr.ssok.userservice.repository.UserRepository;
import kr.ssok.userservice.service.ProfileService;
import kr.ssok.userservice.service.S3FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProfileServiceImpl implements ProfileService {

    private final ProfileImageRepository profileImageRepository;
    private final UserRepository userRepository;
    private final S3FileService s3FileService;

    @Override
    public void uploadProfileImage(String userId, MultipartFile file) {
        User user = findUserById(Long.parseLong(userId));
        
        // 기존 프로필 이미지가 있다면 삭제
        Optional<ProfileImage> existingProfile = profileImageRepository.findByUser(user);
        if (existingProfile.isPresent()) {
            ProfileImage existing = existingProfile.get();
            if (!existing.getStoredFilename().equals("noImage")) {
                s3FileService.deleteFile(existing.getStoredFilename());
            }
            profileImageRepository.delete(existing);
        }
        
        // 새 파일 업로드
        String storedFilename = s3FileService.uploadFile(file, userId);
        String fileUrl = s3FileService.getFileUrl(storedFilename);
        
        ProfileImage profileImage = ProfileImage.builder()
                .user(user)
                .storedFilename(storedFilename)
                .url(fileUrl)
                .contentType(file.getContentType())
                .build();
        
        profileImageRepository.save(profileImage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDto getProfileImage(String userId) {
        User user = findUserById(Long.parseLong(userId));
        
        ProfileImage profileImage = profileImageRepository.findByUser(user)
                .orElseThrow(() -> new UserException(UserResponseStatus.PROFILE_IMAGE_NOT_FOUND));
        
        return ProfileResponseDto.builder()
                .id(profileImage.getId())
                .url(profileImage.getUrl())
                .contentType(profileImage.getContentType())
                .build();
    }

    @Override
    public void updateProfileImage(String userId, MultipartFile file) {
        User user = findUserById(Long.parseLong(userId));

        // 기존 프로필 이미지 찾기
        ProfileImage profileImage = profileImageRepository.findByUser(user).orElseThrow(
                () -> new UserException(UserResponseStatus.PROFILE_IMAGE_NOT_FOUND));

        // S3 프로필 이미지 업로드
        String storedFilename = s3FileService.uploadFile(file, userId);
        String fileUrl = s3FileService.getFileUrl(storedFilename);

        profileImage.updateImage(storedFilename, fileUrl, profileImage.getContentType());
    }

    @Override
    public void deleteProfileImage(String userId) {
        User user = findUserById(Long.parseLong(userId));
        
        ProfileImage profileImage = profileImageRepository.findByUser(user)
                .orElseThrow(() -> new UserException(UserResponseStatus.PROFILE_IMAGE_NOT_FOUND));
        
        // S3에서 파일 삭제
        s3FileService.deleteFile(profileImage.getStoredFilename());

        // 기본 이미지로 변경
        String fileUrl = s3FileService.getFileUrl("noImage");

        profileImage.updateImage("noImage", fileUrl, "webp");
        profileImageRepository.save(profileImage);

        
        log.info("유저의 프로필 이미지 삭제: {}", userId);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserResponseStatus.USER_NOT_FOUND));
    }
}

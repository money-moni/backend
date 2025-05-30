package kr.ssok.userservice.service.impl;

import kr.ssok.userservice.constants.ProfileConstants;
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

        // 기존 프로필 이미지 조회 (반드시 존재해야 함 - 회원가입 시 기본 이미지 생성)
        ProfileImage profileImage = profileImageRepository.findByUser(user)
                .orElseThrow(() -> new UserException(UserResponseStatus.PROFILE_IMAGE_NOT_FOUND));

        // 기존 S3 파일이 기본 이미지가 아닌 경우에만 삭제 대상으로 표시
        String oldStoredFilename = profileImage.getStoredFilename();
        boolean isDefaultImage = ProfileConstants.DEFAULT_IMAGE_FILENAME.equals(oldStoredFilename);

        try {
            // 새 파일을 S3에 업로드
            String newStoredFilename = s3FileService.uploadFile(file, userId);
            String newFileUrl = s3FileService.getFileUrl(newStoredFilename);

            // 기존 엔티티 업데이트 (삭제/생성 없이)
            profileImage.updateImage(newStoredFilename, newFileUrl, file.getContentType());
            profileImageRepository.save(profileImage);

            // S3에서 기존 파일 삭제 (기본 이미지가 아닌 경우에만)
            if (!isDefaultImage) {
                try {
                    s3FileService.deleteFile(oldStoredFilename);
                    log.info("기존 프로필 이미지 S3에서 삭제 완료: {}", oldStoredFilename);
                } catch (Exception e) {
                    log.warn("기존 프로필 이미지 S3 삭제 실패 (데이터는 정상 저장됨): {}", e.getMessage());
                            // S3 삭제 실패해도 데이터는 정상적으로 업데이트된 상태이므로 예외를 던지지 않음
                }
            } else {
                log.info("기본 이미지에서 새 이미지로 변경: {}", newStoredFilename);
            }

            log.info("프로필 이미지 업로드 성공: userId={}, filename={}", userId, newStoredFilename);

        } catch (Exception e) {
            log.error("프로필 이미지 업로드 실패: userId={}, error={}", userId, e.getMessage());
            throw new UserException(UserResponseStatus.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDto getProfileImage(String userId) {
        User user = findUserById(Long.parseLong(userId));

        ProfileImage profileImage = profileImageRepository.findByUser(user)
                .orElseThrow(() -> new UserException(UserResponseStatus.PROFILE_IMAGE_NOT_FOUND));

        return ProfileResponseDto.builder()
                .id(profileImage.getId())
                .url(profileImage.getUrl())  // S3 직접 URL 반환
                .contentType(profileImage.getContentType())
                .build();
    }

    @Override
    public void updateProfileImage(String userId, MultipartFile file) {
        // uploadProfileImage와 동일한 로직으로 처리
        uploadProfileImage(userId, file);
    }

    @Override
    public void deleteProfileImage(String userId) {
        User user = findUserById(Long.parseLong(userId));

        ProfileImage profileImage = profileImageRepository.findByUser(user)
                .orElseThrow(() -> new UserException(UserResponseStatus.PROFILE_IMAGE_NOT_FOUND));

        // 현재 이미지가 기본 이미지인 경우 삭제할 필요 없음
        if (ProfileConstants.DEFAULT_IMAGE_FILENAME.equals(profileImage.getStoredFilename())) {
            log.info("이미 기본 이미지 상태입니다: userId={}", userId);
            return;
        }

        // S3에서 현재 파일 삭제 (기본 이미지가 아니므로 삭제 가능)
        try {
            s3FileService.deleteFile(profileImage.getStoredFilename());
            log.info("프로필 이미지 S3에서 삭제 완료: {}", profileImage.getStoredFilename());
        } catch (Exception e) {
            log.warn("프로필 이미지 S3 삭제 실패: {}", e.getMessage());
                    // S3 삭제 실패해도 기본 이미지로 변경은 진행
        }

        // 기본 이미지로 변경
        String defaultImageUrl = s3FileService.getFileUrl(ProfileConstants.DEFAULT_IMAGE_FILENAME);
        profileImage.updateImage(
                ProfileConstants.DEFAULT_IMAGE_FILENAME,
                defaultImageUrl,
                ProfileConstants.DEFAULT_IMAGE_CONTENT_TYPE
        );
        profileImageRepository.save(profileImage);

        log.info("프로필 이미지를 기본 이미지로 변경 완료: userId={}", userId);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserResponseStatus.USER_NOT_FOUND));
    }
}


package kr.ssok.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ssok.common.exception.BaseResponse;
import kr.ssok.common.logging.annotation.ControllerLogging;
import kr.ssok.userservice.dto.ProfileResponseDto;
import kr.ssok.userservice.exception.UserResponseStatus;
import kr.ssok.userservice.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "프로필 이미지 관리 API")
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 이미지 업로드", description = "사용자의 프로필 이미지를 업로드합니다. 기존 이미지가 있다면 교체됩니다.")
    public ResponseEntity<BaseResponse<Void>> uploadProfileImage(
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "프로필 이미지 파일", required = true)
            @RequestParam("file") MultipartFile file) {

        profileService.uploadProfileImage(userId, file);
        return ResponseEntity.ok(new BaseResponse<>(UserResponseStatus.SUCCESS));
    }

    @GetMapping
    @Operation(summary = "프로필 이미지 조회", description = "사용자의 프로필 이미지 정보를 조회합니다. S3 직접 URL을 반환합니다.")
    public ResponseEntity<BaseResponse<ProfileResponseDto>> getProfileImage(
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("X-User-Id") String userId) {

        ProfileResponseDto responseDto = profileService.getProfileImage(userId);
        return ResponseEntity.ok(new BaseResponse<>(UserResponseStatus.SUCCESS, responseDto));
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 이미지 수정", description = "사용자의 프로필 이미지를 수정합니다. 기존 이미지를 새 이미지로 교체합니다.")
    public ResponseEntity<BaseResponse<Void>> updateProfileImage(
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "새로운 프로필 이미지 파일", required = true)
            @RequestParam("file") MultipartFile file) {

        profileService.updateProfileImage(userId, file);
        return ResponseEntity.ok(new BaseResponse<>(UserResponseStatus.SUCCESS));
    }

    @DeleteMapping
    @Operation(summary = "프로필 이미지 삭제", description = "사용자의 프로필 이미지를 삭제하고 기본 이미지로 복원합니다.")
    public ResponseEntity<BaseResponse<Void>> deleteProfileImage(
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("X-User-Id") String userId) {

        profileService.deleteProfileImage(userId);
        return ResponseEntity.ok(new BaseResponse<>(UserResponseStatus.SUCCESS));
    }
}

package kr.ssok.bluetoothservice.service.impl;

import kr.ssok.bluetoothservice.client.AccountServiceClient;
import kr.ssok.bluetoothservice.client.UserServiceClient;
import kr.ssok.bluetoothservice.client.dto.AccountInfoDto;
import kr.ssok.bluetoothservice.client.dto.UserInfoDto;
import kr.ssok.bluetoothservice.dto.response.BluetoothMatchResponseDto;
import kr.ssok.bluetoothservice.exception.BluetoothException;
import kr.ssok.bluetoothservice.exception.BluetoothResponseStatus;
import kr.ssok.bluetoothservice.service.BluetoothService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bluetooth UUID 비즈니스 로직 구현체
 */
@Service
@RequiredArgsConstructor
public class BluetoothServiceImpl implements BluetoothService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserServiceClient userServiceClient;
    private final AccountServiceClient accountServiceClient;

    @Value("${bluetooth.uuid-ttl-seconds}")
    private long ttlSeconds; // UUID 유효 기간

    /**
     * UUID 등록 메서드
     * - 기존 UUID 제거 (user:{userId}, uuid:{oldUUID})
     * - 새 UUID로 등록 (user:{userId}, uuid:{newUUID})
     *
     * @param userId        사용자 ID
     * @param bluetoothUUID 블루투스 UUID
     */
    @Override
    public void registerBluetoothUuid(Long userId, String bluetoothUUID) {
        // 입력값 검증
        if (bluetoothUUID == null || bluetoothUUID.isBlank()) {
            throw new BluetoothException(BluetoothResponseStatus.UUID_REQUIRED);
        }
        if (userId == null) {
            throw new BluetoothException(BluetoothResponseStatus.USER_ID_REQUIRED);
        }

        try {
            // 기존 UUID 가져오기 (user → uuid)
            String oldUUID = redisTemplate.opsForValue().get(userKey(userId));

            // 기존 UUID와 새 UUID가 동일하면 업데이트하지 않음
            if (bluetoothUUID.equals(oldUUID)) {
                return; // 그대로 사용
            }

            if (oldUUID != null) {
                // 기존 uuid:{UUID} 키 삭제 (TTL 제거)
                redisTemplate.delete(uuidKey(oldUUID));
                // 기존 user:{userId} 키 삭제 (TTL 제거)
                redisTemplate.delete(userKey(userId));
            }

            // 새 값 저장: uuid:{UUID} → userId
            redisTemplate.opsForValue().set(uuidKey(bluetoothUUID), userId.toString(), Duration.ofSeconds(ttlSeconds));

            // 새 값 저장: user:{userId} → UUID
            redisTemplate.opsForValue().set(userKey(userId), bluetoothUUID, Duration.ofSeconds(ttlSeconds));

        } catch (DataAccessException e) {
            throw new BluetoothException(BluetoothResponseStatus.REDIS_ACCESS_FAILED);
        }
    }

    // Redis 키 prefix 헬퍼
    private String uuidKey(String uuid) {
        return "uuid:" + uuid;
    }

    private String userKey(Long userId) {
        return "user:" + userId;
    }

    /**
     * Bluetooth UUID를 이용하여 주변 블루투스 기기와 매칭된 사용자를 조회하는 서비스 메서드
     * - Redis에 등록된 UUID와 비교하여 매칭된 사용자 정보를 조회
     * - 유저 정보를 조회할 때 내부 서비스인 ssok-user-service를 이용
     * - ssok-account-service를 이용하여 현재 사용자의 주 계좌 정보도 함께 반환
     *
     * @param userId        요청을 보낸 사용자의 ID
     * @param bluetoothUUIDs 탐색한 주변 블루투스 UUID 리스트
     * @return BluetoothMatchResponseDto 매칭된 사용자 정보와 주 계좌 정보가 포함된 응답 객체
     * @throws BluetoothException 매칭되는 Bluetooth UUID가 없는 경우
     */
    @Override
    public BluetoothMatchResponseDto matchBluetoothUsers(String userId, List<String> bluetoothUUIDs) {
        if (bluetoothUUIDs == null || bluetoothUUIDs.isEmpty()) {
            throw new BluetoothException(BluetoothResponseStatus.NO_SCAN_UUID);
        }

        try {
            // Bluetooth UUID 목록을 순회하며 Redis에 저장된 사용자 ID를 조회하고, 유저 정보를 수집
            List<UserInfoDto> matchedUsers = bluetoothUUIDs.stream()
                    .map(uuid -> {
                        // Redis에서 블루투스 UUID에 매핑된 사용자 ID를 조회
                        String userIdStr = redisTemplate.opsForValue().get("uuid:" + uuid);
                        if (userIdStr == null) return null;
                        // 유저 서비스에서 사용자 정보를 조회하여 반환
                        return userServiceClient.getUserInfo(userIdStr).getResult();
                    })
                    .filter(user -> user != null)
                    .collect(Collectors.toList());

            // 매칭된 사용자가 없는 경우 예외 발생
            if (matchedUsers.isEmpty()) {
                throw new BluetoothException(BluetoothResponseStatus.NO_MATCH_FOUND);
            }

            // 사용자의 주 계좌 정보를 조회
            AccountInfoDto primaryAccount = accountServiceClient.getPrimaryAccount(userId).getResult();
            return new BluetoothMatchResponseDto(matchedUsers, primaryAccount);
        } catch (DataAccessException e) {
            throw new BluetoothException(BluetoothResponseStatus.REDIS_ACCESS_FAILED);
        }
    }
}

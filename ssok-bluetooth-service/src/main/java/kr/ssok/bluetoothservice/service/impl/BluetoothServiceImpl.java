package kr.ssok.bluetoothservice.service.impl;

import kr.ssok.bluetoothservice.exception.BluetoothException;
import kr.ssok.bluetoothservice.exception.BluetoothResponseStatus;
import kr.ssok.bluetoothservice.service.BluetoothService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Bluetooth UUID 등록 비즈니스 로직 구현체
 */
@Service
@RequiredArgsConstructor
public class BluetoothServiceImpl implements BluetoothService {

    private final RedisTemplate<String, String> redisTemplate;

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
                // 기존 uuid:{UUID} 키 제거
                redisTemplate.delete(uuidKey(oldUUID));
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
}

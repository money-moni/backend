package kr.ssok.bluetoothservice.service.impl;

import kr.ssok.bluetoothservice.exception.BluetoothException;
import kr.ssok.bluetoothservice.exception.BluetoothResponseStatus;
import kr.ssok.bluetoothservice.service.BluetoothService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;

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
     * - 동일 유저 ID가 등록된 기존 UUID 제거
     * - 새 UUID로 등록
     *
     * @param userId        사용자 ID
     * @param bluetoothUUID 블루투스 UUID
     */
    @Override
    public void registerBluetoothUuid(Long userId, String bluetoothUUID) {
        // 유효성 검사
        if (bluetoothUUID == null || bluetoothUUID.isBlank()) {
            throw new BluetoothException(BluetoothResponseStatus.UUID_REQUIRED);
        }

        if (userId == null) {
            throw new BluetoothException(BluetoothResponseStatus.USER_ID_REQUIRED);
        }

        // Scan을 통한 userId가 기존에 등록된 UUID 키 탐색 후 제거
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            ScanOptions options = ScanOptions.scanOptions().match("*").count(100).build();
            Iterator<byte[]> keys = connection.scan(options);

            while (keys.hasNext()) {
                byte[] key = keys.next();
                byte[] valueBytes = connection.stringCommands().get(key);
                if (valueBytes != null) {
                    String value = new String(valueBytes, StandardCharsets.UTF_8);
                    if (value.equals(userId.toString())) {
                        connection.keyCommands().del(key); // 기존 UUID 제거
                        break; // 1개만 삭제
                    }
                }
            }

            // 새 UUID 저장 (TTL 적용)
            redisTemplate.opsForValue().set(bluetoothUUID, userId.toString(), Duration.ofSeconds(ttlSeconds));

        } catch (DataAccessException e) {
            throw new BluetoothException(BluetoothResponseStatus.REDIS_ACCESS_FAILED);
        }
    }
}

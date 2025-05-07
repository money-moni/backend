package kr.ssok.bluetoothservice.service;

import kr.ssok.bluetoothservice.client.AccountServiceClient;
import kr.ssok.bluetoothservice.client.UserServiceClient;
import kr.ssok.bluetoothservice.client.dto.AccountInfoDto;
import kr.ssok.bluetoothservice.client.dto.UserInfoDto;
import kr.ssok.bluetoothservice.dto.response.BluetoothMatchResponseDto;
import kr.ssok.bluetoothservice.exception.BluetoothException;
import kr.ssok.bluetoothservice.exception.BluetoothResponseStatus;
import kr.ssok.bluetoothservice.service.impl.BluetoothServiceImpl;
import kr.ssok.common.exception.BaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * BluetoothServiceImpl 단위 테스트
 */
class BluetoothServiceTest {

    @InjectMocks
    private BluetoothServiceImpl bluetoothService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bluetoothService = new BluetoothServiceImpl(redisTemplate, userServiceClient, accountServiceClient);
        setField(bluetoothService, "ttlSeconds", 3600L); // TTL 주입
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void 기존_UUID_삭제_후_새_UUID_등록() {
        // given
        Long userId = 1L;
        String newUUID = "uuid-999";
        String oldUUID = "uuid-123";
        String userKey = "user:" + userId;
        String oldUuidKey = "uuid:" + oldUUID;

        // 기존 UUID가 존재한다고 가정
        when(valueOperations.get(userKey)).thenReturn(oldUUID);

        // when
        bluetoothService.registerBluetoothUuid(userId, newUUID);

        // then: 기존 UUID 키 삭제
        verify(redisTemplate).delete(oldUuidKey);

        // 새 UUID 등록
        verify(valueOperations).set(eq("uuid:" + newUUID), eq(userId.toString()), eq(Duration.ofSeconds(3600)));
        verify(valueOperations).set(eq("user:" + userId), eq(newUUID), eq(Duration.ofSeconds(3600)));
    }

    @Test
    void 기존_UUID_없을때_삭제없이_등록() {
        // given
        Long userId = 2L;
        String newUUID = "uuid-888";

        when(valueOperations.get("user:" + userId)).thenReturn(null); // 기존 UUID 없음

        // when
        bluetoothService.registerBluetoothUuid(userId, newUUID);

        // then: delete 호출 없음
        verify(redisTemplate, never()).delete((String) any());

        // set 호출
        verify(valueOperations).set(eq("uuid:" + newUUID), eq(userId.toString()), any(Duration.class));
        verify(valueOperations).set(eq("user:" + userId), eq(newUUID), any(Duration.class));
    }

    @Test
    void uuid_null이면_예외() {
        BluetoothException ex = assertThrows(BluetoothException.class, () ->
                bluetoothService.registerBluetoothUuid(1L, null));
        assertEquals(BluetoothResponseStatus.UUID_REQUIRED, ex.getStatus());
    }

    @Test
    void userId_null이면_예외() {
        BluetoothException ex = assertThrows(BluetoothException.class, () ->
                bluetoothService.registerBluetoothUuid(null, "uuid"));
        assertEquals(BluetoothResponseStatus.USER_ID_REQUIRED, ex.getStatus());
    }

    @Test
    void redis_접근_예외시_예외() {
        when(valueOperations.get(anyString())).thenThrow(new DataAccessException("fail") {});
        BluetoothException ex = assertThrows(BluetoothException.class, () ->
                bluetoothService.registerBluetoothUuid(1L, "uuid"));
        assertEquals(BluetoothResponseStatus.REDIS_ACCESS_FAILED, ex.getStatus());
    }

    // Reflection 필드 주입 도우미
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

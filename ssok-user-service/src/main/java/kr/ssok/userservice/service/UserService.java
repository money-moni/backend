package kr.ssok.userservice.service;

import jakarta.validation.Valid;
import kr.ssok.userservice.dto.request.SignupRequestDto;
import kr.ssok.userservice.dto.response.SignupResponseDto;
import org.springframework.validation.BindingResult;

public interface UserService {
    SignupResponseDto registerUser(@Valid SignupRequestDto requestDto, BindingResult bindingResult);
}

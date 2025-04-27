package kr.ssok.userservice.client;

import kr.ssok.userservice.dto.request.BankAccountRequestDto;
import kr.ssok.userservice.dto.response.BankAccountResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "bank-service", url = "${bank.server.url:http://localhost:8082}")
public interface BankClient {
    
    @PostMapping("/api/bank/account")
    BankAccountResponseDto createAccount(@RequestBody BankAccountRequestDto request);
}

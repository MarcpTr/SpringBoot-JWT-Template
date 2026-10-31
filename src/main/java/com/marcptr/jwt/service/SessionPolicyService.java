package com.marcptr.jwt.service;

import org.springframework.stereotype.Service;
import com.marcptr.jwt.exception.FieldValidationException;
import com.marcptr.jwt.exception.ResourceNotFoundException;
import com.marcptr.jwt.model.AppSetting;
import com.marcptr.jwt.model.enums.ErrorCode;
import com.marcptr.jwt.repository.AppSettingRepository;
import com.marcptr.jwt.utils.MessageResolver;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionPolicyService {
    private final MessageResolver messageResolver;
    private final AppSettingRepository repository;

    public int getMaxSessions() {
        return repository.findByConfigKey("security.max_sessions")
                .map(setting -> Integer.parseInt(setting.getConfigValue()))
                .orElse(1);
    }

    @Transactional
    public void updateMaxSessions(int value) {
        if (value < 1) {
            throw new FieldValidationException(ErrorCode.MIN_SESSIONS,
                    messageResolver.resolveMessage(ErrorCode.MIN_SESSIONS));
        }
        if (value > 10) {
            throw new FieldValidationException(ErrorCode.MAX_SESSIONS,
                    messageResolver.resolveMessage(ErrorCode.MAX_SESSIONS));
        }
        AppSetting setting = repository.findByConfigKey("security.max_sessions")
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SETTING_NOT_FOUND,
                        messageResolver.resolveMessage(ErrorCode.SETTING_NOT_FOUND)));

        setting.setConfigValue(String.valueOf(value));
        repository.save(setting);
    }
}
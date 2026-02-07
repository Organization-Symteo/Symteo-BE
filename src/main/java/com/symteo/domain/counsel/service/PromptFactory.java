package com.symteo.domain.counsel.service;

public interface PromptFactory {
    public String createSBPrompt(Long userId, Long reportId);
    public String createDAPrompt(Long userId, Long reportId);
    public String createAPrompt(Long userId, Long reportId);
}

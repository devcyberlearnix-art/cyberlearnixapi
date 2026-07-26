package com.example.notification.service;

import com.example.notification.dto.TemplateRequest;
import com.example.notification.dto.TemplateResponse;
import com.example.notification.entity.Template;
import com.example.notification.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository repository;

    public TemplateResponse createTemplate(TemplateRequest request) {

        if (repository.existsByName(request.getName())) {
            throw new RuntimeException("Template already exists with name: " + request.getName());
        }

        Template template = Template.builder()
                .name(request.getName())
                .title(request.getTitle())
                .content(request.getContent())
                .channel(request.getChannel())
                .build();

        Template saved = repository.save(template);

        return TemplateResponse.builder()
                .templateId(saved.getId())
                .name(saved.getName())
                .title(saved.getTitle())
                .content(saved.getContent())
                .channel(saved.getChannel())
                .active(saved.isActive())
                .createdAt(saved.getCreatedAt())
                .build();
    }
    public Page<TemplateResponse> getTemplates(
            int page,
            int size,
            String channel,
            Boolean active
    ) {

        PageRequest pageable = PageRequest.of(page, size);

        Page<Template> templates;

        if (channel != null && active != null) {
            templates = repository.findByChannelAndActive(channel, active, pageable);
        } else if (channel != null) {
            templates = repository.findByChannel(channel, pageable);
        } else if (active != null) {
            templates = repository.findByActive(active, pageable);
        } else {
            templates = repository.findAll(pageable);
        }

        return templates.map(template ->
                TemplateResponse.builder()
                        .templateId(template.getId())
                        .name(template.getName())
                        .title(template.getTitle())
                        .content(template.getContent())
                        .channel(template.getChannel())
                        .active(template.isActive())
                        .createdAt(template.getCreatedAt())
                        .build()
        );
    }
    public TemplateResponse updateTemplate(String id, TemplateRequest request) {

        // 🔍 Find existing template
        Template template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));

        // 🚫 Prevent duplicate name (only if name changed)
        if (!template.getName().equals(request.getName())
                && repository.existsByName(request.getName())) {
            throw new RuntimeException("Template already exists with name: " + request.getName());
        }

        // ✏️ Update fields
        template.setName(request.getName());
        template.setTitle(request.getTitle());
        template.setContent(request.getContent());
        template.setChannel(request.getChannel());

        Template updated = repository.save(template);

        // 🔁 Map to response
        return TemplateResponse.builder()
                .templateId(updated.getId())
                .name(updated.getName())
                .title(updated.getTitle())
                .content(updated.getContent())
                .channel(updated.getChannel())
                .active(updated.isActive())
                .createdAt(updated.getCreatedAt())
                .build();
    }
    public TemplateResponse deleteTemplate(String id) {

        Template template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));

        // ✅ Soft delete (mark inactive)
        template.setActive(false);

        Template updated = repository.save(template);

        return TemplateResponse.builder()
                .templateId(updated.getId())
                .name(updated.getName())
                .title(updated.getTitle())
                .content(updated.getContent())
                .channel(updated.getChannel())
                .active(updated.isActive())
                .createdAt(updated.getCreatedAt())
                .build();
    }
}
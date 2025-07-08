package com.kood.backend.dto.ConnectionDTOs;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OnlineStatusCheck {
    private String userId;
    private List<String> friendIds;
}

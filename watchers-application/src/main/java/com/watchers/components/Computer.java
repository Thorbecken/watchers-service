package com.watchers.components;

import com.watchers.model.dto.WorldTaskDto;

public interface Computer <T extends WorldTaskDto> {

    void process(T worldTaskDto);
}

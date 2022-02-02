package com.sample.source.property;

import com.sample.source.property.repository.LabelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelMapper mapper;

    public Object findAllLabel() {
        return mapper.findAllLabel();
    }

}

package com.sample.source.property.label;

import com.sample.source.property.repository.LabelMapper;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelMapper mapper;

    public Set<Label> findAllLabels() {
        return mapper.findAllLabels();
    }

}

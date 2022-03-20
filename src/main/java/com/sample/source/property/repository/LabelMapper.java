package com.sample.source.property.repository;

import com.sample.source.property.label.Label;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LabelMapper {

    Set<Label> findAllLabels();

}

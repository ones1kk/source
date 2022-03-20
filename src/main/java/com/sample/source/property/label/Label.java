package com.sample.source.property.label;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class Label {

    private String labelId;

    private String labelKrNm;

    private String labelEngNm;

}

package com.sample.source.bundle;

import lombok.Getter;

@Getter
public enum Bundle {
    LABELS;

    private final String bundleName;

    Bundle() {
        this.bundleName = name().toLowerCase().replace('_', '-');
    }
}

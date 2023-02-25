package com.disposableemail.apache.james.mailet.collector.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Address {
    private String address;
    private String name;
}

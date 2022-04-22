package com.stillcoolme.flink.hotitem.entity.hotpage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageViewCount {

    private String url;
    private Long windowEnd;
    private Long count;
}

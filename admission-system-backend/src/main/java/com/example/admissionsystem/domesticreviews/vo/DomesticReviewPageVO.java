package com.example.admissionsystem.domesticreviews.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomesticReviewPageVO {

    private List<DomesticReviewListItemVO> list;
    private Integer page;
    private Integer pageSize;
    private Long total;
}

package com.example.admissionsystem.schoolreviews.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolReviewPageVO {

    private List<SchoolReviewListItemVO> list;
    private Integer page;
    private Integer pageSize;
    private Long total;
}

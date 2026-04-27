package com.example.admissionsystem.applications.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationPageVO {

    private List<ApplicationListItemVO> list;
    private Integer page;
    private Integer pageSize;
    private Long total;
}

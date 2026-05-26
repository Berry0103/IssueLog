package com.yuanzhang.econexus.dto.Issue;

import com.yuanzhang.econexus.model.MesIssueUpdate;
import lombok.Data;

@Data
public class MesIssueUpdateDTO {
    private MesIssueUpdate issueUpdate;
    private String issueStatusIndex;
    private String actionOwnerIndex;
    private String issueOwnerIndex;
}

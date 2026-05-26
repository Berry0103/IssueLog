package com.yuanzhang.econexus.dto;

import com.yuanzhang.econexus.model.*;
import lombok.Data;

@Data
public class ProjectDTO {
    private String projectIndex;
    private String projectName;
    private String typeName;
    private String typeIndex;
    private String powerName;
    private String powerIndex;
    private String customerName;
    private String customerIndex;
    private String description;

    public static ProjectDTO fromEntity(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setProjectIndex(project.getProjectIndex());
        dto.setProjectName(project.getProjectName());
        dto.setDescription(project.getDescription());

        Dictionary type = project.getProjectType();
        if (type != null) {
            dto.setTypeIndex(type.getDicIndex());
            dto.setTypeName(type.getDicName());
        }

        Dictionary power = project.getPower();
        if (power != null) {
            dto.setPowerIndex(power.getDicIndex());
            dto.setPowerName(power.getDicName());
        }

        Customer customer = project.getCustomer();
        if (customer != null) {
            dto.setCustomerIndex(customer.getCustomerIndex());
            dto.setCustomerName(customer.getCustomerName());
        }

        return dto;
    }
}

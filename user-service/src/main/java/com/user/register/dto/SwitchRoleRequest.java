package com.user.register.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SwitchRoleRequest {

    @JsonProperty("switchRole")
    private String switchRole;

    /** @deprecated use switchRole */
    private String role;

    public String getSwitchRole() {
        return switchRole != null ? switchRole : role;
    }

    public void setSwitchRole(String switchRole) {
        this.switchRole = switchRole;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

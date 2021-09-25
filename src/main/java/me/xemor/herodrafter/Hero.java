package me.xemor.herodrafter;

import java.util.List;

public class Hero {

    private String name;
    private List<String> roles;

    /**
     * Do not use, here for use in deserialisation.
     */
    public Hero() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}

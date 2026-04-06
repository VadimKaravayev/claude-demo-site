package com.claude.demo.core.models;

import java.util.List;

import lombok.Getter;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Getter
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class TeamMemberModel {

    @ValueMapValue
    private String photo;

    @ValueMapValue
    private String name;

    @ValueMapValue
    private String jobTitle;

    @ValueMapValue
    private String bio;

    @ChildResource
    private List<SocialLinkModel> socialLinks;

    public List<SocialLinkModel> getSocialLinks() {
        return socialLinks != null
                ? List.copyOf(socialLinks)
                : List.of();
    }
}

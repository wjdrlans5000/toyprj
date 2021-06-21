package me.gimun.documentapproval.common;

import me.gimun.documentapproval.index.IndexController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.validation.BindingResult;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ErrorsResource extends EntityModel<BindingResult> {
    public ErrorsResource(BindingResult content, Link... links) {
        super(content, links);
        //리소스 변환시 index 링크 정보 추가
        add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
    }
}

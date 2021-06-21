package me.gimun.documentapproval.document;

import me.gimun.documentapproval.document.dto.DocumentResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class DocumentResource extends EntityModel<DocumentResponse> {
    public DocumentResource(DocumentResponse content, Link... links) {
        super(content, links);
        // withSelfRel(): 리소스에 대한 링크를 type-safe 한 method로 제공한다.
        add(linkTo(DocumentController.class).slash(content.getId()).withSelfRel());
    }
}

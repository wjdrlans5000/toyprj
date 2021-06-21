package me.gimun.documentapproval.index;

import me.gimun.documentapproval.document.DocumentController;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
public class IndexController {

    @GetMapping("/api")
    public RepresentationModel index(){
        RepresentationModel index = new RepresentationModel();
        index.add(linkTo(DocumentController.class).withRel("documents"));
        return index;
    }
}
